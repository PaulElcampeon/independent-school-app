# Independent School Data Management System

A Spring Boot REST API application for managing independent school data, including school information, exam results, fees, and student demographics. The application features JWT-based authentication and a web interface for managing school records.

## Table of Contents

- [Technologies](#technologies)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [Docker Deployment](#docker-deployment)

## Technologies

- **Java 21**
- **Spring Boot 3.5.5**
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database persistence
- **PostgreSQL** - Database
- **JWT (JSON Web Tokens)** - Token-based authentication
- **Lombok** - Reducing boilerplate code
- **Caffeine Cache** - Caching support
- **Maven** - Build tool
- **Docker** - Containerization

## Features

- User authentication and authorization with JWT
- CRUD operations for school data
- Advanced search and filtering capabilities
- Pagination support for large datasets
- Comprehensive school data management including:
  - Basic school information (name, address, contact details)
  - Gender profile and religious affiliation
  - Fee structures (boarding and day fees)
  - Student demographics
  - Exam results (GCSE, A-Level, IB, Scottish qualifications)
  - Scholarships information
- Web-based UI for school management
- RESTful API design

## Prerequisites

- Java 21 or higher
- Maven 3.6+ or use the included Maven wrapper (`mvnw`)
- PostgreSQL database
- Docker (optional, for containerized deployment)

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd independent-school-data
```

2. Build the project:
```bash
./mvnw clean install
```

Or using Maven directly:
```bash
mvn clean install
```

## Configuration

The application configuration is located in `src/main/resources/application.yml`.

### Database Configuration

Update the database connection details in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://your-database-host:5432/your-database-name
    username: your-username
    password: your-password
```

### JWT Configuration

JWT secret and expiration can be configured via environment variables or in `application.yml`:

```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: ${JWT_EXPIRATION:86400000}  # 24 hours in milliseconds
```

## Running the Application

### Using Maven

```bash
./mvnw spring-boot:run
```

Or:

```bash
mvn spring-boot:run
```

### Using the JAR file

After building:

```bash
java -jar target/independent-school-data-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080` by default.

## API Endpoints

### Authentication Endpoints

Base URL: `/api/auth`

#### Register User
- **POST** `/api/auth/register`
- **Description**: Register a new user
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response**: `200 OK` with `UserDto` object

#### Login
- **POST** `/api/auth/login`
- **Description**: Authenticate user and receive JWT token
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response**: `200 OK` with token information
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": "user-uuid"
  }
  ```

### School Management Endpoints

Base URL: `/api/v1/schools`

All school endpoints require JWT authentication (except where noted). Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

#### Create School
- **POST** `/api/v1/schools`
- **Description**: Create a new school record
- **Request Body**: `SchoolDto` object (JSON)
- **Response**: `200 OK` with created `SchoolDto`

#### Get All Schools
- **GET** `/api/v1/schools`
- **Description**: Retrieve all schools with pagination and sorting
- **Query Parameters**:
  - `page` (default: 0) - Page number
  - `size` (default: 10) - Number of items per page
  - `sortBy` (default: "name") - Field to sort by
  - `direction` (default: "asc") - Sort direction ("asc" or "desc")
- **Example**: `/api/v1/schools?page=0&size=20&sortBy=name&direction=asc`
- **Response**: `200 OK` with paginated `Page<SchoolDto>`

#### Get School by UUID
- **GET** `/api/v1/schools/{uuid}`
- **Description**: Retrieve a specific school by its UUID
- **Path Parameters**:
  - `uuid` - School UUID
- **Response**: `200 OK` with `Optional<SchoolDto>`

#### Update School
- **PUT** `/api/v1/schools/{uuid}`
- **Description**: Update an existing school record
- **Path Parameters**:
  - `uuid` - School UUID
- **Request Body**: `SchoolDto` object (JSON)
- **Response**: `200 OK` with updated `SchoolDto`

#### Delete School
- **DELETE** `/api/v1/schools/{uuid}`
- **Description**: Delete a school record
- **Path Parameters**:
  - `uuid` - School UUID
- **Response**: `200 OK` (no content)

#### Search Schools by Name
- **GET** `/api/v1/schools/search`
- **Description**: Search schools by name with pagination
- **Query Parameters**:
  - `name` (required) - School name to search for
  - `page` (default: 0) - Page number
  - `size` (default: 10) - Number of items per page
- **Example**: `/api/v1/schools/search?name=Westminster&page=0&size=10`
- **Response**: `200 OK` with paginated `Page<SchoolDto>`

#### Filter by Gender Profile
- **GET** `/api/v1/schools/filter/gender`
- **Description**: Filter schools by gender profile
- **Query Parameters**:
  - `gender` (required) - Gender profile (e.g., "Boys", "Girls", "Mixed")
  - `page` (default: 0) - Page number
  - `size` (default: 10) - Number of items per page
- **Example**: `/api/v1/schools/filter/gender?gender=Mixed&page=0&size=10`
- **Response**: `200 OK` with paginated `Page<SchoolDto>`

#### Filter by Religious Affiliation
- **GET** `/api/v1/schools/filter/religion`
- **Description**: Filter schools by religious affiliation
- **Query Parameters**:
  - `religion` (required) - Religious affiliation
  - `page` (default: 0) - Page number
  - `size` (default: 10) - Number of items per page
- **Example**: `/api/v1/schools/filter/religion?religion=Anglican&page=0&size=10`
- **Response**: `200 OK` with paginated `Page<SchoolDto>`

#### Advanced Filter
- **GET** `/api/v1/schools/filter`
- **Description**: Filter schools by multiple criteria (name, gender, religion)
- **Query Parameters**:
  - `name` (optional) - School name filter
  - `gender` (optional) - Gender profile filter
  - `religion` (optional) - Religious affiliation filter
  - `page` (default: 0) - Page number
  - `size` (default: 10) - Number of items per page
- **Example**: `/api/v1/schools/filter?name=Westminster&gender=Mixed&religion=Anglican&page=0&size=10`
- **Response**: `200 OK` with paginated `Page<SchoolDto>`

### School Data Model

The `SchoolDto` includes comprehensive information:

- **Basic Information**: name, address, postCode, regionName, areaCode, phone, email, website
- **Profile**: genderProfile, size, dayBoardingType, religiousAffiliation, politicalAffiliation
- **Administration**: headName, associations, partnershipsLink, iscRef, dfeRef, mpUrl
- **Descriptions**: description, profileUrl
- **Scholarships**: scholarships, scholarshipsDescription, scholarshipsUrl, scholarshipsAvailability
- **Location**: latitude, longitude
- **Fees**: boardingFees, boardingFeeFrom, boardingFeeTo, dayFees, dayFeeFrom, dayFeeTo, ageRangeToFee
- **Student Demographics**: 
  - Girls: girlsInfo, girlsDayAgeFrom, girlsDayAgeTo, girlsDayCount, girlsBoardingAgeFrom, girlsBoardingAgeTo, girlsBoardingCount, girlsSixthFormCount
  - Boys: boysInfo, boysDayAgeFrom, boysDayAgeTo, boysDayCount, boysBoardingAgeFrom, boysBoardingAgeTo, boysBoardingCount, boysSixthFormCount
- **Performance Metrics**: year13Candidates, percentageWithResultsAt9OrBetter, percentageWithResultsAt8OrBetter, etc.
- **Exam Results**: 
  - IGCSE: igcse9OrBetter, igcse8OrBetter, etc.
  - A-Level: alevelAstar, alevelA_Astar, alevelA_B, alevelA_C
  - IB: ibPoints
  - Scottish Qualifications: national5sA, highersA, advancedHighersA, etc.
- **Other**: averageHousePrice, featured, schoolType

## Project Structure

```
independent-school-data/
├── src/
│   ├── main/
│   │   ├── java/com/paulo/independentschooldata/
│   │   │   ├── config/          # Configuration classes (Security, JWT, Cache)
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── domain/          # Entity models
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exceptions/      # Exception handlers
│   │   │   ├── mappers/         # Entity-DTO mappers
│   │   │   ├── repos/           # JPA repositories
│   │   │   ├── service/         # Business logic
│   │   │   └── utils/           # Utility classes
│   │   └── resources/
│   │       ├── application.yml  # Application configuration
│   │       └── static/          # Static web resources (HTML, JS)
│   └── test/                    # Test files
├── Dockerfile                   # Docker configuration
├── pom.xml                      # Maven dependencies
└── README.md                    # This file
```

## Docker Deployment

The project includes a Dockerfile for containerized deployment.

### Build Docker Image

```bash
docker build -t independent-school-data .
```

### Run Docker Container

```bash
docker run -p 8080:8080 \
  -e JWT_SECRET=your-secret-key \
  -e JWT_EXPIRATION=86400000 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/your-db \
  -e SPRING_DATASOURCE_USERNAME=your-username \
  -e SPRING_DATASOURCE_PASSWORD=your-password \
  independent-school-data
```

The Dockerfile uses a multi-stage build:
1. **Builder stage**: Uses Maven to build the application
2. **Runtime stage**: Uses JRE to run the application

## Web Interface

The application includes a web interface accessible at `http://localhost:8080` that provides:
- User login functionality
- School listing with pagination
- Search and filter capabilities
- Create, update, and delete school records
- Comprehensive school data management forms

## Security

- JWT-based authentication
- Password encryption using Spring Security
- Protected endpoints require valid JWT token
- CORS configuration for cross-origin requests

## License

This project is a demo application for Spring Boot.

