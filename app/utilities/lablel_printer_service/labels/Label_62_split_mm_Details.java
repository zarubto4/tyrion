package utilities.lablel_printer_service.labels;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import models.Model_Board;
import utilities.logger.Class_Logger;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPage;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;


public class Label_62_split_mm_Details {

    // Logger
    private static final Class_Logger terminal_logger = new Class_Logger(Label_62_split_mm_Details.class);

    // For image placing to cell
    private PdfContentByte contentByte;
    private Rectangle Label_12_mm = new RectangleReadOnly(Utilities.millimetersToPoints(62), Utilities.millimetersToPoints(16));


    Model_Board board = null;

    public Label_62_split_mm_Details(Model_Board board) {
        try {

            this.board = board;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ByteArrayOutputStream get_label(){

        ByteArrayOutputStream out = make_label();

        // TODO smazat protože to nebude potřeba
        try(OutputStream outputStream = new FileOutputStream(System.getProperty("user.dir") + "/label_printers/" + "generate_12_mm_detail_" + new Date().getTime()  + ".pdf")) {
            out.writeTo(outputStream);
        }catch (Exception e){

        }
        return out;
    }

    private ByteArrayOutputStream make_label(){
        try {

            // step 1: Create Document
            Document document = new Document(Label_12_mm, Utilities.millimetersToPoints(1), Utilities.millimetersToPoints(1), Utilities.millimetersToPoints(2), Utilities.millimetersToPoints(1));

            // step 2: Get output stream for final File
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // step 3: Create Writer
            PdfWriter writer = PdfWriter.getInstance(document, out);


            // step 4
            writer.setInitialLeading(12);

            // step 5. Open file
            document.open();

            // Step 6.  Add QR kode
            contentByte = writer.getDirectContent();
            document.add(details());

            document.close();

            return out;

        }catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }


    private PdfPCell device_hash_for_Add() throws DocumentException {


        // QR Code for ADD
        // QR Code for ADD
        BarcodeQRCode barcodeQRCode = new BarcodeQRCode(this.board.hash_for_adding, 1000, 1000, null);
        Image codeQrImage = barcodeQRCode.getImage();
        codeQrImage.scaleToFit(Label_12_mm.getWidth(), Label_12_mm.getWidth());

        int odsazeni = 0;

        PdfTemplate template = contentByte.createTemplate(
                Label_12_mm.getHeight() - odsazeni - odsazeni,
                Label_12_mm.getHeight() - odsazeni - odsazeni);
        template.addImage(codeQrImage,
                Label_12_mm.getHeight(), 0, 0,
                Label_12_mm.getHeight(), -odsazeni, -odsazeni);


        PdfPCell cell = new PdfPCell(Image.getInstance(template), true);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        return cell;

    }

    private PdfPTable details() throws DocumentException {

        PdfPTable bottom = new PdfPTable(2);
        bottom.setTotalWidth(80);
        bottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        bottom.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        bottom.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        bottom.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        bottom.setWidths(new float[] { 25, 75 });



        PdfPCell cell_qr_code = device_hash_for_Add();
        cell_qr_code.setBorder(Rectangle.RIGHT);
        cell_qr_code.setRowspan(3);
        bottom.addCell(cell_qr_code);


        Font h1 = new Font();
        h1.setSize(7F);

        Font h3 = new Font();
        h3.setSize(6F);

        Font h5 = new Font();
        h5.setSize(3.9F);


        // Mac Address ID
        Phrase mac_address = new Phrase("MAC: " +board.mac_address, h1);

        PdfPCell cell_company_name = new PdfPCell( mac_address);
        cell_company_name.setBorder(Rectangle.NO_BORDER);
        cell_company_name.setBorder(Rectangle.RIGHT);
        cell_company_name.setVerticalAlignment(Element.ALIGN_CENTER);
        cell_company_name.setHorizontalAlignment(Element.ALIGN_CENTER);


        // Processor ID
        Phrase processor_id = new Phrase("ID: "+ board.id, h3);

        PdfPCell cell_processor_id = new PdfPCell(processor_id);
        cell_processor_id.setBorder(Rectangle.NO_BORDER);
        cell_processor_id.setBorder(Rectangle.RIGHT);
        cell_processor_id.setVerticalAlignment(Element.ALIGN_CENTER);
        cell_processor_id.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Processor ID
        Phrase registration_id = new Phrase("Reg: " + board.hash_for_adding, h5);

        PdfPCell cell_registration_id = new PdfPCell(registration_id);
        cell_registration_id.setBorder(Rectangle.NO_BORDER);
        cell_registration_id.setBorder(Rectangle.RIGHT);
        cell_registration_id.setVerticalAlignment(Element.ALIGN_CENTER);
        cell_registration_id.setHorizontalAlignment(Element.ALIGN_CENTER);


        bottom.addCell(cell_company_name);
        bottom.addCell(cell_processor_id);
        bottom.addCell(cell_registration_id);

        bottom.completeRow();

        return bottom;
    }




}