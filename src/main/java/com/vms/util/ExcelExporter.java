package com.vms.util;

import com.vms.database.DatabaseConnection;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class ExcelExporter {public static String exporter(String nomFichier) throws Exception {
    Connection conn = DatabaseConnection.getConnection();
    XSSFWorkbook workbook = new XSSFWorkbook();

    XSSFCellStyle entete = creerStyleEntete(workbook);

    remplirOnglet(workbook, conn, "Toutes les demandes",
            "SELECT * FROM rapport_toutes_demandes", entete);

    remplirOnglet(workbook, conn, "Attente paiement",
            "SELECT * FROM rapport_attente_paiement", entete);

    remplirOnglet(workbook, conn, "Vouchers",
            "SELECT * FROM rapport_vouchers", entete);

    String chemin = nomFichier + ".xlsx";
    FileOutputStream sortie = new FileOutputStream(chemin);
    workbook.write(sortie);
    sortie.close();
    workbook.close();

    return chemin;
}

    private static XSSFCellStyle creerStyleEntete(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont police = workbook.createFont();
        police.setBold(true);
        police.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(police);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static void remplirOnglet(XSSFWorkbook workbook, Connection conn,
                                      String nomOnglet, String requete,
                                      XSSFCellStyle entete) throws Exception {
        XSSFSheet feuille = workbook.createSheet(nomOnglet);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(requete);
        ResultSetMetaData meta = rs.getMetaData();
        int colonnes = meta.getColumnCount();

        XSSFRow ligneEntete = feuille.createRow(0);
        for (int i = 1; i <= colonnes; i++) {
            var cellule = ligneEntete.createCell(i - 1);
            cellule.setCellValue(meta.getColumnLabel(i));
            cellule.setCellStyle(entete);
        }

        int ligne = 1;
        while (rs.next()) {
            XSSFRow row = feuille.createRow(ligne++);
            for (int i = 1; i <= colonnes; i++) {
                Object val = rs.getObject(i);
                if (val != null) {
                    row.createCell(i - 1).setCellValue(val.toString());
                }
            }
        }

        for (int i = 0; i < colonnes; i++) {
            feuille.autoSizeColumn(i);
        }

        rs.close();
        stmt.close();
    }
}
