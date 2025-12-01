package com.vms.controller;

import com.vms.Main;
import com.vms.dao.UtilisateurDAO;
import com.vms.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkSignup;
    @FXML private Label lblError;

    private static Utilisateur utilisateurConnecte;

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs");
            return;
        }

        try {
            UtilisateurDAO dao = new UtilisateurDAO();
            Utilisateur user = dao.authenticate(username, password);

            if (user != null) {
                utilisateurConnecte = user;
                utilisateurConnecte.setNomComplet(username);

                System.out.println("✅ Connexion réussie : " + username);

                Main.changeScene("dashboard.fxml");
            } else {
                afficherErreur("Username ou mot de passe incorrect");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur base de données : " + e.getMessage());
            afficherErreur("Erreur de connexion à la base de données");
        } catch (IOException e) {
            System.err.println("❌ Erreur navigation : " + e.getMessage());
            afficherErreur("Erreur lors de la navigation");
        }
    }

    @FXML
    private void goToSignup() {
        try {
            Main.changeScene("signup.fxml");
        } catch (IOException e) {
            System.err.println("❌ Erreur navigation vers Sign Up : " + e.getMessage());
            afficherErreur("Erreur lors de la navigation");
        }
    }

    private void afficherErreur(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static void deconnecter() {
        utilisateurConnecte = null;
    }
}