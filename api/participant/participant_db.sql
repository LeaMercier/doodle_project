CREATE USER tlc WITH PASSWORD 'tlc' CREATEDB;
CREATE DATABASE tlc_user
    WITH
    OWNER = tlc
    ENCODING = 'utf8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default;
    CONNECTION LIMIT = -1;
