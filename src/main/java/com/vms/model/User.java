package com.vms.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String nomComplet;
    private String email;
    private String role;
    private boolean actif;
    private LocalDateTime derniereConnexion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeur vide
    public User() {}

    // Constructeur complet
    public User(String username, String passwordHash, String nomComplet,
                String email, String role, boolean actif) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nomComplet = nomComplet;
        this.email = email;
        this.role = role;
        this.actif = actif;
    }

    // Méthodes utiles pour vérifier les rôles
    public boolean hasRole(String requiredRole) {
        return this.role != null && this.role.equals(requiredRole);
    }

    public boolean hasAnyRole(String... roles) {
        if (this.role == null) return false;
        for (String r : roles) {
            if (this.role.equals(r)) return true;
        }
        return false;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nomComplet='" + nomComplet + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", actif=" + actif +
                '}';
    }
}