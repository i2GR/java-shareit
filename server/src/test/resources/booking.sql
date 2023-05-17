DELETE FROM users;

INSERT INTO users (email, name)
VALUES ('owner@host.dom', 'owner'),
('booker@host.dom', 'booker');

DELETE FROM items;
SET @itemOwnerId = SELECT id FROM users WHERE email = 'owner@host.dom';
INSERT INTO items (owner_id, name, description, available)
VALUES(@itemOwnerId ,'item', 'description', true);