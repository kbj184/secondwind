-- Add join_type column to crews table
ALTER TABLE crews 
ADD COLUMN join_type VARCHAR(20) NOT NULL DEFAULT 'AUTO' 
COMMENT 'Join type: AUTO or APPROVAL';

-- Add status column to crew_members table
ALTER TABLE crew_members 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' 
COMMENT 'Member status: PENDING, APPROVED, REJECTED';

-- Update existing crews to have AUTO join type
UPDATE crews SET join_type = 'AUTO' WHERE join_type IS NULL;

-- Update existing members to have APPROVED status
UPDATE crew_members SET status = 'APPROVED' WHERE status IS NULL;
