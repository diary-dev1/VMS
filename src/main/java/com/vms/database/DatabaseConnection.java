package com.vms.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe pour gérer la connexion à la base de données PostgreSQL
 */
public class DatabaseConnection {

    // Configuration de la base de données AlwaysData
    private static final String URL = "jdbc:postgresql://postgresql-diary.alwaysdata.net:5432/diary_vms";
    private static final String USER = "diary";
    private static final String PASSWORD = "Fanekena";

    private static Connection connection = null;

    /**
     * Obtenir une connexion à la base de données
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Charger le driver PostgreSQL
                Class.forName("org.postgresql.Driver");

                System.out.println("📡 Connexion à AlwaysData...");
                System.out.println("🔗 URL : " + URL);
                System.out.println("👤 User : " + USER);

                // Créer la connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);

                System.out.println("✅ Connexion à PostgreSQL réussie !");

            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver PostgreSQL non trouvé !");
                System.err.println("💡 Vérifiez que postgresql-42.7.1.jar est dans pom.xml");
                throw new SQLException("Driver PostgreSQL non trouvé", e);
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion à PostgreSQL !");
                System.err.println("💡 Message : " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    /**
     * Fermer la connexion
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Connexion fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture de la connexion");
                e.printStackTrace();
            }
        }
    }

    /**
     * Tester la connexion
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}