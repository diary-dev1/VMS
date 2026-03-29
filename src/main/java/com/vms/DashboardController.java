package com.vms;

import com.vms.controller.LoginController;
import com.vms.model.User;
import com.vms.util.RBACUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private Label timeLabel;
    @FXML private Label dateLabel;
    @FXML private Label userNameLabel;
    @FXML private Label avatarLabel;
    @FXML private StackPane avatarCircle;
    @FXML private VBox demandesCard;
    @FXML private VBox utilisateursCard;
    @FXML private VBox magasinCard;
    @FXML private VBox clientsCard;
    @FXML private VBox voucherCard;
    @FXML private VBox redemptionCard;

    @FXML
    public void initialize() {
        updateDateTime();

        User user = LoginController.getUtilisateurConnecte();
        if (user != null) {
            String nomComplet = user.getNomComplet();
            userNameLabel.setText(nomComplet);
            avatarLabel.setText(nomComplet.substring(0, 1).toUpperCase());
        } else {
            userNameLabel.setText("Guest");
            avatarLabel.setText("?");
        }

        // RBAC - cacher les cartes selon le rôle
        utilisateursCard.setVisible(RBACUtil.peutAcceder(user, "UTILISATEURS"));
        utilisateursCard.setManaged(RBACUtil.peutAcceder(user, "UTILISATEURS"));
        redemptionCard.setVisible(RBACUtil.peutAcceder(user, "REDEMPTION"));
        redemptionCard.setManaged(RBACUtil.peutAcceder(user, "REDEMPTION"));
        clientsCard.setVisible(RBACUtil.peutAcceder(user, "CLIENTS"));
        clientsCard.setManaged(RBACUtil.peutAcceder(user, "CLIENTS"));

        // Effets hover
        setupCardEffects(demandesCard);
        setupCardEffects(utilisateursCard);
        setupCardEffects(magasinCard);
        setupCardEffects(clientsCard);
        setupCardEffects(voucherCard);
        setupCardEffects(redemptionCard);
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        timeLabel.setText(now.format(timeFormatter));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy");
        dateLabel.setText(now.format(dateFormatter));
    }

    private void setupCardEffects(VBox card) {
        card.setOnMouseEntered(this::handleCardHover);
        card.setOnMouseExited(this::handleCardExit);
    }

    private void handleCardHover(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
        scaleTransition.setToX(1.05);
        scaleTransition.setToY(1.05);
        scaleTransition.play();
        card.setStyle(card.getStyle() + "-fx-background-color: #F7FAFC;");
    }

    private void handleCardExit(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();
        card.setStyle(card.getStyle() + "-fx-background-color: white;");
    }

    @FXML
    private void handleDemandesClick() {
        System.out.println("Navigation vers DEMANDES");
        try {
            Main.changeScene("demandes.fxml");
        } catch (Exception e) {
            showMessage("Erreur lors de la navigation vers DEMANDES");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUtilisateursClick() {
        System.out.println("Navigation vers UTILISATEURS");
        try {
            Main.changeScene("utilisateurs.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur lors du chargement du module Utilisateurs");
        }
    }

    @FXML
    private void handleMagasinClick() {
        System.out.println("Navigation vers MAGASIN");
        try {
            Main.changeScene("magasins.fxml");
        } catch (Exception e) {
            showMessage("Erreur lors de la navigation vers MAGASIN");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClientsClick() {
        System.out.println("Navigation vers CLIENTS");
        try {
            Main.changeScene("clients.fxml");
        } catch (Exception e) {
            showMessage("Erreur lors de la navigation vers CLIENTS");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVoucherClick() {
        System.out.println("Navigation vers VOUCHER");
        try {
            Main.changeScene("vouchers.fxml");
        } catch (Exception e) {
            showMessage("Erreur lors de la navigation vers VOUCHER");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRedemptionClick() {
        System.out.println("Navigation vers RÉDEMPTION");
        try {
            Main.changeScene("redemption.fxml");
        } catch (Exception e) {
            showMessage("Erreur lors de la navigation vers RÉDEMPTION");
            e.printStackTrace();
        }
    }

    private void showMessage(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("VMS");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        System.out.println("Déconnexion");
        try {
            LoginController.deconnecter();
            Main.changeScene("login.fxml");
        } catch (IOException e) {
            showMessage("Erreur lors de la déconnexion");
        }
    }
}