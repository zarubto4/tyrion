package utilities.swagger.input;

import exceptions.BadRequestException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Blob;
import models.Model_CProgramVersion;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import utilities.enums.CompilationStatus;
import utilities.enums.FirmwareType;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static controllers.Controller_Update.logger;

@ApiModel(description = "Json Model with List of Board ID",
        value = "DeployFirmware")
@Constraints.Validate
public class Swagger_DeployFirmware extends _Swagger_Abstract_Default implements  Constraints.Validatable<List<ValidationError>> {

    @Constraints.Required @ApiModelProperty(required = true)
    public UUID hardware_id;

    @ApiModelProperty(required = false, value = "Required if file is not set")
    public UUID c_program_version_id;

    @Constraints.Required
    public FirmwareType firmware_type;


    @ApiModelProperty(required = false, value = "Required, if c_program_version_id is not set. The file is encoded in base64.")
    @Constraints.MaxLength(value = 2333333 , message = "Max Length is 2Mb")
    public String file;


    @ApiModelProperty(hidden = true)
    public Model_CProgramVersion c_program_version;

    @ApiModelProperty(hidden = true)
    public String firmware_build_id;

    @ApiModelProperty(hidden = true)
    public String file_base_64;

    @Override
    public List<ValidationError> validate() throws BadRequestException {

        List<ValidationError> errors = new ArrayList<>();

        //Valid Firmware File
        if (c_program_version_id != null) {

            Model_CProgramVersion c_program_version = Model_CProgramVersion.find.byId(c_program_version_id);

            if (c_program_version.getCompilation() == null) {
                throw new BadRequestException("Compilation is missing");
            }

            if (c_program_version.getCompilation().status != CompilationStatus.SUCCESS) {
                throw new BadRequestException(("You cannot upload code in state:: " + c_program_version.getCompilation().status.name()));
            }

            this.c_program_version = c_program_version;

            if(! (firmware_type == FirmwareType.BACKUP || firmware_type == FirmwareType.FIRMWARE)) {
                errors.add(new ValidationError("c_program_version_id", "firmware_type must be FIRMWARE or BACKUP"));
            }


        // Valid File
        } else  {

            if (file == null || file.equals("") || file.length() < 543) {
                errors.add(new ValidationError("c_program_version_id", "You have to upload file or set CProgram"));
                errors.add(new ValidationError("file", "You have to upload file or set CProgram"));
                throw new BadRequestException("You have to upload file or set CProgram ");
            }

            //  data:image/png;base64,
            String[] parts = file.split(",");
            String[] type = parts[0].split(":");
            String[] content_type = type[1].split(";");
            String dataType = content_type[0].split("/")[1];

            logger.debug("Swagger_DeployFirmware::validate - Cont Type:" + content_type[0] + ":::");
            logger.debug("Swagger_DeployFirmware::validate - Data Type:" + dataType + ":::");
            logger.debug("Swagger_DeployFirmware::validate - Data: " + parts[1].substring(0, 10) + "......");

            this.file_base_64 = parts[1];

            String  decoded = new String ( Model_Blob.get_decoded_binary_string_from_Base64(  this.file_base_64));


            // Find HASH!!!

           // BuildID formát:: "%ON?*%8a4fae81-08b9-4147-89d5-a4139d1ad973"
           // System.out.println(decoded);

            int index = 0;

            index = decoded.indexOf("%%ON?*%%");

            if(index > 0) {
                System.out.println("Firmware_ID" +  decoded.substring(index, index + 44));
                firmware_build_id = decoded.substring(index, index + 44);

            } else {

                if (index < 0) {
                    index = decoded.indexOf("%%OFF?*%%");
                }
                if (index < 0) {
                    index = decoded.indexOf("%%OF?*%%");
                }


                System.out.println("index " + index);

                if (index < 0) {
                    throw new BadRequestException("In Bin file firmware_build_id not found! This is a supported on Firmware version > 1.34.1");
                }

                System.out.println("Firmware_ID" + decoded.substring(index, index + 12));


                firmware_build_id = decoded.substring(index, index + 12);
            }

        }

        System.out.print("Kolik mám Errors: " + errors.size());

        return errors.isEmpty() ? null : errors;
    }



}
