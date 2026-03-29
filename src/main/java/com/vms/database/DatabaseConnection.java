package com.vms.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://postgresql-diary.alwaysdata.net:5432/diary_vms";
    private static final String USER = "diary";
    private static final String PASSWORD = "Fanekena";

    private static Connection connection = null;
    private static boolean driverLoaded = false; // ← évite de recharger le driver

    public static Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection; // ← retourne directement si déjà connecté
        }

        if (!driverLoaded) {
            try {
                Class.forName("org.postgresql.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver PostgreSQL non trouvé", e);
            }
        }

        System.out.println("Connexion à AlwaysData...");
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Connexion réussie !");
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // ← reset pour permettre une reconnexion propre
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean testConnection() {
        try {
            return getConnection() != null;
        } catch (SQLException e) {
            return false;
        }
    }
}