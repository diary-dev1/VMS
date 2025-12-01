# 🎯 VMS - GUIDE COMPLET D'INSTALLATION ET D'UTILISATION

## 📦 CONTENU DU PACKAGE COMPLET

Vous avez maintenant **TOUS LES MODULES** du système VMS !

### ✅ MODULES INCLUS :

1. **Dashboard** - Page d'accueil avec navigation
2. **DEMANDES** - Gestion complète des demandes de bons
3. **CLIENTS** - Gestion des clients
4. **MAGASINS** - Gestion des magasins et points de vente
5. **VOUCHERS** - Gestion des bons cadeau avec QR codes
6. **Base de données PostgreSQL** - Scripts SQL complets
7. **DAO** - Accès aux données (exemple avec Demandes)

---

## 🚀 INSTALLATION RAPIDE

### **Étape 1 : Ouvrir dans IntelliJ**

```
1. Décompressez le ZIP
2. IntelliJ → File → Open
3. Sélectionnez le dossier vms-dashboard
4. Enable Auto-Import (Maven)
5. Attendez que Maven télécharge les dépendances
```

### **Étape 2 : Configurer Java**

```
File → Project Structure → Project
SDK : Java 17 ou 21 (PAS 24 !)
```

### **Étape 3 : Lancer**

```
Ouvrez : src/main/java/com/vms/Main.java
Clic droit → Run 'Main.main()'
```

---

## 🐘 CONFIGURATION POSTGRESQL (Optionnel)

### **Installation PostgreSQL**

```
1. Téléchargez PostgreSQL : https://www.postgresql.org/download/
2. Installez avec le mot de passe : postgres (ou autre)
3. Lancez pgAdmin 4
```

### **Création de la base de données**

```
1. Ouvrez pgAdmin 4
2. Clic droit sur "Databases" → Create → Database
3. Nom : vms_database
4. Save

5. Clic droit sur vms_database → Query Tool
6. Ouvrez le fichier : database/schema.sql
7. Cliquez sur Execute (▶️)
8. ✅ Base de données créée !
```

### **Configuration dans le code**

Modifiez le fichier : `src/main/java/com/vms/database/DatabaseConnection.java`

```java
private static final String URL = "jdbc:postgresql://localhost:5432/vms_database";
private static final String USER = "postgres";
private static final String PASSWORD = "votre_mot_de_passe";  // ← Changez ici
```

---

## 📊 STRUCTURE DU PROJET

```
vms-dashboard/
├── pom.xml                                 # Configuration Maven
├── database/
│   └── schema.sql                          # Script SQL complet
├── src/main/
│   ├── java/com/vms/
│   │   ├── Main.java                       # Point d'entrée
│   │   ├── DashboardController.java        # Dashboard
│   │   ├── model/                          # Modèles de données
│   │   │   ├── Demande.java
│   │   │   ├── Client.java
│   │   │   ├── Magasin.java
│   │   │   └── Voucher.java
│   │   ├── controller/                     # Contrôleurs
│   │   │   ├── DemandesController.java
│   │   │   ├── ClientsController.java
│   │   │   ├── MagasinsController.java
│   │   │   └── VouchersController.java
│   │   ├── database/                       # Connexion DB
│   │   │   └── DatabaseConnection.java
│   │   └── dao/                            # Data Access Objects
│   │       └── DemandeDAO.java
│   └── resources/com/vms/
│       ├── dashboard.fxml                  # Interfaces
│       ├── demandes.fxml
│       ├── clients.fxml
│       ├── magasins.fxml
│       ├── vouchers.fxml
│       └── styles.css                      # Styles CSS
└── Documentation/
    ├── README.md
    ├── GUIDE_COMPLET.md
    └── INSTALLATION_DEMANDES.md
```

---

## 🎮 UTILISATION DES MODULES

### **1. DASHBOARD**

Le point de départ de l'application.

**Fonctionnalités :**
- Navigation vers tous les modules
- Vue d'ensemble du système
- 5 cartes cliquables

**Comment utiliser :**
```
1. Lancez l'application
2. Cliquez sur une carte pour accéder au module
```

---

### **2. MODULE DEMANDES**

Gestion complète des demandes de bons cadeau.

**Fonctionnalités :**
- ✅ Créer une nouvelle demande
- ✅ Liste de toutes les demandes
- ✅ Filtrer par statut
- ✅ Rechercher par référence/client
- ✅ Valider paiement
- ✅ Supprimer une demande
- ✅ Statistiques en temps réel

**Workflow :**
```
1. Cliquez sur DEMANDES
2. ➕ Nouvelle Demande
3. Remplissez :
   - Client
   - Nombre de bons
   - Valeur unitaire
4. ✔ Enregistrer
5. Valider le paiement (bouton ✓)
```

**États d'une demande :**
- 🟡 EN_ATTENTE_PAIEMENT
- 🟢 PAYE
- 🔵 APPROUVE
- ⚫ GENERE

---

### **3. MODULE CLIENTS**

Gestion de la base clients.

**Fonctionnalités :**
- ✅ Ajouter un nouveau client
- ✅ Modifier un client
- ✅ Supprimer un client
- ✅ Voir les détails
- ✅ Rechercher un client
- ✅ Statistiques (total, actifs, demandes)

**Comment créer un client :**
```
1. Cliquez sur CLIENTS
2. ➕ Nouveau Client
3. Remplissez :
   - Nom de l'entreprise
   - Email
   - Téléphone
   - Adresse
   - Personne de contact
4. ✔ Enregistrer
```

**Numéro de compte :**
- Généré automatiquement (ex: CLI00001)

---

### **4. MODULE MAGASINS**

Gestion des points de vente.

**Fonctionnalités :**
- ✅ Ajouter un magasin
- ✅ Modifier un magasin
- ✅ Supprimer un magasin
- ✅ Voir les détails
- ✅ Statistiques (total, actifs, rédemptions)

**Comment créer un magasin :**
```
1. Cliquez sur MAGASIN
2. ➕ Nouveau Magasin
3. Remplissez :
   - Nom du magasin
   - Ville
   - Adresse
   - Téléphone
   - Responsable
4. ✔ Enregistrer
```

**Code magasin :**
- Généré automatiquement (ex: MAG001)

---

### **5. MODULE VOUCHERS**

Gestion des bons cadeau.

**Fonctionnalités :**
- ✅ Liste de tous les vouchers
- ✅ Filtrer par statut (Émis, Rédemés, Expirés)
- ✅ Rechercher par code/client
- ✅ Voir les détails (avec QR code)
- ✅ Rédemier un voucher
- ✅ Statistiques complètes
- ⏳ Export Excel (à implémenter)

**États d'un voucher :**
- 🟢 EMIS - Le bon est valide et utilisable
- 🔵 REDEMME - Le bon a été utilisé
- 🔴 EXPIRE - Le bon a expiré
- ⚫ ANNULE - Le bon a été annulé

**Comment rédemier un voucher :**
```
1. Cliquez sur VOUCHER
2. Trouvez le voucher à rédemier
3. Cliquez sur "✓ Rédemier"
4. Entrez le code magasin
5. Confirmez
```

---

## 🗄️ BASE DE DONNÉES POSTGRESQL

### **Tables créées :**

1. **clients** - Informations clients
2. **magasins** - Points de vente
3. **demandes** - Demandes de bons
4. **vouchers** - Bons cadeau
5. **historique_redemptions** - Historique des rédemptions
6. **utilisateurs** - Comptes utilisateurs
7. **audit_log** - Journal d'audit

### **Fonctions SQL :**

- `generer_vouchers_pour_demande(demande_id)` - Génère automatiquement les vouchers
- `marquer_vouchers_expires()` - Marque les vouchers expirés

### **Vues matérialisées :**

- `vue_stats_clients` - Statistiques par client
- `vue_stats_magasins` - Statistiques par magasin

---

## 💻 UTILISER LES DAO

### **Exemple avec DemandeDAO :**

```java
import com.vms.dao.DemandeDAO;
import com.vms.model.Demande;

// Créer une instance du DAO
DemandeDAO demandeDAO = new DemandeDAO();

// Récupérer toutes les demandes
List<Demande> demandes = demandeDAO.getAllDemandes();

// Créer une nouvelle demande
Demande nouvelleDemande = new Demande();
nouvelleDemande.setClientId(1);
nouvelleDemande.setNombreBons(100);
nouvelleDemande.setValeurUnitaire(500.0);
int id = demandeDAO.createDemande(nouvelleDemande);

// Valider le paiement
demandeDAO.validerPaiement(id);

// Approuver la demande
demandeDAO.approuverDemande(id, "Admin");
```

---

## 🔧 PERSONNALISATION

### **Modifier les couleurs :**

Éditez `src/main/resources/com/vms/styles.css`

```css
/* Changer la couleur principale */
.button-primary {
    -fx-background-color: #YOUR_COLOR;
}

/* Changer le fond du dashboard */
.main-container {
    -fx-background-color: linear-gradient(to bottom right, #COLOR1, #COLOR2);
}
```

### **Ajouter un nouveau module :**

1. Créer le modèle : `src/main/java/com/vms/model/VotreModele.java`
2. Créer le contrôleur : `src/main/java/com/vms/controller/VotreController.java`
3. Créer l'interface : `src/main/resources/com/vms/votre-module.fxml`
4. Ajouter la navigation dans `DashboardController.java`

---

## 📊 RAPPORTS ET EXPORTS

### **Export Excel (à implémenter) :**

Ajoutez la dépendance Apache POI dans `pom.xml` :

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

Exemple de code :

```java
import org.apache.poi.xssf.usermodel.*;

public void exporterVersExcel() {
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("Demandes");
    
    // Ajouter les données...
    
    FileOutputStream out = new FileOutputStream("demandes.xlsx");
    workbook.write(out);
    workbook.close();
}
```

---

## 🔐 SÉCURITÉ

### **Authentification (à implémenter) :**

1. Créer une page login
2. Hasher les mots de passe (BCrypt)
3. Gérer les sessions
4. Contrôler les permissions par rôle

### **Rôles disponibles :**
- ADMIN - Accès complet
- COMPTABLE - Validation paiements
- APPROBATEUR - Approbation demandes
- UTILISATEUR - Création demandes
- MAGASIN - Rédemption vouchers

---

## 🐛 DÉBOGAGE

### **Problèmes courants :**

**1. Erreur de connexion PostgreSQL**
```
Solution :
- Vérifiez que PostgreSQL est lancé
- Vérifiez le mot de passe dans DatabaseConnection.java
- Vérifiez le port (5432 par défaut)
```

**2. Module ne s'affiche pas**
```
Solution :
- Vérifiez que le fichier FXML existe
- Vérifiez le nom du fichier dans Main.changeScene()
- Rechargez Maven
```

**3. Erreur au lancement**
```
Solution :
- Vérifiez Java 17 ou 21 (pas 24)
- Vérifiez que Maven a téléchargé JavaFX
- Nettoyez : mvn clean install
```

---

## 📞 SUPPORT

Pour toute question ou problème :

1. Consultez ce guide
2. Vérifiez les logs d'erreur dans IntelliJ
3. Consultez la documentation PostgreSQL
4. Recherchez l'erreur sur Stack Overflow

---

## 🎉 FÉLICITATIONS !

Vous avez maintenant un système VMS complet et fonctionnel !

**Ce qui est prêt :**
✅ Dashboard
✅ Module Demandes
✅ Module Clients
✅ Module Magasins
✅ Module Vouchers
✅ Base de données PostgreSQL
✅ Exemple de DAO
✅ Architecture complète MVC
✅ Interface moderne et responsive

**À implémenter :**
- Connexion Login/Authentification
- Export Excel
- Envoi d'emails automatiques
- Génération PDF des vouchers
- Scan QR codes en magasin
- Dashboard avec graphiques

**Bon développement ! 🚀**

---

© 2024 - VMS Voucher Management System
BTS SIO - MCCI Business School
