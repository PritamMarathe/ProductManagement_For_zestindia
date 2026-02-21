# ProductManagement_For_zestindia
This demo of zestindia Product Management
# Product Management REST API

A secure, production-ready RESTful API built with **Spring Boot 3** for managing Products and associated Items (one-to-many relationship). This project implements full **CRUD** operations with JWT authentication, role-based authorization, pagination, input validation, standardized error handling, Swagger documentation, and Docker support — as per the Zest India IT Pvt Ltd hiring assignment.

## Features Implemented

- Full **CRUD** operations on Products and Items
- **JWT Authentication** + Refresh Token rotation
- **Role-based authorization** (ADMIN: full access, USER: read-only)
- **API Versioning** (`/api/v1/`)
- **Pagination & Sorting** support on list endpoint
- **Input validation** with Jakarta Bean Validation
- **Standardized error responses** (400, 401, 403, 404, etc.)
- **Swagger / OpenAPI** documentation
- **Unit tests** (JUnit 5 + Mockito) — services
- **Integration tests** (Spring Boot Test + H2) — controllers & auth flow
- **Docker** + **docker-compose** for easy deployment
- Clean layered architecture (Controller → Service → Repository → Entity)

## Technical Stack

- **Java**: 17
- **Framework**: Spring Boot 3.3.1
- **Database**: Microsoft SQL Server (production) + H2 (tests)
- **ORM**: Spring Data JPA + Hibernate
- **Security**: Spring Security + JWT (jjwt) + Refresh Token
- **Validation**: Jakarta Bean Validation
- **Documentation**: Springdoc OpenAPI / Swagger UI
- **Testing**: JUnit 5, Mockito, Spring Boot Test, Spring Security Test
- **Containerization**: Docker + Docker Compose
- **Build Tool**: Maven

## Project Architecture (Clean / Layered)
