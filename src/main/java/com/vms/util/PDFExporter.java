package com.vms.util;

import com.vms.model.Magasin;
import com.vms.model.Voucher;
import javafx.collections.ObservableList;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFExporter {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE = 10;
    private static final float TITLE_FONT_SIZE = 16;
    private static final float HEADER_FONT_SIZE = 11;

    // ==================== EXPORT MAGASINS ====================

    public static String exportMagasins(ObservableList<Magasin> magasins) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Titre
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);
        contentStream.showText("LISTE DES MAGASINS");
        contentStream.endText();

        // Date génération
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN - 20);
        String dateGeneration = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        contentStream.showText("Genere le : " + dateGeneration);
        contentStream.endText();

        // Ligne de séparation
        contentStream.moveTo(MARGIN, page.getMediaBox().getHeight() - MARGIN - 35);
        contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, page.getMediaBox().getHeight() - MARGIN - 35);
        contentStream.stroke();

        float yPosition = page.getMediaBox().getHeight() - MARGIN - 55;

        // En-tête du tableau
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Code");
        contentStream.newLineAtOffset(70, 0);
        contentStream.showText("Nom");
        contentStream.newLineAtOffset(150, 0);
        contentStream.showText("Ville");
        contentStream.newLineAtOffset(100, 0);
        contentStream.showText("Telephone");
        contentStream.newLineAtOffset(100, 0);
        contentStream.showText("Statut");
        contentStream.endText();

        yPosition -= 5;

        // Ligne sous en-tête
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
        contentStream.stroke();

        yPosition -= 15;

        // Données
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);

        int pageNumber = 1;
        int itemsOnPage = 0;
        final int MAX_ITEMS_PER_PAGE = 35;

        for (Magasin magasin : magasins) {
            if (itemsOnPage >= MAX_ITEMS_PER_PAGE) {
                // Numéro de page
                contentStream.beginText();
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() / 2 - 10, 30);
                contentStream.showText("Page " + pageNumber);
                contentStream.endText();

                contentStream.close();

                // Nouvelle page
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);

                yPosition = page.getMediaBox().getHeight() - MARGIN;
                itemsOnPage = 0;
                pageNumber++;
            }

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);

            // Code
            contentStream.showText(truncate(magasin.getCode(), 12));
            contentStream.newLineAtOffset(70, 0);

            // Nom
            contentStream.showText(truncate(magasin.getNom(), 25));
            contentStream.newLineAtOffset(150, 0);

            // Ville
            contentStream.showText(truncate(magasin.getVille() != null ? magasin.getVille() : "", 15));
            contentStream.newLineAtOffset(100, 0);

            // Téléphone
            contentStream.showText(truncate(magasin.getTelephone() != null ? magasin.getTelephone() : "", 15));
            contentStream.newLineAtOffset(100, 0);

            // Statut
            contentStream.showText(magasin.isActif() ? "Actif" : "Inactif");

            contentStream.endText();

            yPosition -= 15;
            itemsOnPage++;
        }

        // Statistiques en bas
        yPosition -= 20;
        if (yPosition < 100) {
            contentStream.close();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            yPosition = page.getMediaBox().getHeight() - MARGIN;
        }

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("STATISTIQUES");
        contentStream.endText();

        yPosition -= 20;

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);

        long actifs = magasins.stream().filter(Magasin::isActif).count();
        long inactifs = magasins.size() - actifs;

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Total magasins : " + magasins.size());
        contentStream.endText();

        yPosition -= 15;

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Magasins actifs : " + actifs);
        contentStream.endText();

        yPosition -= 15;

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Magasins inactifs : " + inactifs);
        contentStream.endText();

        // Numéro de page final
        contentStream.beginText();
        contentStream.newLineAtOffset(page.getMediaBox().getWidth() / 2 - 10, 30);
        contentStream.showText("Page " + pageNumber);
        contentStream.endText();

        contentStream.close();

        // Nom du fichier avec timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "Magasins_" + timestamp + ".pdf";

        // Enregistrer
        document.save(filename);
        document.close();

        return filename;
    }

    // ==================== EXPORT VOUCHERS ====================

    public static String exportVouchers(ObservableList<Voucher> vouchers) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Titre
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);
        contentStream.showText("LISTE DES VOUCHERS");
        contentStream.endText();

        // Date génération
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN - 20);
        String dateGeneration = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        contentStream.showText("Genere le : " + dateGeneration);
        contentStream.endText();

        // Ligne de séparation
        contentStream.moveTo(MARGIN, page.getMediaBox().getHeight() - MARGIN - 35);
        contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, page.getMediaBox().getHeight() - MARGIN - 35);
        contentStream.stroke();

        float yPosition = page.getMediaBox().getHeight() - MARGIN - 55;

        // En-tête du tableau
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Code");
        contentStream.newLineAtOffset(100, 0);
        contentStream.showText("Client");
        contentStream.newLineAtOffset(130, 0);
        contentStream.showText("Valeur");
        contentStream.newLineAtOffset(80, 0);
        contentStream.showText("Emission");
        contentStream.newLineAtOffset(80, 0);
        contentStream.showText("Statut");
        contentStream.endText();

        yPosition -= 5;

        // Ligne sous en-tête
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
        contentStream.stroke();

        yPosition -= 15;

        // Données
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);

        int pageNumber = 1;
        int itemsOnPage = 0;
        final int MAX_ITEMS_PER_PAGE = 35;

        for (Voucher voucher : vouchers) {
            if (itemsOnPage >= MAX_ITEMS_PER_PAGE) {
                // Numéro de page
                contentStream.beginText();
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() / 2 - 10, 30);
                contentStream.showText("Page " + pageNumber);
                contentStream.endText();

                contentStream.close();

                // Nouvelle page
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);

                yPosition = page.getMediaBox().getHeight() - MARGIN;
                itemsOnPage = 0;
                pageNumber++;
            }

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);

            // Code
            contentStream.showText(truncate(voucher.getCode(), 18));
            contentStream.newLineAtOffset(100, 0);

            // Client
            contentStream.showText(truncate(voucher.getClientNom(), 20));
            contentStream.newLineAtOffset(130, 0);

            // Valeur
            contentStream.showText(String.format("%.2f Rs", voucher.getValeur()));
            contentStream.newLineAtOffset(80, 0);

            // Date émission
            contentStream.showText(voucher.getDateEmission() != null ? voucher.getDateEmission().toString() : "");
            contentStream.newLineAtOffset(80, 0);

            // Statut
            contentStream.showText(voucher.getStatut());

            contentStream.endText();

            yPosition -= 15;
            itemsOnPage++;
        }

        // Statistiques
        yPosition -= 20;
        if (yPosition < 100) {
            contentStream.close();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            yPosition = page.getMediaBox().getHeight() - MARGIN;
        }

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("STATISTIQUES");
        contentStream.endText();

        yPosition -= 20;

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);

        double montantTotal = vouchers.stream().mapToDouble(Voucher::getValeur).sum();

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Total vouchers : " + vouchers.size());
        contentStream.endText();

        yPosition -= 15;

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Montant total : " + String.format("%.2f Rs", montantTotal));
        contentStream.endText();

        // Numéro de page final
        contentStream.beginText();
        contentStream.newLineAtOffset(page.getMediaBox().getWidth() / 2 - 10, 30);
        contentStream.showText("Page " + pageNumber);
        contentStream.endText();

        contentStream.close();

        // Nom du fichier avec timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "Vouchers_" + timestamp + ".pdf";

        // Enregistrer
        document.save(filename);
        document.close();

        return filename;
    }

    // ==================== MÉTHODE UTILITAIRE ====================

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}