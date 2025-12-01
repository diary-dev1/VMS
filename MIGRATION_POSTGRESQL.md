# 🐘 GUIDE COMPLET - MIGRATION VERS POSTGRESQL

## 📋 TABLE DES MATIÈRES

1. Installation PostgreSQL
2. Configuration de la base de données
3. Modification des contrôleurs
4. Tests et validation
5. Déploiement production

---

## 1️⃣ INSTALLATION POSTGRESQL (10 minutes)

### **Étape 1 : Télécharger**
```
1. https://www.postgresql.org/download/windows/
2. PostgreSQL 16.x (dernière version stable)
3. Exécutez l'installateur
```

### **Étape 2 : Installation**
```
- Password : postgres (ou votre choix)
- Port : 5432
- ⚠️ NOTEZ LE MOT DE PASSE !
```

---

## 2️⃣ CONFIGURATION BASE DE DONNÉES (5 minutes)

### **Méthode 1 : pgAdmin 4**

```
1. Lancez pgAdmin 4
2. Entrez votre mot de passe
3. Clic droit "Databases" → Create → Database
4. Nom : vms_database
5. Save
6. Clic droit "vms_database" → Query Tool
7. File → Open → database/schema.sql
8. Execute (F5)
9. ✅ SUCCESS !
```

### **Vérification**
```
Dans pgAdmin, déroulez :
vms_database → Schemas → public → Tables

Vous devez voir :
✅ clients
✅ demandes
✅ magasins
✅ vouchers
✅ historique_redemptions
✅ utilisateurs
✅ audit_log
```

---

## 3️⃣ CONFIGURATION DU CODE (2 minutes)

### **Fichier : DatabaseConnection.java**

Modifiez les lignes 7-9 :

```java
private static final String URL = "jdbc:postgresql://localhost:5432/vms_database";
private static final String USER = "postgres";
private static final String PASSWORD = "VOTRE_MOT_DE_PASSE_ICI";  ← CHANGEZ !
```

---

## 4️⃣ MODIFICATION DES CONTRÔLEURS

### **A. DemandesController.java**

#### **Importer le DAO**
```java
import com.vms.dao.DemandeDAO;
import java.sql.SQLException;
```

#### **Ajouter une instance DAO**
```java
public class DemandesController {
    private DemandeDAO demandeDAO;
    
    @FXML
    public void initialize() {
        demandeDAO = new DemandeDAO(); // ← AJOUTEZ
        // ... reste du code
    }
}
```

#### **Remplacer chargerDonneesTest() par chargerDepuisDB()**

REMPLACEZ :
```java
private void chargerDonneesTest() {
    // ... données de test ...
}
```

PAR :
```java
private void chargerDepuisDB() {
    try {
        List<Demande> demandes = demandeDAO.getAllDemandes();
        listeDemandes.clear();
        listeDemandes.addAll(demandes);
        listeDemandesFiltree.clear();
        listeDemandesFiltree.addAll(demandes);
        System.out.println("✅ " + demandes.size() + " demandes chargées depuis PostgreSQL");
    } catch (SQLException e) {
        System.err.println("❌ Erreur PostgreSQL : " + e.getMessage());
        afficherErreur("Erreur DB", "Impossible de charger les demandes");
        // Fallback sur données de test si échec
        chargerDonneesTest();
    }
}

private void chargerDonneesTest() {
    // Garder les données de test comme backup
    listeDemandes.add(new Demande(1, "VR0001-200", 1, "ABC Company Ltd", ...));
    // etc.
}
```

#### **Dans initialize(), appeler chargerDepuisDB()**

```java
@FXML
public void initialize() {
    demandeDAO = new DemandeDAO();
    listeDemandes = FXCollections.observableArrayList();
    listeDemandesFiltree = FXCollections.observableArrayList();
    configurerTableau();
    configurerFiltres();
    
    // Charger depuis DB
    chargerDepuisDB();  // ← CHANGEZ ICI
    
    mettreAJourStatistiques();
}
```

#### **Modifier enregistrerDemande() pour sauvegarder en DB**

AJOUTEZ au début de la méthode :
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
        
        // ← AJOUTEZ CECI
        int id = demandeDAO.createDemande(demandeEnCours);
        demandeEnCours.setId(id);
        System.out.println("✅ Demande sauvegardée en DB avec ID: " + id);
        // FIN AJOUT
        
        listeDemandes.add(demandeEnCours);
        listeDemandesFiltree.add(demandeEnCours);
        // ... reste ...
    } catch (SQLException e) {
        afficherErreur("Erreur DB", "Impossible de sauvegarder : " + e.getMessage());
    }
}
```

#### **Modifier validerPaiement()**

```java
private void validerPaiement(Demande demande) {
    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    // ... code confirmation ...
    
    confirmation.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            try {
                // ← AJOUTEZ CECI
                if (demandeDAO.validerPaiement(demande.getId())) {
                    demande.setStatut("PAYE");
                    demande.setDatePaiement(LocalDate.now());
                    tableDemandes.refresh();
                    mettreAJourStatistiques();
                    afficherSucces("Succès", "Paiement validé et sauvegardé !");
                } else {
                    afficherErreur("Erreur", "Impossible de valider le paiement");
                }
            } catch (SQLException e) {
                afficherErreur("Erreur DB", e.getMessage());
            }
        }
    });
}
```

#### **Modifier supprimerDemande()**

```java
private void supprimerDemande(Demande demande) {
    // ... code confirmation ...
    
    confirmation.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            try {
                // ← AJOUTEZ CECI
                if (demandeDAO.deleteDemande(demande.getId())) {
                    listeDemandes.remove(demande);
                    listeDemandesFiltree.remove(demande);
                    mettreAJourStatistiques();
                    afficherSucces("Succès", "Demande supprimée !");
                } else {
                    afficherErreur("Erreur", "Impossible de supprimer");
                }
            } catch (SQLException e) {
                afficherErreur("Erreur DB", e.getMessage());
            }
        }
    });
}
```

---

### **B. ClientsController.java - MÊME PRINCIPE**

```java
import com.vms.dao.ClientDAO;
import java.sql.SQLException;

public class ClientsController {
    private ClientDAO clientDAO;
    
    @FXML
    public void initialize() {
        clientDAO = new ClientDAO();
        // ...
        chargerDepuisDB();
    }
    
    private void chargerDepuisDB() {
        try {
            List<Client> clients = clientDAO.getAllClients();
            listeClients.clear();
            listeClients.addAll(clients);
            listeClientsFiltree.clear();
            listeClientsFiltree.addAll(clients);
        } catch (SQLException e) {
            chargerDonneesTest();
        }
    }
    
    @FXML
    private void enregistrerClient() {
        try {
            // ... validation ...
            int id = clientDAO.createClient(clientEnCours);
            clientEnCours.setId(id);
            // ... reste ...
        } catch (SQLException e) {
            afficherErreur("Erreur DB", e.getMessage());
        }
    }
}
```

---

### **C. MagasinsController.java - MÊME PRINCIPE**

```java
import com.vms.dao.MagasinDAO;

public class MagasinsController {
    private MagasinDAO magasinDAO;
    
    // Même structure que ci-dessus
}
```

---

### **D. VouchersController.java - MÊME PRINCIPE**

```java
import com.vms.dao.VoucherDAO;

public class VouchersController {
    private VoucherDAO voucherDAO;
    
    private void chargerDepuisDB() {
        try {
            List<Voucher> vouchers = voucherDAO.getAllVouchers();
            listeVouchers.clear();
            listeVouchers.addAll(vouchers);
            listeVouchersFiltree.clear();
            listeVouchersFiltree.addAll(vouchers);
        } catch (SQLException e) {
            chargerDonneesTest();
        }
    }
    
    private void redemierVoucher(Voucher voucher) {
        // ... dialog magasin ...
        try {
            int magasinId = 1; // À récupérer du dialog
            if (voucherDAO.redemierVoucher(voucher.getId(), magasinId, "Caisse01")) {
                voucher.redemier(magasinId, "Magasin Central");
                tableVouchers.refresh();
                mettreAJourStatistiques();
                afficherSucces("Succès", "Voucher rédemé !");
            }
        } catch (SQLException e) {
            afficherErreur("Erreur DB", e.getMessage());
        }
    }
}
```

---

## 5️⃣ TESTS (15 minutes)

### **Test 1 : Connexion**
```
1. Lancez l'application
2. Regardez la console
3. ✅ Devrait afficher : "✅ Connexion à PostgreSQL réussie !"
4. ✅ "✅ X demandes chargées depuis PostgreSQL"
```

### **Test 2 : CRUD Demandes**
```
1. Module DEMANDES
2. Créez une nouvelle demande
3. ✅ Apparaît dans le tableau
4. Fermez l'application
5. Relancez
6. ✅ La demande est toujours là ! (sauvegardée en DB)
```

### **Test 3 : CRUD Clients**
```
1. Module CLIENTS
2. Créez un nouveau client
3. Fermez et relancez
4. ✅ Le client est toujours là !
```

### **Test 4 : Vouchers**
```
1. Module VOUCHERS
2. Rédemier un voucher
3. Fermez et relancez
4. ✅ Le statut "Rédemé" est conservé !
```

---

## 6️⃣ VÉRIFICATION DANS PGADMIN

```
1. Ouvrez pgAdmin 4
2. vms_database → Schemas → public → Tables
3. Clic droit sur "demandes" → View/Edit Data → All Rows
4. ✅ Vous voyez les demandes créées dans l'application !
```

---

## 7️⃣ DÉPLOIEMENT PRODUCTION

### **Checklist :**
```
☐ PostgreSQL installé sur le serveur
☐ Base de données "vms_database" créée
☐ Script schema.sql exécuté
☐ DatabaseConnection.java configuré avec les bons identifiants
☐ Tous les contrôleurs modifiés
☐ Tests réussis
☐ Backup de la base configuré
☐ Utilisateurs créés
☐ Formation des utilisateurs faite
```

---

## 8️⃣ MAINTENANCE

### **Backup quotidien**
```sql
-- Avec pg_dump (ligne de commande)
pg_dump -U postgres -d vms_database > backup_$(date +%Y%m%d).sql
```

### **Marquer les vouchers expirés**
```sql
-- À exécuter chaque jour
SELECT marquer_vouchers_expires();
```

### **Rafraîchir les statistiques**
```sql
REFRESH MATERIALIZED VIEW vue_stats_clients;
REFRESH MATERIALIZED VIEW vue_stats_magasins;
```

---

## 🎯 RÉSUMÉ

### **Avant (Sans PostgreSQL) :**
```
❌ Données en mémoire
❌ Perdues à la fermeture
✅ Parfait pour démo
```

### **Après (Avec PostgreSQL) :**
```
✅ Données permanentes
✅ Multi-utilisateurs
✅ Historique complet
✅ Prêt production
✅ Backup possible
```

---

## ⏱️ TEMPS TOTAL ESTIMÉ

```
Installation PostgreSQL :     10 min
Configuration DB :             5 min
Modification code :           30 min
Tests :                       15 min
─────────────────────────────────────
TOTAL :                       60 min
```

---

**Une fois ces modifications faites, votre système sera 100% production-ready ! 🚀**
