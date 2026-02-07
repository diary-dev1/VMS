package com.vms.controller;

import com.vms.Main;
import com.vms.dao.UserDAO;
import com.vms.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    // ==================== LISTE DES UTILISATEURS ====================
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colNomComplet;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Boolean> colActif;

    @FXML private Button btnCreate;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnToggleActive;
    @FXML private Button btnRefresh;
    @FXML private TextField txtSearch;

    // ==================== FORMULAIRE CREATE/EDIT ====================
    @FXML private VBox formContainer;
    @FXML private TextField txtUsername;
    @FXML private TextField txtNomComplet;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> comboRole;
    @FXML private CheckBox checkActif;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Label lblFormTitle;
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;
    @FXML private Label userNameLabel;
    @FXML private Label avatarLabel;

    private UserDAO userDAO;
    private ObservableList<User> userList;  // ✅ AJOUTÉ
    private User selectedUser;
    private boolean isEditMode = false;

    public UserController() {
        this.userDAO = new UserDAO();
        this.userList = FXCollections.observableArrayList();  // ✅ AJOUTÉ
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier que l'utilisateur connecté est ADMINISTRATEUR
        User currentUser = LoginController.getUtilisateurConnecte();
        if (currentUser == null || !currentUser.hasRole("ADMINISTRATEUR")) {
            System.err.println("❌ Accès refusé !");
            // ✅ Retourne APRÈS l'affichage de l'alerte
            javafx.application.Platform.runLater(() -> {
                afficherErreur("Accès refusé", "Vous devez être administrateur pour accéder à cette page");
                retourDashboard();
            });
            return;  // ✅ STOP ICI
        }

        // Initialiser le tableau
        initializeTable();

        // Initialiser le formulaire
        initializeForm();

        // Charger les utilisateurs
        loadUsers();

        // Masquer le formulaire au départ
        if (formContainer != null) {
            formContainer.setVisible(false);
        }
    }

    // ==================== INITIALISATION ====================

    private void initializeTable() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colUsername != null) colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        if (colNomComplet != null) colNomComplet.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colRole != null) colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (colActif != null) {
            colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
            colActif.setCellFactory(column -> new TableCell<User, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item ? "✓ Actif" : "✗ Inactif");
                        setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                    }
                }
            });
        }

        // Listener pour la sélection
        if (tableUsers != null) {
            tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                selectedUser = newSelection;
                updateButtonStates();
            });
        }
    }

    private void initializeForm() {
        if (comboRole != null) {
            comboRole.setItems(FXCollections.observableArrayList(
                    "CLIENT",
                    "APPROBATEUR",
                    "COMPTABLE",
                    "SUPERVISEUR_MAGASIN",
                    "ADMINISTRATEUR"
            ));
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedUser != null;
        if (btnEdit != null) btnEdit.setDisable(!hasSelection);
        if (btnDelete != null) btnDelete.setDisable(!hasSelection);
        if (btnToggleActive != null) btnToggleActive.setDisable(!hasSelection);
    }

    // ==================== READ (LOAD USERS) ====================

    @FXML
    private void loadUsers() {
        try {
            List<User> users = userDAO.findAll();
            userList.clear();  // ✅ MODIFIÉ
            userList.addAll(users);  // ✅ MODIFIÉ
            if (tableUsers != null) {
                tableUsers.setItems(userList);
            }
            System.out.println("✅ " + users.size() + " utilisateurs chargés");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des utilisateurs : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de charger les utilisateurs");
        }
    }

    @FXML
    private void handleSearch() {
        if (txtSearch == null || txtSearch.getText().trim().isEmpty()) {
            loadUsers();
            return;
        }

        String searchText = txtSearch.getText().trim().toLowerCase();
        ObservableList<User> filteredList = FXCollections.observableArrayList();

        for (User user : userList) {
            if (user.getUsername().toLowerCase().contains(searchText) ||
                    user.getNomComplet().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText) ||
                    user.getRole().toLowerCase().contains(searchText)) {
                filteredList.add(user);
            }
        }

        if (tableUsers != null) {
            tableUsers.setItems(filteredList);
        }
    }

    // ==================== CREATE ====================

    @FXML
    private void showCreateForm() {
        isEditMode = false;
        selectedUser = null;

        if (lblFormTitle != null) lblFormTitle.setText("Créer un nouvel utilisateur");

        clearForm();

        if (txtPassword != null) txtPassword.setDisable(false);
        if (txtConfirmPassword != null) txtConfirmPassword.setDisable(false);

        // ✅ AJOUTE CES LIGNES ICI :
        if (comboRole != null) {
            comboRole.setItems(FXCollections.observableArrayList(
                    "CLIENT",
                    "APPROBATEUR",
                    "COMPTABLE",
                    "SUPERVISEUR_MAGASIN",
                    "ADMINISTRATEUR"
            ));
        }

        if (formContainer != null) formContainer.setVisible(true);
    }
    // ==================== UPDATE ====================

    @FXML
    private void showEditForm() {
        if (selectedUser == null) {
            afficherErreur("Erreur", "Veuillez sélectionner un utilisateur");
            return;
        }

        isEditMode = true;

        if (lblFormTitle != null) lblFormTitle.setText("Modifier l'utilisateur");
        // ✅ AJOUTE CETTE LIGNE :
        if (comboRole != null) {
            comboRole.setItems(FXCollections.observableArrayList(
                    "CLIENT",
                    "APPROBATEUR",
                    "COMPTABLE",
                    "SUPERVISEUR_MAGASIN",
                    "ADMINISTRATEUR"
            ));
            comboRole.setValue(selectedUser.getRole());  // Puis set la valeur
        }


        // Remplir le formulaire avec les données existantes
        if (txtUsername != null) txtUsername.setText(selectedUser.getUsername());
        if (txtNomComplet != null) txtNomComplet.setText(selectedUser.getNomComplet());
        if (txtEmail != null) txtEmail.setText(selectedUser.getEmail());
        if (comboRole != null) comboRole.setValue(selectedUser.getRole());
        if (checkActif != null) checkActif.setSelected(selectedUser.isActif());

        // Désactiver les champs de mot de passe en mode édition
        if (txtPassword != null) {
            txtPassword.clear();
            txtPassword.setDisable(true);
        }
        if (txtConfirmPassword != null) {
            txtConfirmPassword.clear();
            txtConfirmPassword.setDisable(true);
        }

        if (formContainer != null) formContainer.setVisible(true);
    }

    @FXML
    private void handleSave() {
        // Validation
        if (!validateForm()) {
            return;
        }

        try {
            if (isEditMode) {
                // UPDATE
                updateUser();
            } else {
                // CREATE
                createUser();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    private void createUser() {
        String username = txtUsername.getText().trim();
        String nomComplet = txtNomComplet.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String role = comboRole.getValue();
        boolean actif = checkActif.isSelected();

        // Vérifier si le username existe déjà
        if (userDAO.usernameExists(username)) {
            afficherErreur("Erreur", "Ce nom d'utilisateur existe déjà");
            return;
        }

        // Vérifier si l'email existe déjà
        if (userDAO.emailExists(email)) {
            afficherErreur("Erreur", "Cet email est déjà utilisé");
            return;
        }

        // ✅ CRÉATION DIRECTE (remplace authService.register)
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(password);  // TODO: Implémenter BCrypt pour hasher
        newUser.setNomComplet(nomComplet);
        newUser.setEmail(email);
        newUser.setRole(role);
        newUser.setActif(actif);

        boolean success = userDAO.create(newUser);

        if (success) {
            afficherSucces("Succès", "Utilisateur créé avec succès");
            hideForm();
            loadUsers();
        } else {
            afficherErreur("Erreur", "Erreur lors de la création de l'utilisateur");
        }
    }

    private void updateUser() {
        String username = txtUsername.getText().trim();
        String nomComplet = txtNomComplet.getText().trim();
        String email = txtEmail.getText().trim();
        String role = comboRole.getValue();
        boolean actif = checkActif.isSelected();

        // Vérifier si le username existe (sauf pour cet utilisateur)
        if (userDAO.usernameExistsExcept(username, selectedUser.getId())) {
            afficherErreur("Erreur", "Ce nom d'utilisateur existe déjà");
            return;
        }

        // Vérifier si l'email existe (sauf pour cet utilisateur)
        if (userDAO.emailExistsExcept(email, selectedUser.getId())) {
            afficherErreur("Erreur", "Cet email est déjà utilisé");
            return;
        }

        // Mettre à jour l'utilisateur
        selectedUser.setUsername(username);
        selectedUser.setNomComplet(nomComplet);
        selectedUser.setEmail(email);
        selectedUser.setRole(role);
        selectedUser.setActif(actif);

        boolean success = userDAO.update(selectedUser);

        if (success) {
            afficherSucces("Succès", "Utilisateur modifié avec succès");
            hideForm();
            loadUsers();
        } else {
            afficherErreur("Erreur", "Erreur lors de la modification de l'utilisateur");
        }
    }

    // ==================== DELETE ====================

    @FXML
    private void handleDelete() {
        if (selectedUser == null) {
            afficherErreur("Erreur", "Veuillez sélectionner un utilisateur");
            return;
        }

        // Vérifier qu'on ne supprime pas l'utilisateur connecté
        User currentUser = LoginController.getUtilisateurConnecte();
        if (selectedUser.getId() == currentUser.getId()) {
            afficherErreur("Erreur", "Vous ne pouvez pas supprimer votre propre compte");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'utilisateur");
        alert.setContentText("Voulez-vous vraiment désactiver l'utilisateur " + selectedUser.getUsername() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = userDAO.softDelete(selectedUser.getId());

            if (success) {
                afficherSucces("Succès", "Utilisateur désactivé avec succès");
                loadUsers();
            } else {
                afficherErreur("Erreur", "Erreur lors de la suppression");
            }
        }
    }

    // ==================== TOGGLE ACTIVE ====================

    @FXML
    private void handleToggleActive() {
        if (selectedUser == null) {
            afficherErreur("Erreur", "Veuillez sélectionner un utilisateur");
            return;
        }

        // Vérifier qu'on ne désactive pas l'utilisateur connecté
        User currentUser = LoginController.getUtilisateurConnecte();
        if (selectedUser.getId() == currentUser.getId()) {
            afficherErreur("Erreur", "Vous ne pouvez pas désactiver votre propre compte");
            return;
        }

        String action = selectedUser.isActif() ? "désactiver" : "activer";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Changer le statut");
        alert.setContentText("Voulez-vous vraiment " + action + " l'utilisateur " + selectedUser.getUsername() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = userDAO.toggleActive(selectedUser.getId());

            if (success) {
                afficherSucces("Succès", "Statut modifié avec succès");
                loadUsers();
            } else {
                afficherErreur("Erreur", "Erreur lors du changement de statut");
            }
        }
    }

    // ==================== VALIDATION ====================

    private boolean validateForm() {
        if (txtUsername == null || txtUsername.getText().trim().isEmpty()) {
            afficherErreur("Validation", "Le nom d'utilisateur est obligatoire");
            return false;
        }

        if (txtNomComplet == null || txtNomComplet.getText().trim().isEmpty()) {
            afficherErreur("Validation", "Le nom complet est obligatoire");
            return false;
        }

        if (txtEmail == null || txtEmail.getText().trim().isEmpty()) {
            afficherErreur("Validation", "L'email est obligatoire");
            return false;
        }

        if (!isValidEmail(txtEmail.getText().trim())) {
            afficherErreur("Validation", "Format d'email invalide");
            return false;
        }

        if (comboRole == null || comboRole.getValue() == null) {
            afficherErreur("Validation", "Le rôle est obligatoire");
            return false;
        }

        // Validation du mot de passe seulement en mode création
        if (!isEditMode) {
            if (txtPassword == null || txtPassword.getText().isEmpty()) {
                afficherErreur("Validation", "Le mot de passe est obligatoire");
                return false;
            }

            if (txtPassword.getText().length() < 6) {
                afficherErreur("Validation", "Le mot de passe doit contenir au moins 6 caractères");
                return false;
            }

            if (txtConfirmPassword == null || !txtPassword.getText().equals(txtConfirmPassword.getText())) {
                afficherErreur("Validation", "Les mots de passe ne correspondent pas");
                return false;
            }
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    // ==================== UTILITAIRES ====================

    @FXML
    private void handleCancel() {
        hideForm();
    }

    private void hideForm() {
        if (formContainer != null) {
            formContainer.setVisible(false);
        }
        clearForm();
        selectedUser = null;
        isEditMode = false;
    }

    private void clearForm() {
        if (txtUsername != null) txtUsername.clear();
        if (txtNomComplet != null) txtNomComplet.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtPassword != null) txtPassword.clear();
        if (txtConfirmPassword != null) txtConfirmPassword.clear();
        if (comboRole != null) comboRole.setValue(null);
        if (checkActif != null) checkActif.setSelected(true);
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void retourDashboard() {
        try {
            Main.changeScene("demandes.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        retourDashboard();
    }
    @FXML
    private void handleLogout() {
        try {
            LoginController.deconnecter();
            Main.changeScene("login.fxml");
        } catch (IOException e) {
            afficherErreur("Erreur", "Erreur lors de la déconnexion");
        }
    }
}