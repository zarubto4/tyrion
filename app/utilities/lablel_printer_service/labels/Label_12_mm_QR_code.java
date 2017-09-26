package utilities.lablel_printer_service.labels;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import models.Model_Board;
import utilities.logger.Class_Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

public class Label_12_mm_QR_code {

    // Logger
    private static final Class_Logger terminal_logger = new Class_Logger(Label_12_mm_QR_code.class);

    // For image placing to cell
    private PdfContentByte contentByte;
    private Rectangle Label_12_mm = new RectangleReadOnly(Utilities.millimetersToPoints(12), Utilities.millimetersToPoints(12));


    Model_Board board = null;

    public Label_12_mm_QR_code(Model_Board board) {
        try {
            this.board = board;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ByteArrayOutputStream get_label(){

        ByteArrayOutputStream out = make_label();

        // Zkusím prototypově uložit
        // TODO smazat protože to nebude potřeba
        try(OutputStream outputStream = new FileOutputStream(System.getProperty("user.dir") + "/label_printers/" + "generate_12_mm_qr_code_" + new Date().getTime()  + ".pdf")) {
            out.writeTo(outputStream);
        }catch (Exception e){

        }

        return out;
    }

    private ByteArrayOutputStream make_label(){
        try {

            // step 2: Create Document
            Document document = new Document(Label_12_mm, Utilities.millimetersToPoints(0), Utilities.millimetersToPoints(0), Utilities.millimetersToPoints(0), Utilities.millimetersToPoints(0));

            // step 3: Get output stream for final File
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // step 4: Create Writer
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // step 2
            writer.setInitialLeading(12);

            // step 5. Open file
            document.open();


            contentByte = writer.getDirectContent();

            // Step 6.  Add QR kode
            document.add(device_hash_for_Add(this.board.hash_for_adding));



            document.close();

            return out;

        }catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }


    private PdfPTable device_hash_for_Add(String hash_for_Add) throws DocumentException {

        PdfPTable table = new PdfPTable(1);
                table.setTotalWidth(Label_12_mm.getWidth());
                table.getDefaultCell().setFixedHeight(Label_12_mm.getWidth());
                table.setLockedWidth(true);
                table.setWidthPercentage(100);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

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

        table.addCell(cell);

        return  table;
    }




}