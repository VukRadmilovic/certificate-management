INSERT INTO ROLE (NAME) VALUES
    ('ROLE_AUTHENTICATED');
INSERT INTO ROLE (NAME) VALUES
    ('ROLE_ADMIN');

INSERT INTO USERS(EMAIL, PASSWORD, NAME, SURNAME, PHONE_NUMBER) VALUES ('mail1@mail.com',
                    '$2a$10$IgPBrBNNOaCVhb4dGmEKLeMPndC09k30PbQq..kMghoDzZNYicVG6',
                    'Maja',
                    'Varga',
                    '+381627834992'
                    );
INSERT INTO USERS(EMAIL, PASSWORD, NAME, SURNAME, PHONE_NUMBER) VALUES ('mail2@mail.com',
                    '$2a$10$E3fnG2Z/pNYdQCuMOSYCn.UyTLW1zXfCwR.ds5j9IztyJ0TIjRyJG',
                    'Milan',
                    'Simić',
                    '+381641183201'
                                                                       );
INSERT INTO USER_ROLE (user_id, role_id) VALUES (1, 1);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (2, 1);