# 🪙 BEKAL - Backend API

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![SQL Server](https://img.shields.io/badge/Database-MSSQL%202022-red.svg?logo=microsoftsqlserver)](https://www.microsoft.com/sql-server)
[![Redis](https://img.shields.io/badge/Cache-Redis%207-critical.svg?logo=redis)](https://redis.io/)
[![Scalar](https://img.shields.io/badge/API%20Docs-Scalar-blue.svg?logo=openapiinitiative)](http://localhost:8080/scalar)
[![JaCoCo](https://img.shields.io/badge/Coverage-80%25%2B-success.svg)](https://www.jacoco.org/jacoco/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg?logo=docker)](https://www.docker.com/)

**BEKAL** (BCA Finance Loan & Financing Application) is an enterprise-grade backend service built to orchestrate end-to-end multi-finance loan origination, credit scoring, multi-tier approvals, fund disbursement, and installment repayment workflows.

---

## 📑 Table of Contents

- [Architectural Overview](#-architectural-overview)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Role-Based Access Control (RBAC)](#-role-based-access-control-rbac)
- [Interactive API Documentation (Scalar)](#-interactive-api-documentation-scalar)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Setup](#environment-setup)
  - [Running with Docker Compose](#running-with-docker-compose-recommended)
  - [Running Locally](#running-locally)
- [API Endpoints Overview](#-api-endpoints-overview)
- [Testing & Quality Assurance](#-testing--quality-assurance)
- [CI/CD & Deployment](#-cicd--deployment)
- [License](#-license)

---

## 🏛 Architectural Overview

```
                      ┌────────────────────────────────────────┐
                      │    Client (Angular Web / Mobile App)   │
                      └───────────────────┬────────────────────┘
                                          │  HTTPS / REST / JWT
                                          ▼
                               ┌──────────────────────┐
                               │  Spring Security &   │
                               │  JWT Authentication  │
                               └──────────┬───────────┘
                                          │
        ┌─────────────────────────────────┼────────────────────────────────┐
        ▼                                 ▼                                ▼
┌─────────────────┐             ┌───────────────────┐            ┌───────────────────┐
│ Master & Auth   │             │ Loan Origination  │            │ Plafond & Payment │
│ - Users / Roles │             │ - Applications    │            │ - Credit Limits   │
│ - Branches      │             │ - Credit Scoring  │            │ - Installments    │
│ - Bank Accounts │             │ - BM Approval     │            │ - Loan Balances   │
│ - Dynamic Menus │             │ - Disbursement    │            │ - FCM Push Alerts │
└────────┬────────┘             └─────────┬─────────┘            └─────────┬─────────┘
         │                                │                                │
         └────────────────────────────────┼────────────────────────────────┘
                                          ▼
                      ┌───────────────────────────────────────┐
                      │      Data & Integration Tier         │
                      │  ┌───────────────┐ ┌───────────────┐  │
                      │  │ MSSQL Server  │ │ Redis Cache   │  │
                      │  └───────────────┘ └───────────────┘  │
                      │  ┌───────────────┐ ┌───────────────┐  │
                      │  │ Firebase FCM  │ │ SMTP Mailer   │  │
                      │  └───────────────┘ └───────────────┘  │
                      └───────────────────────────────────────┘
```

---

## ✨ Key Features

### 1. 🔐 Authentication & Identity Management
- **Customer Authentication**: Secure registration, email/password login, JWT access & refresh tokens.
- **Internal Employee Authentication**: Dedicated portal authentication for Branch Managers, Back Office, and Marketing staff.
- **Password Recovery & OTP**: 6-digit OTP verification delivered via SMTP email.
- **Social Login**: Google OAuth 2.0 token exchange.

### 2. 👥 Customer & KYC Management
- Customer profile registration and data validation (NIK, email, phone number).
- Document management (KTP, KK, NPWP, salary slip uploads) with file type detection and local/cloud storage.
- Employment background tracking (company details, position, monthly income).

### 3. 📝 Complete Loan Origination Lifecycle
- **Application Submission**: Flexible vehicle/multipurpose loan requests with customizable tenors and financing amounts.
- **Marketing / Credit Analyst Review**: Credit assessment, survey verification scoring, and internal recommendations.
- **Branch Manager Approval**: Multi-tier approval pipeline (approve or reject with audit trail).
- **Back Office Disbursement**: Automated loan activation and bank transfer recording.

### 4. 💳 Credit Plafond & Repayments
- Dynamic customer plafond (credit limit) calculation based on risk tier and active obligations.
- Real-time installment payments, balance tracking, payment schedules, and settlement history.

### 5. 🏢 Administration & Master Data
- Hierarchical role management and dynamic menu trees delivered securely per role.
- Internal staff management and branch office maintenance.
- Real-time Admin Dashboard KPIs, disbursement stats, and activity feeds.
- Firebase Cloud Messaging (FCM) integration for real-time mobile push notifications.

---

## 🛠 Technology Stack

| Category | Technologies |
| :--- | :--- |
| **Language & Runtime** | Java 21 LTS |
| **Core Framework** | Spring Boot 4.x / Spring Framework 7.x |
| **Security** | Spring Security, JJWT (io.jsonwebtoken 0.12.6) |
| **Persistence & ORM** | Spring Data JPA, Hibernate, Microsoft SQL Server JDBC |
| **Caching & Session** | Spring Data Redis, Redis 7 Alpine |
| **API Documentation** | Scalar UI, Springdoc OpenAPI 3.1.1 |
| **Push Notifications** | Google Firebase Admin SDK (v9.4.1) |
| **Email Services** | Spring Boot Starter Mail (SMTP) |
| **Build & Quality** | Apache Maven, JaCoCo Code Coverage (80% minimum target) |
| **Containerization** | Docker, Docker Compose |

---

## 🛡 Role-Based Access Control (RBAC)

The system enforces hierarchical permissions using Spring Security Method Security:

```
SUPER_ADMIN
  └── BRANCH_MANAGER
        └── MARKETING
  └── BACK_OFFICE
```

- **`CUSTOMER`**: Access to personal profile, plafond inquiries, loan applications, payment execution, and document uploads.
- **`MARKETING`**: Access to loan application queues, credit review scoring, survey data entry, and review history.
- **`BRANCH_MANAGER`**: Access to marketing-reviewed applications, final approval/rejection authority, and approval history.
- **`BACK_OFFICE`**: Access to approved loans ready for disbursement, payment transaction tracking, and execution history.
- **`SUPER_ADMIN`**: Full platform authority, internal employee provisioning, master data, branch configurations, and dynamic role-menu permission matrix.

---

## 📖 Interactive API Documentation (Scalar)

The project integrates **Scalar** for an interactive, modern API documentation and testing experience.

* **Scalar Interactive UI**: `http://localhost:8080/scalar`
* **OpenAPI Raw Specification (JSON)**: `http://localhost:8080/v3/api-docs`

### How to Authenticate in Scalar
1. Authenticate via `POST /api/v1/auth/login` (Customer) or `POST /api/v1/auth/login-employee` (Staff).
2. Copy the `token` string from the JSON response.
3. Click the **Authorize / Bearer** button in the top navigation of Scalar.
4. Paste the token into the `BearerAuth` input field. All subsequent requests in Scalar will automatically carry the `Authorization: Bearer <token>` header.

---

## 🚀 Getting Started

### Prerequisites
- [JDK 21](https://adoptium.net/) or higher
- [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/)
- [Maven 3.9+](https://maven.apache.org/) (or use the included `./mvnw` wrapper)

### Environment Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/Edwin-Jaya/bekal-backend.git
   cd bekal-backend
   ```

2. Copy the environment configuration template:
   ```bash
   cp .env.example .env
   ```

3. Update the variables in `.env` to match your environment:
   ```properties
   # Database
   DB_URL=jdbc:sqlserver://localhost:1433;databaseName=bekal;encrypt=true;trustServerCertificate=true
   DB_USERNAME=sa
   DB_PASSWORD=YourStrongDatabasePassword!

   # Redis
   REDIS_HOST=localhost
   REDIS_PORT=6379

   # JWT Security
   JWT_SECRET=your-secure-jwt-secret-key-at-least-256-bits-long
   JWT_TTL_MINUTES=180m

   # Mail (SMTP)
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   ```

---

### Running with Docker Compose (Recommended)

Start the entire infrastructure stack (SQL Server 2022, database auto-initialization, Redis, and Spring Boot application) with a single command:

```bash
docker compose up -d
```

To view logs:
```bash
docker compose logs -f app
```

To stop containers:
```bash
docker compose down
```

---

### Running Locally

If running the application directly from your IDE or CLI:

1. **Start database and cache containers:**
   ```bash
   docker compose up -d sqlserver redis sqlserver-init
   ```

2. **Build and run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Verify running application:**
   - Health / Base: `http://localhost:8080`
   - Scalar Docs: `http://localhost:8080/scalar`

---

## 📌 API Endpoints Overview

| Module | Base Path | Key Capabilities |
| :--- | :--- | :--- |
| **Auth** | `/api/v1/auth` | Login, Employee Login, Refresh Token, Logout, Google OAuth, Password Reset OTP |
| **Customer** | `/api/v1/customers` | Register, Check Account, Profile (`/me`), Customer List, Profile Updates |
| **Documents** | `/api/v1/document` | Multipart KYC Upload (KTP, NPWP, KK), Document Management |
| **Employment**| `/api/v1/employment` | Customer Employment Info & Income Verification |
| **Bank Accounts** | `/api/v1/bank-accounts` | Customer Bank Accounts for Disbursement & Repayment |
| **Branches** | `/api/v1/branches` | Branch Offices, Operating Cities, Branch Status |
| **Internal Users**| `/api/v1/internal-user` | Staff Account Creation, Role Assignment, Status Toggling |
| **Roles & Menus** | `/api/v1/role`, `/api/v1/menu` | Role Definitions, Dynamic Menu Trees, Navigation Permissions |
| **Role Matrix** | `/api/v1/role-menu-access` | Granular Role-to-Menu Permission Matrix Synchronization |
| **User Profile** | `/api/v1/me` | Current Authenticated Employee Context & Permitted Menus |
| **Loans** | `/api/v1/loan-applications` | Submit Loan, Customer Application History, Queue Monitoring |
| **Review** | `/api/v1/loan-reviews` | Credit Scoring, Survey Feedback, Review History |
| **Approval** | `/api/v1/loan-approval` | BM Decision Submission (Approve / Reject), Approval History |
| **Disbursement**| `/api/v1/loan-disbursement`| Fund Transfer Execution, Disbursement History |
| **Plafond** | `/api/v1/plafonds` | Real-time Customer Credit Plafond & Available Balance |
| **Payments** | `/api/v1/payments` | Installment Repayment Processing, Loan Balance, History |
| **Admin** | `/api/v1/admin/dashboard` | Executive KPIs, Total Loan Portfolio, Recent Activities |
| **Home** | `/api/v1/home` | Mobile Customer Home Data & Financing Summary |
| **Notifications**| `/api/v1/notifications`| Firebase Cloud Messaging (FCM) Device Token Registration |
| **Files** | `/api/v1/files` | Document Preview & Inline Streaming |

---

## 🧪 Testing & Quality Assurance

The project includes unit and integration tests covering domain services, controllers, and security flows.

* **Execute test suite:**
  ```bash
  ./mvnw test
  ```

* **Verify test compilation:**
  ```bash
  ./mvnw test-compile -DskipTests
  ```

* **JaCoCo Code Coverage:**
  The project enforces an **80% instruction and branch coverage** rule. Generate coverage reports with:
  ```bash
  ./mvnw clean test jacoco:report
  ```
  The HTML coverage report will be available at:
  `target/site/jacoco/index.html`

---

## 🚢 CI/CD & Deployment

- **GitHub Actions**: Configured pipelines validate builds, run unit tests, verify JaCoCo coverage thresholds, and build container images.
- **Container Registry**: Production OCI images are packaged and published to GitHub Packages (`ghcr.io/edwin-jaya/bekal-backend`).
- **Nginx Reverse Proxy**: Ready configuration available via [`setup-nginx.sh`](./setup-nginx.sh) for reverse proxying and SSL termination.

---

## 📄 License

This project is licensed under the Apache 2.0 License.
