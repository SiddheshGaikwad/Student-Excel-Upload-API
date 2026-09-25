# Student Excel Upload & Validation API

A Spring Boot REST API that uploads student data from an Excel `.xlsx` file, validates each row, and saves only valid records into MySQL.

## Technologies

* Java 21
* Spring Boot
* Spring Data JPA / Hibernate
* MySQL
* Apache POI
* Maven
* Postman

## API

**POST**

```text
/api/students/upload-excel
```

Upload Excel using Postman `form-data`:

```text
Key: file
Type: File
```

## Excel Columns

```text
student_name | email | mobile | course | city | fees
```

## Validation

* Student name: mandatory, minimum 3 characters
* Email: valid and unique
* Mobile: exactly 10 digits and unique
* Course: Java, Python, Testing, Data Analytics
* City: mandatory
* Fees: numeric and greater than 0

## Processing

Valid rows are inserted into the database.

Invalid rows are skipped and returned with row-wise validation errors.

Example:

```text
Total Rows: 5
Inserted: 3
Failed: 2
```

## Project Structure

```text
controller
entity
repository
service
```

## Author

**Siddhesh Gaikwad**
