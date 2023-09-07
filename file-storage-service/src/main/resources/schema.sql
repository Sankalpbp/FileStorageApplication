
CREATE DATABASE IF NOT EXISTS file_metadata;

USE file_metadata;

CREATE TABLE IF NOT EXISTS metadata (
    uniqueIdentifier BINARY(16) PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    createdAt TIMESTAMP NOT NULL,
    lastModifiedAt TIMESTAMP,
    size NUMERIC NOT NULL,
    fileType VARCHAR(255) NOT NULL,
    fileURL VARCHAR(255) NOT NULL
);
