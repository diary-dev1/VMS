package com.vms.controller;

import com.vms.Main;
import com.vms.dao.ClientDAO;
import com.vms.dao.DemandeDAO;
import com.vms.dao.VoucherDAO;
import com.vms.model.Client;
import com.vms.model.Demande;
import com.vms.model.Voucher;
import com.vms.util.QRCodeGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

public class DemandesController {

    @FXML private Label userNameLabel;
    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> filtreStatut;

    @FXML private Label totalDemandes;
    @FXML private Label demandesEnAttente;
    @FXML private Label montantTotal;

    @FXML private TableView<Demande> tableDemandes;
    @FXML private TableColumn<Demande, String> colReference;
    @FXML private TableColumn<Demande, String> colClient;
    @FXML private TableColumn<Demande, Integer> colNombreBons;
    @FXML private TableColumn<Demande, Double> colValeurUnitaire;
    @FXML private TableColumn<Demande, Double> colMontantTotal;
    @FXML private TableColumn<Demande, String> colDateCreation;
    @FXML private TableColumn<Demande, String> colStatut;
    @FXML private TableColumn<Demande, Void> colActions;

    @FXML private VBox panelFormulaire;
    @FXML private ComboBox<Client> comboClient;
    @FXML private TextField txtNombreBons;
    @FXML private TextField txtValeurUnitaire;
    @FXML private TextArea txtRemarques;
    @FXML private Label lblMontantTotal;

    // Configuration email
    private static final String EMAIL_FROM = "votre-email@gmail.com";
    private static final String EMAIL_PASSWORD = "votre-mot-de-passe-app";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    private ObservableList<Demande> listeDemandes;
    private ObservableList<Demande> listeDemandesFiltree;
    private ObservableList<Client> listeClients;
    private Demande demandeEnCours;

    @FXML
    public void initialize() {
        listeDemandes = FXCollections.observableArrayList();
        listeDemandesFiltree = FXCollections.observableArrayList();
        listeClients = FXCollections.observableArrayList();

        configurerFiltres();
        configurerTableau();
        chargerDepuisDB();
        chargerClients();
        mettreAJourStatistiques();

        userNameLabel.setText("Admin");
        champRecherche.textProperty().addListener((obs, old, val) -> appliquerFiltre());

        // Calculer montant total automatiquement
        txtNombreBons.textProperty().addListener((obs, old, val) -> calculerMontantTotal());
        txtValeurUnitaire.textProperty().addListener((obs, old, val) -> calculerMontantTotal());
    }

    private void calculerMontantTotal() {
        try {
            int nombreBons = Integer.parseInt(txtNombreBons.getText());
            double valeurUnitaire = Double.parseDouble(txtValeurUnitaire.getText());
            double montantTotal = nombreBons * valeurUnitaire;
            lblMontantTotal.setText(String.format("%.2f Rs", montantTotal));
        } catch (NumberFormatException e) {
            lblMontantTotal.setText("0.00 Rs");
        }
    }

    private void configurerFiltres() {
        filtreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "EN_ATTENTE_PAIEMENT", "PAYE", "APPROUVE", "GENERE", "ANNULE"
        ));
        filtreStatut.setValue("Tous");
        filtreStatut.setOnAction(e -> appliquerFiltre());
    }

    private void chargerDepuisDB() {
        try {
            DemandeDAO dao = new DemandeDAO();
            List<Demande> demandes = dao.getAllDemandes();
            listeDemandes.clear();
            listeDemandes.addAll(demandes);
            listeDemandesFiltree.clear();
            listeDemandesFiltree.addAll(demandes);
            System.out.println("✅ " + demandes.size() + " demandes chargées");
        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            afficherErreur("Erreur", "Impossible de charger les demandes");
        }
    }

    private void chargerClients() {
        try {
            ClientDAO dao = new ClientDAO();
            List<Client> clients = dao.getAllClients();
            listeClients.clear();
            listeClients.addAll(clients);
            comboClient.setItems(listeClients);
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement clients");
        }
    }

    private void configurerTableau() {
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientNom"));
        colNombreBons.setCellValueFactory(new PropertyValueFactory<>("nombreBons"));
        colValeurUnitaire.setCellValueFactory(new PropertyValueFactory<>("valeurUnitaire"));
        colMontantTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));

        colDateCreation.setCellValueFactory(data -> {
            LocalDate date = data.getValue().getDateCreation();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.toString() : "");
        });

        // Colonne Statut avec badges
        colStatut.setCellFactory(col -> new TableCell<Demande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Demande demande = getTableRow().getItem();
                    Label badge = new Label();
                    badge.setStyle("-fx-padding: 5 15; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");

                    String statut = demande.getStatut();
                    switch (statut) {
                        case "EN_ATTENTE_PAIEMENT":
                            badge.setText("Open");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E;");
                            break;
                        case "PAYE":
                            badge.setText("Booked");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;");
                            break;
                        case "APPROUVE":
                            badge.setText("Completed");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46;");
                            break;
                        case "GENERE":
                            badge.setText("Generé");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #E0E7FF; -fx-text-fill: #3730A3;");
                            break;
                        case "ANNULE":
                            badge.setText("Cancelled");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;");
                            break;
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Colonne Actions
        colActions.setCellFactory(col -> new TableCell<Demande, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Demande demande = getTableRow().getItem();

                    Button btnPayer = new Button("💳 Payer");
                    btnPayer.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");
                    btnPayer.setOnAction(e -> validerPaiementDemande(demande));
                    btnPayer.setVisible("EN_ATTENTE_PAIEMENT".equals(demande.getStatut()));
                    btnPayer.setManaged("EN_ATTENTE_PAIEMENT".equals(demande.getStatut()));

                    Button btnApprouver = new Button("✓ Approuver");
                    btnApprouver.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");
                    btnApprouver.setOnAction(e -> approuverDemandeDirecte(demande));
                    btnApprouver.setVisible("PAYE".equals(demande.getStatut()));
                    btnApprouver.setManaged("PAYE".equals(demande.getStatut()));

                    // NOUVEAU : Bouton Générer Vouchers
                    Button btnGenererVouchers = new Button("🎫 Générer Vouchers");
                    btnGenererVouchers.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");
                    btnGenererVouchers.setOnAction(e -> genererVouchersPourDemande(demande));
                    btnGenererVouchers.setVisible("APPROUVE".equals(demande.getStatut()));
                    btnGenererVouchers.setManaged("APPROUVE".equals(demande.getStatut()));

                    Button btnEmail = new Button("📧");
                    btnEmail.setStyle("-fx-background-color: transparent; -fx-text-fill: #3B82F6; -fx-cursor: hand; -fx-font-size: 16px;");
                    btnEmail.setOnAction(e -> envoyerEmailNouvelleDemande(demande));

                    Button btnModifier = new Button("✏");
                    btnModifier.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B5CF6; -fx-cursor: hand; -fx-font-size: 16px;");
                    btnModifier.setOnAction(e -> modifierDemande(demande));

                    Button btnSupprimer = new Button("🗑");
                    btnSupprimer.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 16px;");
                    btnSupprimer.setOnAction(e -> supprimerDemande(demande));

                    Button btnVoir = new Button("👁");
                    btnVoir.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-cursor: hand; -fx-font-size: 16px;");
                    btnVoir.setOnAction(e -> voirDetailsDemande(demande));

                    HBox actions = new HBox(5, btnPayer, btnApprouver, btnGenererVouchers, btnEmail, btnModifier, btnSupprimer, btnVoir);
                    actions.setAlignment(Pos.CENTER);
                    setGraphic(actions);
                }
            }
        });

        tableDemandes.setItems(listeDemandesFiltree);
    }

    // NOUVELLE MÉTHODE : Générer les vouchers pour une demande
    private void genererVouchersPourDemande(Demande demande) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Générer les vouchers ?");
        confirmation.setContentText(String.format(
                "Demande : %s\n" +
                        "Client : %s\n" +
                        "Nombre de bons : %d\n" +
                        "Valeur unitaire : %.2f Rs\n\n" +
                        "Cela va créer %d vouchers.",
                demande.getReference(),
                demande.getClientNom(),
                demande.getNombreBons(),
                demande.getValeurUnitaire(),
                demande.getNombreBons()
        ));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    VoucherDAO voucherDAO = new VoucherDAO();
                    int vouchersCreated = 0;

                    // Créer X vouchers selon le nombre de bons
                    for (int i = 1; i <= demande.getNombreBons(); i++) {
                        Voucher voucher = new Voucher();
                        voucher.setCode("VCH-" + demande.getReference() + "-" + i);
                        voucher.setDemandeId(demande.getId());
                        voucher.setDemandeReference(demande.getReference());
                        voucher.setClientId(demande.getClientId());
                        voucher.setClientNom(demande.getClientNom());
                        voucher.setValeur(demande.getValeurUnitaire());
                        voucher.setStatut("GENERE");
                        voucher.setDateEmission(LocalDate.now());
                        voucher.setDateExpiration(LocalDate.now().plusYears(1));

                        // Générer QR Code
                        String qrData = voucher.getCode() + "|" + voucher.getValeur() + "|" + voucher.getDateExpiration();
                        String qrCodePath = QRCodeGenerator.generateQRCode(qrData, voucher.getCode());
                        voucher.setQrCode(qrCodePath);

                        int id = voucherDAO.createVoucher(voucher);
                        if (id > 0) {
                            vouchersCreated++;
                        }
                    }

                    // Mettre à jour le statut de la demande
                    demande.setStatut("GENERE");
                    DemandeDAO demandeDAO = new DemandeDAO();
                    demandeDAO.updateDemande(demande);

                    tableDemandes.refresh();
                    mettreAJourStatistiques();

                    afficherSucces("Succès",
                            String.format("%d vouchers générés avec succès !\n\n" +
                                            "Vous pouvez les consulter dans le module Vouchers.",
                                    vouchersCreated));

                } catch (Exception e) {
                    afficherErreur("Erreur", "Erreur lors de la génération des vouchers : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void validerPaiementDemande(Demande demande) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Valider le paiement ?");
        confirmation.setContentText("Demande : " + demande.getReference() + "\nMontant : " + demande.getMontantTotal() + " Rs");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    demande.setStatut("PAYE");
                    demande.setDatePaiement(LocalDate.now());
                    DemandeDAO dao = new DemandeDAO();
                    boolean success = dao.updateDemande(demande);

                    if (success) {
                        envoyerEmailPaiementValide(demande);
                        tableDemandes.refresh();
                        afficherSucces("Succès", "Paiement validé !");
                        mettreAJourStatistiques();
                    }
                } catch (SQLException e) {
                    afficherErreur("Erreur", e.getMessage());
                }
            }
        });
    }

    private void approuverDemandeDirecte(Demande demande) {
        if (!"PAYE".equals(demande.getStatut())) {
            afficherErreur("Erreur", "Cette demande doit être payée avant d'être approuvée");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Approuver cette demande ?");
        confirmation.setContentText("Demande : " + demande.getReference());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    demande.setStatut("APPROUVE");
                    demande.setDateApprobation(LocalDate.now());
                    DemandeDAO dao = new DemandeDAO();
                    boolean success = dao.updateDemande(demande);

                    if (success) {
                        envoyerEmailApprobation(demande);
                        tableDemandes.refresh();
                        afficherSucces("Succès", "Demande approuvée !");
                        mettreAJourStatistiques();
                    }
                } catch (SQLException e) {
                    afficherErreur("Erreur", e.getMessage());
                }
            }
        });
    }

    private void modifierDemande(Demande demande) {
        panelFormulaire.setVisible(true);
        panelFormulaire.setManaged(true);
        demandeEnCours = demande;

        comboClient.setValue(listeClients.stream()
                .filter(c -> c.getId() == demande.getClientId())
                .findFirst().orElse(null));
        txtNombreBons.setText(String.valueOf(demande.getNombreBons()));
        txtValeurUnitaire.setText(String.valueOf(demande.getValeurUnitaire()));
        txtRemarques.setText(demande.getRemarques());
    }

    private void supprimerDemande(Demande demande) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer cette demande ?");
        confirmation.setContentText("Référence : " + demande.getReference());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    DemandeDAO dao = new DemandeDAO();
                    boolean success = dao.deleteDemande(demande.getId());

                    if (success) {
                        listeDemandes.remove(demande);
                        listeDemandesFiltree.remove(demande);
                        afficherSucces("Succès", "Demande supprimée");
                        mettreAJourStatistiques();
                    }
                } catch (SQLException e) {
                    afficherErreur("Erreur", e.getMessage());
                }
            }
        });
    }

    private void voirDetailsDemande(Demande demande) {
        String details = String.format(
                "📋 DÉTAILS DE LA DEMANDE\n\n" +
                        "Référence: %s\n" +
                        "Client: %s\n" +
                        "Nombre de bons: %d\n" +
                        "Valeur unitaire: %.2f Rs\n" +
                        "Montant total: %.2f Rs\n" +
                        "Date création: %s\n" +
                        "Statut: %s\n" +
                        "Remarques: %s",
                demande.getReference(),
                demande.getClientNom(),
                demande.getNombreBons(),
                demande.getValeurUnitaire(),
                demande.getMontantTotal(),
                demande.getDateCreation(),
                demande.getStatut(),
                demande.getRemarques() != null ? demande.getRemarques() : "Aucune"
        );

        afficherInfo("Détails Demande", details);
    }

    private void appliquerFiltre() {
        String recherche = champRecherche.getText().toLowerCase();
        String statut = filtreStatut.getValue();

        listeDemandesFiltree.clear();

        for (Demande demande : listeDemandes) {
            boolean matchRecherche = recherche.isEmpty() ||
                    demande.getReference().toLowerCase().contains(recherche) ||
                    (demande.getClientNom() != null && demande.getClientNom().toLowerCase().contains(recherche));

            boolean matchStatut = statut.equals("Tous") || demande.getStatut().equals(statut);

            if (matchRecherche && matchStatut) {
                listeDemandesFiltree.add(demande);
            }
        }

        mettreAJourStatistiques();
    }

    private void mettreAJourStatistiques() {
        int total = listeDemandes.size();
        long enAttente = listeDemandes.stream()
                .filter(d -> "EN_ATTENTE_PAIEMENT".equals(d.getStatut()))
                .count();
        double montant = listeDemandes.stream()
                .mapToDouble(Demande::getMontantTotal)
                .sum();

        totalDemandes.setText(String.valueOf(total));
        demandesEnAttente.setText(String.valueOf(enAttente));
        montantTotal.setText(String.format("%.2f Rs", montant));
    }

    @FXML
    private void afficherFormulaireNouveau() {
        panelFormulaire.setVisible(true);
        panelFormulaire.setManaged(true);
        demandeEnCours = new Demande();

        comboClient.setValue(null);
        txtNombreBons.clear();
        txtValeurUnitaire.clear();
        txtRemarques.clear();
        lblMontantTotal.setText("0.00 Rs");
    }

    @FXML
    private void enregistrerDemande() {
        if (comboClient.getValue() == null || txtNombreBons.getText().isEmpty() || txtValeurUnitaire.getText().isEmpty()) {
            afficherErreur("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        try {
            boolean estNouveau = (demandeEnCours == null || demandeEnCours.getId() == 0);

            if (estNouveau) {
                demandeEnCours = new Demande();
                demandeEnCours.setReference("DEM-" + System.currentTimeMillis());
                demandeEnCours.setDateCreation(LocalDate.now());
                demandeEnCours.setStatut("EN_ATTENTE_PAIEMENT");
            }

            Client client = comboClient.getValue();
            demandeEnCours.setClientId(client.getId());
            demandeEnCours.setClientNom(client.getNom());
            demandeEnCours.setNombreBons(Integer.parseInt(txtNombreBons.getText()));
            demandeEnCours.setValeurUnitaire(Double.parseDouble(txtValeurUnitaire.getText()));
            demandeEnCours.setMontantTotal(demandeEnCours.getNombreBons() * demandeEnCours.getValeurUnitaire());
            demandeEnCours.setRemarques(txtRemarques.getText());

            DemandeDAO dao = new DemandeDAO();

            if (estNouveau) {
                int id = dao.createDemande(demandeEnCours);
                if (id > 0) {
                    demandeEnCours.setId(id);
                    listeDemandes.add(demandeEnCours);
                    listeDemandesFiltree.add(demandeEnCours);
                    envoyerEmailNouvelleDemande(demandeEnCours);
                    afficherSucces("Succès", "Demande créée !");
                }
            } else {
                boolean success = dao.updateDemande(demandeEnCours);
                if (success) {
                    tableDemandes.refresh();
                    afficherSucces("Succès", "Demande modifiée !");
                }
            }

            mettreAJourStatistiques();
            annulerFormulaire();

        } catch (Exception e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }

    // Méthodes d'envoi d'emails
    private void envoyerEmailNouvelleDemande(Demande demande) {
        String sujet = "Nouvelle demande de vouchers - " + demande.getReference();
        String contenu = String.format(
                "Bonjour,\n\n" +
                        "Une nouvelle demande de vouchers a été créée :\n\n" +
                        "Référence : %s\n" +
                        "Nombre de bons : %d\n" +
                        "Valeur unitaire : %.2f Rs\n" +
                        "Montant total : %.2f Rs\n" +
                        "Date : %s\n" +
                        "Statut : EN_ATTENTE_PAIEMENT\n\n" +
                        "Cordialement,\nVMS System",
                demande.getReference(),
                demande.getNombreBons(),
                demande.getValeurUnitaire(),
                demande.getMontantTotal(),
                demande.getDateCreation()
        );

        envoyerEmail(sujet, contenu);
    }

    private void envoyerEmailPaiementValide(Demande demande) {
        String sujet = "Paiement validé - " + demande.getReference();
        String contenu = String.format(
                "Bonjour,\n\n" +
                        "Le paiement de la demande %s a été validé.\n\n" +
                        "Montant payé : %.2f Rs\n" +
                        "Date de paiement : %s\n" +
                        "Statut : PAYE\n\n" +
                        "Cordialement,\nVMS System",
                demande.getReference(),
                demande.getMontantTotal(),
                demande.getDatePaiement()
        );

        envoyerEmail(sujet, contenu);
    }

    private void envoyerEmailApprobation(Demande demande) {
        String sujet = "Demande approuvée - " + demande.getReference();
        String contenu = String.format(
                "Bonjour,\n\n" +
                        "La demande %s a été approuvée.\n\n" +
                        "Montant : %.2f Rs\n" +
                        "Les vouchers seront générés prochainement.\n\n" +
                        "Cordialement,\nVMS System",
                demande.getReference(),
                demande.getMontantTotal()
        );

        envoyerEmail(sujet, contenu);
    }

    private void envoyerEmail(String sujet, String contenu) {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_FROM));
                message.setSubject(sujet);
                message.setText(contenu);

                Transport.send(message);
                System.out.println("✅ Email envoyé : " + sujet);

            } catch (Exception e) {
                System.err.println("❌ Erreur envoi email : " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void annulerFormulaire() {
        panelFormulaire.setVisible(false);
        panelFormulaire.setManaged(false);
        demandeEnCours = null;
    }

    @FXML
    private void retourDashboard() {
        try {
            Main.changeScene("dashboard.fxml");
        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de retourner au dashboard");
        }
    }

    @FXML
    private void actualiserListe() {
        chargerDepuisDB();
        mettreAJourStatistiques();
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

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}