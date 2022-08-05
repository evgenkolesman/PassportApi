CREATE TABLE IF NOT EXISTS Person
(
    id              varchar(50) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    birthday        DATE         NOT NULL,
    birthdayCountry VARCHAR(2)   NOT NULL
);
CREATE TABLE IF NOT EXISTS Passport
(
    id             VARCHAR(50) PRIMARY KEY,
    number         VARCHAR(12) NOT NULL,
    givenDate      TIMESTAMP   NOT NULL,
    departmentCode VARCHAR(6)  NOT NULL,
    active         BOOLEAN     NOT NULL,
    description    TEXT,
    person_id      VARCHAR REFERENCES Person (id) ON DELETE CASCADE
);


