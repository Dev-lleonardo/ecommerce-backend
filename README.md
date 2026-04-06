# 🛒 E-commerce Backend API

**Status:** 🚧 Work In Progress (WIP)

A robust e-commerce REST API built with **Java 21** and **Spring Boot 3.2.5**, designed with a focus on security, scalability, and clean code principles.

## 🚀 Technologies & Tools
* **Language:** Java 21
* **Framework:** Spring Boot 3.2.5
* **Security:** Stateless Authentication with JWT (io.jsonwebtoken 0.12.6) and BCrypt
* **Database:** MySQL
* **Documentation:** Interactive API docs with Swagger/OpenAPI 3
* **Architecture:** Layered pattern (Controller-Service-Repository)

## 🛠️ Current Features
* **User Management:** Registration and authentication with Role-Based Access Control (RBAC).
* **Data Initialization:** Automatic setup for Roles (USER/ADMIN) and a Default Admin user upon startup.
* **Security:** Protected endpoints requiring Bearer Token validation.

## 🔧 How to Run
1. Clone the repository.
2. Create a `.env` file based on the `.env.example` provided.
3. Run the application via your IDE or `./mvnw spring-boot:run`.
4. Access the interactive documentation at: `http://localhost:8080/swagger-ui.html`.