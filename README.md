# 💎 FinTrack — Premium Personal Finance Tracker

A robust, full-stack personal finance management application built with **Object-Oriented Programming (OOP)** principles. FinTrack allows users to track income, manage budgets, and visualize spending habits through a high-performance, modern interface.

---

## 🚀 Key Features

### 💰 Specialized Salary Logic
- **Premium Salary Tracker**: A dedicated workflow for income tracking with a high-impact dashboard banner.
- **Goal-Oriented Progress**: Unlike regular expense budgets, Salary categories track positive progress toward earnings targets.
- **Quick Action**: Dedicated "+ Add Salary" button for one-click income recording.

### 📊 Advanced Reporting & Data
- **Detailed CSV Export**: Generate comprehensive financial reports including transaction lists and detailed summaries (Totals, Category breakdowns, and Percentages).
- **Intelligent Insights**: Real-time feedback on spending health using the Observer pattern for budget alerts.
- **Interactive Visuals**: Beautiful monthly cash flow charts and categorical hierarchy visualizations.

### 🛡️ Core OOP Architecture
- **Service Layer Pattern**: Centralized business logic in `FinanceService`.
- **Observer Pattern**: Dynamic alert system for budget monitoring.
- **Abstraction & Polymorphism**: Flexible transaction system handling diverse spending and income types.
- **Persistence Layer**: Clean file-based storage with data integrity checks.

---

## 🛠️ Technology Stack

- **Backend**: Java 8 (Core OOP, HTTP Server, File I/O)
- **Frontend**: React, Vite, TailwindCSS (Vanilla architecture)
- **Data**: CSV-based Persistent Storage
- **Deployment**: Docker, Vercel (Frontend), Railway (Backend)

---

## ⚙️ Local Setup

### 1. Backend (Java)
Navigate to the `backend` directory:
```bash
# Compile
javac -d out src/com/financetracker/abstract_base/*.java src/com/financetracker/interfaces/*.java src/com/financetracker/model/*.java src/com/financetracker/pattern/*.java src/com/financetracker/report/*.java src/com/financetracker/server/*.java src/com/financetracker/service/*.java src/com/financetracker/storage/*.java src/com/financetracker/main/*.java

# Run
java -cp out com.financetracker.main.Main
```

### 2. Frontend (React)
Navigate to the `frontend` directory:
```bash
npm install
npm run dev
```
Visit: `http://localhost:5173`

---

## ☁️ Deployment

### Backend → Railway
1. Set root directory to `/backend`.
2. Railway detects the `Dockerfile` and deploys automatically.

### Frontend → Vercel
1. Set root directory to `/frontend`.
2. Configure `VITE_API_URL` to point to your Railway backend.

---

## 📄 Documentation
- **UML Class Diagram**: Available in both `UML_Class_Diagram.html` and PDF formats in the root directory.
- **Implementation Plans**: Detailed history of feature development available in the `.brain` directory.

---
*Built with ❤️ as an OOP Semester Project.*
