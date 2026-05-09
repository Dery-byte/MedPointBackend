# MedPoint Business Suite Backend

MedPoint Backend is a robust Spring Boot REST API designed to power the MedPoint Business Suite. It provides a comprehensive set of services for managing diverse business operations, including Marts, Hotels, Restaurants, Drugstores, and more.

## 🚀 Features

- **Multi-Module Support**: specialized controllers for Marts, Hotels, Restaurants, and Drugstores.
- **Secure Authentication**: JWT-based security with Spring Security.
- **Payment Integration**: Support for multiple payment gateways including **Paystack** and **Moolre**.
- **Role-Based Access Control**: Different access levels for Admins, Staff, and Customers.
- **Dynamic Configuration**: Store settings and payment gateway selection can be updated via the API.
- **Advanced Media Handling**: Image optimization and WebP support for efficient storage.
- **Comprehensive Reporting**: Excel-based report generation for business analytics.
- **Messaging Services**: Integrated SMS (MNotify) and Email (SMTP) notifications.

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.4
- **Language**: Java 21
- **Security**: Spring Security & JWT (JJWT)
- **Database**: MySQL
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Deployment**: Docker
- **Utilities**: Lombok, Apache POI, Thumbnailator

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
- **JDK 21** or higher
- **Maven 3.8+**
- **MySQL 8.0+**
- **Docker** (optional, for containerized deployment)

## ⚙️ Configuration

The application uses Spring profiles (`dev`, `prod`). Configuration is primarily handled via `src/main/resources/application-*.yml`.

### Key Environment Variables
Ensure the following variables are set in your environment or specified in your properties file:

| Variable | Description |
|----------|-------------|
| `PAYSTACK_SECRET_KEY` | Your Paystack secret API key |
| `PAYSTACK_PUBLIC_KEY` | Your Paystack public API key |
| `DB_PASSWORD` | MySQL database password |
| `JWT_SECRET` | Secret key for JWT signing |

## 🏃 Running Locally

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd medpoint-backend
   ```

2. **Configure the database**:
   - Create a MySQL database named `medpointdb`.
   - Update `src/main/resources/application-dev.yml` with your database credentials.

3. **Build the project**:
   ```bash
   mvn clean install
   ```

4. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
   The API will be available at `http://localhost:8080/api`.

## 🐳 Running with Docker

1. **Build the Docker image**:
   ```bash
   docker build -t medpoint-backend .
   ```

2. **Run the container**:
   ```bash
   docker run -p 8080:8080 medpoint-backend
   ```

## 🛣️ API Documentation

The API follows standard RESTful principles. Major endpoint categories include:

- `/auth`: Authentication and registration.
- `/admin`: Administrative management.
- `/payments`: Payment initiation and verification.
- `/drugstore`, `/hotel`, `/mart`, `/restaurant`: Business-specific modules.
- `/orders`: Transaction and order processing.
- `/webhooks`: Third-party integration callbacks.

## 📁 Project Structure

```text
src/main/java/com/medpoint/
├── config/       # Configuration classes (Security, Web, etc.)
├── controller/   # REST API endpoints
├── dto/          # Data Transfer Objects
├── entity/       # JPA Entities
├── repository/   # Data Access Layer
├── service/      # Business Logic
├── security/     # Security filters and JWT logic
└── webhook/      # External integration handlers
```

## 📄 License

This project is proprietary and confidential.

---
*Built with ❤️ by the MedPoint Team*
