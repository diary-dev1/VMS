# 🐘 GUIDE INSTALLATION POSTGRESQL - VMS

## 📥 TÉLÉCHARGEMENT ET INSTALLATION

### **Étape 1 : Télécharger PostgreSQL**

```
1. Allez sur : https://www.postgresql.org/download/windows/
2. Cliquez sur "Download the installer"
3. Téléchargez PostgreSQL 16 (version stable)
4. Lancez l'installateur
```

### **Étape 2 : Installation**

```
1. Cliquez "Next" sur l'écran d'accueil
2. Installation Directory : Laissez par défaut
3. Select Components : 
   ✅ PostgreSQL Server
   ✅ pgAdmin 4
   ✅ Command Line Tools
4. Data Directory : Laissez par défaut
5. Password : Choisissez un mot de passe (ex: postgres)
   ⚠️ IMPORTANT : Notez ce mot de passe !
6. Port : 5432 (par défaut)
7. Locale : Default locale
8. Cliquez "Next" puis "Install"
9. Attendez l'installation (2-5 minutes)
10. Décochez "Stack Builder" et terminez
```

---

## 🗄️ CRÉATION DE LA BASE DE DONNÉES

### **Méthode 1 : Avec pgAdmin 4 (Recommandé)**

```
1. Lancez pgAdmin 4 (cherchez dans le menu Démarrer)
2. Entrez le mot de passe que vous avez défini
3. Dans le panneau de gauche :
   - Clic droit sur "Databases"
   - Create → Database
4. Dans la fenêtre :
   - Database : vms_database
   - Owner : postgres
   - Cliquez "Save"
5. ✅ La base de données est créée !
```

### **Méthode 2 : Avec SQL Shell (psql)**

```
1. Lancez "SQL Shell (psql)" depuis le menu Démarrer
2. Appuyez sur Entrée 4 fois (pour accepter les valeurs par défaut)
3. Entrez votre mot de passe
4. Tapez cette commande :

CREATE DATABASE vms_database;

5. Tapez : \c vms_database
6. ✅ Vous êtes connecté à la base !
```

---

## 📊 EXÉCUTER LE SCRIPT SQL

### **Avec pgAdmin 4 :**

```
1. Dans pgAdmin 4, sélectionnez "vms_database"
2. Clic droit sur "vms_database"
3. Query Tool
4. Cliquez sur l'icône "Ouvrir un fichier" 📁
5. Sélectionnez : vms-dashboard/database/schema.sql
6. Cliquez sur le bouton Play ▶️ (ou F5)
7. ✅ Vous devriez voir : "Query returned successfully"
```

### **Vérifier que tout est créé :**

```
1. Dans pgAdmin, déroulez :
   vms_database → Schemas → public → Tables
2. Vous devriez voir :
   ✅ clients
   ✅ demandes
   ✅ magasins
   ✅ vouchers
   ✅ historique_redemptions
   ✅ utilisateurs
   ✅ audit_log
```

---

## ⚙️ CONFIGURATION DANS LE CODE

### **Étape 1 : Modifier DatabaseConnection.java**

```
Ouvrez : src/main/java/com/vms/database/DatabaseConnection.java

Modifiez les lignes 7-9 :

private static final String URL = "jdbc:postgresql://localhost:5432/vms_database";
private static final String USER = "postgres";
private static final String PASSWORD = "votre_mot_de_passe";  ← Mettez VOTRE mot de passe ici
```

### **Étape 2 : Tester la connexion**

```java
// Ajoutez ceci dans Main.java (dans la méthode start, avant de charger le FXML) :

if (DatabaseConnection.testConnection()) {
    System.out.println("✅ Connexion PostgreSQL OK !");
} else {
    System.out.println("❌ Erreur connexion PostgreSQL");
}
```

---

## 🔄 UTILISER LES DAO DANS LES CONTRÔLEURS

### **Exemple : DemandesController avec DAO**

Remplacez la méthode `chargerDonneesTest()` par :

```java
private void chargerDonneesDepuisDB() {
    try {
        DemandeDAO demandeDAO = new DemandeDAO();
        List<Demande> demandes = demandeDAO.getAllDemandes();
        
        listeDemandes.clear();
        listeDemandes.addAll(demandes);
        listeDemandesFiltree.clear();
        listeDemandesFiltree.addAll(demandes);
        
        System.out.println("✅ " + demandes.size() + " demandes chargées depuis la DB");
    } catch (SQLException e) {
        System.err.println("❌ Erreur chargement demandes : " + e.getMessage());
        // Fallback sur les données de test
        chargerDonneesTest();
    }
}
```

Et dans `initialize()` :

```java
@FXML
public void initialize() {
    listeDemandes = FXCollections.observableArrayList();
    listeDemandesFiltree = FXCollections.observableArrayList();
    configurerTableau();
    
    // Essayer de charger depuis la DB, sinon données de test
    if (DatabaseConnection.testConnection()) {
        chargerDonneesDepuisDB();
    } else {
        chargerDonneesTest();
    }
    
    mettreAJourStatistiques();
}
```

### **Créer une demande avec DAO :**

Dans `enregistrerDemande()` :

```java
@FXML
private void enregistrerDemande() {
    // ... validation ...
    
    try {
        demandeEnCours.setClientNom(comboClient.getValue());
        demandeEnCours.setNombreBons(nombreBons);
        demandeEnCours.setValeurUnitaire(valeurUnitaire);
        demandeEnCours.setRemarques(txtRemarques.getText());
        demandeEnCours.genererReference(listeDemandes.size() + 1);
        
        // Sauvegarder dans la base de données
        DemandeDAO demandeDAO = new DemandeDAO();
        int id = demandeDAO.createDemande(demandeEnCours);
        demandeEnCours.setId(id);
        
        // Ajouter à la liste
        listeDemandes.add(demandeEnCours);
        listeDemandesFiltree.add(demandeEnCours);
        
        mettreAJourStatistiques();
        annulerFormulaire();
        
        afficherSucces("Succès", "Demande enregistrée dans la base de données !");
        
    } catch (SQLException e) {
        afficherErreur("Erreur DB", "Impossible de sauvegarder : " + e.getMessage());
    } catch (NumberFormatException e) {
        afficherErreur("Erreur", "Veuillez entrer des nombres valides");
    }
}
```

---

## 🧪 TESTER LA CONNEXION

### **Test simple :**

```
1. Lancez l'application
2. Ouvrez la console IntelliJ (en bas)
3. Vous devriez voir :
   ✅ Connexion à PostgreSQL réussie !
   ✅ X demandes chargées depuis la DB
```

### **Test complet :**

```
1. Créez une nouvelle demande dans l'application
2. Fermez l'application
3. Relancez l'application
4. ✅ La demande est toujours là ! (sauvegardée en DB)
```

---

## 🔧 DÉPANNAGE

### **Erreur : "Connection refused"**

```
✅ Vérifiez que PostgreSQL est lancé :
   - Windows : Services → postgresql-x64-16 → Démarrer
   - Ou : pgAdmin 4 → Dashboard (si le serveur est vert, c'est OK)
```

### **Erreur : "password authentication failed"**

```
✅ Vérifiez le mot de passe dans DatabaseConnection.java
✅ Le mot de passe doit correspondre à celui que vous avez défini à l'installation
```

### **Erreur : "database does not exist"**

```
✅ Créez la base de données vms_database dans pgAdmin
✅ Ou avec psql : CREATE DATABASE vms_database;
```

### **Erreur : "relation does not exist"**

```
✅ Exécutez le script schema.sql dans pgAdmin Query Tool
✅ Vérifiez que les tables sont créées : Tables → vms_database
```

---

## 📊 DONNÉES DE TEST EN BASE DE DONNÉES

Les données de test sont **déjà insérées** par le script SQL :

```sql
-- 3 clients
INSERT INTO clients ...

-- 3 magasins  
INSERT INTO magasins ...

-- 1 utilisateur admin
INSERT INTO utilisateurs ...
```

Vous pouvez ajouter plus de données en exécutant des INSERT dans pgAdmin !

---

## ✅ CHECKLIST

```
☐ PostgreSQL installé
☐ pgAdmin 4 fonctionne
☐ Base de données "vms_database" créée
☐ Script schema.sql exécuté
☐ 7 tables créées
☐ DatabaseConnection.java configuré avec le bon mot de passe
☐ Test de connexion OK
☐ Application charge les données depuis la DB
```

---

## 🎉 FÉLICITATIONS !

Une fois tout configuré, vous avez :

✅ Une vraie base de données PostgreSQL  
✅ Sauvegarde permanente des données  
✅ Système prêt pour production  
✅ Possibilité d'avoir plusieurs utilisateurs  
✅ Historique complet  
✅ Statistiques avancées avec SQL  

---

**Besoin d'aide ? Consultez la documentation ou demandez de l'aide ! 😊**
