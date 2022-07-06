CREATE TABLE IF NOT EXISTS passportapi1.public.Person(
                                                         id varchar(50) PRIMARY KEY,
                                                         name VARCHAR(255) NOT NULL,
                                                         birthday DATE NOT NULL,
                                                         birthdayCountry VARCHAR(5) NOT NULL
);
CREATE TABLE IF NOT EXISTS passportapi1.public.Passport(
                                                           id VARCHAR(50) PRIMARY KEY,
                                                           number VARCHAR(12) NOT NULL ,
                                                           givenDate DATE NOT NULL,
                                                           departmentCode VARCHAR(5) NOT NULL,
                                                           active BOOLEAN NOT NULL,
                                                           description TEXT,
                                                           person_id VARCHAR REFERENCES passportapi1.public.Person(id)
);

