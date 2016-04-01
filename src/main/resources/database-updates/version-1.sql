CREATE TABLE player (
  id             INT         NOT NULL PRIMARY KEY AUTO_INCREMENT,
  uuid           BINARY(16)  NOT NULL UNIQUE,
  last_username  VARCHAR(16) NOT NULL UNIQUE,
  default_island INT
);

CREATE TABLE island (
  id       INT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
  owner    INT       NOT NULL,
  created  TIMESTAMP NOT NULL             DEFAULT CURRENT_TIMESTAMP(),
  pos_x    INT       NOT NULL,
  pos_z    INT       NOT NULL,
  offset_x DOUBLE    NOT NULL             DEFAULT 0.0,
  offset_y DOUBLE    NOT NULL             DEFAULT 0.0,
  offset_z DOUBLE    NOT NULL             DEFAULT 0.0,
  FOREIGN KEY (owner) REFERENCES player (id) ON UPDATE CASCADE
);

// Can't create a foreign key referencing a table which doesn't exist yet...
ALTER TABLE player ADD FOREIGN KEY (default_island) REFERENCES island (id);