# Hobbie Backend

> A comprehensive Spring Boot application for hobby discovery and management, connecting hobby enthusiasts with local businesses offering recreational activities.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue.svg)](https://www.postgresql.org/)
[![AWS S3](https://img.shields.io/badge/AWS-S3-orange.svg)](https://aws.amazon.com/s3/)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Security](#security)
- [Testing](#testing)

---

##  Overview

**Hobbie Backend** is a robust REST API that powers a hobby discovery platform. It enables users to find and save hobbies based on personalized recommendations, while allowing businesses to showcase their recreational offerings. The platform uses an intelligent matching algorithm that connects users with activities based on their preferences, location, and interests.

### Key Highlights

- **Smart Matching**: Personalized hobby recommendations based on user preferences
- **Dual User System**: Separate flows for regular users and business owners
- **Cloud Storage**: AWS S3 integration for efficient media management
- **OAuth2 Support**: Google authentication for seamless user onboarding
- **Comprehensive Security**: JWT-based authentication with role-based access control

---

##  Features

### For Regular Users
-  **Secure Authentication**: Register/login with credentials or Google OAuth2
-  **Personality Quiz**: Take a comprehensive test to discover matching hobbies
-  **Smart Recommendations**: Get personalized hobby suggestions based on preferences and location
- ️**Favorites Management**: Save and manage favorite hobbies
-  **Profile Management**: Update personal information and preferences
-  **Email Notifications**: Password reset and account management emails

### For Business Users
-  **Business Registration**: Create and manage business profiles
-  **Hobby Listings**: Create, update, and delete hobby offerings
-  **Media Management**: Upload profile and gallery images to AWS S3
-  **Portfolio View**: Manage all business hobby offerings in one place
-  **Pricing Control**: Set and update pricing for services

### Admin Features
-  **User Management**: Full CRUD operations on user accounts
-  **System Oversight**: Monitor platform usage and activities

---

## Technology Stack

### Core Framework
- **Spring Boot 3.5.6**: Main application framework
- **Java 17**: Programming language
- **Maven**: Dependency management and build tool

### Security & Authentication
- **Spring Security**: Security framework
- **JWT (JSON Web Tokens)**: Stateless authentication
- **OAuth2**: Google social login integration
- **BCrypt**: Password encryption

### Database & Persistence
- **PostgreSQL**: Primary database
- **Spring Data JPA**: Data access layer
- **Hibernate**: ORM framework

### Cloud & Storage
- **AWS S3**: Cloud storage for images
- **AWS SDK**: S3 client integration

### Documentation & API
- **SpringDoc OpenAPI 3**: API documentation
- **Swagger UI**: Interactive API explorer

### Utilities
- **Lombok**: Reduce boilerplate code
- **ModelMapper**: Object mapping
- **JavaMailSender**: Email functionality

---

##  Architecture

```
hobbiebackend/
├── config/                 # Configuration classes
│   ├── AwsS3Config        # AWS S3 setup
│   ├── SecurityConfiguration # Spring Security config
│   └── OpenApi30Config    # Swagger/OpenAPI config
├── filter/                # Request filters
│   └── JwtFilter          # JWT authentication filter
├── handler/               # Exception handlers
│   ├── GlobalAdvice       # Global exception handling
│   └── Custom exceptions
├── model/
│   ├── dto/               # Data Transfer Objects
│   ├── entities/          # JPA entities
│   ├── enums/             # Enumeration types
│   └── repository/        # Spring Data repositories
├── security/              # Security components
│   ├── HobbieUserDetailsService
│   └── OAuth2LoginSuccessHandler
├── service/               # Business logic layer
│   ├── impl/              # Service implementations
│   └── interfaces/        # Service contracts
├── utility/               # Utility classes
│   └── JWTUtility         # JWT token operations
└── web/                   # REST controllers
    ├── UserController
    ├── HobbyController
    ├── TestController
    └── HomeController
```

### Design Patterns
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic separation
- **DTO Pattern**: Data transfer optimization
- **Filter Chain Pattern**: Request processing
- **Factory Pattern**: Bean creation and configuration

---

##  Getting Started

### Prerequisites

```bash
# Required
Java 17 or higher
PostgreSQL 12 or higher
Maven 3.6+

# Optional (for full features)
AWS Account (for S3 storage)
Google OAuth2 credentials
SMTP server (for emails)
```

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/Nikkk28/Hobbie-backend-using-Spring-Boot.git
cd hobbiebackend
```

2. **Set up PostgreSQL database**
```sql
CREATE DATABASE hobbie_db;
CREATE USER hobbie_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE hobbie_db TO hobbie_user;
```

3. **Configure environment variables**

Create a `.env` file or set system environment variables:

```properties
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hobbie_db
SPRING_DATASOURCE_USERNAME=hobbie_user
SPRING_DATASOURCE_PASSWORD=your_password

# AWS S3
AWS_S3_BUCKET_NAME=your-bucket-name
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key

# OAuth2 (Google)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

4. **Build the project**
```bash
mvn clean install
```

5. **Run the application**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Initial Setup

On first run, the application automatically:
- Creates database tables
- Seeds initial user roles (ADMIN, USER, BUSINESS_USER)
- Creates default test users
- Initializes hobby categories
- Sets up location data

**Default Test Accounts:**
```
Regular User:
Username: user
Password: topsecret

Business User:
Username: business
Password: topsecret
```

---

## API Documentation

### Accessing Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Key Endpoints

#### Authentication
```http
POST /authenticate          # Login and get JWT token
POST /signup                # Register new user
POST /register              # Register new business
POST /login                 # Get user role
```

#### Hobbies
```http
GET    /hobbies/{id}        # Get hobby details
POST   /hobbies             # Create hobby (Business only)
PUT    /hobbies/{id}        # Update hobby (Business only)
DELETE /hobbies/{id}        # Delete hobby (Business only)
POST   /hobbies/save        # Save hobby to favorites
DELETE /hobbies/remove      # Remove from favorites
GET    /hobbies/saved       # Get saved hobbies
GET    /hobbies/is-saved    # Check if hobby is saved
```

#### User Management
```http
GET    /client              # Get user profile
PUT    /user                # Update user profile
GET    /business            # Get business profile
PUT    /business            # Update business profile
DELETE /user/{id}           # Delete user account
POST   /notification        # Send password reset email
PUT    /password            # Update password
```

#### Test & Matching
```http
POST /test                  # Submit personality quiz results
GET  /home                  # Get personalized recommendations
```

### Authentication Flow

1. **Login/Register**: Call `/authenticate` or `/signup`
2. **Receive JWT**: Get token in response
3. **Use Token**: Include in header: `Authorization: Bearer <token>`
4. **Access Protected Routes**: All authenticated endpoints require valid JWT

---

## ⚙️ Configuration

### Application Properties

Key configurations in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# File Upload
file.max-size=10485760
file.allowed-types=image/jpeg,image/png,image/gif

# CORS
# Configured for Angular frontend on localhost:4200
```

### Profile-Specific Configuration

For different environments, create:
- `application-dev.properties`
- `application-prod.properties`
- `application-test.properties`

Run with: `mvn spring-boot:run -Dspring.profiles.active=dev`

---

## Security

### Authentication Mechanisms

1. **JWT Tokens**
    - Stateless authentication
    - 5-hour token validity
    - HS512 signature algorithm

2. **OAuth2 (Google)**
    - Social login integration
    - Automatic user creation
    - Secure token exchange

### Authorization

Role-based access control (RBAC):

| Role | Permissions |
|------|-------------|
| **USER** | View hobbies, save favorites, take quiz, update profile |
| **BUSINESS_USER** | All USER permissions + Create/update/delete hobbies |
| **ADMIN** | Full system access, user management |

### Security Features

- Password encryption with BCrypt (strength: 12)
-  CSRF protection disabled for REST API
-  CORS configuration for trusted origins
-  SQL injection prevention via JPA
-  XSS protection through proper encoding
-  Secure password reset flow
-  Rate limiting on authentication endpoints

---

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn clean test jacoco:report
```

### Test Structure

```
src/test/java/
├── service/
│   ├── impl/
│   │   ├── UserServiceTest
│   │   ├── HobbyServiceImplTest
│   │   ├── CategoryServiceTest
│   │   └── LocationServiceImplTest
└── web/
    ├── UserControllerTest
    ├── HobbyControllerTest
    └── TestControllerTest
```

### Test Coverage

The project includes:
- Unit tests for service layer
- Integration tests for controllers
- Mock-based testing with Mockito
- Spring Boot test utilities

---

## Deployment

### Docker Support (Coming Soon)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/hobbiebackend-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Production Checklist

- [ ] Update JWT secret key
- [ ] Configure production database
- [ ] Set up AWS S3 bucket
- [ ] Enable HTTPS
- [ ] Configure proper CORS origins
- [ ] Set up monitoring and logging
- [ ] Configure backup strategy
- [ ] Review security settings
- [ ] Set up CI/CD pipeline

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Use Lombok for reducing boilerplate
- Write meaningful commit messages
- Add unit tests for new features
- Update documentation as needed