package com.vms.controller;

import com.vms.Main;
import com.vms.model.Voucher;
import com.vms.model.Client;
import com.vms.model.Magasin;
import com.vms.dao.VoucherDAO;
import com.vms.dao.ClientDAO;
import com.vms.dao.MagasinDAO;
import com.vms.util.PDFExporter;
import com.vms.util.QRCodeGenerator;
import com.vms.util.VoucherPDFGenerator;
import com.vms.util.EmailSender;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;

import java.io.IOException;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VouchersController {

    @FXML private Label userNameLabel;
    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> filtreStatut;

    @FXML private Label totalVouchers;
    @FXML private Label vouchersActifs;
    @FXML private Label vouchersUtilises;
    @FXML private Label montantTotal;

    @FXML private TableView<Voucher> tableVouchers;
    @FXML private TableColumn<Voucher, String> colCode;
    @FXML private TableColumn<Voucher, String> colClient;
    @FXML private TableColumn<Voucher, Double> colValeur;
    @FXML private TableColumn<Voucher, String> colDateEmission;
    @FXML private TableColumn<Voucher, String> colDateExpiration;
    @FXML private TableColumn<Voucher, String> colStatut;
    @FXML private TableColumn<Voucher, Void> colActions;

    @FXML private VBox panelFormulaire;
    @FXML private ComboBox<Client> comboClient;
    @FXML private ComboBox<Magasin> comboMagasin;
    @FXML private TextField txtValeur;
    @FXML private DatePicker dateExpiration;
    @FXML private TextArea txtRemarques;
    @FXML private ImageView qrCodeImage;

    private ObservableList<Voucher> listeVouchers;
    private ObservableList<Voucher> listeVouchersFiltree;
    private ObservableList<Client> listeClients;
    private ObservableList<Magasin> listeMagasins;
    private Voucher voucherEnCours;

    @FXML
    public void initialize() {
        listeVouchers = FXCollections.observableArrayList();
        listeVouchersFiltree = FXCollections.observableArrayList();
        listeClients = FXCollections.observableArrayList();
        listeMagasins = FXCollections.observableArrayList();

        configurerFiltres();
        configurerTableau();
        chargerDepuisDB();
        chargerClients();
        chargerMagasins();
        mettreAJourStatistiques();

        userNameLabel.setText("Admin");
        champRecherche.textProperty().addListener((obs, old, val) -> appliquerFiltre());
    }

    private void configurerFiltres() {
        filtreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "GENERE", "EMIS", "UTILISE", "EXPIRE", "ANNULE"
        ));
        filtreStatut.setValue("Tous");
        filtreStatut.setOnAction(e -> appliquerFiltre());
    }

    private void chargerDepuisDB() {
        try {
            VoucherDAO dao = new VoucherDAO();
            List<Voucher> vouchers = dao.getAllVouchers();
            listeVouchers.clear();
            listeVouchers.addAll(vouchers);
            listeVouchersFiltree.clear();
            listeVouchersFiltree.addAll(vouchers);
            System.out.println("✅ " + vouchers.size() + " vouchers chargés depuis AlwaysData");
        } catch (SQLException e) {
            System.err.println("❌ Erreur base de données : " + e.getMessage());
            afficherErreur("Erreur", "Impossible de charger les vouchers: " + e.getMessage());
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
            System.err.println("❌ Erreur chargement clients : " + e.getMessage());
        }
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
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientNom"));
        colValeur.setCellValueFactory(new PropertyValueFactory<>("valeur"));

        colDateEmission.setCellValueFactory(data -> {
            LocalDate date = data.getValue().getDateEmission();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.toString() : "");
        });

        colDateExpiration.setCellValueFactory(data -> {
            LocalDate date = data.getValue().getDateExpiration();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.toString() : "");
        });

        // Colonne Statut avec badges
        colStatut.setCellFactory(col -> new TableCell<Voucher, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Voucher voucher = getTableRow().getItem();
                    Label badge = new Label();
                    badge.setStyle("-fx-padding: 5 15; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");

                    String statutActuel = voucher.getStatut();
                    switch (statutActuel) {
                        case "GENERE":
                            badge.setText("Généré");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;");
                            break;
                        case "EMIS":
                            badge.setText("Émis");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46;");
                            break;
                        case "UTILISE":
                            badge.setText("Utilisé");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #FED7AA; -fx-text-fill: #92400E;");
                            break;
                        case "EXPIRE":
                            badge.setText("Expiré");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #FECACA; -fx-text-fill: #991B1B;");
                            break;
                        case "ANNULE":
                            badge.setText("Annulé");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #E5E7EB; -fx-text-fill: #4B5563;");
                            break;
                        default:
                            badge.setText(statutActuel);
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Colonne Actions
        colActions.setCellFactory(col -> new TableCell<Voucher, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Voucher voucher = getTableRow().getItem();

                    Button btnQR = new Button("📱");
                    btnQR.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");
                    btnQR.setOnAction(e -> afficherQRCode(voucher));

                    Button btnImprimer = new Button("🖨");
                    btnImprimer.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");
                    btnImprimer.setOnAction(e -> imprimerVoucher(voucher));

                    Button btnEmettre = new Button("📧");
                    btnEmettre.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");
                    btnEmettre.setOnAction(e -> emettreVoucher(voucher));
                    btnEmettre.setDisable(!"GENERE".equals(voucher.getStatut()));

                    Button btnAnnuler = new Button("🗑");
                    btnAnnuler.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 6 10;");
                    btnAnnuler.setOnAction(e -> annulerVoucher(voucher));

                    Button btnVoir = new Button("👁");
                    btnVoir.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 6 10;");
                    btnVoir.setOnAction(e -> voirDetailsVoucher(voucher));

                    HBox actions = new HBox(5, btnQR, btnImprimer, btnEmettre, btnAnnuler, btnVoir);
                    actions.setAlignment(Pos.CENTER);
                    setGraphic(actions);
                }
            }
        });

        tableVouchers.setItems(listeVouchersFiltree);
    }

    private void appliquerFiltre() {
        String recherche = champRecherche.getText().toLowerCase();
        String statut = filtreStatut.getValue();

        listeVouchersFiltree.clear();

        for (Voucher voucher : listeVouchers) {
            boolean matchRecherche = recherche.isEmpty() ||
                    voucher.getCode().toLowerCase().contains(recherche) ||
                    (voucher.getClientNom() != null && voucher.getClientNom().toLowerCase().contains(recherche));

            boolean matchStatut = statut.equals("Tous") || voucher.getStatut().equals(statut);

            if (matchRecherche && matchStatut) {
                listeVouchersFiltree.add(voucher);
            }
        }

        mettreAJourStatistiques();
    }

    private void mettreAJourStatistiques() {
        int total = listeVouchers.size();
        long actifs = listeVouchers.stream()
                .filter(v -> "EMIS".equals(v.getStatut()))
                .count();
        long utilises = listeVouchers.stream()
                .filter(v -> "UTILISE".equals(v.getStatut()))
                .count();
        double montant = listeVouchers.stream()
                .mapToDouble(Voucher::getValeur)
                .sum();

        totalVouchers.setText(String.valueOf(total));
        vouchersActifs.setText(String.valueOf(actifs));
        vouchersUtilises.setText(String.valueOf(utilises));
        montantTotal.setText(String.format("%.2f Rs", montant));
    }

    @FXML
    private void afficherFormulaireNouveau() {
        panelFormulaire.setVisible(true);
        panelFormulaire.setManaged(true);
        voucherEnCours = new Voucher();

        comboClient.setValue(null);
        comboMagasin.setValue(null);
        txtValeur.clear();
        dateExpiration.setValue(LocalDate.now().plusYears(1));
        txtRemarques.clear();
        qrCodeImage.setImage(null);
    }

    @FXML
    private void genererVoucher() {
        if (comboClient.getValue() == null || txtValeur.getText().isEmpty()) {
            afficherErreur("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        try {
            Client client = comboClient.getValue();
            double valeur = Double.parseDouble(txtValeur.getText());

            Voucher nouveauVoucher = new Voucher();
            nouveauVoucher.setCode("VCH-" + System.currentTimeMillis());
            nouveauVoucher.setClientId(client.getId());
            nouveauVoucher.setClientNom(client.getNom());

            if (comboMagasin.getValue() != null) {
                Magasin magasin = comboMagasin.getValue();
                nouveauVoucher.setMagasinId(magasin.getId());
                nouveauVoucher.setMagasinNom(magasin.getNom());
            }

            nouveauVoucher.setValeur(valeur);
            nouveauVoucher.setStatut("GENERE");
            nouveauVoucher.setDateEmission(LocalDate.now());
            nouveauVoucher.setDateExpiration(dateExpiration.getValue());
            nouveauVoucher.setRemarques(txtRemarques.getText());

            // Générer QR Code
            String qrData = nouveauVoucher.getCode() + "|" + valeur + "|" + nouveauVoucher.getDateExpiration();
            String qrCodePath = QRCodeGenerator.generateQRCode(qrData, nouveauVoucher.getCode());
            nouveauVoucher.setQrCode(qrCodePath);

            // Afficher le QR Code dans l'interface
            try {
                File qrFile = new File(qrCodePath);
                if (qrFile.exists()) {
                    Image qrImage = new Image(qrFile.toURI().toString());
                    qrCodeImage.setImage(qrImage);
                }
            } catch (Exception e) {
                System.err.println("Erreur affichage QR : " + e.getMessage());
            }

            VoucherDAO dao = new VoucherDAO();
            int id = dao.createVoucher(nouveauVoucher);

            if (id > 0) {
                nouveauVoucher.setId(id);
                listeVouchers.add(nouveauVoucher);
                listeVouchersFiltree.add(nouveauVoucher);

                afficherSucces("Succès", "Voucher généré avec succès !\nQR Code : " + qrCodePath);
                mettreAJourStatistiques();
            } else {
                afficherErreur("Erreur", "Impossible de générer le voucher");
            }

        } catch (NumberFormatException e) {
            afficherErreur("Erreur", "Valeur invalide");
        } catch (Exception e) {
            afficherErreur("Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    private void afficherQRCode(Voucher voucher) {
        if (voucher.getQrCode() == null || voucher.getQrCode().isEmpty()) {
            afficherErreur("Erreur", "Ce voucher n'a pas de QR Code généré");
            return;
        }

        try {
            File qrFile = new File(voucher.getQrCode());
            if (!qrFile.exists()) {
                afficherErreur("Erreur", "Fichier QR Code introuvable : " + voucher.getQrCode());
                return;
            }

            // Créer une fenêtre modale pour afficher le QR Code en grand
            Stage qrStage = new Stage();
            qrStage.setTitle("QR Code - " + voucher.getCode());
            qrStage.initModality(Modality.APPLICATION_MODAL);

            ImageView imageView = new ImageView(new Image(qrFile.toURI().toString()));
            imageView.setFitWidth(400);
            imageView.setFitHeight(400);
            imageView.setPreserveRatio(true);

            VBox vbox = new VBox(15);
            vbox.setAlignment(Pos.CENTER);
            vbox.setStyle("-fx-padding: 30; -fx-background-color: white;");

            Label lblCode = new Label("Code: " + voucher.getCode());
            lblCode.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label lblValeur = new Label("Valeur: " + voucher.getValeur() + " Rs");
            lblValeur.setStyle("-fx-font-size: 14px;");

            Label lblExpiration = new Label("Expire le: " + voucher.getDateExpiration());
            lblExpiration.setStyle("-fx-font-size: 14px;");

            Button btnFermer = new Button("Fermer");
            btnFermer.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-padding: 10 30; -fx-font-size: 14px; -fx-cursor: hand;");
            btnFermer.setOnAction(e -> qrStage.close());

            vbox.getChildren().addAll(lblCode, lblValeur, lblExpiration, imageView, btnFermer);

            Scene scene = new Scene(vbox);
            qrStage.setScene(scene);
            qrStage.showAndWait();

        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible d'afficher le QR Code : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void imprimerVoucher(Voucher voucher) {
        try {
            String filename = VoucherPDFGenerator.generateVoucherPDF(voucher);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Impression réussie");
            alert.setHeaderText("Voucher imprimé !");
            alert.setContentText("Fichier créé : " + filename + "\n\nVous pouvez maintenant l'ouvrir et l'imprimer.");

            // Bouton pour ouvrir le fichier
            ButtonType btnOuvrir = new ButtonType("Ouvrir le fichier");
            ButtonType btnFermer = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnOuvrir, btnFermer);

            alert.showAndWait().ifPresent(response -> {
                if (response == btnOuvrir) {
                    try {
                        // Ouvrir le fichier avec l'application par défaut
                        File file = new File(filename);
                        if (file.exists()) {
                            java.awt.Desktop.getDesktop().open(file);
                        }
                    } catch (Exception e) {
                        afficherErreur("Erreur", "Impossible d'ouvrir le fichier : " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de générer le PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void emettreVoucher(Voucher voucher) {
        // Vérifier la configuration email
        if (!EmailSender.verifierConfiguration()) {
            afficherErreur("Configuration Email",
                    "Veuillez configurer vos identifiants email dans EmailSender.java\n\n" +
                            "1. Allez dans src/main/java/com/vms/util/EmailSender.java\n" +
                            "2. Remplacez EMAIL_FROM et EMAIL_PASSWORD\n" +
                            "3. Utilisez un mot de passe d'application Gmail");
            return;
        }

        // Demander l'email du destinataire
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Envoyer Voucher par Email");
        dialog.setHeaderText("Émettre et envoyer le voucher à : " + voucher.getClientNom());
        dialog.setContentText("Adresse email :");

        dialog.showAndWait().ifPresent(email -> {
            if (email.isEmpty() || !email.contains("@")) {
                afficherErreur("Erreur", "Adresse email invalide");
                return;
            }

            // Confirmation
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Émettre et envoyer ce voucher ?");
            confirmation.setContentText(
                    "Code : " + voucher.getCode() + "\n" +
                            "Valeur : " + voucher.getValeur() + " Rs\n" +
                            "Email : " + email + "\n\n" +
                            "Le voucher sera émis et envoyé par email avec le PDF."
            );

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Afficher une progression
                    Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
                    progressAlert.setTitle("Envoi en cours");
                    progressAlert.setHeaderText("Génération et envoi du voucher...");
                    progressAlert.setContentText("Veuillez patienter...");
                    progressAlert.show();

                    // Exécuter dans un thread séparé pour ne pas bloquer l'interface
                    new Thread(() -> {
                        try {
                            // 1. Générer le PDF
                            String pdfPath = VoucherPDFGenerator.generateVoucherPDF(voucher);

                            // 2. Envoyer par email
                            boolean emailEnvoye = EmailSender.envoyerVoucherParEmail(voucher, email, pdfPath);

                            // 3. Mettre à jour le statut
                            if (emailEnvoye) {
                                voucher.setStatut("EMIS");
                                VoucherDAO dao = new VoucherDAO();
                                dao.updateVoucher(voucher);

                                // Mise à jour de l'interface (sur le thread JavaFX)
                                javafx.application.Platform.runLater(() -> {
                                    progressAlert.close();
                                    tableVouchers.refresh();
                                    mettreAJourStatistiques();
                                    afficherSucces("Succès",
                                            "Voucher émis et envoyé avec succès !\n\n" +
                                                    "Email : " + email + "\n" +
                                                    "PDF : " + pdfPath);
                                });
                            } else {
                                javafx.application.Platform.runLater(() -> {
                                    progressAlert.close();
                                    afficherErreur("Erreur",
                                            "Impossible d'envoyer l'email.\n\n" +
                                                    "Vérifiez :\n" +
                                                    "- Votre connexion Internet\n" +
                                                    "- Vos identifiants Gmail\n" +
                                                    "- Le mot de passe d'application");
                                });
                            }

                        } catch (Exception e) {
                            javafx.application.Platform.runLater(() -> {
                                progressAlert.close();
                                afficherErreur("Erreur", "Erreur : " + e.getMessage());
                            });
                            e.printStackTrace();
                        }
                    }).start();
                }
            });
        });
    }

    private void annulerVoucher(Voucher voucher) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Annuler ce voucher ?");
        confirmation.setContentText("Code : " + voucher.getCode());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    voucher.setStatut("ANNULE");
                    VoucherDAO dao = new VoucherDAO();
                    boolean success = dao.updateVoucher(voucher);

                    if (success) {
                        tableVouchers.refresh();
                        afficherSucces("Succès", "Voucher annulé");
                        mettreAJourStatistiques();
                    }
                } catch (SQLException e) {
                    afficherErreur("Erreur", e.getMessage());
                }
            }
        });
    }

    private void voirDetailsVoucher(Voucher voucher) {
        String details = String.format(
                "📱 DÉTAILS DU VOUCHER\n\n" +
                        "Code: %s\n" +
                        "Client: %s\n" +
                        "Valeur: %.2f Rs\n" +
                        "Magasin: %s\n" +
                        "Date émission: %s\n" +
                        "Date expiration: %s\n" +
                        "Statut: %s\n" +
                        "QR Code: %s",
                voucher.getCode(),
                voucher.getClientNom(),
                voucher.getValeur(),
                voucher.getMagasinNom() != null ? voucher.getMagasinNom() : "Tous magasins",
                voucher.getDateEmission(),
                voucher.getDateExpiration(),
                voucher.getStatut(),
                voucher.getQrCode() != null ? "Généré" : "Non généré"
        );

        afficherInfo("Détails Voucher", details);
    }

    @FXML
    private void exporterPDF() {
        try {
            String filename = PDFExporter.exportVouchers(listeVouchersFiltree);
            afficherSucces("Export réussi", "Fichier PDF créé : " + filename);
        } catch (Exception e) {
            afficherErreur("Erreur export", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void annulerFormulaire() {
        panelFormulaire.setVisible(false);
        panelFormulaire.setManaged(false);
        voucherEnCours = null;
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