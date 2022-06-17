1) REST API

Describe OpenAPI for passport office (a person has several passports of different types)

people management (name, birthday, birth country)

passport management (number, given date, given department code)

lost passport handling, a person has lost his passport and wants to get a new one

Get current active passports of a person

Search a person by passport number

Search passports by given dates range

Request Validation

2) Implement API for passport office using Java Collections as repositories. 
Use Bean Validation to validate requests and return corresponding HTTP error codes and messages


3) REST API tests

Implement API contract tests for passport office using Rest-Assured https://rest-assured.io

Validate both successful and erroneous scenarios.

4) SQL

Implement passport office using Postgres instead of Java Collections.

Use Flyway for SQL schema migrations

Use Spring JdbcTemplate for database operations

Use Postgres in Docker for tests https://www.testcontainers.org  