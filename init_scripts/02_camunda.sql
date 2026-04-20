-- ===================================================
-- 02_camunda.sql: Camunda-specific database setup
-- ===================================================

-- Create Camunda schema (if not using default)
CREATE SCHEMA IF NOT EXISTS camunda;

-- Note: Camunda Operate and Tasklist use Elasticsearch, not PostgreSQL
-- However, Identity uses PostgreSQL for user management
-- The actual process data is stored in Zeebe (in-memory + snapshots)

-- Create user/role management tables for Identity integration
CREATE TABLE IF NOT EXISTS camunda.users (
                                             id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS camunda.roles (
                                             id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS camunda.user_roles (
                                                  user_id VARCHAR(255) NOT NULL,
    role_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES camunda.users(id),
    FOREIGN KEY (role_id) REFERENCES camunda.roles(id)
    );

-- Process configuration table
CREATE TABLE IF NOT EXISTS camunda.process_definitions (
                                                           id BIGSERIAL PRIMARY KEY,
                                                           process_key VARCHAR(255) UNIQUE NOT NULL,
    process_name VARCHAR(255) NOT NULL,
    bpmn_file_path VARCHAR(500),
    version INT DEFAULT 1,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA camunda TO admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA camunda TO admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA camunda TO admin;

-- Default roles for Camunda
INSERT INTO camunda.roles (id, name, description) VALUES
                                                      ('process-admin', 'Process Administrator', 'Can manage all processes'),
                                                      ('process-viewer', 'Process Viewer', 'Can view processes'),
                                                      ('task-user', 'Task User', 'Can claim and complete user tasks')
    ON CONFLICT DO NOTHING;

-- Default admin user
INSERT INTO camunda.users (id, username, email, first_name, last_name, enabled) VALUES
    ('admin-user', 'admin', 'admin@example.com', 'Admin', 'User', TRUE)
    ON CONFLICT DO NOTHING;

-- Assign admin role to admin user
INSERT INTO camunda.user_roles (user_id, role_id) VALUES
    ('admin-user', 'process-admin')
    ON CONFLICT DO NOTHING;