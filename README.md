# Elab Library Management System

**Version 2.0.0** — Smart digital library management for academic institutions.

Built with Java Swing and Firebase Firestore, this system provides a modern, production-ready interface for managing books, members, and issue/return workflows.

---

## Features

- **Modern UI** — Dark sidebar navigation, card-based layout, colored stat badges
- **Dashboard** — Live stats (total books, available, issued, members), recent activity, overdue alerts
- **Book Management** — Add, update, delete books with search and category filtering
- **Member Management** — Register students/members, view issue history per member
- **Issue Workflow** — Step-by-step book issuing with availability validation
- **Return Workflow** — Automatic overdue detection and fine calculation
- **Transactions** — Full issue/return history with status filtering and CSV export
- **Reports** — Generate and export reports for books, members, overdue items, and more
- **Settings** — Configure library name, fine rate, borrowing period, and export folder
- **Authentication** — Secure login and signup with SHA-256 password hashing

---

## Requirements

- Java 17 or later
- Firebase Firestore service account JSON file
- `elab-library.jar` on the classpath (included)

---

## Quick Start

### 1. Set up Firebase

Place your Firebase service account JSON file in the project root or at:

```
library_management_system/e-library.json
```

Alternatively, set the environment variable:

```bash
export FIREBASE_SERVICE_ACCOUNT=/path/to/service-account.json
```

### 2. Run the application

**Windows:**
```
RUN_APP.bat
```

**Manual (any OS):**
```bash
javac -cp elab-library.jar library_management_system/*.java
java -cp .;elab-library.jar library_management_system.AppLauncher
```

On Linux/macOS use `:` instead of `;` as the classpath separator.

---

## Application Structure

```
library_management_system/
├── AppLauncher.java          # Entry point — bootstraps Firebase and launches UI
├── FirebaseBootstrap.java    # All Firestore REST API operations
├── UiTheme.java              # Shared colors, fonts, and UI component factories
├── WelcomeScreen.java        # Splash/welcome screen
├── LOGIN_FORM.java           # Modern login screen (split layout)
├── SIGNUP_FORM.java          # Account creation screen
├── MainWindow.java           # Main window with sidebar + CardLayout navigation
├── DashboardPanel.java       # Dashboard with stats, quick actions, recent activity
├── BooksPanel.java           # Book management (CRUD + search/filter)
├── MembersPanel.java         # Member management (CRUD + issue history)
├── IssuePanel.java           # Issue book workflow
├── ReturnPanel.java          # Return book workflow with fine calculation
├── TransactionsPanel.java    # All issue/return records with export
├── ReportsPanel.java         # Report generation and CSV export
├── SettingsPanel.java        # App settings (persisted to ~/.elab-library.properties)
├── AboutPanel.java           # About screen and usage guide
├── Dashboard.java            # Legacy dashboard (kept for compatibility)
├── HOME.java                 # Legacy operations center (kept for compatibility)
└── ...                       # Other legacy search/record windows
```

---

## Navigation

After login, the **MainWindow** opens with a sidebar containing:

| Section | Description |
|---|---|
| 🏠 Dashboard | Live stats, quick actions, recent activity |
| 📚 Books | Add, edit, delete, search books |
| 👥 Members | Register and manage library members |
| 📤 Issue Book | Issue a book to a member |
| 📥 Return Book | Process a book return |
| 📋 Transactions | View all issue/return records |
| 📊 Reports | Generate and export reports |
| ⚙️ Settings | Configure library settings |
| ❓ Help / About | App info and usage guide |

---

## Settings

Settings are stored in `~/.elab-library.properties`:

| Key | Default | Description |
|---|---|---|
| `library.name` | Elab Library | Institution name |
| `fine.rate.per.day` | 0.50 | Fine per overdue day |
| `borrowing.period.days` | 14 | Default loan period |
| `export.folder` | user home | Default CSV export location |

---

## Data Model

### Firestore Collections

**`books`**
- `bookId`, `bookName`, `author`, `category`, `price`, `issued` (yes/no)

**`students`**
- `registrationNo`, `studentName`, `mobileNo`, `branch`

**`issue_records`**
- `bookId`, `bookName`, `author`, `category`, `price`
- `registrationNo`, `studentName`
- `issueDate`, `returnDate`, `issued` (yes/no)

**`employees`**
- `employeeName`, `passwordHash` (SHA-256)

---

## Technologies

- **Java 17+** — Core language
- **Java Swing** — GUI framework
- **Firebase Firestore** — Cloud database (REST API, no SDK dependency)
- **Java HTTP Client** — Built-in `java.net.http` for Firestore calls
- **SHA-256** — Password hashing via `java.security.MessageDigest`

---

## Developer

**Elab Development Team**  
Kampala International University  
Faculty of Computing & Information Technology

---

## Changelog

### v2.0.0
- Complete UI overhaul with modern sidebar navigation
- New MainWindow with CardLayout panel system
- Rewritten LOGIN_FORM and SIGNUP_FORM (no NetBeans generated code)
- New panels: DashboardPanel, BooksPanel, MembersPanel, IssuePanel, ReturnPanel, TransactionsPanel, ReportsPanel, SettingsPanel, AboutPanel
- Enhanced UiTheme with button factories, form helpers, table styling, and dialog utilities
- Fine calculation and overdue detection in ReturnPanel
- CSV export in TransactionsPanel and ReportsPanel
- Settings persistence to `~/.elab-library.properties`
- SwingWorker used throughout for non-blocking Firestore operations
