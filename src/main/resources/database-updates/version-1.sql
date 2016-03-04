CREATE TABLE players (
  id            INT         NOT NULL PRIMARY KEY AUTO_INCREMENT,
  uuid          BINARY(16)  NOT NULL UNIQUE,
  last_username VARCHAR(16) NOT NULL UNIQUE
);

CREATE TABLE islands (
  id      INT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
  owner   INT       NOT NULL,
  created TIMESTAMP NOT NULL             DEFAULT CURRENT_TIMESTAMP(),
  pos_x   INT       NOT NULL,
  pos_z   INT       NOT NULL,
  FOREIGN KEY (owner) REFERENCES players (id) ON DELETE CASCADE
);