package com.vms.dao;

import com.vms.database.DatabaseConnection;
import com.vms.model.Voucher;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {

    public List<Voucher> getAllVouchers() throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        String query = "SELECT v.*, d.reference as demande_ref, c.nom as client_nom, m.nom as magasin_nom " +
                "FROM vouchers v " +
                "LEFT JOIN demandes d ON v.demande_id = d.id " +
                "LEFT JOIN clients c ON v.client_id = c.id " +
                "LEFT JOIN magasins m ON v.magasin_id = m.id " +
                "ORDER BY v.date_emission DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                vouchers.add(extractVoucherFromResultSet(rs));
            }
        }

        return vouchers;
    }

    public Voucher getVoucherById(int id) throws SQLException {
        String query = "SELECT v.*, d.reference as demande_ref, c.nom as client_nom, m.nom as magasin_nom " +
                "FROM vouchers v " +
                "LEFT JOIN demandes d ON v.demande_id = d.id " +
                "LEFT JOIN clients c ON v.client_id = c.id " +
                "LEFT JOIN magasins m ON v.magasin_id = m.id " +
                "WHERE v.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractVoucherFromResultSet(rs);
            }
        }

        return null;
    }

    public Voucher getVoucherByCode(String code) throws SQLException {
        String query = "SELECT v.*, d.reference as demande_ref, c.nom as client_nom, m.nom as magasin_nom " +
                "FROM vouchers v " +
                "LEFT JOIN demandes d ON v.demande_id = d.id " +
                "LEFT JOIN clients c ON v.client_id = c.id " +
                "LEFT JOIN magasins m ON v.magasin_id = m.id " +
                "WHERE v.code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractVoucherFromResultSet(rs);
            }
        }

        return null;
    }

    public int createVoucher(Voucher voucher) throws SQLException {
        String query = "INSERT INTO vouchers (code, demande_id, client_id, magasin_id, valeur, " +
                "statut, date_emission, date_expiration, qr_code, remarques) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, voucher.getCode());
            pstmt.setObject(2, voucher.getDemandeId() > 0 ? voucher.getDemandeId() : null);
            pstmt.setInt(3, voucher.getClientId());
            pstmt.setObject(4, voucher.getMagasinId() > 0 ? voucher.getMagasinId() : null);
            pstmt.setDouble(5, voucher.getValeur());
            pstmt.setString(6, voucher.getStatut());
            pstmt.setDate(7, Date.valueOf(voucher.getDateEmission()));
            pstmt.setDate(8, Date.valueOf(voucher.getDateExpiration()));
            pstmt.setString(9, voucher.getQrCode());
            pstmt.setString(10, voucher.getRemarques());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public boolean updateVoucher(Voucher voucher) throws SQLException {
        String query = "UPDATE vouchers SET statut = ?, magasin_id = ?, date_utilisation = ?, remarques = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, voucher.getStatut());
            pstmt.setObject(2, voucher.getMagasinId() > 0 ? voucher.getMagasinId() : null);
            pstmt.setObject(3, voucher.getDateUtilisation() != null ? Date.valueOf(voucher.getDateUtilisation()) : null);
            pstmt.setString(4, voucher.getRemarques());
            pstmt.setInt(5, voucher.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteVoucher(int id) throws SQLException {
        String query = "DELETE FROM vouchers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Voucher> getVouchersByStatut(String statut) throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        String query = "SELECT v.*, d.reference as demande_ref, c.nom as client_nom, m.nom as magasin_nom " +
                "FROM vouchers v " +
                "LEFT JOIN demandes d ON v.demande_id = d.id " +
                "LEFT JOIN clients c ON v.client_id = c.id " +
                "LEFT JOIN magasins m ON v.magasin_id = m.id " +
                "WHERE v.statut = ? ORDER BY v.date_emission DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                vouchers.add(extractVoucherFromResultSet(rs));
            }
        }

        return vouchers;
    }

    public List<Voucher> getVouchersByClient(int clientId) throws SQLException {
        List<Voucher> vouchers = new ArrayList<>();
        String query = "SELECT v.*, d.reference as demande_ref, c.nom as client_nom, m.nom as magasin_nom " +
                "FROM vouchers v " +
                "LEFT JOIN demandes d ON v.demande_id = d.id " +
                "LEFT JOIN clients c ON v.client_id = c.id " +
                "LEFT JOIN magasins m ON v.magasin_id = m.id " +
                "WHERE v.client_id = ? ORDER BY v.date_emission DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                vouchers.add(extractVoucherFromResultSet(rs));
            }
        }

        return vouchers;
    }

    private Voucher extractVoucherFromResultSet(ResultSet rs) throws SQLException {
        Voucher voucher = new Voucher();
        voucher.setId(rs.getInt("id"));
        voucher.setCode(rs.getString("code"));
        voucher.setDemandeId(rs.getInt("demande_id"));
        voucher.setDemandeReference(rs.getString("demande_ref"));
        voucher.setClientId(rs.getInt("client_id"));
        voucher.setClientNom(rs.getString("client_nom"));
        voucher.setMagasinId(rs.getInt("magasin_id"));
        voucher.setMagasinNom(rs.getString("magasin_nom"));
        voucher.setValeur(rs.getDouble("valeur"));
        voucher.setStatut(rs.getString("statut"));

        Date dateEmission = rs.getDate("date_emission");
        if (dateEmission != null) {
            voucher.setDateEmission(dateEmission.toLocalDate());
        }

        Date dateExpiration = rs.getDate("date_expiration");
        if (dateExpiration != null) {
            voucher.setDateExpiration(dateExpiration.toLocalDate());
        }

        Date dateUtilisation = rs.getDate("date_utilisation");
        if (dateUtilisation != null) {
            voucher.setDateUtilisation(dateUtilisation.toLocalDate());
        }

        voucher.setQrCode(rs.getString("qr_code"));
        voucher.setRemarques(rs.getString("remarques"));

        return voucher;
    }
}