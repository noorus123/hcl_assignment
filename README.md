# Warehouse Management System Implementation

![Build Status](https://github.com/noorus123/hcl_assignment/actions/workflows/maven-build.yml/badge.svg)

## 📋 Solution Overview
This repository contains the completed code assessment. It demonstrates **Clean Architecture**, **Data Consistency**, and **Automated Testing**.

### ✅ Requirements Checklist (Delivered)
| Requirement | Status | Implementation Details |
| :--- | :--- | :--- |
| **Code Implementation** | ✅ Done | Implemented Warehouse Logic, Location Gateway, and Store Transaction fixes. |
| **Architecture** | ✅ Done | Used **Hexagonal Architecture** for the Warehouse module to isolate business rules. |
| **Unit Testing** | ✅ Done | **100% Coverage** on Core Domain Logic using **JUnit 5 & Mockito**. |
| **Code Coverage** | ✅ Done | **JaCoCo** integrated. Reports generated automatically. |
| **Best Practices** | ✅ Done | Strict Transaction Management and Custom Exception Handling. |
| **Documentation** | ✅ Done | Architectural decisions documented in [QUESTIONS.md](QUESTIONS.md). |
| **CI/CD Pipeline** | ✅ **Bonus** | GitHub Actions Workflow configured for auto-build and test. |
| **Health Checks** | ✅ **Bonus** | SmallRye Health integrated at `/q/health`. |

## 🚀 How to Run

### Prerequisites
*   JDK 17+
*   Docker (Optional, for running the full database integration)

### 1. Run Tests & Coverage
This will execute unit tests using the H2 in-memory database (no Docker required for this step).
```bash
./mvnw test