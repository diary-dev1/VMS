# 📋 MODULE DEMANDES - GUIDE D'INSTALLATION

## 🎯 Ce qui a été ajouté

### ✅ Nouveaux fichiers créés :

1. **Model** : `src/main/java/com/vms/model/Demande.java`
   - Classe pour gérer les données d'une demande
   - Attributs : référence, client, nombre de bons, valeur, statut, etc.

2. **Controller** : `src/main/java/com/vms/controller/DemandesController.java`
   - Gère toute la logique de la page Demandes
   - Tableau, formulaire, filtres, statistiques

3. **View** : `src/main/resources/com/vms/demandes.fxml`
   - Interface graphique de la page Demandes
   - Tableau + formulaire + statistiques

4. **Styles** : Ajout de styles dans `styles.css`
   - Boutons, cartes statistiques, formulaires

---

## 🚀 INSTALLATION (2 OPTIONS)

### **Option 1 : Copier les nouveaux fichiers (RAPIDE)**

Si vous avez déjà le projet qui fonctionne :

```
1. Téléchargez le ZIP : vms-dashboard-avec-demandes.zip

2. Décompressez-le

3. Copiez SEULEMENT ces nouveaux fichiers/dossiers dans votre projet :

   📁 src/main/java/com/vms/model/
      └── Demande.java
   
   📁 src/main/java/com/vms/controller/
      └── DemandesController.java
   
   📁 src/main/resources/com/vms/
      └── demandes.fxml
   
   📄 src/main/java/com/vms/DashboardController.java (REMPLACER)
   📄 src/main/resources/com/vms/styles.css (REMPLACER)

4. Dans IntelliJ, clic droit sur le projet → Maven → Reload Project

5. Lancez l'application
```

---

### **Option 2 : Projet complet (RECOMMANDÉ)**

```
1. Fermez votre projet actuel dans IntelliJ

2. Décompressez le nouveau ZIP

3. File → Open → Sélectionnez le dossier vms-dashboard

4. Enable Auto-Import (Maven)

5. Lancez avec le bouton Play ▶️
```

---

## 🎨 FONCTIONNALITÉS DU MODULE DEMANDES

### **📊 Tableau des demandes**
- ✅ Liste de toutes les demandes
- ✅ Colonnes : Référence, Client, Nb Bons, Valeur, Montant, Statut, Date
- ✅ Boutons d'action : Voir, Valider paiement, Supprimer

### **➕ Formulaire de création**
- ✅ Sélection du client
- ✅ Nombre de bons
- ✅ Valeur unitaire
- ✅ Calcul automatique du montant total
- ✅ Remarques optionnelles
- ✅ Génération automatique de la référence (VR0001-200)

### **📈 Statistiques en temps réel**
- ✅ Total des demandes
- ✅ Demandes en attente de paiement
- ✅ Demandes approuvées
- ✅ Montant total

### **🔍 Filtres et recherche**
- ✅ Filtrer par statut (Tous, En attente, Payé, Approuvé, Générés)
- ✅ Recherche par référence ou nom de client
- ✅ Actualiser la liste

### **⚙️ Actions**
- ✅ Voir détails d'une demande
- ✅ Valider le paiement (change le statut)
- ✅ Supprimer une demande (avec confirmation)

---

## 🎯 COMMENT UTILISER

### **Depuis le Dashboard**

```
1. Lancez l'application
2. Sur le dashboard, cliquez sur la carte "DEMANDES"
3. Vous arrivez sur la page de gestion des demandes
```

### **Créer une nouvelle demande**

```
1. Cliquez sur "➕ Nouvelle Demande"
2. Un formulaire s'affiche à droite
3. Remplissez :
   - Client (liste déroulante)
   - Nombre de bons
   - Valeur unitaire
   - Remarques (optionnel)
4. Le montant total se calcule automatiquement
5. Cliquez sur "✔ Enregistrer"
```

### **Valider un paiement**

```
1. Dans le tableau, trouvez la demande
2. Cliquez sur le bouton "✓" (valider)
3. Confirmez
4. Le statut passe à "Payé"
```

### **Rechercher une demande**

```
1. Utilisez la barre de recherche en haut à droite
2. Tapez la référence ou le nom du client
3. Le tableau se filtre automatiquement
```

### **Filtrer par statut**

```
1. Utilisez le menu déroulant "Filtrer par statut"
2. Sélectionnez : Tous, En attente, Payé, etc.
3. Le tableau affiche uniquement les demandes du statut choisi
```

---

## 📝 DONNÉES DE TEST

Le module contient 4 demandes de test :
- VR0001-200 : ABC Company Ltd (En attente paiement)
- VR0002-150 : XYZ Corporation (Payé)
- VR0003-300 : Tech Solutions (Approuvé)
- VR0004-100 : Global Enterprises (Bons générés)

**Ces données sont en mémoire** (pas encore en base de données).

---

## 🔄 PROCHAINES ÉTAPES

### **1. Connecter à PostgreSQL**

Pour sauvegarder les demandes en base de données :

```sql
-- Créer la table demandes
CREATE TABLE demandes (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) UNIQUE NOT NULL,
    client_id INTEGER,
    client_nom VARCHAR(200),
    nombre_bons INTEGER NOT NULL,
    valeur_unitaire DECIMAL(10,2) NOT NULL,
    montant_total DECIMAL(10,2),
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE_PAIEMENT',
    date_creation DATE DEFAULT CURRENT_DATE,
    date_paiement DATE,
    date_approbation DATE,
    cree_par VARCHAR(100),
    remarques TEXT
);
```

### **2. Créer le DAO**

Créer `DemandeDAO.java` avec les méthodes :
- `getAllDemandes()` : Récupérer toutes les demandes
- `createDemande()` : Créer une nouvelle demande
- `updateDemande()` : Modifier une demande
- `deleteDemande()` : Supprimer une demande

### **3. Améliorer le module**

- Ajouter une vraie table clients
- Génération automatique de PDF pour les bons
- Envoi d'emails automatiques
- Validation des approbations
- Historique des modifications

---

## ❓ PROBLÈMES COURANTS

### Erreur : "Cannot find demandes.fxml"
**Solution :** Vérifiez que le fichier est bien dans `src/main/resources/com/vms/`

### Erreur : "Cannot find Demande class"
**Solution :** Reloadez Maven (clic droit projet → Maven → Reload Project)

### Le bouton DEMANDES ne fait rien
**Solution :** Vérifiez que `DashboardController.java` a été remplacé

### Erreur de compilation
**Solution :** Recompilez le projet (Build → Rebuild Project)

---

## 🎉 C'EST FAIT !

Vous avez maintenant un module DEMANDES complet et fonctionnel !

**Testez-le** :
1. Lancez l'application
2. Cliquez sur DEMANDES
3. Créez une nouvelle demande
4. Validez un paiement
5. Filtrez et recherchez

---

## 📞 BESOIN D'AIDE ?

Si vous voulez ajouter :
- Les autres modules (Utilisateurs, Clients, etc.)
- La connexion PostgreSQL
- L'export vers Excel
- L'envoi d'emails

Demandez-moi ! 😊
