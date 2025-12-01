package com.vms.controller;

import com.vms.Main;
import com.vms.model.Client;
import com.vms.dao.ClientDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientsController {

    @FXML private Label userNameLabel;
    @FXML private TextField champRecherche;
    @FXML private Label totalClients, clientsActifs, totalDemandes;
    @FXML private TableView<Client> tableClients;
    @FXML private TableColumn<Client, String> colNumero, colNom, colEmail, colTelephone, colContact, colStatut;
    @FXML private TableColumn<Client, LocalDate> colDateInscription;
    @FXML private TableColumn<Client, Void> colActions;
    @FXML private VBox panelFormulaire;
    @FXML private TextField txtNom, txtEmail, txtTelephone, txtContact;
    @FXML private TextArea txtAdresse, txtRemarques;

    private ObservableList<Client> listeClients;
    private ObservableList<Client> listeClientsFiltree;
    private Client clientEnCours;

    @FXML
    public void initialize() {
        listeClients = FXCollections.observableArrayList();
        listeClientsFiltree = FXCollections.observableArrayList();
        configurerTableau();
        
        // TOUJOURS utiliser la base de données
        chargerDepuisDB();
        
        mettreAJourStatistiques();
    }

    private void chargerDepuisDB() {
        try {
            ClientDAO dao = new ClientDAO();
            List<Client> clients = dao.getAllClients();
            listeClients.clear();
            listeClients.addAll(clients);
            listeClientsFiltree.clear();
            listeClientsFiltree.addAll(clients);
            System.out.println("✅ " + clients.size() + " clients chargés depuis AlwaysData");
        } catch (SQLException e) {
            System.err.println("❌ Erreur base de données : " + e.getMessage());
            afficherErreur("Erreur", "Impossible de charger les clients depuis la base de données");
        }
    }

    private void configurerTableau() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroCompte"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactPersonne"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateInscription.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        colDateInscription.setCellFactory(col -> new TableCell<Client, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        });

        colActions.setCellFactory(col -> new TableCell<Client, Void>() {
            private final Button btnVoir = new Button("👁");
            private final Button btnModifier = new Button("✏");
            private final Button btnSupprimer = new Button("🗑");

            {
                btnVoir.setOnAction(e -> voirDetails(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(e -> modifierClient(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(e -> supprimerClient(getTableView().getItems().get(getIndex())));
                
                btnVoir.getStyleClass().add("button-small");
                btnModifier.getStyleClass().add("button-small");
                btnSupprimer.getStyleClass().add("button-small-danger");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, btnVoir, btnModifier, btnSupprimer));
            }
        });

        tableClients.setItems(listeClientsFiltree);
    }

    private void chargerDonneesTest() {
        Client c1 = new Client(1, "ABC Company Ltd", "contact@abc.com", "+230 5123 4567", 
            "Port Louis, Maurice", "John Doe", LocalDate.now().minusMonths(6));
        c1.genererNumeroCompte(1);
        c1.setNombreDemandesTotal(5);
        
        Client c2 = new Client(2, "XYZ Corporation", "info@xyz.com", "+230 5234 5678",
            "Rose-Hill, Maurice", "Jane Smith", LocalDate.now().minusMonths(3));
        c2.genererNumeroCompte(2);
        c2.setNombreDemandesTotal(3);
        
        Client c3 = new Client(3, "Tech Solutions", "hello@tech.com", "+230 5345 6789",
            "Ebene, Maurice", "Bob Wilson", LocalDate.now().minusMonths(1));
        c3.genererNumeroCompte(3);
        c3.setNombreDemandesTotal(2);

        listeClients.addAll(c1, c2, c3);
        listeClientsFiltree.addAll(listeClients);
    }

    private void mettreAJourStatistiques() {
        totalClients.setText(String.valueOf(listeClients.size()));
        clientsActifs.setText(String.valueOf(listeClients.stream().filter(Client::isActif).count()));
        totalDemandes.setText(String.valueOf(listeClients.stream().mapToInt(Client::getNombreDemandesTotal).sum()));
    }

    @FXML
    private void retourDashboard() {
        try { Main.changeScene("dashboard.fxml"); } 
        catch (IOException e) { afficherErreur("Erreur", "Impossible de retourner au dashboard"); }
    }

    @FXML
    private void afficherFormulaireNouveau() {
        clientEnCours = new Client(); // NOUVEAU client avec id = 0
        panelFormulaire.setVisible(true);
        panelFormulaire.setManaged(true);
        viderFormulaire();
    }

    @FXML
    private void annulerFormulaire() {
        panelFormulaire.setVisible(false);
        panelFormulaire.setManaged(false);
    }

    @FXML
    private void enregistrerClient() {
        if (txtNom.getText().isEmpty()) {
            afficherErreur("Erreur", "Le nom est obligatoire");
            return;
        }

        boolean estNouveau = clientEnCours.getId() == 0;

        // Mettre à jour les données du client
        clientEnCours.setNom(txtNom.getText());
        clientEnCours.setEmail(txtEmail.getText());
        clientEnCours.setTelephone(txtTelephone.getText());
        clientEnCours.setAdresse(txtAdresse.getText());
        clientEnCours.setContactPersonne(txtContact.getText());
        clientEnCours.setRemarques(txtRemarques.getText());

        try {
            ClientDAO dao = new ClientDAO();
            
            if (estNouveau) {
                // NOUVEAU client → Sauvegarder dans AlwaysData
                clientEnCours.genererNumeroCompte(listeClients.size() + 1);
                int id = dao.createClient(clientEnCours);
                
                if (id > 0) {
                    clientEnCours.setId(id);
                    listeClients.add(clientEnCours);
                    listeClientsFiltree.add(clientEnCours);
                    afficherSucces("Succès", "Client créé et sauvegardé dans AlwaysData !");
                } else {
                    afficherErreur("Erreur", "Impossible de créer le client");
                }
            } else {
                // MODIFICATION → Mettre à jour dans AlwaysData
                boolean success = dao.updateClient(clientEnCours);
                
                if (success) {
                    tableClients.refresh();
                    afficherSucces("Succès", "Client modifié dans AlwaysData !");
                } else {
                    afficherErreur("Erreur", "Impossible de modifier le client");
                }
            }
        } catch (SQLException e) {
            afficherErreur("Erreur base de données", e.getMessage());
        }

        mettreAJourStatistiques();
        annulerFormulaire();
    }

    @FXML
    private void actualiserListe() {
        tableClients.refresh();
        mettreAJourStatistiques();
    }

    @FXML
    private void rechercherClient() {
        String recherche = champRecherche.getText().toLowerCase();
        listeClientsFiltree.clear();
        if (recherche.isEmpty()) {
            listeClientsFiltree.addAll(listeClients);
        } else {
            listeClients.stream()
                .filter(c -> c.getNom().toLowerCase().contains(recherche) || 
                            c.getEmail().toLowerCase().contains(recherche))
                .forEach(listeClientsFiltree::add);
        }
    }

    private void voirDetails(Client client) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du client");
        alert.setHeaderText(client.getNom());
        alert.setContentText(
            "N° Compte : " + client.getNumeroCompte() + "\n" +
            "Email : " + client.getEmail() + "\n" +
            "Téléphone : " + client.getTelephone() + "\n" +
            "Contact : " + client.getContactPersonne() + "\n" +
            "Adresse : " + client.getAdresse() + "\n" +
            "Statut : " + client.getStatut() + "\n" +
            "Demandes : " + client.getNombreDemandesTotal()
        );
        alert.showAndWait();
    }

    private void modifierClient(Client client) {
        clientEnCours = client;
        txtNom.setText(client.getNom());
        txtEmail.setText(client.getEmail());
        txtTelephone.setText(client.getTelephone());
        txtAdresse.setText(client.getAdresse());
        txtContact.setText(client.getContactPersonne());
        txtRemarques.setText(client.getRemarques());
        panelFormulaire.setVisible(true);
        panelFormulaire.setManaged(true);
    }

    private void supprimerClient(Client client) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Suppression");
        confirmation.setHeaderText("Supprimer ce client ?");
        confirmation.setContentText("Client : " + client.getNom());
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                listeClients.remove(client);
                listeClientsFiltree.remove(client);
                mettreAJourStatistiques();
                afficherSucces("Succès", "Client supprimé !");
            }
        });
    }

    private void viderFormulaire() {
        txtNom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtContact.clear();
        txtRemarques.clear();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
