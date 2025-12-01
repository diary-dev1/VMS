package com.vms.util;

import com.vms.model.Voucher;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VoucherPDFGenerator {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE = 12;
    private static final float TITLE_FONT_SIZE = 24;
    private static final float HEADER_FONT_SIZE = 16;

    /**
     * Générer un PDF pour un voucher individuel avec QR Code
     */
    public static String generateVoucherPDF(Voucher voucher) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        float yPosition = pageHeight - MARGIN;

        // ========== EN-TÊTE ==========
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("VOUCHER VMS");
        contentStream.endText();

        yPosition -= 40;

        // Ligne de séparation
        contentStream.setLineWidth(2);
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(pageWidth - MARGIN, yPosition);
        contentStream.stroke();

        yPosition -= 40;

        // ========== INFORMATIONS DU VOUCHER ==========
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Code du Voucher:");
        contentStream.endText();

        contentStream.setFont(PDType1Font.HELVETICA, HEADER_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN + 200, yPosition);
        contentStream.showText(voucher.getCode());
        contentStream.endText();

        yPosition -= 30;

        // Client
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Client:");
        contentStream.endText();

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN + 200, yPosition);
        contentStream.showText(voucher.getClientNom());
        contentStream.endText();

        yPosition -= 25;

        // Valeur
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Valeur:");
        contentStream.endText();

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN + 200, yPosition);
        contentStream.showText(String.format("%.2f Rs", voucher.getValeur()));
        contentStream.endText();

        yPosition -= 25;

        // Date d'émission
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Date d'emission:");
        contentStream.endText();

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN + 200, yPosition);
        contentStream.showText(voucher.getDateEmission() != null ? voucher.getDateEmission().toString() : "N/A");
        contentStream.endText();

        yPosition -= 25;

        // Date d'expiration
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Date d'expiration:");
        contentStream.endText();

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN + 200, yPosition);
        contentStream.showText(voucher.getDateExpiration() != null ? voucher.getDateExpiration().toString() : "N/A");
        contentStream.endText();

        yPosition -= 25;

        // Statut
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Statut:");
        contentStream.endText();

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN + 200, yPosition);
        contentStream.showText(voucher.getStatut());
        contentStream.endText();

        yPosition -= 40;

        // Ligne de séparation
        contentStream.setLineWidth(1);
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(pageWidth - MARGIN, yPosition);
        contentStream.stroke();

        yPosition -= 40;

        // ========== QR CODE ==========
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Scannez ce QR Code:");
        contentStream.endText();

        yPosition -= 40;

        // Charger et afficher l'image QR Code
        if (voucher.getQrCode() != null && !voucher.getQrCode().isEmpty()) {
            File qrFile = new File(voucher.getQrCode());
            if (qrFile.exists()) {
                PDImageXObject qrImage = PDImageXObject.createFromFile(voucher.getQrCode(), document);

                float qrSize = 250; // Taille du QR Code
                float xPosition = (pageWidth - qrSize) / 2; // Centrer horizontalement

                contentStream.drawImage(qrImage, xPosition, yPosition - qrSize, qrSize, qrSize);

                yPosition -= qrSize + 40;
            }
        } else {
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("QR Code non disponible");
            contentStream.endText();
            yPosition -= 40;
        }

        // ========== INSTRUCTIONS ==========
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Instructions:");
        contentStream.endText();

        yPosition -= 20;

        contentStream.setFont(PDType1Font.HELVETICA, 10);
        String[] instructions = {
                "1. Presentez ce voucher au magasin partenaire",
                "2. Le magasin scannera le QR Code pour validation",
                "3. Le montant sera deduit de la valeur du voucher",
                "4. Ce voucher est valable jusqu'a la date d'expiration"
        };

        for (String instruction : instructions) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + 10, yPosition);
            contentStream.showText(instruction);
            contentStream.endText();
            yPosition -= 15;
        }

        yPosition -= 20;

        // ========== PIED DE PAGE ==========
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, 50);
        contentStream.showText("VMS - Voucher Management System");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(pageWidth - MARGIN - 150, 50);
        String dateGeneration = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        contentStream.showText("Genere le: " + dateGeneration);
        contentStream.endText();

        contentStream.close();

        // Nom du fichier
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "Voucher_" + voucher.getCode() + "_" + timestamp + ".pdf";

        // Enregistrer
        document.save(filename);
        document.close();

        return filename;
    }

    /**
     * Générer un PDF pour plusieurs vouchers (batch)
     */
    public static String generateBatchVouchersPDF(java.util.List<Voucher> vouchers) throws IOException {
        PDDocument document = new PDDocument();

        for (Voucher voucher : vouchers) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float yPosition = pageHeight - MARGIN;

            // Titre
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("VOUCHER VMS");
            contentStream.endText();

            yPosition -= 40;

            // Ligne
            contentStream.setLineWidth(2);
            contentStream.moveTo(MARGIN, yPosition);
            contentStream.lineTo(pageWidth - MARGIN, yPosition);
            contentStream.stroke();

            yPosition -= 40;

            // Informations basiques
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Code: " + voucher.getCode());
            contentStream.endText();

            yPosition -= 30;

            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Client: " + voucher.getClientNom());
            contentStream.endText();

            yPosition -= 25;

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Valeur: " + String.format("%.2f Rs", voucher.getValeur()));
            contentStream.endText();

            yPosition -= 25;

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Expire le: " + (voucher.getDateExpiration() != null ? voucher.getDateExpiration().toString() : "N/A"));
            contentStream.endText();

            yPosition -= 40;

            // QR Code
            if (voucher.getQrCode() != null && !voucher.getQrCode().isEmpty()) {
                File qrFile = new File(voucher.getQrCode());
                if (qrFile.exists()) {
                    PDImageXObject qrImage = PDImageXObject.createFromFile(voucher.getQrCode(), document);
                    float qrSize = 200;
                    float xPosition = (pageWidth - qrSize) / 2;
                    contentStream.drawImage(qrImage, xPosition, yPosition - qrSize, qrSize, qrSize);
                }
            }

            contentStream.close();
        }

        // Nom du fichier
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "Vouchers_Batch_" + timestamp + ".pdf";

        document.save(filename);
        document.close();

        return filename;
    }
}