-- Fix passwords: update BCrypt hashes to match documented passwords

-- Admin password: admin123
UPDATE users
SET password = '$2a$10$mhLGVjDOwMN4GB9GBC4pMOVRxKFKeNXDphOeWBoC4SUSpOLMPFi8e'
WHERE role = 'ADMIN';

-- Student password: student123
UPDATE users
SET password = '$2a$10$Z/f.jUK2V5ATXFC13EJdEeBYyfk2tDQw3.3AQcx86BRt5rWoQ7KCi'
WHERE role = 'STUDENT';
