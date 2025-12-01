# 🎯 VMS Dashboard - Maven + JavaFX + FXML

## 📋 Description
Application JavaFX professionnelle pour le système de gestion de bons cadeau (VMS - Voucher Management System).

**Technologies utilisées :**
- ☕ Java 17
- 🎨 JavaFX 21
- 📄 FXML pour l'interface
- 🔧 Maven pour la gestion des dépendances
- 🐘 PostgreSQL (prêt pour l'intégration)

---

## 📁 Structure du Projet

```
vms-dashboard/
├── pom.xml                          # Configuration Maven
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── vms/
│       │           ├── Main.java                    # Point d'entrée
│       │           └── DashboardController.java     # Contrôleur FXML
│       └── resources/
│           └── com/
│               └── vms/
│                   ├── dashboard.fxml               # Interface FXML
│                   └── styles.css                   # Styles CSS
└── README.md
```

---

## 🚀 Installation avec IntelliJ IDEA

### Étape 1 : Prérequis
- ✅ **IntelliJ IDEA** (Community ou Ultimate)
- ✅ **JDK 17** ou supérieur
- ✅ **Maven** (inclus dans IntelliJ)

### Étape 2 : Ouvrir le Projet

1. **Décompressez** le fichier `vms-dashboard.zip`
2. Lancez **IntelliJ IDEA**
3. Cliquez sur **File → Open** (ou **Open** sur l'écran d'accueil)
4. Sélectionnez le dossier **`vms-dashboard`**
5. Cliquez sur **OK**

### Étape 3 : Import Maven Automatique

IntelliJ va automatiquement :
- ✅ Détecter le fichier `pom.xml`
- ✅ Télécharger toutes les dépendances (JavaFX, PostgreSQL, etc.)
- ✅ Configurer le projet

**💡 Une notification apparaît en bas à droite :**
```
"Maven projects need to be imported"
```
→ Cliquez sur **"Import"** ou **"Enable Auto-Import"**

### Étape 4 : Attendre le Téléchargement

Maven va télécharger toutes les dépendances JavaFX (première fois seulement).
Regardez la barre de progression en bas de l'IDE.

⏱️ **Temps estimé :** 2-5 minutes

### Étape 5 : Exécuter l'Application

**Méthode 1 : Clic droit (Recommandé)**
1. Dans le panneau Project, naviguez vers :
   ```
   src/main/java/com/vms/Main.java
   ```
2. **Clic droit** sur `Main.java`
3. Sélectionnez **Run 'Main.main()'**
4. 🎉 **Le dashboard s'ouvre !**

**Méthode 2 : Maven**
1. Ouvrez le terminal intégré d'IntelliJ (en bas)
2. Tapez :
   ```bash
   mvn clean javafx:run
   ```

**Méthode 3 : Bouton Play**
1. Ouvrez `Main.java`
2. Cliquez sur le **bouton Play vert ▶** à côté de `public class Main`
3. Choisissez **Run 'Main.main()'**

---

## 🎨 Fonctionnalités du Dashboard

### 📊 Modules Disponibles

| Module | Description | Status |
|--------|-------------|--------|
| **DEMANDES** | Gestion des demandes de bons cadeau | 🔄 En développement |
| **UTILISATEURS** | Administration des utilisateurs | 🔄 En développement |
| **MAGASIN** | Gestion des magasins et points de vente | 🔄 En développement |
| **CLIENTS** | Base de données clients | 🔄 En développement |
| **VOUCHER** | Création et suivi des bons cadeau | 🔄 En développement |

### ✨ Caractéristiques

- ✅ Interface moderne et responsive
- ✅ Effets hover sur les cartes
- ✅ Animations fluides
- ✅ Design fidèle à votre maquette
- ✅ Architecture MVC (Model-View-Controller)
- ✅ Code propre et commenté
- ✅ Prêt pour PostgreSQL

---

## 🔧 Configuration Maven (pom.xml)

Le fichier `pom.xml` inclut :

### Dépendances
- **JavaFX Controls** - Composants UI
- **JavaFX FXML** - Support FXML
- **JavaFX Graphics** - Graphiques et effets
- **PostgreSQL JDBC Driver** - Connexion base de données
- **ControlsFX** - Composants UI avancés
- **FontAwesomeFX** - Icônes

### Plugins Maven
- **maven-compiler-plugin** - Compilation Java
- **javafx-maven-plugin** - Exécution JavaFX
- **maven-shade-plugin** - Création de JAR exécutable

---

## 📝 Commandes Maven Utiles

```bash
# Nettoyer le projet
mvn clean

# Compiler le projet
mvn compile

# Lancer l'application
mvn javafx:run

# Créer un JAR exécutable
mvn package

# Tout nettoyer et recompiler
mvn clean install

# Télécharger les dépendances
mvn dependency:resolve
```

---

## 🎯 Prochaines Étapes

### 1. Créer les Modules
Pour chaque module (Demandes, Utilisateurs, etc.), créez :
- Un fichier FXML (ex: `demandes.fxml`)
- Un contrôleur (ex: `DemandesController.java`)
- Une classe modèle (ex: `Demande.java`)

### 2. Connexion PostgreSQL
```java
// Exemple dans un fichier DatabaseConnection.java
public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/vms";
    private static final String USER = "votre_user";
    private static final String PASSWORD = "votre_password";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### 3. Créer les Tables
```sql
-- Exemple de table
CREATE TABLE demandes (
    id SERIAL PRIMARY KEY,
    client_id INTEGER,
    nombre_bons INTEGER,
    valeur_unitaire DECIMAL(10,2),
    statut VARCHAR(50),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4. Implémenter les DAO (Data Access Object)
```java
public class DemandeDAO {
    public List<Demande> getAllDemandes() { ... }
    public void createDemande(Demande demande) { ... }
    public void updateDemande(Demande demande) { ... }
    public void deleteDemande(int id) { ... }
}
```

---

## ❌ Résolution de Problèmes

### Problème : "Cannot resolve symbol 'javafx'"
**Solution :** Maven n'a pas téléchargé les dépendances
```bash
mvn clean install -U
```

### Problème : "Error: JavaFX runtime components are missing"
**Solution :** Utilisez Maven pour lancer l'application
```bash
mvn javafx:run
```

### Problème : Le projet ne compile pas
**Solution :** Vérifiez votre JDK
1. File → Project Structure → Project
2. Vérifiez que SDK est JDK 17+

### Problème : Maven ne télécharge rien
**Solution :** Vérifiez votre connexion Internet ou le proxy Maven
1. Settings → Build, Execution, Deployment → Build Tools → Maven
2. Vérifiez les paramètres réseau

---

## 📚 Ressources Utiles

- [Documentation JavaFX](https://openjfx.io/)
- [Maven Getting Started](https://maven.apache.org/guides/getting-started/)
- [PostgreSQL JDBC](https://jdbc.postgresql.org/)
- [IntelliJ IDEA Guide](https://www.jetbrains.com/idea/guide/)
- [FXML Reference](https://docs.oracle.com/javafx/2/api/javafx/fxml/doc-files/introduction_to_fxml.html)

---

## 👥 Support

Pour toute question :
1. Vérifiez que Java 17+ est installé : `java -version`
2. Vérifiez que Maven fonctionne : `mvn -version`
3. Consultez les logs d'IntelliJ
4. Recherchez l'erreur sur Stack Overflow

---

## 📄 Licence
Projet académique - BTS SIO MCCI Business School

**Bon développement ! 🚀**
