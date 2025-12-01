-- ============================================================================
-- SCRIPT DE CRÉATION DE LA BASE DE DONNÉES VMS
-- Voucher Management System
-- PostgreSQL 12+
-- ============================================================================

-- Créer la base de données
CREATE DATABASE diary_vms;

-- Se connecter à la base de données
\c diary_vms;

-- ============================================================================
-- TABLE: clients
-- ============================================================================
CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    numero_compte VARCHAR(20) UNIQUE NOT NULL,
    nom VARCHAR(200) NOT NULL,
    email VARCHAR(150),
    telephone VARCHAR(20),
    adresse TEXT,
    contact_personne VARCHAR(150),
    date_inscription DATE DEFAULT CURRENT_DATE,
    actif BOOLEAN DEFAULT TRUE,
    remarques TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour recherche rapide
CREATE INDEX idx_clients_nom ON clients(nom);
CREATE INDEX idx_clients_email ON clients(email);

-- ============================================================================
-- TABLE: magasins
-- ============================================================================
CREATE TABLE magasins (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    nom VARCHAR(200) NOT NULL,
    adresse TEXT,
    ville VARCHAR(100),
    telephone VARCHAR(20),
    responsable VARCHAR(150),
    date_ouverture DATE DEFAULT CURRENT_DATE,
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_magasins_nom ON magasins(nom);
CREATE INDEX idx_magasins_ville ON magasins(ville);

-- ============================================================================
-- TABLE: demandes
-- ============================================================================
CREATE TABLE demandes (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) UNIQUE NOT NULL,
    client_id INTEGER REFERENCES clients(id) ON DELETE CASCADE,
    nombre_bons INTEGER NOT NULL CHECK (nombre_bons > 0),
    valeur_unitaire DECIMAL(10,2) NOT NULL CHECK (valeur_unitaire > 0),
    montant_total DECIMAL(12,2) GENERATED ALWAYS AS (nombre_bons * valeur_unitaire) STORED,
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE_PAIEMENT' 
        CHECK (statut IN ('EN_ATTENTE_PAIEMENT', 'PAYE', 'APPROUVE', 'GENERE', 'ANNULE')),
    date_creation DATE DEFAULT CURRENT_DATE,
    date_paiement DATE,
    date_approbation DATE,
    cree_par VARCHAR(100),
    approuve_par VARCHAR(100),
    remarques TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_demandes_reference ON demandes(reference);
CREATE INDEX idx_demandes_client ON demandes(client_id);
CREATE INDEX idx_demandes_statut ON demandes(statut);
CREATE INDEX idx_demandes_date_creation ON demandes(date_creation);

-- ============================================================================
-- TABLE: vouchers
-- ============================================================================
CREATE TABLE vouchers (
    id SERIAL PRIMARY KEY,
    code VARCHAR(30) UNIQUE NOT NULL,
    demande_id INTEGER REFERENCES demandes(id) ON DELETE CASCADE,
    valeur DECIMAL(10,2) NOT NULL CHECK (valeur > 0),
    qr_code VARCHAR(100) UNIQUE,
    statut VARCHAR(50) DEFAULT 'EMIS'
        CHECK (statut IN ('EMIS', 'REDEMME', 'EXPIRE', 'ANNULE')),
    date_emission DATE DEFAULT CURRENT_DATE,
    date_expiration DATE NOT NULL,
    date_redemption DATE,
    magasin_redemption_id INTEGER REFERENCES magasins(id),
    redemme_par VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_date_expiration CHECK (date_expiration > date_emission),
    CONSTRAINT chk_redemption CHECK (
        (statut = 'REDEMME' AND date_redemption IS NOT NULL) OR
        (statut != 'REDEMME' AND date_redemption IS NULL)
    )
);

CREATE INDEX idx_vouchers_code ON vouchers(code);
CREATE INDEX idx_vouchers_demande ON vouchers(demande_id);
CREATE INDEX idx_vouchers_statut ON vouchers(statut);
CREATE INDEX idx_vouchers_qr ON vouchers(qr_code);
CREATE INDEX idx_vouchers_expiration ON vouchers(date_expiration);

-- ============================================================================
-- TABLE: historique_redemptions
-- ============================================================================
CREATE TABLE historique_redemptions (
    id SERIAL PRIMARY KEY,
    voucher_id INTEGER REFERENCES vouchers(id) ON DELETE CASCADE,
    magasin_id INTEGER REFERENCES magasins(id),
    date_redemption TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    redemme_par VARCHAR(100),
    montant DECIMAL(10,2),
    remarques TEXT
);

CREATE INDEX idx_historique_voucher ON historique_redemptions(voucher_id);
CREATE INDEX idx_historique_magasin ON historique_redemptions(magasin_id);
CREATE INDEX idx_historique_date ON historique_redemptions(date_redemption);

-- ============================================================================
-- TABLE: utilisateurs (pour authentification future)
-- ============================================================================
CREATE TABLE utilisateurs (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nom_complet VARCHAR(150),
    email VARCHAR(150),
    role VARCHAR(50) DEFAULT 'UTILISATEUR'
        CHECK (role IN ('ADMIN', 'COMPTABLE', 'APPROBATEUR', 'UTILISATEUR', 'MAGASIN')),
    actif BOOLEAN DEFAULT TRUE,
    derniere_connexion TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_utilisateurs_username ON utilisateurs(username);
CREATE INDEX idx_utilisateurs_email ON utilisateurs(email);

-- ============================================================================
-- TABLE: audit_log
-- ============================================================================
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    table_name VARCHAR(50),
    record_id INTEGER,
    action VARCHAR(20) CHECK (action IN ('INSERT', 'UPDATE', 'DELETE')),
    old_values JSONB,
    new_values JSONB,
    user_id INTEGER REFERENCES utilisateurs(id),
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_table ON audit_log(table_name);
CREATE INDEX idx_audit_record ON audit_log(record_id);
CREATE INDEX idx_audit_date ON audit_log(created_at);

-- ============================================================================
-- FONCTIONS ET TRIGGERS
-- ============================================================================

-- Fonction pour mettre à jour updated_at automatiquement
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Appliquer le trigger sur toutes les tables
CREATE TRIGGER update_clients_updated_at BEFORE UPDATE ON clients
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_magasins_updated_at BEFORE UPDATE ON magasins
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_demandes_updated_at BEFORE UPDATE ON demandes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vouchers_updated_at BEFORE UPDATE ON vouchers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_utilisateurs_updated_at BEFORE UPDATE ON utilisateurs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- PROCÉDURE: Générer les vouchers pour une demande
-- ============================================================================
CREATE OR REPLACE FUNCTION generer_vouchers_pour_demande(p_demande_id INTEGER)
RETURNS INTEGER AS $$
DECLARE
    v_demande RECORD;
    v_count INTEGER := 0;
    v_code VARCHAR(30);
    i INTEGER;
BEGIN
    -- Récupérer les infos de la demande
    SELECT * INTO v_demande FROM demandes WHERE id = p_demande_id;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Demande non trouvée';
    END IF;
    
    IF v_demande.statut != 'APPROUVE' THEN
        RAISE EXCEPTION 'La demande doit être approuvée avant de générer les vouchers';
    END IF;
    
    -- Générer les vouchers
    FOR i IN 1..v_demande.nombre_bons LOOP
        v_code := 'VC' || LPAD((NEXTVAL('vouchers_id_seq'))::TEXT, 10, '0');
        
        INSERT INTO vouchers (
            code, demande_id, valeur, qr_code, date_expiration
        ) VALUES (
            v_code,
            p_demande_id,
            v_demande.valeur_unitaire,
            'QR-' || v_code,
            CURRENT_DATE + INTERVAL '6 months'
        );
        
        v_count := v_count + 1;
    END LOOP;
    
    -- Mettre à jour le statut de la demande
    UPDATE demandes SET statut = 'GENERE' WHERE id = p_demande_id;
    
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- PROCÉDURE: Marquer les vouchers expirés
-- ============================================================================
CREATE OR REPLACE FUNCTION marquer_vouchers_expires()
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER;
BEGIN
    UPDATE vouchers 
    SET statut = 'EXPIRE'
    WHERE statut = 'EMIS' 
      AND date_expiration < CURRENT_DATE;
    
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- VUES MATÉRIALISÉES pour reporting
-- ============================================================================

-- Vue: Statistiques par client
CREATE MATERIALIZED VIEW vue_stats_clients AS
SELECT 
    c.id,
    c.numero_compte,
    c.nom,
    COUNT(DISTINCT d.id) as nombre_demandes,
    COALESCE(SUM(d.montant_total), 0) as montant_total,
    COUNT(DISTINCT v.id) as nombre_vouchers,
    COUNT(DISTINCT CASE WHEN v.statut = 'REDEMME' THEN v.id END) as vouchers_redemies
FROM clients c
LEFT JOIN demandes d ON c.id = d.client_id
LEFT JOIN vouchers v ON d.id = v.demande_id
GROUP BY c.id, c.numero_compte, c.nom;

CREATE UNIQUE INDEX ON vue_stats_clients(id);

-- Vue: Statistiques par magasin
CREATE MATERIALIZED VIEW vue_stats_magasins AS
SELECT 
    m.id,
    m.code,
    m.nom,
    m.ville,
    COUNT(v.id) as nombre_redemptions,
    COALESCE(SUM(v.valeur), 0) as valeur_totale_redemptions
FROM magasins m
LEFT JOIN vouchers v ON m.id = v.magasin_redemption_id AND v.statut = 'REDEMME'
GROUP BY m.id, m.code, m.nom, m.ville;

CREATE UNIQUE INDEX ON vue_stats_magasins(id);

-- ============================================================================
-- DONNÉES DE TEST
-- ============================================================================

-- Insérer des clients de test
INSERT INTO clients (numero_compte, nom, email, telephone, adresse, contact_personne) VALUES
('CLI00001', 'ABC Company Ltd', 'contact@abc.com', '+230 5123 4567', '123 Royal Road, Port Louis', 'John Doe'),
('CLI00002', 'XYZ Corporation', 'info@xyz.com', '+230 5234 5678', '45 Victoria Avenue, Rose-Hill', 'Jane Smith'),
('CLI00003', 'Tech Solutions', 'hello@tech.com', '+230 5345 6789', 'Cyber Tower 1, Ebene', 'Bob Wilson');

-- Insérer des magasins de test
INSERT INTO magasins (code, nom, adresse, ville, telephone, responsable) VALUES
('MAG001', 'Magasin Central', '123 Rue Royale', 'Port Louis', '+230 5111 1111', 'Pierre Dupont'),
('MAG002', 'Magasin Rose-Hill', '45 Avenue Victoria', 'Rose-Hill', '+230 5222 2222', 'Marie Laurent'),
('MAG003', 'Magasin Ebene', 'Cyber Tower 1', 'Ebene', '+230 5333 3333', 'Jean Martin');

-- Insérer un utilisateur admin de test (mot de passe: admin123)
INSERT INTO utilisateurs (username, password_hash, nom_complet, email, role) VALUES
('admin', '$2a$10$YourHashHere', 'Administrateur', 'admin@vms.com', 'ADMIN');

-- ============================================================================
-- PERMISSIONS
-- ============================================================================

-- Créer un rôle pour l'application
CREATE ROLE vms_app_role;

-- Donner les permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO vms_app_role;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO vms_app_role;

-- ============================================================================
-- SCRIPTS DE MAINTENANCE
-- ============================================================================

-- Rafraîchir les vues matérialisées (à exécuter régulièrement)
-- REFRESH MATERIALIZED VIEW vue_stats_clients;
-- REFRESH MATERIALIZED VIEW vue_stats_magasins;

-- Marquer les vouchers expirés (à exécuter quotidiennement)
-- SELECT marquer_vouchers_expires();

COMMENT ON DATABASE vms_database IS 'Base de données VMS - Voucher Management System';
