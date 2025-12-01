# 🏪 INSTALLATION COMPLÈTE VMS - PRÊT POUR LE SUPERMARCHÉ

## 🎯 OBJECTIF
Installer le système VMS complet et fonctionnel avec PostgreSQL pour utilisation en production dans un supermarché.

---

## ⏱️ TEMPS TOTAL ESTIMÉ : 2-3 HEURES

- PostgreSQL : 30 min
- Configuration : 30 min  
- Adaptation code : 1-1.5h
- Tests : 30 min

---

## 📋 PRÉREQUIS

```
✅ Windows 10/11
✅ Java 17 ou 21 installé
✅ IntelliJ IDEA installé
✅ Connexion Internet
✅ Droits administrateur
```

---

# PARTIE 1 : INSTALLATION POSTGRESQL (30 MIN)

## Étape 1.1 : Télécharger PostgreSQL

```
1. Allez sur : https://www.postgresql.org/download/windows/
2. Cliquez "Download the installer"
3. Téléchargez PostgreSQL 16.x
4. Lancez l'installateur (postgresql-16.x-windows-x64.exe)
```

## Étape 1.2 : Installer PostgreSQL

```
1. Welcome → Next
2. Installation Directory : C:\Program Files\PostgreSQL\16 (par défaut)
3. Components :
   ☑ PostgreSQL Server
   ☑ pgAdmin 4
   ☑ Command Line Tools
   ☐ Stack Builder (décochez)
4. Data Directory : (par défaut) → Next
5. Password : 
   Entrez un mot de passe SIMPLE
   Exemple : postgres
   ⚠️ NOTEZ CE MOT DE PASSE !
6. Port : 5432 (par défaut)
7. Locale : Default locale
8. Next → Next → Install
9. Attendez l'installation (5 min)
10. Finish (décochez Stack Builder)
```

## Étape 1.3 : Vérifier l'installation

```
1. Menu Démarrer → Recherchez "pgAdmin 4"
2. Lancez pgAdmin 4
3. Entrez votre mot de passe master
4. Dans le panneau gauche : PostgreSQL 16
5. Si vous voyez "Databases", c'est OK ✅
```

---

# PARTIE 2 : CRÉER LA BASE DE DONNÉES (10 MIN)

## Étape 2.1 : Créer vms_database

### Méthode A : Avec pgAdmin 4 (Recommandé)

```
1. pgAdmin 4 ouvert
2. Panneau gauche → Servers → PostgreSQL 16
3. Clic droit sur "Databases"
4. Create → Database...
5. General tab :
   Database : vms_database
   Owner : postgres
6. Save
7. ✅ Vous voyez "vms_database" dans la liste !
```

### Méthode B : Avec SQL Shell (psql)

```
1. Menu Démarrer → "SQL Shell (psql)"
2. Appuyez Entrée 4 fois (valeurs par défaut)
3. Password : [votre mot de passe]
4. Tapez : CREATE DATABASE vms_database;
5. Tapez : \l (pour lister les DB)
6. ✅ Vous voyez vms_database !
```

## Étape 2.2 : Exécuter le script SQL

```
1. pgAdmin 4 → Sélectionnez "vms_database"
2. Clic droit sur "vms_database" → Query Tool
3. Menu → File → Open (icône dossier 📁)
4. Naviguez vers : vms-dashboard/database/schema.sql
5. Ouvrez le fichier
6. Cliquez sur le bouton Play ▶️ (ou F5)
7. Attendez... (10-20 secondes)
8. ✅ Message : "Query returned successfully"
```

## Étape 2.3 : Vérifier les tables

```
1. pgAdmin 4, panneau gauche
2. vms_database → Schemas → public → Tables
3. Clic droit sur "Tables" → Refresh
4. Vous devez voir :
   ✅ clients
   ✅ demandes
   ✅ historique_redemptions
   ✅ magasins
   ✅ utilisateurs
   ✅ vouchers
   ✅ audit_log
```

---

# PARTIE 3 : CONFIGURER L'APPLICATION (5 MIN)

## Étape 3.1 : Ouvrir le projet

```
1. Décompressez vms-complet-production.zip
2. IntelliJ IDEA → File → Open
3. Sélectionnez le dossier "vms-dashboard"
4. Click OK
5. "Trust Project" → Trust
6. Enable Auto-Import (Maven)
7. Attendez Maven (2-3 min)
```

## Étape 3.2 : Configurer PostgreSQL dans le code

```
1. Ouvrez : src/main/java/com/vms/database/DatabaseConnection.java

2. Modifiez les lignes 7-9 :

   private static final String URL = "jdbc:postgresql://localhost:5432/vms_database";
   private static final String USER = "postgres";
   private static final String PASSWORD = "postgres";  ← VOTRE mot de passe ici !

3. Sauvegardez (Ctrl+S)
```

## Étape 3.3 : Tester la connexion

```
1. Ouvrez : src/main/java/com/vms/Main.java

2. Dans la méthode start(), AVANT le chargement du FXML, ajoutez :

   // Test connexion PostgreSQL
   if (DatabaseConnection.testConnection()) {
       System.out.println("✅ PostgreSQL connecté !");
   } else {
       System.out.println("❌ Erreur PostgreSQL - Mode démo");
   }

3. Sauvegardez
```

---

# PARTIE 4 : MODE HYBRIDE DB/MÉMOIRE (15 MIN)

Le système fonctionne en mode HYBRIDE :
- ✅ Essaie d'abord PostgreSQL
- ✅ Si échec → Fallback sur données de test

## Étape 4.1 : Vérifier DemandesController

Le contrôleur doit avoir cette structure :

```java
@FXML
public void initialize() {
    listeDemandes = FXCollections.observableArrayList();
    listeDemandesFiltree = FXCollections.observableArrayList();
    configurerTableau();
    configurerFiltres();
    
    // Mode Hybride : PostgreSQL ou Mémoire
    if (DatabaseConnection.testConnection()) {
        chargerDepuisDB();
    } else {
        chargerDonneesTest();
    }
    
    mettreAJourStatistiques();
}

private void chargerDepuisDB() {
    try {
        DemandeDAO dao = new DemandeDAO();
        List<Demande> demandes = dao.getAllDemandes();
        listeDemandes.addAll(demandes);
        listeDemandesFiltree.addAll(demandes);
        System.out.println("✅ " + demandes.size() + " demandes chargées de PostgreSQL");
    } catch (SQLException e) {
        System.err.println("❌ Erreur DB : " + e.getMessage());
        chargerDonneesTest(); // Fallback
    }
}
```

## Étape 4.2 : Même chose pour les autres contrôleurs

Appliquez la même logique à :
- ClientsController
- MagasinsController  
- VouchersController

---

# PARTIE 5 : LANCER ET TESTER (30 MIN)

## Étape 5.1 : Configuration IntelliJ

```
1. File → Project Structure → Project
   SDK : Java 17 ou 21
   
2. Run → Edit Configurations
   Main class : com.vms.Main
   VM options : [vos options JavaFX habituelles]
   
3. Apply → OK
```

## Étape 5.2 : Premier lancement

```
1. Cliquez sur Play ▶️
2. Console IntelliJ (en bas) :
   Cherchez : "✅ PostgreSQL connecté !"
3. Si vous voyez ça, c'est BON ! 🎉
4. Si vous voyez "❌", vérifiez :
   - PostgreSQL lancé ?
   - Mot de passe correct ?
   - Base de données créée ?
```

## Étape 5.3 : Tests fonctionnels

### Test 1 : CLIENTS (5 min)

```
1. Dashboard → CLIENTS
2. ✅ Vous voyez les 3 clients de test (de la DB)
3. Créez un nouveau client :
   - Nom : "Test Supermarché SA"
   - Email : "test@supermarche.mu"
   - Téléphone : "+230 5999 9999"
   - Cliquez Enregistrer
4. ✅ Client ajouté
5. Fermez l'application
6. Relancez
7. ✅ Le client "Test Supermarché SA" est toujours là !
   (Preuve que c'est sauvegardé en DB)
```

### Test 2 : DEMANDES (5 min)

```
1. Dashboard → DEMANDES
2. Créez une demande :
   - Client : Test Supermarché SA
   - Nombre de bons : 100
   - Valeur unitaire : 500
3. ✅ Demande créée
4. Cliquez "✓ Valider paiement"
5. ✅ Statut change à "Payé"
6. Fermez et relancez
7. ✅ La demande est persistée !
```

### Test 3 : VOUCHERS avec QR (5 min)

```
1. Dashboard → VOUCHERS
2. Cliquez "👁 Voir" sur un voucher
3. ✅ QR code s'affiche
4. Cliquez "✓ Rédemier"
5. Entrez code magasin
6. ✅ Voucher rédemié
7. Fermez et relancez
8. ✅ Le statut persiste !
```

### Test 4 : Export Excel (2 min)

```
1. VOUCHERS → "📄 Export Excel"
2. ✅ Message avec le chemin
3. Ouvrez le fichier dans Downloads
4. ✅ Toutes les données sont là !
```

---

# PARTIE 6 : DONNÉES DE PRODUCTION (10 MIN)

## Étape 6.1 : Nettoyer les données de test

```sql
-- Dans pgAdmin Query Tool sur vms_database :

DELETE FROM vouchers;
DELETE FROM demandes;
DELETE FROM clients;
DELETE FROM magasins;

-- ✅ Base propre pour production
```

## Étape 6.2 : Ajouter vos vrais magasins

```sql
INSERT INTO magasins (code, nom, adresse, ville, telephone, responsable)
VALUES 
('MAG001', 'Supermarché Central', '123 Royal Road', 'Port Louis', '+230 xxx xxxx', 'Nom Responsable'),
('MAG002', 'Supermarché Rose-Hill', '45 Avenue Victoria', 'Rose-Hill', '+230 xxx xxxx', 'Nom Responsable');

-- Ajoutez tous vos magasins réels
```

## Étape 6.3 : Ajouter vos vrais clients

```sql
INSERT INTO clients (numero_compte, nom, email, telephone, contact_personne)
VALUES
('CLI00001', 'Nom Client Réel', 'email@client.mu', '+230 xxx xxxx', 'Contact');

-- Ajoutez tous vos clients réels
```

---

# PARTIE 7 : FORMATION UTILISATEURS (30 MIN)

## Guide rapide pour les employés

### CRÉER UNE DEMANDE :

```
1. Ouvrir l'application VMS
2. Cliquer sur "DEMANDES"
3. Cliquer "➕ Nouvelle Demande"
4. Remplir :
   - Sélectionner le client
   - Entrer le nombre de bons
   - Entrer la valeur unitaire
5. Cliquer "✔ Enregistrer"
6. ✅ Demande créée !
```

### VALIDER UN PAIEMENT :

```
1. DEMANDES → Trouver la demande
2. Cliquer sur le bouton "✓" (valider)
3. Confirmer
4. ✅ Statut passe à "Payé"
```

### RÉDEMIER UN BON (En magasin) :

```
1. VOUCHERS → Rechercher le code
2. Cliquer "✓ Rédemier"
3. Scanner le QR code OU entrer le code manuellement
4. Confirmer
5. ✅ Bon rédemié !
```

### EXPORTER POUR COMPTABILITÉ :

```
1. VOUCHERS → "📄 Export Excel"
2. Ouvrir le fichier dans Downloads
3. ✅ Envoyer à la comptabilité
```

---

# PARTIE 8 : MAINTENANCE (IMPORTANT)

## Backup quotidien

```bash
# Créer un script backup_vms.bat :

@echo off
set PGPASSWORD=votre_mot_de_passe
"C:\Program Files\PostgreSQL\16\bin\pg_dump.exe" -U postgres -d vms_database > "C:\Backups\vms_backup_%date:~-4,4%%date:~-7,2%%date:~-10,2%.sql"
echo Backup effectué !

# Planifier dans le Planificateur de tâches Windows (tous les jours à 23h)
```

## Marquer les vouchers expirés

```sql
-- À exécuter chaque jour (ou automatisé) :
SELECT marquer_vouchers_expires();
```

## Statistiques

```sql
-- Statistiques du jour :
SELECT COUNT(*) FROM vouchers WHERE date_emission = CURRENT_DATE;
SELECT COUNT(*) FROM vouchers WHERE date_redemption = CURRENT_DATE;

-- Top clients :
SELECT c.nom, COUNT(d.id) as nb_demandes, SUM(d.montant_total) as total
FROM clients c
JOIN demandes d ON c.id = d.client_id
GROUP BY c.nom
ORDER BY total DESC
LIMIT 10;
```

---

# PARTIE 9 : DÉPANNAGE

## Problème : "Connection refused"

```
✅ Vérifiez que PostgreSQL est lancé :
   Services Windows → postgresql-x64-16 → Démarrer
```

## Problème : "password authentication failed"

```
✅ Vérifiez le mot de passe dans DatabaseConnection.java
```

## Problème : "Cannot find QRCodeGenerator"

```
✅ Maven → Reload Project
✅ Attendez le téléchargement des dépendances
```

## Problème : Application lente

```
✅ Ajoutez des index sur les colonnes recherchées souvent
✅ Rafraîchissez les vues matérialisées :
   REFRESH MATERIALIZED VIEW vue_stats_clients;
```

---

# ✅ CHECKLIST FINALE

## Installation :
```
☐ PostgreSQL installé
☐ pgAdmin 4 fonctionne
☐ Base vms_database créée
☐ Script schema.sql exécuté
☐ 7 tables créées
☐ Données de test visibles dans pgAdmin
```

## Configuration :
```
☐ Projet ouvert dans IntelliJ
☐ Maven dépendances téléchargées
☐ DatabaseConnection.java configuré
☐ Java 17/21 configuré
☐ VM options configurées
```

## Tests :
```
☐ Application se lance
☐ Console affiche "✅ PostgreSQL connecté !"
☐ CLIENTS fonctionne (lecture/écriture)
☐ DEMANDES fonctionne (lecture/écriture)
☐ VOUCHERS fonctionne (lecture/écriture)
☐ QR codes s'affichent
☐ Export Excel fonctionne
☐ Données persistent après fermeture
```

## Production :
```
☐ Données de test supprimées
☐ Vrais magasins ajoutés
☐ Vrais clients ajoutés
☐ Backup automatique configuré
☐ Utilisateurs formés
```

---

# 🎉 FÉLICITATIONS !

Vous avez maintenant un système VMS **100% FONCTIONNEL** et **PRÊT POUR PRODUCTION** ! 

## Support :
- Documentation : Tous les fichiers .md dans le projet
- Logs : Console IntelliJ
- Base de données : pgAdmin 4

**Le système est maintenant prêt pour votre supermarché ! 🏪🚀**
