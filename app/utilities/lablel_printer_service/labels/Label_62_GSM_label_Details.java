package utilities.lablel_printer_service.labels;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import models.Model_GSM;
import models.Model_HardwareRegistrationEntity;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class Label_62_GSM_label_Details {

    // Logger
    private static final Logger logger = new Logger(Label_62_GSM_label_Details.class);

    // For image placing to cell
    private PdfContentByte contentByte;
    private Rectangle Label_12_mm = new RectangleReadOnly(Utilities.millimetersToPoints(62), Utilities.millimetersToPoints(15));



    Model_GSM gsm = null;

    public Label_62_GSM_label_Details(Model_GSM gsm) {
        try {
            this.gsm = gsm;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ByteArrayOutputStream get_label() {

        ByteArrayOutputStream out = make_label();

        /*
        try(OutputStream outputStream = new FileOutputStream(System.getProperty("user.dir") + "/label_printers/" + "generate_12_mm_detail_" + new Date().getTime()  + ".pdf")) {
            out.writeTo(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        return out;
    }

    private ByteArrayOutputStream make_label() {
        try {

            // step 1: Create Document
            Document document = new Document(Label_12_mm, 1.1F, 0F, 5F, 1.5F);

            // step 2: Get output stream for final File
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // step 3: Create Writer
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // step 4
            writer.setInitialLeading(8);

            // step 5. Open file
            document.open();

            // Step 6. Add QR kode
            contentByte = writer.getDirectContent();
            document.add(details());

            document.close();

            return out;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


    private PdfPCell device_hash_for_Add() throws DocumentException, _Base_Result_Exception, IOException {

        // QR Code for ADD
        BarcodeQRCode barcodeQRCode = new BarcodeQRCode(gsm.registration_hash.toString(), 1000, 1000, null);
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
        // cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        return cell;

    }

    private PdfPTable details() throws DocumentException, _Base_Result_Exception, IOException {

        // 62 = 100%
        // 12 + 30
        PdfPTable bottom = new PdfPTable(2);
        bottom.setWidthPercentage(63);

        bottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        bottom.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        bottom.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        bottom.setWidths(new float[] { 30,  70  });


        PdfPCell cell_qr_code = device_hash_for_Add();
        cell_qr_code.setBorder(Rectangle.RIGHT);
        bottom.addCell(cell_qr_code);



        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 6.3F, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 5.2F, Font.NORMAL);
        Font registFont = new Font(Font.FontFamily.TIMES_ROMAN, 6.3F, Font.BOLD);

        Font font_space = new Font(Font.FontFamily.COURIER, 1.3F, Font.NORMAL, BaseColor.WHITE);



        Phrase phrase_firstLine = new Phrase("MSISDN: " + gsm.MSINumber + " \n", boldFont);
        Phrase thirthLine = new Phrase("Hash: " + gsm.registration_hash + "\n", registFont);


        Phrase mac_address = new Phrase();
        mac_address.add( phrase_firstLine);
        mac_address.add( new Phrase(".....  \n", font_space));
        mac_address.add( thirthLine);

        PdfPCell cell_mac_address = new PdfPCell( mac_address);
        cell_mac_address.setBorder(Rectangle.RIGHT);
        cell_mac_address.setVerticalAlignment(Element.ALIGN_CENTER);
        cell_mac_address.setHorizontalAlignment(Element.ALIGN_CENTER);

        bottom.addCell(cell_mac_address);

        bottom.completeRow();

        return bottom;
    }
}