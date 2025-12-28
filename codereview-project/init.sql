-- ============================
-- USERS
-- ============================
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

-- ============================
-- PROJECTS
-- ============================
CREATE TABLE IF NOT EXISTS projects (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id INTEGER NOT NULL REFERENCES users(id)
);

-- ============================
-- PROJECT MEMBERS
-- ============================
CREATE TABLE IF NOT EXISTS project_members (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, project_id)
);

-- ============================
-- PROJECT FILES (base code)
-- ============================
CREATE TABLE IF NOT EXISTS project_files (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    last_review_id INTEGER,
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Одинаковые имена файлов запрещены в одном проекте
CREATE UNIQUE INDEX IF NOT EXISTS idx_project_files_unique
    ON project_files(project_id, filename);

-- ============================
-- CODE REVIEWS
-- ============================
CREATE TABLE IF NOT EXISTS code_reviews (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    author_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    status VARCHAR(50) DEFAULT 'WIP',
    current_revision_number INTEGER NOT NULL DEFAULT 1,
    locked_revision_number INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- REVIEW FILES (modified code per review)
-- ============================
CREATE TABLE IF NOT EXISTS review_files (
    id SERIAL PRIMARY KEY,
    review_id INTEGER NOT NULL REFERENCES code_reviews(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    revision_number INTEGER NOT NULL DEFAULT 1,
    change_type VARCHAR(20) NOT NULL DEFAULT 'MODIFIED'
);

-- ============================
-- REVIEW REVIEWERS
-- ============================
CREATE TABLE IF NOT EXISTS review_reviewers (
    id SERIAL PRIMARY KEY,
    review_id INTEGER NOT NULL REFERENCES code_reviews(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(16) NOT NULL DEFAULT 'REVIEWER',
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_review_reviewers_review_user
    ON review_reviewers(review_id, user_id);

-- ============================
-- REVIEW VOTES
-- ============================
CREATE TABLE IF NOT EXISTS review_votes (
    id SERIAL PRIMARY KEY,
    review_id INTEGER NOT NULL REFERENCES code_reviews(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label VARCHAR(32) NOT NULL,
    value INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_review_votes_review_user_label
    ON review_votes(review_id, user_id, label);

-- ============================
-- REVIEW COMMENTS (global + per-file + per-line)
-- ============================
CREATE TABLE IF NOT EXISTS review_comments (
    id SERIAL PRIMARY KEY,

    review_id INTEGER NOT NULL REFERENCES code_reviews(id) ON DELETE CASCADE,

    author_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    review_file_id INTEGER REFERENCES review_files(id) ON DELETE CASCADE,

    line_number INTEGER,

    parent_id INTEGER REFERENCES review_comments(id) ON DELETE CASCADE,

    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    resolved_at TIMESTAMP,
    revision_number INTEGER NOT NULL DEFAULT 1,

    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- INDICES
-- ============================
CREATE INDEX IF NOT EXISTS idx_comments_review_id
    ON review_comments(review_id);

CREATE INDEX IF NOT EXISTS idx_comments_review_file
    ON review_comments(review_file_id);

CREATE INDEX IF NOT EXISTS idx_comments_parent_id
    ON review_comments(parent_id);

CREATE INDEX IF NOT EXISTS idx_comments_resolved
    ON review_comments(resolved);
