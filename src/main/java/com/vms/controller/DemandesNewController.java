package com.vms.controller;

import com.vms.Main;
import com.vms.model.Demande;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DemandesNewController {

    @FXML private TableView<Demande> tableDemandes;
    @FXML private TableColumn<Demande, String> colNom;
    @FXML private TableColumn<Demande, String> colPrenom;
    @FXML private TableColumn<Demande, String> colTelephone;
    @FXML private TableColumn<Demande, String> colDate;
    @FXML private TableColumn<Demande, String> colStatut;
    @FXML private TableColumn<Demande, Void> colAction;
    @FXML private TextField txtRecherche;

    private ObservableList<Demande> listeDemandes;

    @FXML
    public void initialize() {
        listeDemandes = FXCollections.observableArrayList();
        configurerTableau();
        chargerDonneesTest();
    }

    private void configurerTableau() {
        // Colonnes de base
        colNom.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(extraireNom(data.getValue())));
        colPrenom.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(extrairePrenom(data.getValue())));
        colTelephone.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getClientNom()));
        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDateCreation().format(DateTimeFormatter.ofPattern("dd.MMM.yyyy")) + " at 10:00 AM"));

        // Colonne Statut avec badge coloré
        colStatut.setCellFactory(col -> new TableCell<Demande, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Demande demande = getTableRow().getItem();
                    Label badge = new Label(demande.getStatut());
                    badge.getStyleClass().add("status-badge");

                    // Couleur selon statut
                    switch (demande.getStatut().toUpperCase()) {
                        case "EN_ATTENTE":
                        case "OPEN":
                            badge.getStyleClass().add("status-open");
                            badge.setText("Open");
                            break;
                        case "PAYE":
                        case "BOOKED":
                            badge.getStyleClass().add("status-booked");
                            badge.setText("Booked");
                            break;
                        case "COMPLETE":
                        case "COMPLETED":
                            badge.getStyleClass().add("status-completed");
                            badge.setText("Completed");
                            break;
                        default:
                            badge.getStyleClass().add("status-open");
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Colonne Actions avec boutons modernes
        colAction.setCellFactory(col -> new TableCell<Demande, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Demande demande = getTableRow().getItem();

                    // Bouton Envoyer (vert)
                    Button btnEnvoyer = new Button("Envoyer");
                    btnEnvoyer.getStyleClass().add("btn-send");
                    btnEnvoyer.setOnAction(e -> envoyerDemande(demande));

                    // Bouton Modifier (violet)
                    Button btnModifier = new Button("Modifier");
                    btnModifier.getStyleClass().add("btn-modify");
                    btnModifier.setOnAction(e -> modifierDemande(demande));

                    // Bouton Supprimer (icône)
                    Button btnSupprimer = new Button("🗑");
                    btnSupprimer.getStyleClass().add("btn-delete");
                    btnSupprimer.setOnAction(e -> supprimerDemande(demande));

                    // Bouton Voir (icône)
                    Button btnVoir = new Button("👁");
                    btnVoir.getStyleClass().add("btn-view");
                    btnVoir.setOnAction(e -> voirDemande(demande));

                    HBox actions = new HBox(5, btnEnvoyer, btnModifier, btnSupprimer, btnVoir);
                    actions.setAlignment(Pos.CENTER);
                    setGraphic(actions);
                }
            }
        });

        tableDemandes.setItems(listeDemandes);
    }

    private void chargerDonneesTest() {
        // Données de test comme dans l'image
        Demande d1 = new Demande(1, "DEM-001", 1, "Jane Cooper", 10, 500, "EN_ATTENTE", LocalDate.of(2023, 8, 13));

        Demande d2 = new Demande(2, "DEM-002", 2, "Wade Warren", 20, 500, "PAYE", LocalDate.of(2023, 8, 13));

        Demande d3 = new Demande(3, "DEM-003", 3, "Brooklyn Simmons", 15, 500, "COMPLETE", LocalDate.of(2023, 8, 13));

        Demande d4 = new Demande(4, "DEM-004", 4, "Cameron Williamson", 25, 500, "EN_ATTENTE", LocalDate.of(2023, 8, 13));

        Demande d5 = new Demande(5, "DEM-005", 5, "Leslie Alexander", 30, 500, "EN_ATTENTE", LocalDate.of(2023, 8, 13));

        Demande d6 = new Demande(6, "DEM-006", 6, "Savannah Nguyen", 12, 500, "EN_ATTENTE", LocalDate.of(2023, 8, 13));

        Demande d7 = new Demande(7, "DEM-007", 7, "Darlene Robertson", 18, 500, "COMPLETE", LocalDate.of(2023, 8, 13));

        Demande d8 = new Demande(8, "DEM-008", 8, "Ronald Richards", 22, 500, "EN_ATTENTE", LocalDate.of(2023, 8, 13));

        Demande d9 = new Demande(9, "DEM-009", 9, "Kathryn Murphy", 16, 500, "EN_ATTENTE", LocalDate.of(2023, 8, 13));

        Demande d10 = new Demande(10, "DEM-010", 10, "Darrell Steward", 20, 500, "EN_ATTENTE", LocalDate.of(2023, 8, 13));

        listeDemandes.addAll(d1, d2, d3, d4, d5, d6, d7, d8, d9, d10);
    }

    private String extraireNom(Demande demande) {
        String[] parts = demande.getClientNom().split(" ");
        return parts.length > 0 ? parts[0] : "";
    }

    private String extrairePrenom(Demande demande) {
        String[] parts = demande.getClientNom().split(" ");
        return parts.length > 1 ? parts[1] : "";
    }

    private void envoyerDemande(Demande demande) {
        showInfo("Envoyer", "Demande de " + demande.getClientNom() + " envoyée !");
    }

    private void modifierDemande(Demande demande) {
        showInfo("Modifier", "Modification de la demande de " + demande.getClientNom());
    }

    private void supprimerDemande(Demande demande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cette demande ?");
        alert.setContentText("Client : " + demande.getClientNom());
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                listeDemandes.remove(demande);
                showInfo("Succès", "Demande supprimée");
            }
        });
    }

    private void voirDemande(Demande demande) {
        showInfo("Détails", "Demande #" + demande.getReference() + "\nClient: " + demande.getClientNom());
    }

    @FXML
    private void handleLogout() {
        try {
            Main.changeScene("login.fxml");
        } catch (IOException e) {
            showError("Erreur", "Impossible de se déconnecter");
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}