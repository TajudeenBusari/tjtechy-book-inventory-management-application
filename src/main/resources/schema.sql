CREATE TABLE IF NOT EXISTS users (
                                     user_id SERIAL PRIMARY KEY,
                                     user_name VARCHAR(255) NOT NULL,
                                     password VARCHAR(255) NOT NULL,
                                     enabled BOOLEAN NOT NULL,
                                     roles VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS author(
                                      author_id SERIAL PRIMARY KEY,
                                      first_name VARCHAR(255),
                                      last_name VARCHAR(255),
                                      email VARCHAR(255) UNIQUE,
                                      biography TEXT
);

CREATE TABLE IF NOT EXISTS books (
                                     book_id SERIAL PRIMARY KEY,
                                     isbn UUID UNIQUE,
                                     title VARCHAR(255),
                                     publisher VARCHAR(255),
                                     publication_date TIMESTAMP,
                                     genre VARCHAR(255),
                                     edition VARCHAR(255),
                                     language VARCHAR(255),
                                     pages INT,
                                     description TEXT,
                                     price DECIMAL(10, 2),
                                     quantity VARCHAR(255),
                                     owner_author_id BIGINT, -- Foreign key to authors table
                                     CONSTRAINT fk_author FOREIGN KEY (owner_author_id) REFERENCES author(author_id)
);