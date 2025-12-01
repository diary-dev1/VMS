package com.vms.controller;

import com.vms.Main;
import com.vms.dao.UtilisateurDAO;
import com.vms.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;

public class SignupController {

    @FXML private TextField txtNomComplet;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnSignup;
    @FXML private Hyperlink linkLogin;

    /**
     * Gérer l'inscription
     */
    @FXML
    private void handleSignup() {
        // Récupérer les valeurs
        String nomComplet = txtNomComplet.getText().trim();
        String email = txtEmail.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        // Validation des champs
        if (nomComplet.isEmpty() || email.isEmpty() || username.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            afficherErreur("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        // Validation email
        if (!email.contains("@")) {
            afficherErreur("Erreur", "Veuillez entrer un email valide");
            return;
        }

        // Validation username (minimum 3 caractères)
        if (username.length() < 3) {
            afficherErreur("Erreur", "Le username doit contenir au moins 3 caractères");
            return;
        }

        // Validation password (minimum 6 caractères)
        if (password.length() < 6) {
            afficherErreur("Erreur", "Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        // Vérifier que les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            afficherErreur("Erreur", "Les mots de passe ne correspondent pas");
            return;
        }

        try {
            // Créer l'utilisateur
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setUsername(username);
            utilisateur.setPasswordHash(password); // En production, hasher le mot de passe
            utilisateur.setNomComplet(nomComplet);
            utilisateur.setEmail(email);
            utilisateur.setRole("UTILISATEUR"); // Rôle par défaut
            utilisateur.setActif(true);

            // Sauvegarder dans AlwaysData
            UtilisateurDAO dao = new UtilisateurDAO();
            int id = dao.createUtilisateur(utilisateur);

            if (id > 0) {
                afficherSucces("Succès",
                        "Compte créé avec succès !\n\n" +
                                "Username : " + username + "\n" +
                                "Vous pouvez maintenant vous connecter.");

                // Rediriger vers Login
                goToLogin();
            } else {
                afficherErreur("Erreur", "Impossible de créer le compte");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key") || e.getMessage().contains("unique")) {
                afficherErreur("Erreur", "Ce username ou email existe déjà");
            } else {
                afficherErreur("Erreur base de données",
                        "Impossible de créer le compte.\n" + e.getMessage());
            }
        }
    }

    /**
     * Retour vers Login
     */
    @FXML
    private void goToLogin() {
        try {
            Main.changeScene("login.fxml");
        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de revenir à la page de connexion");
        }
    }

    /**
     * Afficher un message de succès
     */
    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher un message d'erreur
     */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}