package com.vms.controller;

import com.vms.Main;
import com.vms.model.Magasin;
import com.vms.dao.MagasinDAO;
import com.vms.util.PDFExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MagasinsController {

    @FXML private Label userNameLabel;
    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> filtreVille;
    @FXML private ComboBox<String> filtreStatut;

    @FXML private Label totalMagasins;
    @FXML private Label magasinsActifs;
    @FXML private Label totalRedemptions;

    @FXML private TableView<Magasin> tableMagasins;
    @FXML private TableColumn<Magasin, String> colCode;
    @FXML private TableColumn<Magasin, String> colNom;
    @FXML private TableColumn<Magasin, String> colVille;
    @FXML private TableColumn<Magasin, String> colTelephone;
    @FXML private TableColumn<Magasin, String> colResponsable;
    @FXML private TableColumn<Magasin, String> colStatut;
    @FXML private TableColumn<Magasin, Integer> colRedemptions;
    @FXML private TableColumn<Magasin, Void> colActions;

    @FXML private VBox panelFormulaire;
    @FXML private TextField txtNom;
    @FXML private TextArea txtAdresse;
    @FXML private TextField txtVille;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtResponsable;
    @FXML private ComboBox<String> comboTypeMagasin;
    @FXML private TextField txtHeureOuverture;
    @FXML private TextField txtHeureFermeture;
    @FXML private CheckBox chkActif;

    private ObservableList<Magasin> listeMagasins;
    private ObservableList<Magasin> listeMagasinsFiltree;
    private Magasin magasinEnCours;

    @FXML
    public void initialize() {
        listeMagasins = FXCollections.observableArrayList();
        listeMagasinsFiltree = FXCollections.observableArrayList();

        configurerFiltres();
        configurerTableau();
        chargerDepuisDB();
        mettreAJourStatistiques();

        userNameLabel.setText("Admin");

        champRecherche.textProperty().addListener((obs, old, val) -> appliquerFiltre());
    }

    private void configurerFiltres() {
        // Type de magasin
        comboTypeMagasin.setItems(FXCollections.observableArrayList(
                "Supermarché", "Boutique", "Restaurant", "Pharmacie", "Station Service", "Autre"
        ));

        // Filtre par ville
        filtreVille.setItems(FXCollections.observableArrayList(
                "Toutes", "Port Louis", "Rose-Hill", "Quatre Bornes", "Curepipe", "Vacoas"
        ));
        filtreVille.setValue("Toutes");
        filtreVille.setOnAction(e -> appliquerFiltre());

        // Filtre par statut
        filtreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "Actifs", "Inactifs"
        ));
        filtreStatut.setValue("Tous");
        filtreStatut.setOnAction(e -> appliquerFiltre());
    }

    private void chargerDepuisDB() {
        try {
            MagasinDAO dao = new MagasinDAO();
            List<Magasin> magasins = dao.getAllMagasins();
            listeMagasins.clear();
            listeMagasins.addAll(magasins);
            listeMagasinsFiltree.clear();
            listeMagasinsFiltree.addAll(magasins);
            System.out.println("✅ " + magasins.size() + " magasins chargés depuis AlwaysData");
        } catch (SQLException e) {
            System.err.println("❌ Erreur base de données : " + e.getMessage());
            afficherErreur("Erreur", "Impossible de charger les magasins: " + e.getMessage());
        }
    }

    private void configurerTableau() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));

        // Colonne Statut avec badge
        colStatut.setCellFactory(col -> new TableCell<Magasin, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Magasin magasin = getTableRow().getItem();
                    Label badge = new Label();
                    badge.setStyle("-fx-padding: 5 15; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");

                    if (magasin.isActif()) {
                        badge.setText("✓ Actif");
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46;");
                    } else {
                        badge.setText("✗ Inactif");
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;");
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Colonne Rédemptions
        colRedemptions.setCellValueFactory(new PropertyValueFactory<>("nombreRedemptions"));

        // Colonne Actions
        colActions.setCellFactory(col -> new TableCell<Magasin, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Magasin magasin = getTableRow().getItem();

                    Button btnVoir = new Button("👁");
                    btnVoir.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");
                    btnVoir.setOnAction(e -> voirDetailsMagasin(magasin));

                    Button btnModifier = new Button("✏");
                    btnModifier.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");
                    btnModifier.setOnAction(e -> modifierMagasin(magasin));

                    Button btnSupprimer = new Button("🗑");
                    btnSupprimer.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 6 10;");
                    btnSupprimer.setOnAction(e -> supprimerMagasin(magasin));

                    HBox actions = new HBox(5, btnVoir, btnModifier, btnSupprimer);
                    actions.setAlignment(Pos.CENTER);
                    setGraphic(actions);
                }
            }
        });

        tableMagasins.setItems(listeMagasinsFiltree);
    }

    private void appliquerFiltre() {
        String recherche = champRecherche.getText().toLowerCase();
        String ville = filtreVille.getValue();
        String statut = filtreStatut.getValue();

        listeMagasinsFiltree.clear();

        for (Magasin magasin : listeMagasins) {
            boolean matchRecherche = recherche.isEmpty() ||
                    magasin.getCode().toLowerCase().contains(recherche) ||
                    magasin.getNom().toLowerCase().contains(recherche) ||
                    (magasin.getVille() != null && magasin.getVille().toLowerCase().contains(recherche));

            boolean matchVille = ville.equals("Toutes") ||
                    (magasin.getVille() != null && magasin.getVille().equals(ville));

            boolean matchStatut = statut.equals("Tous") ||
                    (statut.equals("Actifs") && magasin.isActif()) ||
                    (statut.equals("Inactifs") && !magasin.isActif());

            if (matchRecherche && matchVille && matchStatut) {
                listeMagasinsFiltree.add(magasin);
            }
        }

        mettreAJourStatistiques();
    }

    private void mettreAJourStatistiques() {
        int total = listeMagasins.size();
        long actifs = listeMagasins.stream().filter(Magasin::isActif).count();
        int redemptions = listeMagasins.stream().mapToInt(Magasin::getNombreRedemptions).sum();

        totalMagasins.setText(String.valueOf(total));
        magasinsActifs.setText(String.valueOf(actifs));
        totalRedemptions.setText(String.valueOf(redemptions));
    }

    @FXML
    private void afficherFormulaireNouveau() {
        panelFormulaire.setVisible(true);
        panelFormulaire.setManaged(true);
        magasinEnCours = new Magasin();

        txtNom.clear();
        txtAdresse.clear();
        txtVille.clear();
        txtTelephone.clear();
        txtResponsable.clear();
        comboTypeMagasin.setValue(null);
        txtHeureOuverture.clear();
        txtHeureFermeture.clear();
        chkActif.setSelected(true);
    }

    @FXML
    private void enregistrerMagasin() {
        if (txtNom.getText().isEmpty()) {
            afficherErreur("Erreur", "Le nom est obligatoire");
            return;
        }

        boolean estNouveau = (magasinEnCours == null || magasinEnCours.getId() == 0);

        if (estNouveau) {
            magasinEnCours = new Magasin();
            magasinEnCours.setCode("MAG-" + System.currentTimeMillis());
            magasinEnCours.setDateOuverture(LocalDate.now());
        }

        magasinEnCours.setNom(txtNom.getText());
        magasinEnCours.setAdresse(txtAdresse.getText());
        magasinEnCours.setVille(txtVille.getText());
        magasinEnCours.setTelephone(txtTelephone.getText());
        magasinEnCours.setResponsable(txtResponsable.getText());
        magasinEnCours.setTypeMagasin(comboTypeMagasin.getValue());
        magasinEnCours.setActif(chkActif.isSelected());

        // Horaires
        try {
            if (!txtHeureOuverture.getText().isEmpty()) {
                magasinEnCours.setHeureOuverture(LocalTime.parse(txtHeureOuverture.getText()));
            }
            if (!txtHeureFermeture.getText().isEmpty()) {
                magasinEnCours.setHeureFermeture(LocalTime.parse(txtHeureFermeture.getText()));
            }
        } catch (Exception e) {
            afficherErreur("Erreur", "Format d'horaire invalide. Utilisez HH:MM (ex: 09:00)");
            return;
        }

        try {
            MagasinDAO dao = new MagasinDAO();

            if (estNouveau) {
                int id = dao.createMagasin(magasinEnCours);
                if (id > 0) {
                    magasinEnCours.setId(id);
                    listeMagasins.add(magasinEnCours);
                    listeMagasinsFiltree.add(magasinEnCours);
                    afficherSucces("Succès", "Magasin créé et sauvegardé !");
                } else {
                    afficherErreur("Erreur", "Impossible de créer le magasin");
                }
            } else {
                boolean success = dao.updateMagasin(magasinEnCours);
                if (success) {
                    tableMagasins.refresh();
                    afficherSucces("Succès", "Magasin modifié !");
                } else {
                    afficherErreur("Erreur", "Impossible de modifier le magasin");
                }
            }
        } catch (SQLException e) {
            afficherErreur("Erreur base de données", e.getMessage());
            e.printStackTrace();
        }

        mettreAJourStatistiques();
        annulerFormulaire();
    }

    private void voirDetailsMagasin(Magasin magasin) {
        String details = String.format(
                "📍 DÉTAILS DU MAGASIN\n\n" +
                        "Code: %s\n" +
                        "Nom: %s\n" +
                        "Type: %s\n" +
                        "Ville: %s\n" +
                        "Adresse: %s\n" +
                        "Téléphone: %s\n" +
                        "Responsable: %s\n" +
                        "Horaires: %s\n" +
                        "Statut: %s\n" +
                        "Date d'ouverture: %s\n" +
                        "Rédemptions: %d",
                magasin.getCode(),
                magasin.getNom(),
                magasin.getTypeMagasin() != null ? magasin.getTypeMagasin() : "Non défini",
                magasin.getVille(),
                magasin.getAdresse(),
                magasin.getTelephone(),
                magasin.getResponsable(),
                magasin.getHoraires(),
                magasin.isActif() ? "✓ Actif" : "✗ Inactif",
                magasin.getDateOuverture(),
                magasin.getNombreRedemptions()
        );

        afficherInfo("Détails Magasin", details);
    }

    private void modifierMagasin(Magasin magasin) {
        panelFormulaire.setVisible(true);
        panelFormulaire.setManaged(true);
        magasinEnCours = magasin;

        txtNom.setText(magasin.getNom());
        txtAdresse.setText(magasin.getAdresse());
        txtVille.setText(magasin.getVille());
        txtTelephone.setText(magasin.getTelephone());
        txtResponsable.setText(magasin.getResponsable());
        comboTypeMagasin.setValue(magasin.getTypeMagasin());

        if (magasin.getHeureOuverture() != null) {
            txtHeureOuverture.setText(magasin.getHeureOuverture().toString());
        }
        if (magasin.getHeureFermeture() != null) {
            txtHeureFermeture.setText(magasin.getHeureFermeture().toString());
        }

        chkActif.setSelected(magasin.isActif());
    }

    private void supprimerMagasin(Magasin magasin) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer ce magasin ?");
        confirmation.setContentText("Magasin : " + magasin.getNom() + "\n\n⚠️ Cette action est irréversible !");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    MagasinDAO dao = new MagasinDAO();
                    boolean success = dao.deleteMagasin(magasin.getId());

                    if (success) {
                        listeMagasins.remove(magasin);
                        listeMagasinsFiltree.remove(magasin);
                        afficherSucces("Succès", "Magasin supprimé");
                        mettreAJourStatistiques();
                    }
                } catch (SQLException e) {
                    afficherErreur("Erreur", "Impossible de supprimer : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void exporterPDF() {
        try {
            String filename = PDFExporter.exportMagasins(listeMagasinsFiltree);
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
        magasinEnCours = null;
    }

    @FXML
    private void retourDashboard() {
        try {
            Main.changeScene("dashboard.fxml");
        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de retourner au dashboard");
            e.printStackTrace();
        }
    }

    @FXML
    private void actualiserListe() {
        chargerDepuisDB();
        mettreAJourStatistiques();
    }

    @FXML
    private void rechercherMagasin() {
        appliquerFiltre();
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