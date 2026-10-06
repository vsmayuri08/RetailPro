# RetailPro – Multi-Branch Retail Management System

RetailPro is a full-stack **multi-branch retail management system** designed to simplify and automate day-to-day retail operations. The system provides centralized management of products, inventory, sales, customers, suppliers, employees, and branch operations through a role-based platform.

The project also incorporates **retail analytics, customer segmentation, demand analysis, sales prediction, and AI-assisted features** to support data-driven decision-making.

---

## 🚀 Features

### 🛍️ Product & Inventory Management

* Add, update, delete, and view products
* Manage product categories
* Track stock levels
* Monitor inventory across multiple branches
* Reorder suggestions for low-stock products
* Supplier management

### 💳 Point of Sale (POS)

* Product-based billing
* Cart management
* Automatic bill calculation
* Sales transaction management
* Order and payment tracking

### 👥 Customer Management

* Customer registration and management
* Customer purchase history
* Customer loyalty management
* Customer segmentation using RFM analysis

### 🏢 Multi-Branch Management

* Manage multiple retail branches
* Branch-specific inventory
* Branch-wise sales tracking
* Centralized retail management

### 👨‍💼 Employee & Payroll Management

* Employee records
* Employee role management
* Payroll-related information
* Role-based access control

### 📊 Retail Analytics

RetailPro includes analytical modules to help understand business performance:

* **ABC Analysis** – classifies products based on their contribution to overall sales/value
* **RFM Analysis** – segments customers based on Recency, Frequency, and Monetary value
* **Market Basket Analysis** – identifies products that are frequently purchased together
* **Demand Forecasting** – helps estimate future product demand
* **Sales Trend Prediction** – analyzes sales patterns and trends
* **Reorder Suggestions** – assists in identifying products that may need restocking

### 🤖 AI Assistance

* Gemini-based AI assistance
* AI-supported retail insights
* Natural-language interaction for selected system operations and analysis

### 🔐 Security

* Role-based access control
* Authentication and authorization
* JWT-based authentication
* Environment-based configuration for sensitive credentials

---

## 🏗️ System Architecture

```text
                    ┌─────────────────────┐
                    │      User / Admin   │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    React Frontend   │
                    │       / Vite        │
                    └──────────┬──────────┘
                               │
                         REST API
                               │
                               ▼
                    ┌─────────────────────┐
                    │   Spring Boot       │
                    │     Backend         │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
        ┌──────────┐     ┌────────────┐   ┌─────────────┐
        │  MySQL   │     │ Analytics  │   │ Gemini AI   │
        │ Database │     │  Modules   │   │ Assistance  │
        └──────────┘     └────────────┘   └─────────────┘
```

---

## 🛠️ Technology Stack

### Frontend

* React
* Vite
* JavaScript
* HTML5
* CSS3

### Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* Maven

### Database

* MySQL

### Security

* JWT Authentication
* Role-Based Access Control

### Analytics

* ABC Analysis
* RFM Customer Segmentation
* Market Basket Analysis
* Demand Forecasting
* Sales Trend Prediction

### AI

* Google Gemini API

### Development Tools

* Visual Studio Code
* IntelliJ IDEA / Eclipse
* Git
* GitHub
* MySQL

---

## 📁 Project Structure

```text
RetailPro/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── ...
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   │
│   │   └── test/
│   │
│   ├── pom.xml
│   └── ...
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── ...
│
├── .gitignore
├── PROJECT_GUIDE.md
└── README.md
```

---

## ⚙️ Installation & Setup

> For demo logins, roles and a typical demo walkthrough, see [PROJECT_GUIDE.md](PROJECT_GUIDE.md).

### 1. Clone the Repository

```bash
git clone https://github.com/vsmayuri08/RetailPro.git
```

Navigate into the project:

```bash
cd RetailPro
```

---

### 2. Backend Setup

Navigate to the backend:

```bash
cd backend
```

Make sure **Java 21** and **Maven** are installed.

Configure the required environment variables:

```text
DB_USERNAME
DB_PASSWORD
JWT_SECRET
GEMINI_API_KEY
```

The application uses environment variables for sensitive credentials instead of storing them directly in the source code.

---

### 3. Database Setup

Make sure MySQL is installed and running.

The application uses:

```text
Database: retail_management
```

The database configuration is handled through the Spring Boot configuration.

---

### 4. Run the Backend

From the `backend` directory:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

### 5. Frontend Setup

Open a new terminal and navigate to:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will be available at the URL displayed by Vite, typically:

```text
http://localhost:5173
```

---

## 🔑 Environment Variables

Sensitive credentials should **never be committed to GitHub**.

Example configuration:

```text
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_jwt_secret
GEMINI_API_KEY=your_gemini_api_key
```

> **Note:** Do not upload your actual API keys, database passwords, or other secrets to GitHub.

---

## 📈 Analytics Modules

### ABC Analysis

ABC analysis categorizes products according to their contribution to overall business value.

```text
A → High-value products
B → Medium-value products
C → Low-value products
```

This helps businesses prioritize inventory management.

### RFM Analysis

RFM stands for:

* **R – Recency**
* **F – Frequency**
* **M – Monetary Value**

It helps identify valuable customer groups based on their purchasing behavior.

### Market Basket Analysis

Market Basket Analysis identifies relationships between products purchased together.

For example:

```text
Product A + Product B
        ↓
Frequently purchased together
```

This can support product recommendations and promotional strategies.

### Demand Forecasting

Historical sales information can be analyzed to estimate future product demand and support inventory planning.

### Sales Trend Prediction

Sales patterns can be analyzed to identify trends and support business planning.

---

## 🔐 Security

RetailPro uses authentication and authorization mechanisms to protect application resources.

Security features include:

* JWT-based authentication
* Role-based access control
* Protected backend APIs
* Environment variables for sensitive credentials
* Secure database configuration

---

## 👨‍💻 Team Contributions

This project was developed collaboratively by **two contributors**.

### Contributor 1 – Jerisha M Github-https://github.com/jerisham
### Contributor 2 – VS Mayuri Github-https://github.com/vsmayuri08

> Both contributors collaborated on system integration, testing, debugging, documentation, and overall project development.

---

## 🎯 Project Objectives

The main objectives of RetailPro are:

1. To develop a centralized multi-branch retail management system.
2. To simplify product and inventory management.
3. To automate retail billing and sales operations.
4. To manage customers, suppliers, employees, and branches.
5. To provide analytical insights for better decision-making.
6. To implement customer segmentation using RFM analysis.
7. To analyze product relationships using Market Basket Analysis.
8. To support inventory planning through demand forecasting and reorder suggestions.
9. To integrate AI assistance into retail operations.
10. To provide a secure role-based full-stack application.

---

## 🌟 Key Highlights

* Full-stack retail management application
* Java 21 and Spring Boot backend
* React + Vite frontend
* MySQL database
* JWT authentication
* Multi-branch support
* POS billing
* Inventory and supplier management
* Customer loyalty and segmentation
* ABC Analysis
* RFM Analysis
* Market Basket Analysis
* Demand Forecasting
* Sales Trend Prediction
* AI-assisted functionality using Gemini
* Role-based access control

---

## 🔮 Future Enhancements

Possible future improvements include:

* Mobile application support
* Advanced real-time dashboards
* Improved sales forecasting models
* Automated inventory replenishment
* Advanced recommendation systems
* Cloud-based deployment
* Real-time branch synchronization
* Enhanced AI-powered business insights

---

## 📚 Academic Project

**Project:** RetailPro – Multi-Branch Retail Management System
**Project Type:** Java / Full-Stack PBL Project
**Backend:** Java 21, Spring Boot
**Frontend:** React, Vite
**Database:** MySQL
**AI:** Google Gemini
**Team Size:** 2

---

## 📄 License

This project was developed as an academic project for educational and demonstration purposes.
