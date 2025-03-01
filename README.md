# 🚗 Rent A CAR API

![Project Status](https://img.shields.io/badge/status-active-brightgreen)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Java](https://img.shields.io/badge/Java-23-007396?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?logo=spring&logoColor=white)

## 📋 Table of Contents
* [👋 Introduction](#introduction)
* [💻 Technologies](#technologies)
* [✨ Features](#features)
* [🏗️ Project Structure](#project-structure)
* [🛠️ Development Process](#development-process)
* [🎓 Key Learnings](#key-learnings)
* [🚀 Best Practices Implemented](#best-practices-implemented)
* [📈 Areas for Improvement](#areas-for-improvement)

## 👋 Introduction
RentACar is a robust Spring Boot application for managing a car rental service. It provides a comprehensive API for handling accounts, vehicles, and rental operations with proper validation and business rule enforcement. The system supports the complete lifecycle of vehicle rental operations, from account creation to vehicle maintenance scheduling.

## 💻 Technologies
* Java 23
* Spring Boot 3.4.3
* Spring Data JPA
* MySQL
* Hibernate Validator
* Lombok
* MapStruct
* SpringDoc OpenAPI
* Notion (Project Planning)
* APIdog (API Testing)
* Postman (API Testing)
* DBeaver (Database Management)
* Docker (Coming Soon)

## ✨ Features
* **Account Management**: Create, update, activate/deactivate, and delete user accounts
* **Vehicle Management**: Track vehicle information, status, and maintenance
* **Rental Operations**: Create rentals, track active rentals, and process returns
* **Status Tracking**: Monitor vehicle and rental status through their lifecycle
* **Validation**: Comprehensive input validation with custom exceptions
* **Aspect-Oriented Logging**: Detailed logging of operations through AOP
* **Exception Handling**: Global exception handling with custom exceptions for business rules

## 🏗️ Project Structure
The project follows a clean, layered architecture:

* **Controller Layer**: REST endpoints for client interaction
* **Service Layer**: Business logic and validation
* **Repository Layer**: Data access and persistence
* **Entity Layer**: JPA entities representing database schema
* **DTO Layer**: Data transfer objects for API requests/responses
* **Mapper Layer**: Conversion between DTOs and entities
* **Exception Layer**: Custom exceptions for domain-specific error handling
* **Aspect Layer**: Cross-cutting concerns like logging

## 🛠️ Development Process
1. **Initial Setup**: Spring Boot project configuration with dependencies
2. **Database Design**: Entity modeling and relationship mapping
3. **API Design**: REST endpoint design and implementation
4. **Business Logic**: Service layer implementation with validation
5. **Testing**: Unit testing with JUnit and Mockito
6. **Documentation**: API documentation with SpringDoc OpenAPI
7. **Refactoring**: Code improvement for better maintainability
8. **Edge Cases**: Handling special cases and error scenarios

## 🎓 Key Learnings
* **Spring Boot Architecture**: Clean layering of controllers, services, and repositories
* **DTO Pattern**: Separating API contracts from internal entities
* **Exception Handling**: Custom exception hierarchy for business rules
* **MapStruct**: Efficient entity-to-DTO mapping
* **Aspect-Oriented Programming**: Cross-cutting concerns with Spring AOP
* **Validation**: JSR-380 validation combined with custom business validations
* **REST Best Practices**: Resource-based API design
* **State Management**: Tracking and transitioning entity states
* **Testing**: Mocking dependencies for effective unit testing

## 🚀 Best Practices Implemented
* **Separation of Concerns**: Clear boundaries between layers
* **DRY (Don't Repeat Yourself)**: Reusable validation and business logic
* **Input Validation**: Multiple layers of validation (annotation-based and business rule)
* **Exception Handling**: Detailed, client-friendly error messages
* **Logging**: Comprehensive logging of operations and errors
* **Design Patterns**: DTO, Repository, and Service patterns
* **Code Organization**: Logical package structure and naming conventions
* **Testing**: Unit tests for service layer logic

## 📈 Areas for Improvement
* **Name Validation**: Add regex validation for proper name formatting
* **Input Sanitization**: Prevent injection attacks with input sanitization
* **Duplicate Handling**: Check for duplicate phone numbers
* **Date Validation**: Enhance rental date validation
* **Kilometer Validation**: Improve validation for vehicle usage tracking
* **Vehicle State Machine**: Formalize status transitions
* **Transactional Integrity**: Ensure atomic operations
* **Reporting Features**: Implement usage statistics and reporting
* **Account Rental Limits**: Implement maximum active rentals per account
* **Payment Processing**: Add payment management functionality

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License
This project is open-source. Please check the license file for details.
