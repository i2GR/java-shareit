DELETE FROM users;

INSERT INTO users (email, name)
VALUES ('owner@host.dom', 'owner');
INSERT INTO users (email, name)
('booker@host.dom', 'booker');

DELETE FROM items;
INSERT INTO items (owner_id, name, description, available)
VALUES(1 ,'item', 'description', true);