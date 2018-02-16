package utilities.lablel_printer_service.labels;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.PdfWriter;
import models.Model_Hardware;
import utilities.logger.Logger;

import java.io.ByteArrayOutputStream;


public class Label_62_split_mm_Details {

    // Logger
    private static final Logger logger = new Logger(Label_62_split_mm_Details.class);

    // For image placing to cell
    private PdfContentByte contentByte;
    private Rectangle Label_12_mm = new RectangleReadOnly(Utilities.millimetersToPoints(62), Utilities.millimetersToPoints(15));


    Model_Hardware hardware = null;

    public Label_62_split_mm_Details(Model_Hardware hardware) {
        try {

            this.hardware = hardware;

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

            // Step 6.  Add QR kode
            contentByte = writer.getDirectContent();
            document.add(details());

            document.close();

            return out;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


    private PdfPCell device_hash_for_Add() throws DocumentException {

        // QR Code for ADD
        BarcodeQRCode barcodeQRCode = new BarcodeQRCode(this.hardware.registration_hash, 1000, 1000, null);
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

    private PdfPTable details() throws DocumentException {

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



        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 5.7F, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 5.2F, Font.NORMAL);
        Font registFont = new Font(Font.FontFamily.TIMES_ROMAN, 4.6F, Font.BOLD);

        Font font_space = new Font(Font.FontFamily.COURIER, 1.3F, Font.NORMAL, BaseColor.WHITE);

        // Mac Address ID


        Phrase phrase_firstLine = new Phrase("MAC: " + hardware.mac_address + " \n", boldFont);
        Phrase secondLine = new Phrase("ID: "+ hardware.id +  " \n" , normalFont );
        Phrase thirthLine = new Phrase("Registration: " + hardware.registration_hash + "\n", registFont);


        Phrase mac_address = new Phrase();
        mac_address.add( phrase_firstLine);
        mac_address.add( new Phrase(".....  \n", font_space));
        mac_address.add( secondLine);
        mac_address.add( new Phrase(".....  \n", font_space));
        mac_address.add( thirthLine);

        PdfPCell cell_mac_address = new PdfPCell( mac_address);
        cell_mac_address.setBorder(Rectangle.RIGHT);
        cell_mac_address.setVerticalAlignment(Element.ALIGN_CENTER);
        cell_mac_address.setHorizontalAlignment(Element.ALIGN_CENTER);

/*
        // Processor ID
         Phrase processor_id = new Phrase("ID: "+ hardware.id, h3);
        PdfPCell cell_processor_id = new PdfPCell(processor_id);
        cell_processor_id.setBorder(Rectangle.RIGHT);
        cell_processor_id.setVerticalAlignment(Element.ALIGN_CENTER);
        cell_processor_id.setHorizontalAlignment(Element.ALIGN_CENTER);



        // Processor ID
        Phrase registration_id = new Phrase("Registration: " + hardware.registration_hash.substring(0, 13) + "\n" + hardware.registration_hash.substring(14), h5);

        PdfPCell cell_registration_id = new PdfPCell(registration_id);
        cell_registration_id.setFixedHeight(3F);
        cell_registration_id.setBorder(Rectangle.RIGHT);
        cell_registration_id.setVerticalAlignment(Element.ALIGN_CENTER);
        cell_registration_id.setHorizontalAlignment(Element.ALIGN_CENTER);

*/

        bottom.addCell(cell_mac_address);
        // bottom.addCell(cell_processor_id);
        // bottom.addCell(cell_registration_id);

        bottom.completeRow();

        return bottom;
    }




}