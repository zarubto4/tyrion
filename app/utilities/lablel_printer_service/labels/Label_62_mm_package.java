package utilities.lablel_printer_service.labels;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import controllers._BaseController;
import models.*;
import mongo.ModelMongo_Hardware_BatchCollection;
import mongo.ModelMongo_Hardware_RegistrationEntity;
import utilities.logger.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.Date;

/**
 * Na KRABIČKu IODY
 */
public class Label_62_mm_package {

    // Logger
    private static final Logger terminal_logger = new Logger(Label_62_mm_package.class);

    // For image placing to cell
    private PdfContentByte contentByte;
    private Rectangle Label_65_mm_Antistatic_Package = new RectangleReadOnly(Utilities.millimetersToPoints(62), Utilities.millimetersToPoints(75));

    ModelMongo_Hardware_RegistrationEntity board = null;
    ModelMongo_Hardware_BatchCollection print_info = null;
    Model_HardwareType print_type = null;
    Model_Garfield garfield = null;

    public Label_62_mm_package(ModelMongo_Hardware_RegistrationEntity board, ModelMongo_Hardware_BatchCollection batch, Model_HardwareType print_type, Model_Garfield garfield) throws IllegalArgumentException{

        this.board = board;
        this.print_info = batch;
        this.garfield = garfield;
        this.print_type = print_type;

        Model_CProgramVersion test_version = print_type.test_program.default_main_version;
        Model_CProgramVersion production_version = print_type.get_main_c_program().default_main_version;

        if (test_version == null) {
            terminal_logger.error("Label_62_mm_package:: Test Firmware is not set");
        }

        if (production_version == null) {
            terminal_logger.error("Label_62_mm_package:: Production Firmware is not set");
        }

        if (garfield == null) {
            throw new IllegalArgumentException("Garfield is not set");
        }
    }

    public ByteArrayOutputStream get_label() {

        try {

            ByteArrayOutputStream out = make_label();

            // Zkusím prototypově uložit
            /* TODO smazat protože to nebude potřeba
            try (OutputStream outputStream = new FileOutputStream(System.getProperty("user.dir") + "/label_printers/" + "generate_" + new Date().getTime() + ".pdf")) {
                out.writeTo(outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            */
            return out;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            e.printStackTrace();
            return null;
        }
    }

    private ByteArrayOutputStream make_label() {
        try {

            // step 2: Create Document
            Document document = new Document(Label_65_mm_Antistatic_Package, Utilities.millimetersToPoints(2), Utilities.millimetersToPoints(2), Utilities.millimetersToPoints(3), Utilities.millimetersToPoints(2));

            // step 3: Get output stream for final File
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // step 4: Create Writer
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // step 2
            writer.setInitialLeading(12);

            // step 5. Open file
            document.open();

            // Step 6. Add elements
            // Add Head
            document.add(head());

            contentByte = writer.getDirectContent();

            // Add Basic info details
            document.add(info_details());

            // Add Device Barcode
            document.add(device_id_barcode());

            // Add Bottom
            document.add(bottom());

            document.close();

            return out;

        } catch (Exception e) {
            e.printStackTrace();
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    private PdfPTable head() {

        PdfPTable head = new PdfPTable(1);
        head.setWidthPercentage(100);
        //head.setTotalWidth(Utilities.millimetersToPoints(66));

        Font h1 = new Font();
             h1.setSize(10F);

        Phrase title = new Phrase(print_info.customer_product_name);
               title.setFont(h1);

        PdfPCell cell = new PdfPCell(title);
                 cell.setVerticalAlignment(Element.ALIGN_CENTER);
                 cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        head.addCell(cell);

        return head;
    }

    private PdfPTable info_details() throws ParseException {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        Font regular = new Font(Font.FontFamily.TIMES_ROMAN, 5);
        Font bold = new Font(Font.FontFamily.TIMES_ROMAN, 5, Font.BOLD);

        // the cell object
        PdfPCell cell_Right = new PdfPCell();

            Paragraph p_code = new Paragraph("Code: ", bold);
                      p_code.add(new Chunk(print_type.name, regular));

            Paragraph p_product = new Paragraph("Product Revision: ", bold);
                      p_product.add(new Chunk(print_info.revision , regular));

            Paragraph p_batch = new Paragraph("Production batch_id: ", bold);
                      p_batch.add(new Chunk(print_info.production_batch, regular));

            Paragraph p_made = new Paragraph("Made: ", bold);
                      p_made.add(new Chunk( print_info.date_of_assembly.toString(), regular));

        cell_Right.addElement(p_code);
        cell_Right.addElement(p_product);
        cell_Right.addElement(p_batch);
        cell_Right.addElement(p_made);


        PdfPCell cell_Left = new PdfPCell();


            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

            String date = DATE_FORMAT.format(board.created);

            Paragraph p_tested = new Paragraph("Tested: ", bold);
                      p_tested.add(new Chunk(date, regular));


            print_type.main_test_c_program();


        Model_CProgramVersion test_version = print_type.test_program.default_main_version;
        Model_CProgramVersion production_version = print_type.get_main_c_program().default_main_version;

            Paragraph p_test_version = new Paragraph("FW Test Version: ", bold);
                      p_test_version.add(new Chunk(test_version != null ? test_version.name : "Not Tracked ", regular));

            Paragraph p_prod_version = new Paragraph("FW Prod Version: ", bold);
                      p_prod_version.add(new Chunk(production_version != null ? production_version.name : "Not Tracked ", regular));

            Paragraph p_who_tested = new Paragraph("Who tested it: ", bold);
                      p_who_tested.add(new Chunk(_BaseController.person().full_name(), regular));

            Paragraph station = new Paragraph("Test Station: ", bold);
                      station.add(new Chunk(garfield.name, regular));

        cell_Left.addElement(p_tested);
        cell_Left.addElement(p_test_version);
        cell_Left.addElement(p_prod_version);
        cell_Left.addElement(p_who_tested);
        cell_Left.addElement(station);

        table.addCell(cell_Right);
        table.addCell(cell_Left);

        return table;
    }

    private PdfPTable device_id_barcode() throws DocumentException, IOException {


        PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.setWidths(new float[] { 50, 50 });

        // Font
        Font bold = new Font(Font.FontFamily.TIMES_ROMAN, 6, Font.BOLD);
        Font regular = new Font(Font.FontFamily.TIMES_ROMAN, 5);

        // EAN Code
        BarcodeEAN ean_code = new BarcodeEAN();
        ean_code.setCodeType(Barcode.EAN13); // 13 characters.
        ean_code.setCode(print_info.ean_number.toString());

        Image ean_code_image = ean_code.createImageWithBarcode(contentByte, null, null);
        ean_code_image.setAlignment(Image.ALIGN_MIDDLE);

        Paragraph ean_description = new Paragraph("EAN ", bold);
        ean_description.setAlignment(Element.ALIGN_MIDDLE);

        Paragraph add_description = new Paragraph("Registration Hash ", bold);
        add_description.setAlignment(Element.ALIGN_MIDDLE);


        // QR Code for ADD
        BarcodeQRCode barcodeQRCode = new BarcodeQRCode(board.hash_for_adding, 1000, 1000, null);
        Image codeQrImage = barcodeQRCode.getImage();
        codeQrImage.scaleAbsolute(50 , 50);


        PdfPCell cell_Right = new PdfPCell(codeQrImage, true);
                 cell_Right.setBorder(Rectangle.NO_BORDER);
                 cell_Right.setVerticalAlignment(Element.ALIGN_MIDDLE);
                 cell_Right.setHorizontalAlignment(Element.ALIGN_CENTER);
                 cell_Right.setRowspan(4);
                 table.addCell(cell_Right);


        // Left Cell 1
        Paragraph p_code = new Paragraph("Processor ID ", bold);
                  p_code.setAlignment(Element.ALIGN_MIDDLE);
                  p_code.add( Chunk.NEWLINE );
                  p_code.add(new Chunk(board.full_id, regular));
                  table.addCell(p_code);

        // Left Cell 2
        Paragraph database_id = new Paragraph("Database ID ", bold);
                  database_id.add( Chunk.NEWLINE );
                  database_id.add(new Chunk(board.full_id, regular));
                  table.addCell(database_id);

        // Left Cell 3
        Paragraph mac_add = new Paragraph("Mac Address: ", bold);
                  mac_add.add(new Chunk(board.mac_address, regular));
                  table.addCell(mac_add);

        // Left Cell 4
        PdfPCell ean_cell = new PdfPCell(ean_code_image);
                 ean_cell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
                 table.addCell(ean_cell);



        PdfPCell cell_Right_2 = new PdfPCell(add_description);
                 cell_Right_2.setHorizontalAlignment(Element.ALIGN_CENTER);
                 cell_Right_2.setBorder(Rectangle.NO_BORDER);
                 table.addCell(cell_Right_2);

        PdfPCell ean_description_cell = new PdfPCell(ean_description);
                ean_description_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                ean_description_cell.setBorder(Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT);
                table.addCell(ean_description_cell);


        return  table;
    }

    private PdfPTable bottom() {

        PdfPTable bottom = new PdfPTable(1);
        bottom.setWidthPercentage(100);

        Font h1 = new Font();
             h1.setSize(10F);

        Font h3 = new Font();
             h3.setSize(5F);

        Phrase company_name = new Phrase(print_info.customer_company_name, h1);

        PdfPCell cell_company_name = new PdfPCell(company_name);
                 cell_company_name.setVerticalAlignment(Element.ALIGN_CENTER);
                 cell_company_name.setHorizontalAlignment(Element.ALIGN_CENTER);


        Phrase made_in = new Phrase(print_info.customer_company_made_description, h3);

        PdfPCell cell_made_in = new PdfPCell(made_in);
                 cell_made_in.setVerticalAlignment(Element.ALIGN_CENTER);
                 cell_made_in.setHorizontalAlignment(Element.ALIGN_CENTER);

        bottom.addCell(cell_company_name);
        bottom.addCell(cell_made_in);

        bottom.completeRow();

        return bottom;
    }



}