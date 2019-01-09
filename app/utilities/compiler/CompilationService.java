package utilities.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.microsoft.azure.storage.StorageException;
import controllers._BaseFormFactory;
import exceptions.*;
import models.*;
import org.apache.commons.io.FileExistsException;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.enums.CompilationStatus;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_C_Program_Version_Update;
import utilities.swagger.input.Swagger_Library_File_Load;
import utilities.swagger.input.Swagger_Library_Record;
import utilities.swagger.output.Swagger_Compilation_Build_Error;
import websocket.Request;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

import java.net.ConnectException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class CompilationService {

    private static final Logger logger = new Logger(CompilationService.class);

    private final CompilerService compilerService;
    private final _BaseFormFactory formFactory;
    private final WSClient wsClient;

    @Inject
    public CompilationService(CompilerService compilerService, _BaseFormFactory formFactory, WSClient wsClient) {
        this.compilerService = compilerService;
        this.formFactory = formFactory;
        this.wsClient = wsClient;
    }

    public boolean isAvailable() {
        return this.compilerService.isAvailable();
    }

    public void compileAsync(Model_CProgramVersion version, String libraryVersion) {
        new Thread(() -> {
            try {
                this.compile(version, libraryVersion);
            } catch (BaseException e) {
                logger.info("compileAsync - error during compilation: {}", e.getMessage());
            } catch (Exception e) {
                logger.internalServerError(e);
            }
            version.invalidate();
            version.refresh();

        }).start();
    }

    public List<Swagger_Compilation_Build_Error> compile(Model_CProgramVersion version, String libraryVersion) {

        Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("c_programs.id", version.get_c_program_id()).findOne();

        Model_Compilation compilation = version.getCompilation();

        if (compilation != null) {
            if (!compilation.status.equals(CompilationStatus.SERVER_OFFLINE) && !compilation.status.equals(CompilationStatus.SERVER_ERROR)) {
                return new ArrayList<>(); // TODO maybe something else
            }
        } else {
            compilation = new Model_Compilation();
            compilation.firmware_version_lib = libraryVersion;
            compilation.version = version;
            compilation.save();
            version.refresh();
        }

        if (!this.compilerService.isAvailable()) {
            logger.warn("compile - server is offline");
            compilation.status = CompilationStatus.SERVER_OFFLINE;
            compilation.update();
            throw new ServerOfflineException("Compilation server is offline! It will be compiled as soon as possible!");
        }

        compilation.status = CompilationStatus.IN_PROGRESS;
        compilation.update();

        if (version.file == null) {

            logger.internalServerError(new Exception("File not found! Version is not compilable!"));

            compilation.status = CompilationStatus.FILE_NOT_FOUND;
            compilation.update();

            throw new BadRequestException("Server has no content from version");
        }

        // Zpracování Json
        JsonNode json = Json.parse(version.file.downloadString());

        Swagger_C_Program_Version_Update code_file;

        try {

            code_file = this.formFactory.formFromJsonWithValidation(Swagger_C_Program_Version_Update.class, json);

        } catch (InvalidBodyException e) {
            logger.internalServerError(e);
            compilation.status = CompilationStatus.BROKEN_JSON;
            compilation.update();
            throw new RuntimeException("Version was not saved correctly, file is broken. Version id: " + version.getId());
        }

        List<Swagger_Library_Record> library_files = new ArrayList<>();

        for (UUID libraryId : code_file.imported_libraries) {

            Model_LibraryVersion lib_version = Model_LibraryVersion.find.byId(libraryId);

            if (lib_version.file != null) {

                JsonNode j = Json.parse(lib_version.file.downloadString());

                try {

                    Swagger_Library_File_Load lib_file = this.formFactory.formFromJsonWithValidation(Swagger_Library_File_Load.class, j);
                    library_files.addAll(lib_file.files);

                } catch (InvalidBodyException e) {
                    logger.internalServerError(e);
                    compilation.status = CompilationStatus.BROKEN_JSON;
                    compilation.update();
                    throw new RuntimeException("Library was not saved correctly, file is broken. Library id: " + libraryId);
                }
            }
        }

        ObjectNode includes = Json.newObject();

        for (Swagger_Library_Record file_lib : library_files) {
            if (file_lib.file_name.equals("README.md") || file_lib.file_name.equals("readme.md")) continue;
            includes.put(file_lib.file_name, file_lib.content);
        }

        if (code_file.files != null) {
            for (Swagger_Library_Record user_file : code_file.files) {
                includes.put(user_file.file_name, user_file.content);
            }
        }

        WS_Message_Make_compilation compilationResult;

        try {
            compilationResult = this.compilerService.compile(new Request(new WS_Message_Make_compilation().make_request(hardwareType, libraryVersion, version.id, code_file.main, includes)));
        } catch (ServerOfflineException e) {
            logger.warn("compile - server is offline");
            compilation.status = CompilationStatus.SERVER_OFFLINE;
            compilation.update();
            throw new ServerOfflineException("Compilation server is offline! It will be compiled as soon as possible!");
        } catch (Exception e) {
            logger.internalServerError(e);
            compilation.status = CompilationStatus.SERVER_ERROR;
            compilation.update();
            throw new ExternalErrorException();
        }

        // Když obsahuje chyby - vrátím rovnou Becki - Toto je regulérní správná odpověd - chyby způsobil v c++ kodu uživatel
        if (!compilationResult.build_errors.isEmpty()) {

            logger.trace("compile - compilation contains user Errors");

            compilation.status = CompilationStatus.FAILED;
            compilation.update();

            return compilationResult.build_errors;
        }

        if (compilationResult.interface_code == null || compilationResult.build_url == null) {

            logger.internalServerError(new Exception("Missing fields ('interface_code' or 'build_url') in result from Code Server. Result: " + Json.toJson(compilationResult).toString()));

            compilation.status = CompilationStatus.BROKEN_JSON;
            compilation.update();

            throw new BadRequestException("Json code is broken - contact tech support!");
        }

        if (compilationResult.error_message != null) {

            logger.internalServerError(new Exception("Error is empty, but status is not 'success' in result from Code Server. Result: " + Json.toJson(compilationResult).toString()));

            compilation.status = CompilationStatus.SERVER_ERROR;
            compilation.update();

            throw new ExternalErrorException();
        }

        logger.trace("compile - compilation was successful");

        try {

            logger.trace("compile - try to download file");

            CompletionStage<? extends WSResponse> responsePromise = wsClient.url(compilationResult.build_url)
                    .setContentType("undefined")
                    .setRequestTimeout(Duration.ofMillis(7500))
                    .get();

            byte[] body = responsePromise.toCompletableFuture().get().asByteArray();

            if (body == null || body.length == 0) {
                throw new FileExistsException("Body length is 0");
            }

            logger.trace("compile - Body is ok - uploading to Azure");

            // Daný soubor potřebuji dostat na Azure a Propojit s verzí
            compilation.blob = Model_Blob.upload(body, "application/octet-stream", "firmware.bin", compilation.get_path());

            logger.trace("compile - Body is ok - uploading to Azure was successful");
            compilation.status = CompilationStatus.SUCCESS;
            compilation.build_url = compilationResult.build_url;
            compilation.firmware_build_id = compilationResult.build_id_in_firmware;
            compilation.virtual_input_output = compilationResult.interface_code;
            compilation.firmware_build_datetime = new Date();
            compilation.update();

            return new ArrayList<>();

        } catch (StorageException e) {
            logger.internalServerError(new Exception("StorageException" + compilationResult.build_url, e));
            compilation.status = CompilationStatus.SERVER_ERROR;
        } catch (ConnectException e) {
            logger.internalServerError(new Exception("Compilation Server is probably offline on URL: " + compilationResult.build_url, e));
            compilation.status = CompilationStatus.SUCCESS_DOWNLOAD_FAILED;
        } catch (FileExistsException e) {
            logger.internalServerError(new Exception("Compilation body is empty.", e));
            compilation.status = CompilationStatus.SUCCESS_DOWNLOAD_FAILED;
        } catch (Exception e) {
            logger.internalServerError(e);
            compilation.status = CompilationStatus.SERVER_ERROR;
        }

        compilation.update();
        throw new ExternalErrorException();
    }
}
