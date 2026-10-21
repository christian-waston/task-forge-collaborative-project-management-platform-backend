# TaskForge Collaborative Project Management Platform — Backend

Mid-term project examination — **INSY 8322 WEB TECHNOLOGY AND INTERNET**, AUCA.

- **Student:** Abumukiza Leon Christian
- **Student ID:** 26478
- **Project:** TaskForge Collaborative Project Management Platform (backend)

---

## Requirements

- Java 26 (JDK)
- Maven
- PostgreSQL running on `localhost:5432`

---

## Steps to Run

1. Create the database:

   ```sql
   CREATE DATABASE taskforge_midterm;
   ```

2. Adjust `src/main/resources/application.properties` if your PostgreSQL credentials differ:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/taskforge_midterm
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

3. Run the application:

   ```bash
   ./mvnw spring-boot:run
   ```

   The API starts on `http://localhost:8082`.

---

## Testing

Import `task-forge-collaborative-project-management-platform-midterm.postman_collection.json` from the root of this repository into Postman. Every request is named by its requirement ID.
