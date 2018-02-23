package utilities.lablel_printer_service.labels;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import models.Model_Hardware;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.hardware_registration_auhtority.Hardware_Registration_Authority;
import utilities.hardware_registration_auhtority.DM_Board_Registration_Central_Authority;
import utilities.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Label_12_mm_QR_code {

    // Logger
    private static final Logger terminal_logger = new Logger(Label_12_mm_QR_code.class);

    // For image placing to cell
    private PdfContentByte contentByte;
    private Rectangle Label_12_mm = new RectangleReadOnly(Utilities.millimetersToPoints(12), Utilities.millimetersToPoints(12));


    Model_Hardware hardware = null;

    public Label_12_mm_QR_code(Model_Hardware hardware) {
        try {
            this.hardware = hardware;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ByteArrayOutputStream get_label() {

        ByteArrayOutputStream out = make_label();

        return out;
    }

    private ByteArrayOutputStream make_label() {
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
            document.add(device_hash_for_Add());

            document.close();

            return out;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }


    private PdfPTable device_hash_for_Add() throws DocumentException, _Base_Result_Exception, IOException {

        PdfPTable table = new PdfPTable(1);
                table.setTotalWidth(Label_12_mm.getWidth());
                table.getDefaultCell().setFixedHeight(Label_12_mm.getWidth());
                table.setLockedWidth(true);
                table.setWidthPercentage(100);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

        // Mac Address ID
        DM_Board_Registration_Central_Authority hw = Hardware_Registration_Authority.get_registration_hardware_from_central_authority_by_full_id(hardware.full_id);
        if(hw == null) {
            throw new Result_Error_NotFound(Model_Hardware.class);
        }

        // QR Code for ADD
        // QR Code for ADD
        BarcodeQRCode barcodeQRCode = new BarcodeQRCode(hw.hash_for_adding, 1000, 1000, null);
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