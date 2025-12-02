package com.vms.controller;

import com.vms.Main;
import com.vms.model.Voucher;
import com.vms.model.Magasin;
import com.vms.dao.VoucherDAO;
import com.vms.dao.MagasinDAO;
import com.vms.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.io.IOException;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class RedemptionController {

    @FXML private Label userNameLabel;
    @FXML private TextField txtCodeVoucher;
    @FXML private ComboBox<Magasin> comboMagasin;
    @FXML private TextField txtMontantUtilise;
    @FXML private TextArea txtRemarques;

    @FXML private VBox panelInfoVoucher;
    @FXML private Label lblCodeVoucher;
    @FXML private Label lblClient;
    @FXML private Label lblValeur;
    @FXML private Label lblDateExpiration;
    @FXML private Label lblStatut;
    @FXML private ImageView imgQRCode;

    @FXML private TableView<HistoriqueRedemption> tableHistorique;
    @FXML private TableColumn<HistoriqueRedemption, String> colCode;
    @FXML private TableColumn<HistoriqueRedemption, String> colMagasin;
    @FXML private TableColumn<HistoriqueRedemption, String> colDate;
    @FXML private TableColumn<HistoriqueRedemption, Double> colMontant;
    @FXML private TableColumn<HistoriqueRedemption, String> colPar;

    @FXML private Label totalValides;
    @FXML private Label montantTotal;

    private ObservableList<Magasin> listeMagasins;
    private ObservableList<HistoriqueRedemption> listeHistorique;
    private Voucher voucherActuel;

    @FXML
    public void initialize() {
        listeMagasins = FXCollections.observableArrayList();
        listeHistorique = FXCollections.observableArrayList();

        configurerTableau();
        chargerMagasins();
        chargerHistorique();
        mettreAJourStatistiques();

        userNameLabel.setText("Admin");
        panelInfoVoucher.setVisible(false);
        panelInfoVoucher.setManaged(false);
    }

    private void chargerMagasins() {
        try {
            MagasinDAO dao = new MagasinDAO();
            List<Magasin> magasins = dao.getMagasinsActifs();
            listeMagasins.clear();
            listeMagasins.addAll(magasins);
            comboMagasin.setItems(listeMagasins);
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement magasins : " + e.getMessage());
        }
    }

    private void configurerTableau() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("codeVoucher"));
        colMagasin.setCellValueFactory(new PropertyValueFactory<>("magasinNom"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateRedemption"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colPar.setCellValueFactory(new PropertyValueFactory<>("redemmePar"));

        tableHistorique.setItems(listeHistorique);
    }

    private void chargerHistorique() {
        try {
            // Requête pour récupérer l'historique
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT hr.*, v.code as voucher_code, m.nom as magasin_nom " +
                    "FROM historique_redemptions hr " +
                    "LEFT JOIN vouchers v ON hr.voucher_id = v.id " +
                    "LEFT JOIN magasins m ON hr.magasin_id = m.id " +
                    "ORDER BY hr.date_redemption DESC LIMIT 50";

            PreparedStatement stmt = conn.prepareStatement(query);
            var rs = stmt.executeQuery();

            listeHistorique.clear();

            while (rs.next()) {
                HistoriqueRedemption hr = new HistoriqueRedemption();
                hr.setId(rs.getInt("id"));
                hr.setVoucherId(rs.getInt("voucher_id"));
                hr.setCodeVoucher(rs.getString("voucher_code"));
                hr.setMagasinId(rs.getInt("magasin_id"));
                hr.setMagasinNom(rs.getString("magasin_nom"));
                hr.setDateRedemption(rs.getTimestamp("date_redemption").toLocalDateTime().toString());
                hr.setRedemmePar(rs.getString("redemme_par"));
                hr.setMontant(rs.getDouble("montant"));
                hr.setRemarques(rs.getString("remarques"));

                listeHistorique.add(hr);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement historique : " + e.getMessage());
        }
    }

    @FXML
    private void rechercherVoucher() {
        String code = txtCodeVoucher.getText().trim();

        if (code.isEmpty()) {
            afficherErreur("Erreur", "Veuillez saisir un code voucher");
            return;
        }

        try {
            VoucherDAO dao = new VoucherDAO();
            Voucher voucher = dao.getVoucherByCode(code);

            if (voucher == null) {
                afficherErreur("Voucher introuvable", "Aucun voucher avec le code : " + code);
                panelInfoVoucher.setVisible(false);
                panelInfoVoucher.setManaged(false);
                voucherActuel = null;
                return;
            }

            // Afficher les informations du voucher
            voucherActuel = voucher;
            afficherInfoVoucher(voucher);

        } catch (SQLException e) {
            afficherErreur("Erreur", "Erreur de recherche : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void afficherInfoVoucher(Voucher voucher) {
        panelInfoVoucher.setVisible(true);
        panelInfoVoucher.setManaged(true);

        lblCodeVoucher.setText(voucher.getCode());
        lblClient.setText(voucher.getClientNom());
        lblValeur.setText(String.format("%.2f Rs", voucher.getValeur()));
        lblDateExpiration.setText(voucher.getDateExpiration().toString());
        lblStatut.setText(voucher.getStatut());

        // Afficher le QR Code
        if (voucher.getQrCode() != null && !voucher.getQrCode().isEmpty()) {
            File qrFile = new File(voucher.getQrCode());
            if (qrFile.exists()) {
                Image qrImage = new Image(qrFile.toURI().toString());
                imgQRCode.setImage(qrImage);
            }
        }

        // Couleur du statut
        switch (voucher.getStatut()) {
            case "GENERE":
                lblStatut.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-padding: 5 10; -fx-background-radius: 5;");
                break;
            case "EMIS":
                lblStatut.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 5 10; -fx-background-radius: 5;");
                break;
            case "UTILISE":
                lblStatut.setStyle("-fx-background-color: #FED7AA; -fx-text-fill: #92400E; -fx-padding: 5 10; -fx-background-radius: 5;");
                break;
            case "EXPIRE":
                lblStatut.setStyle("-fx-background-color: #FECACA; -fx-text-fill: #991B1B; -fx-padding: 5 10; -fx-background-radius: 5;");
                break;
            case "ANNULE":
                lblStatut.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #4B5563; -fx-padding: 5 10; -fx-background-radius: 5;");
                break;
        }
    }

    @FXML
    private void validerRedemption() {
        if (voucherActuel == null) {
            afficherErreur("Erreur", "Aucun voucher sélectionné");
            return;
        }

        if (comboMagasin.getValue() == null) {
            afficherErreur("Erreur", "Veuillez sélectionner un magasin");
            return;
        }

        if (txtMontantUtilise.getText().isEmpty()) {
            afficherErreur("Erreur", "Veuillez saisir le montant utilisé");
            return;
        }

        // Vérifications
        if (!voucherActuel.getStatut().equals("EMIS")) {
            afficherErreur("Voucher non valide",
                    "Ce voucher ne peut pas être utilisé.\n\n" +
                            "Statut actuel : " + voucherActuel.getStatut() + "\n" +
                            "Seuls les vouchers avec le statut 'EMIS' peuvent être validés.");
            return;
        }

        if (voucherActuel.getDateExpiration().isBefore(LocalDate.now())) {
            afficherErreur("Voucher expiré",
                    "Ce voucher a expiré le " + voucherActuel.getDateExpiration());
            return;
        }

        try {
            double montantUtilise = Double.parseDouble(txtMontantUtilise.getText());

            if (montantUtilise <= 0 || montantUtilise > voucherActuel.getValeur()) {
                afficherErreur("Montant invalide",
                        "Le montant doit être entre 0 et " + voucherActuel.getValeur() + " Rs");
                return;
            }

            // Confirmation
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Valider cette rédemption ?");
            confirmation.setContentText(
                    "Voucher : " + voucherActuel.getCode() + "\n" +
                            "Client : " + voucherActuel.getClientNom() + "\n" +
                            "Valeur totale : " + voucherActuel.getValeur() + " Rs\n" +
                            "Montant utilisé : " + montantUtilise + " Rs\n" +
                            "Magasin : " + comboMagasin.getValue().getNom()
            );

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        effectuerRedemption(montantUtilise);
                    } catch (SQLException e) {
                        afficherErreur("Erreur", "Erreur lors de la validation : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

        } catch (NumberFormatException e) {
            afficherErreur("Erreur", "Montant invalide");
        }
    }

    private void effectuerRedemption(double montantUtilise) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            // 1. Mettre à jour le statut du voucher
            String updateVoucher = "UPDATE vouchers SET statut = 'UTILISE', date_utilisation = ? WHERE id = ?";
            PreparedStatement stmtVoucher = conn.prepareStatement(updateVoucher);
            stmtVoucher.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            stmtVoucher.setInt(2, voucherActuel.getId());
            stmtVoucher.executeUpdate();

            // 2. Insérer dans l'historique
            String insertHistorique = "INSERT INTO historique_redemptions " +
                    "(voucher_id, magasin_id, date_redemption, redemme_par, montant, remarques) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmtHistorique = conn.prepareStatement(insertHistorique);
            stmtHistorique.setInt(1, voucherActuel.getId());
            stmtHistorique.setInt(2, comboMagasin.getValue().getId());
            stmtHistorique.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmtHistorique.setString(4, "Admin");
            stmtHistorique.setDouble(5, montantUtilise);
            stmtHistorique.setString(6, txtRemarques.getText());
            stmtHistorique.executeUpdate();

            conn.commit();

            afficherSucces("Succès",
                    "Voucher validé avec succès !\n\n" +
                            "Code : " + voucherActuel.getCode() + "\n" +
                            "Montant utilisé : " + montantUtilise + " Rs");

            // Réinitialiser le formulaire
            txtCodeVoucher.clear();
            txtMontantUtilise.clear();
            txtRemarques.clear();
            panelInfoVoucher.setVisible(false);
            panelInfoVoucher.setManaged(false);
            voucherActuel = null;

            // Recharger l'historique
            chargerHistorique();
            mettreAJourStatistiques();

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private void mettreAJourStatistiques() {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // Total validés aujourd'hui
            String queryTotal = "SELECT COUNT(*) as total FROM historique_redemptions " +
                    "WHERE DATE(date_redemption) = CURRENT_DATE";
            PreparedStatement stmtTotal = conn.prepareStatement(queryTotal);
            var rsTotal = stmtTotal.executeQuery();
            if (rsTotal.next()) {
                totalValides.setText(String.valueOf(rsTotal.getInt("total")));
            }

            // Montant total aujourd'hui
            String queryMontant = "SELECT SUM(montant) as total FROM historique_redemptions " +
                    "WHERE DATE(date_redemption) = CURRENT_DATE";
            PreparedStatement stmtMontant = conn.prepareStatement(queryMontant);
            var rsMontant = stmtMontant.executeQuery();
            if (rsMontant.next()) {
                double montant = rsMontant.getDouble("total");
                montantTotal.setText(String.format("%.2f Rs", montant));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur statistiques : " + e.getMessage());
        }
    }

    @FXML
    private void annulerRedemption() {
        txtCodeVoucher.clear();
        txtMontantUtilise.clear();
        txtRemarques.clear();
        panelInfoVoucher.setVisible(false);
        panelInfoVoucher.setManaged(false);
        voucherActuel = null;
    }

    @FXML
    private void actualiserHistorique() {
        chargerHistorique();
        mettreAJourStatistiques();
    }

    @FXML
    private void retourDashboard() {
        try {
            Main.changeScene("dashboard.fxml");
        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de retourner au dashboard");
        }
    }

    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe interne pour l'historique
    public static class HistoriqueRedemption {
        private int id;
        private int voucherId;
        private String codeVoucher;
        private int magasinId;
        private String magasinNom;
        private String dateRedemption;
        private String redemmePar;
        private double montant;
        private String remarques;

        // Getters et Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getVoucherId() { return voucherId; }
        public void setVoucherId(int voucherId) { this.voucherId = voucherId; }

        public String getCodeVoucher() { return codeVoucher; }
        public void setCodeVoucher(String codeVoucher) { this.codeVoucher = codeVoucher; }

        public int getMagasinId() { return magasinId; }
        public void setMagasinId(int magasinId) { this.magasinId = magasinId; }

        public String getMagasinNom() { return magasinNom; }
        public void setMagasinNom(String magasinNom) { this.magasinNom = magasinNom; }

        public String getDateRedemption() { return dateRedemption; }
        public void setDateRedemption(String dateRedemption) { this.dateRedemption = dateRedemption; }

        public String getRedemmePar() { return redemmePar; }
        public void setRedemmePar(String redemmePar) { this.redemmePar = redemmePar; }

        public double getMontant() { return montant; }
        public void setMontant(double montant) { this.montant = montant; }

        public String getRemarques() { return remarques; }
        public void setRemarques(String remarques) { this.remarques = remarques; }
    }
}