package com.vms.util;

import com.vms.model.Voucher;
import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EmailSender {

    // Configuration email - À MODIFIER avec vos identifiants Gmail
    private static final String EMAIL_FROM = "sojavelodiary@gmail.com";
    private static final String EMAIL_PASSWORD = "amizrdyfkktrooft";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    /**
     * Envoyer un voucher par email avec PDF en pièce jointe
     */
    public static boolean envoyerVoucherParEmail(Voucher voucher, String emailDestinataire, String pdfPath) {
        try {
            // Configuration SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            // Création de la session
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinataire));
            message.setSubject("Votre Voucher VMS - " + voucher.getCode());

            // Corps du message (partie texte)
            MimeBodyPart textPart = new MimeBodyPart();
            String contenuEmail = String.format(
                    "Bonjour,\n\n" +
                            "Veuillez trouver ci-joint votre voucher VMS.\n\n" +
                            "Détails du voucher :\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "Code           : %s\n" +
                            "Valeur         : %.2f Rs\n" +
                            "Date émission  : %s\n" +
                            "Date expiration: %s\n" +
                            "Statut         : %s\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "Instructions :\n" +
                            "1. Téléchargez le PDF ci-joint\n" +
                            "2. Présentez-le au magasin partenaire\n" +
                            "3. Le magasin scannera le QR Code pour validation\n" +
                            "4. Le montant sera déduit de la valeur du voucher\n\n" +
                            "Ce voucher est valable jusqu'au %s.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe VMS\n" +
                            "Voucher Management System",
                    voucher.getCode(),
                    voucher.getValeur(),
                    voucher.getDateEmission(),
                    voucher.getDateExpiration(),
                    voucher.getStatut(),
                    voucher.getDateExpiration()
            );
            textPart.setText(contenuEmail);

            // Pièce jointe (PDF)
            MimeBodyPart attachmentPart = new MimeBodyPart();
            File pdfFile = new File(pdfPath);

            // Lire le fichier PDF
            FileInputStream fileInputStream = new FileInputStream(pdfFile);
            byte[] pdfBytes = new byte[(int) pdfFile.length()];
            fileInputStream.read(pdfBytes);
            fileInputStream.close();

            // Créer la source de données
            ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName(pdfFile.getName());

            // Assemblage du message
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            // Envoi
            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès à " + emailDestinataire);
            return true;

        } catch (MessagingException | IOException e) {
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoyer un email simple (sans pièce jointe)
     */
    public static boolean envoyerEmailSimple(String destinataire, String sujet, String contenu) {
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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(contenu);

            Transport.send(message);

            System.out.println("✅ Email simple envoyé à " + destinataire);
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifier la configuration email
     */
    public static boolean verifierConfiguration() {
        if (EMAIL_FROM.equals("votre-email@gmail.com") || EMAIL_PASSWORD.equals("votre-mot-de-passe-app")) {
            System.err.println("⚠️ Configuration email non définie !");
            return false;
        }
        return true;
    }
}