-- ============================================================
-- TaskFlow - Task Management System
-- MySQL Workbench Schema Script
-- Run this in MySQL Workbench on database: task_manager_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS task_manager_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE task_manager_db;

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255)        NOT NULL,
    email       VARCHAR(255)        NOT NULL UNIQUE,
    password    VARCHAR(255)        NOT NULL,
    role        ENUM('user','team','manager','admin') NOT NULL DEFAULT 'user',
    avatar_url  VARCHAR(500),
    phone       VARCHAR(50),
    department  VARCHAR(255),
    created_at  DATETIME,
    updated_at  DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLE: tasks
-- ============================================================
CREATE TABLE IF NOT EXISTS tasks (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255)  NOT NULL,
    description  TEXT,
    assigned_to  VARCHAR(255),
    created_by   VARCHAR(255)  NOT NULL,
    due_date     DATE,
    priority     VARCHAR(50)   NOT NULL DEFAULT 'Low',
    status       VARCHAR(100)  NOT NULL DEFAULT 'Todo',
    category     VARCHAR(255)  NOT NULL DEFAULT 'Uncategorized',
    labels       VARCHAR(500),
    completed_by VARCHAR(255),
    completed_at DATETIME,
    created_at   DATETIME,
    updated_at   DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLE: comments
-- ============================================================
CREATE TABLE IF NOT EXISTS comments (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id      BIGINT        NOT NULL,
    text         TEXT          NOT NULL,
    author_name  VARCHAR(255),
    author_email VARCHAR(255),
    created_at   DATETIME,
    CONSTRAINT fk_comment_task FOREIGN KEY (task_id)
        REFERENCES tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLE: notifications
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    to_email   VARCHAR(255)  NOT NULL,
    message    TEXT          NOT NULL,
    type       VARCHAR(50)   NOT NULL DEFAULT 'INFO',
    is_read    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLE: file_attachments
-- ============================================================
CREATE TABLE IF NOT EXISTS file_attachments (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id       BIGINT        NOT NULL,
    file_name     VARCHAR(500)  NOT NULL,
    original_name VARCHAR(500)  NOT NULL,
    file_type     VARCHAR(255),
    file_size     BIGINT,
    file_path     VARCHAR(1000),
    uploaded_by   VARCHAR(255),
    uploaded_at   DATETIME,
    CONSTRAINT fk_file_task FOREIGN KEY (task_id)
        REFERENCES tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- SEED DATA: Default Users (password stored as plain text here,
--            Spring Boot will hash with BCrypt on first run)
-- ============================================================
INSERT IGNORE INTO users (name, email, password, role, department, created_at, updated_at) VALUES
('Admin User',   'admin@gmail.com',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTpyYMTN7Wm', 'admin',   'Administration', NOW(), NOW()),
('Manager User', 'manager@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTpyYMTN7Wm', 'manager', 'Engineering',    NOW(), NOW()),
('Team Member',  'member@gmail.com',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTpyYMTN7Wm', 'team',    'Development',    NOW(), NOW()),
('Regular User', 'user@gmail.com',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTpyYMTN7Wm', 'user',    'Operations',     NOW(), NOW());
-- Note: The BCrypt hash above = "password123"
-- Spring Boot DataSeeder will create users with correct password on startup

-- ============================================================
-- SEED DATA: Sample Tasks
-- ============================================================
INSERT IGNORE INTO tasks (title, description, assigned_to, created_by, due_date, priority, status, category, labels, created_at, updated_at) VALUES
('Design new landing page',  'Redesign the homepage with new branding',    'member@gmail.com',  'manager@gmail.com', '2026-10-05', 'High',   'In Progress',      'Design',      'urgent,design',   NOW(), NOW()),
('Fix login bug',            'Users cannot login on mobile devices',        'member@gmail.com',  'manager@gmail.com', '2026-10-03', 'High',   'Todo',             'Development', 'bug',             NOW(), NOW()),
('Write API documentation',  'Document all REST endpoints for the team',    'user@gmail.com',    'manager@gmail.com', '2026-09-30', 'Medium', 'Completed',        'Research',    'docs',            NOW(), NOW()),
('Database optimization',    'Optimize slow running queries',               'member@gmail.com',  'manager@gmail.com', '2026-10-08', 'Medium', 'Pending Approval', 'Development', 'performance',     NOW(), NOW()),
('Q4 Marketing Plan',        'Plan Q4 digital marketing campaigns',         'user@gmail.com',    'admin@gmail.com',   '2026-10-10', 'High',   'In Progress',      'Marketing',   'strategy',        NOW(), NOW()),
('Team onboarding docs',     'Create onboarding materials for new hires',   'member@gmail.com',  'manager@gmail.com', '2026-10-15', 'Low',    'Todo',             'Operations',  '',                NOW(), NOW()),
('Security audit',           'Audit the authentication flow for issues',    'user@gmail.com',    'admin@gmail.com',   '2026-10-04', 'High',   'Todo',             'Development', 'security',        NOW(), NOW()),
('User feedback analysis',   'Analyse last month user feedback reports',    'member@gmail.com',  'manager@gmail.com', '2026-09-28', 'Low',    'Completed',        'Research',    'analytics',       NOW(), NOW());

-- ============================================================
-- VERIFY
-- ============================================================
SELECT 'users'            AS table_name, COUNT(*) AS rows FROM users
UNION ALL
SELECT 'tasks',                          COUNT(*)          FROM tasks
UNION ALL
SELECT 'comments',                       COUNT(*)          FROM comments
UNION ALL
SELECT 'notifications',                  COUNT(*)          FROM notifications
UNION ALL
SELECT 'file_attachments',               COUNT(*)          FROM file_attachments;
