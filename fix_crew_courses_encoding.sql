-- Fix character encoding for crew_courses table
-- This script converts the table and all text columns to UTF-8

ALTER TABLE crew_courses CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify the change
SHOW CREATE TABLE crew_courses;
