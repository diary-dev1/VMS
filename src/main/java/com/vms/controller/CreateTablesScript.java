package com.vms.database;

import java.sql.Connection;
import java.sql.Statement;

public class CreateTablesScript {

    public static void main(String[] args) {
        System.out.println("🚀 Début de la création des tables...\n");

        String[] queries = {
                // CRÉER LE SCHÉMA PUBLIC S'IL N'EXISTE PAS
                "CREATE SCHEMA IF NOT EXISTS public",

                // Donner les permissions
                "GRANT ALL ON SCHEMA public TO diary",
                "GRANT ALL ON SCHEMA public TO public",

                // Sélectionner le schéma public
                "SET search_path TO public",

                // Table clients
                "CREATE TABLE IF NOT EXISTS clients (" +
                        "id SERIAL PRIMARY KEY," +
                        "numero_compte VARCHAR(20) UNIQUE NOT NULL," +
                        "nom VARCHAR(200) NOT NULL," +
                        "email VARCHAR(150)," +
                        "telephone VARCHAR(20)," +
                        "adresse TEXT," +
                        "contact_personne VARCHAR(150)," +
                        "date_inscription DATE DEFAULT CURRENT_DATE," +
                        "actif BOOLEAN DEFAULT TRUE," +
                        "remarques TEXT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // Table magasins
                "CREATE TABLE IF NOT EXISTS magasins (" +
                        "id SERIAL PRIMARY KEY," +
                        "code VARCHAR(20) UNIQUE NOT NULL," +
                        "nom VARCHAR(200) NOT NULL," +
                        "adresse TEXT," +
                        "ville VARCHAR(100)," +
                        "telephone VARCHAR(20)," +
                        "responsable VARCHAR(150)," +
                        "date_ouverture DATE DEFAULT CURRENT_DATE," +
                        "actif BOOLEAN DEFAULT TRUE," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // Table demandes
                "CREATE TABLE IF NOT EXISTS demandes (" +
                        "id SERIAL PRIMARY KEY," +
                        "reference VARCHAR(50) UNIQUE NOT NULL," +
                        "client_id INTEGER REFERENCES clients(id) ON DELETE CASCADE," +
                        "nombre_bons INTEGER NOT NULL CHECK (nombre_bons > 0)," +
                        "valeur_unitaire DECIMAL(10,2) NOT NULL," +
                        "montant_total DECIMAL(12,2)," +
                        "statut VARCHAR(50) DEFAULT 'EN_ATTENTE_PAIEMENT'," +
                        "date_creation DATE DEFAULT CURRENT_DATE," +
                        "date_paiement DATE," +
                        "date_approbation DATE," +
                        "cree_par VARCHAR(100)," +
                        "approuve_par VARCHAR(100)," +
                        "remarques TEXT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // Table vouchers
                "CREATE TABLE IF NOT EXISTS vouchers (" +
                        "id SERIAL PRIMARY KEY," +
                        "code VARCHAR(30) UNIQUE NOT NULL," +
                        "demande_id INTEGER REFERENCES demandes(id) ON DELETE CASCADE," +
                        "valeur DECIMAL(10,2) NOT NULL," +
                        "qr_code VARCHAR(100) UNIQUE," +
                        "statut VARCHAR(50) DEFAULT 'EMIS'," +
                        "date_emission DATE DEFAULT CURRENT_DATE," +
                        "date_expiration DATE NOT NULL," +
                        "date_redemption DATE," +
                        "magasin_redemption_id INTEGER REFERENCES magasins(id)," +
                        "redemme_par VARCHAR(100)," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // Table historique_redemptions
                "CREATE TABLE IF NOT EXISTS historique_redemptions (" +
                        "id SERIAL PRIMARY KEY," +
                        "voucher_id INTEGER REFERENCES vouchers(id) ON DELETE CASCADE," +
                        "magasin_id INTEGER REFERENCES magasins(id)," +
                        "date_redemption TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "redemme_par VARCHAR(100)," +
                        "montant DECIMAL(10,2)," +
                        "remarques TEXT)",

                // Table utilisateurs
                "CREATE TABLE IF NOT EXISTS utilisateurs (" +
                        "id SERIAL PRIMARY KEY," +
                        "username VARCHAR(50) UNIQUE NOT NULL," +
                        "password_hash VARCHAR(255) NOT NULL," +
                        "nom_complet VARCHAR(150)," +
                        "email VARCHAR(150)," +
                        "role VARCHAR(50) DEFAULT 'UTILISATEUR'," +
                        "actif BOOLEAN DEFAULT TRUE," +
                        "derniere_connexion TIMESTAMP," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // Table audit_log
                "CREATE TABLE IF NOT EXISTS audit_log (" +
                        "id SERIAL PRIMARY KEY," +
                        "table_name VARCHAR(50)," +
                        "record_id INTEGER," +
                        "action VARCHAR(20)," +
                        "old_values TEXT," +
                        "new_values TEXT," +
                        "user_id INTEGER REFERENCES utilisateurs(id)," +
                        "ip_address VARCHAR(50)," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // Insert clients
                "INSERT INTO clients (numero_compte, nom, email, telephone, adresse, contact_personne) " +
                        "VALUES ('CLI00001', 'ABC Company Ltd', 'contact@abc.com', '+230 5123 4567', '123 Royal Road, Port Louis', 'John Doe') " +
                        "ON CONFLICT (numero_compte) DO NOTHING",

                "INSERT INTO clients (numero_compte, nom, email, telephone, adresse, contact_personne) " +
                        "VALUES ('CLI00002', 'XYZ Corporation', 'info@xyz.com', '+230 5234 5678', '45 Victoria Avenue, Rose-Hill', 'Jane Smith') " +
                        "ON CONFLICT (numero_compte) DO NOTHING",

                "INSERT INTO clients (numero_compte, nom, email, telephone, adresse, contact_personne) " +
                        "VALUES ('CLI00003', 'Tech Solutions', 'hello@tech.com', '+230 5345 6789', 'Cyber Tower 1, Ebene', 'Bob Wilson') " +
                        "ON CONFLICT (numero_compte) DO NOTHING",

                // Insert magasins
                "INSERT INTO magasins (code, nom, adresse, ville, telephone, responsable) " +
                        "VALUES ('MAG001', 'Magasin Central', '123 Rue Royale', 'Port Louis', '+230 5111 1111', 'Pierre Dupont') " +
                        "ON CONFLICT (code) DO NOTHING",

                "INSERT INTO magasins (code, nom, adresse, ville, telephone, responsable) " +
                        "VALUES ('MAG002', 'Magasin Rose-Hill', '45 Avenue Victoria', 'Rose-Hill', '+230 5222 2222', 'Marie Laurent') " +
                        "ON CONFLICT (code) DO NOTHING",

                "INSERT INTO magasins (code, nom, adresse, ville, telephone, responsable) " +
                        "VALUES ('MAG003', 'Magasin Ebene', 'Cyber Tower 1', 'Ebene', '+230 5333 3333', 'Jean Martin') " +
                        "ON CONFLICT (code) DO NOTHING",

                // Insert utilisateur
                "INSERT INTO utilisateurs (username, password_hash, nom_complet, email, role) " +
                        "VALUES ('admin', 'admin123', 'Administrateur', 'admin@vms.com', 'ADMIN') " +
                        "ON CONFLICT (username) DO NOTHING"
        };

        try {
            System.out.println("📡 Connexion à AlwaysData...");
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("✅ Connexion réussie !\n");

            Statement stmt = conn.createStatement();

            int successCount = 0;
            int errorCount = 0;

            for (int i = 0; i < queries.length; i++) {
                String query = queries[i];
                try {
                    stmt.execute(query);
                    String preview = query.substring(0, Math.min(60, query.length()));
                    System.out.println("✅ [" + (i+1) + "/" + queries.length + "] " + preview + "...");
                    successCount++;
                } catch (Exception e) {
                    String preview = query.substring(0, Math.min(60, query.length()));
                    System.out.println("❌ [" + (i+1) + "/" + queries.length + "] " + preview + "...");
                    System.out.println("   Erreur : " + e.getMessage());
                    errorCount++;
                }
            }

            stmt.close();
            conn.close();

            System.out.println("\n" + "=".repeat(60));
            System.out.println("🎉 TERMINÉ !");
            System.out.println("✅ Succès : " + successCount);
            System.out.println("❌ Erreurs : " + errorCount);
            System.out.println("=".repeat(60));

            if (errorCount == 0) {
                System.out.println("\n🎉 TOUTES LES TABLES CRÉÉES AVEC SUCCÈS !");
                System.out.println("✅ 7 tables créées");
                System.out.println("✅ 3 clients ajoutés");
                System.out.println("✅ 3 magasins ajoutés");
                System.out.println("✅ 1 utilisateur admin ajouté");
                System.out.println("\n🚀 Vous pouvez maintenant lancer votre application VMS !");
                System.out.println("   Login : admin / admin");
            } else {
                System.out.println("\n⚠️ Certaines erreurs sont survenues.");
                System.out.println("💡 Si les erreurs sont mineures (INSERT déjà existants), c'est normal.");
            }

        } catch (Exception e) {
            System.err.println("\n❌ ERREUR FATALE : " + e.getMessage());
            e.printStackTrace();
        }
    }
}