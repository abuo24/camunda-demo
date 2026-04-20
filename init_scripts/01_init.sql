-- ===================================================
-- 01-init.sql: Application database initialization
-- ===================================================

-- Create application schema
CREATE SCHEMA IF NOT EXISTS app;

-- Products table (from previous example)
CREATE TABLE IF NOT EXISTS app.products (
                                            id BIGSERIAL PRIMARY KEY,
                                            name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    price DECIMAL(18, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Process instances table (to track Camunda processes)
CREATE TABLE IF NOT EXISTS app.process_instances (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     process_definition_key VARCHAR(255) NOT NULL,
    process_instance_id VARCHAR(255) UNIQUE NOT NULL,
    business_key VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    product_id BIGINT REFERENCES app.products(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- User tasks tracking
CREATE TABLE IF NOT EXISTS app.user_tasks (
                                              id BIGSERIAL PRIMARY KEY,
                                              task_id VARCHAR(255) UNIQUE NOT NULL,
    process_instance_id VARCHAR(255) NOT NULL,
    task_key VARCHAR(255) NOT NULL,
    assignee VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (process_instance_id) REFERENCES app.process_instances(process_instance_id)
    );

-- Audit log for process events
CREATE TABLE IF NOT EXISTS app.process_audit_log (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     process_instance_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (process_instance_id) REFERENCES app.process_instances(process_instance_id)
    );

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_products_category ON app.products(category);
CREATE INDEX IF NOT EXISTS idx_products_brand ON app.products(brand);
CREATE INDEX IF NOT EXISTS idx_process_instances_status ON app.process_instances(status);
CREATE INDEX IF NOT EXISTS idx_process_instances_key ON app.process_instances(process_definition_key);
CREATE INDEX IF NOT EXISTS idx_user_tasks_status ON app.user_tasks(status);
CREATE INDEX IF NOT EXISTS idx_user_tasks_assignee ON app.user_tasks(assignee);
CREATE INDEX IF NOT EXISTS idx_process_audit_log_instance ON app.process_audit_log(process_instance_id);

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA app TO admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA app TO admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA app TO admin;
