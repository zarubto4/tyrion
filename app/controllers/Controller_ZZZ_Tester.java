package controllers;

import com.itextpdf.text.pdf.qrcode.Mode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.Model_Board;
import models.Model_CProgram;
import models.Model_Product;
import models.Model_VersionObject;
import org.mindrot.jbcrypt.BCrypt;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.lablel_printer_service.Printer_Api;
import utilities.lablel_printer_service.labels.Label_12_mm;
import utilities.lablel_printer_service.printNodeModels.PrinterOption;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.response.GlobalResult;
import utilities.scheduler.jobs.Job_SpendingCredit;

import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_ZZZ_Tester.class);

    @ApiOperation(value = "Hidden test Method", hidden = true)
     public Result test1(){
         try {

             String id = request().body().asJson().get("id").asText();
             if (id == null) return badRequest("id is null");

             Model_Product product = Model_Product.get_byId(id);

             Job_SpendingCredit.spend(product);

             return ok("Credit was spent");

         }catch (Exception e){
             terminal_logger.internalServerError(e);
             return badRequest();
         }
     }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test2(){
        try {

            // terminal_logger.error(BCrypt.hashpw("password", BCrypt.gensalt(12)));

            return GlobalResult.result_ok();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3(){
        try {

            System.out.println("Testuji Apu Printer");

            Printer_Api api = new Printer_Api();

            Model_Board board = new Model_Board();
            board.hash_for_adding = UUID.randomUUID().toString();

            PrinterOption option = new PrinterOption();
            option.media = "label";
            option.dpi = "100800";

            // Test of printer
            Label_12_mm label_12_mm = new Label_12_mm(board);

           // api.printFile(279215, 1, "Garfield Print QR Hash", label_12_mm.get_label());
              api.printFile(279214, 1, "Garfield Print QR Hash", label_12_mm.get_label(), option);
           // api.printFile(279213, 1, "Garfield Print QR Hash", label_12_mm.get_label());


            return GlobalResult.result_ok();
        }catch (Exception e){
            e.printStackTrace();
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}