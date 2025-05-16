-- Inserimanto di un utente di esempio
INSERT INTO users (username, email, password, created_at) VALUES ('admin', 'admin@aulab.it', '$2a$10$oMiUOq5ToRfUI/Zprg5nE.qt8nT9KKJZoDBu1SIWuj.UGx8aRHwxS', CURRENT_TIMESTAMP); -- Password criptata per "12345678"

-- Inserimento dei ruoli disponibili
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_REVISOR'), ('ROLE_WRITER'), ('ROLE_USER');

-- Assegnazione del ruolo 'ROLE_ADMIN' all'utente con id 1 (verificare l'id corretto dopo l'inserimento dell'utente) [cite: 30]
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);

-- Inserimento delle categorie predefinite
INSERT INTO categories (name) VALUES ('Politica'), ('Economia'), ('Food&Drink'), ('Sport'), ('Intrattenimento'), ('Tech');