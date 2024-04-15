CREATE DATABASE auth_service;
\c auth_service

CREATE TABLE IF NOT EXISTS auth_user(
    oauth2_id VARCHAR(255) PRIMARY KEY,
    provider VARCHAR(20),
    is_blocked BOOLEAN
);

CREATE TABLE IF NOT EXISTS role(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS permission(
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS auth_user_role(
    role_id BIGINT,
    oauth2_id VARCHAR(255),
    PRIMARY KEY (role_id, oauth2_id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (oauth2_id) REFERENCES auth_user(oauth2_id)
);

CREATE TABLE IF NOT EXISTS role_permission(
    permission_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (permission_id, role_id),
    FOREIGN KEY (permission_id) REFERENCES permission(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

INSERT INTO role(id, name) VALUES (1, 'ROLE_USER');
INSERT INTO role(id, name) VALUES (2, 'ROLE_ADMIN');

INSERT INTO permission(id, description, url) VALUES (1 ,'Auth url', '/auth-service/api/auth/**');
INSERT INTO permission(id, description, url) VALUES (2, 'User url', '/user-service/api/user/**');
INSERT INTO permission(id, description, url) VALUES (3, 'Admin url', '/**/api/admin/**');

INSERT INTO role_permission(permission_id, role_id) VALUES (1, 1);
INSERT INTO role_permission(permission_id, role_id) VALUES (2, 1);
INSERT INTO role_permission(permission_id, role_id) VALUES (3, 2);