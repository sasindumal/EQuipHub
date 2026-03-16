# 📱 EQuipHub Mobile App

A fully-featured React Native mobile application for the **EQuipHub** equipment management system, built with **Expo SDK 52**. Supports all user roles — Students, Department Admins, Technical Officers, Lecturers, and System Admins.

---

## 🎨 Features

### 🔐 Authentication
- **Login** — Email & password with JWT token authentication
- **Register** — Student self-registration with university email validation
- **Email Verification** — 6-digit OTP code input with auto-focus and resend
- **Secure Token Storage** — JWT stored in Expo SecureStore (encrypted)
- **Auto Token Refresh** — 401 interceptor with silent refresh

### 🎒 Student Portal
| Screen | Features |
|---|---|
| **Dashboard** | Stat cards (active borrows, pending, penalties), quick actions, recent requests |
| **Equipment Catalog** | Grid layout, search, status badges, detail modal, quick request |
| **My Requests** | Request list, search, detail modal, cancel, FAB for new request |
| **New Request** | Purpose, equipment, dates, auto-submit form |
| **Penalties** | Stats, list, detail modal, appeal submission |
| **Profile** | Account info, role badge, logout |

### 🏢 Department Admin Portal
| Screen | Features |
|---|---|
| **Dashboard** | Department stats (staff, students, equipment, requests) |
| **Equipment** | Equipment list with search and status filter |
| **Requests** | Request list with approve/reject actions |
| **Staff** | Staff members list |
| **Profile** | Account details, logout |

### 🔧 Technical Officer Portal
| Screen | Features |
|---|---|
| **Dashboard** | Stats (ready to issue, inspections), damage alerts |
| **Issue Equipment** | Approved requests list, condition selection, notes |
| **Process Return** | In-use items, condition + damage assessment |
| **Profile** | Account details, logout |

### 📋 Lecturer Portal
| Screen | Features |
|---|---|
| **Approval Queue** | Pending requests, detail modal, approve/reject with comments |
| **Profile** | Account details, logout |

### ⚙️ System Admin Portal
| Screen | Features |
|---|---|
| **Dashboard** | System stats (depts, users, staff, students), recent users list |
| **Profile** | Account details, server connection info, logout |

---

## 🛠 Tech Stack

| Technology | Purpose |
|---|---|
| **React Native** | Cross-platform mobile framework |
| **Expo SDK 52** | Development toolchain & build system |
| **React Navigation 6** | Tab + Stack navigation |
| **Axios** | HTTP client with interceptors |
| **Expo SecureStore** | Encrypted token storage |
| **Ionicons** | Icon library via @expo/vector-icons |

---

## 🚀 Quick Start

### Prerequisites

1. **Node.js** ≥ 18 (LTS recommended)
2. **npm** ≥ 9
3. **Expo Go** app on your phone ([iOS](https://apps.apple.com/app/expo-go/id982107779) / [Android](https://play.google.com/store/apps/details?id=host.exp.exponent))
4. **EQuipHub backend** running (default: `http://localhost:8080`)

### Step 1: Install Dependencies

```bash
cd app
npm install
```

### Step 2: Configure Backend URL

The app connects to the backend API. By default:
- **iOS Simulator**: `http://localhost:8080/api/v1`
- **Android Emulator**: `http://10.0.2.2:8080/api/v1`

#### For Physical Device (over WiFi)

Edit `src/lib/api.js` and change the `API_BASE_URL`:

```js
// Find your machine's local IP (e.g., 192.168.1.100)
const API_BASE_URL = 'http://192.168.1.100:8080/api/v1';
```

<details>
<summary>💡 How to find your local IP</summary>

**macOS:**
```bash
ipconfig getifaddr en0
```

**Windows:**
```bash
ipconfig
# Look for "IPv4 Address" under your WiFi adapter
```

**Linux:**
```bash
hostname -I | awk '{print $1}'
```
</details>

### Step 3: Start the Backend

Make sure the EQuipHub Spring Boot backend is running:

```bash
cd ../backend/api
./mvnw spring-boot:run
```

The backend should be accessible at `http://localhost:8080`.

### Step 4: Start the App

```bash
# Start Expo development server
npx expo start
```

You'll see a QR code in the terminal. Then:

| Platform | How to Open |
|---|---|
| **iOS Physical** | Open Camera → scan QR code → opens in Expo Go |
| **Android Physical** | Open Expo Go → scan QR code |
| **iOS Simulator** | Press `i` in the terminal |
| **Android Emulator** | Press `a` in the terminal |

---

## 📁 Project Structure

```
app/
├── App.js                          # Root — navigation + auth provider
├── index.js                        # Entry point
├── app.json                        # Expo configuration
├── package.json                    # Dependencies
├── assets/                         # App icons & splash screen
│   ├── icon.png
│   ├── splash-icon.png
│   ├── adaptive-icon.png
│   └── favicon.png
└── src/
    ├── lib/
    │   ├── api.js                  # Axios client + all API modules
    │   └── theme.js                # Design tokens (colors, fonts, etc.)
    ├── context/
    │   └── AuthContext.js           # Auth state + login/logout/register
    ├── components/
    │   └── UI.js                   # Reusable components (Badge, Card, etc.)
    └── screens/
        ├── ProfileScreen.js         # Shared profile (all roles)
        ├── auth/
        │   ├── LoginScreen.js
        │   ├── RegisterScreen.js
        │   └── VerifyEmailScreen.js
        ├── student/
        │   ├── DashboardScreen.js
        │   ├── EquipmentScreen.js
        │   ├── RequestsScreen.js
        │   ├── NewRequestScreen.js
        │   └── PenaltiesScreen.js
        ├── admin/
        │   └── DashboardScreen.js
        ├── deptAdmin/
        │   └── Screens.js           # Dashboard, Requests, Equipment, Staff, Students
        ├── technicalOfficer/
        │   └── Screens.js           # Dashboard, Issue, Return
        └── lecturer/
            └── ApprovalScreen.js
```

---

## 🎨 Design System

The mobile app uses the same **glassmorphism** color palette as the web frontend:

| Color | Hex | Usage |
|---|---|---|
| Primary | `#3D52A0` | Buttons, active states, links |
| Primary Light | `#7091E6` | Highlights, gradients |
| Secondary | `#8697C4` | Secondary text, icons |
| Muted | `#ADBBDA` | Borders, placeholders |
| Background | `#EDE8F5` | Screen backgrounds |
| White | `#FFFFFF` | Cards, inputs |
| Black | `#000000` | Primary text |

---

## 🔌 API Integration

All API endpoints mirror the web frontend:

| Module | Endpoints |
|---|---|
| `authAPI` | Login, Register, Verify Email, Get Current User |
| `adminAPI` | Dashboard, Departments, Config |
| `userAPI` | All Users, Staff, Students, Suspend/Activate |
| `deptAdminAPI` | My Department, Staff, Students, Stats, Config |
| `equipmentAPI` | CRUD, Status Updates |
| `requestAPI` | My Requests, Create, Submit, Cancel |
| `approvalAPI` | My Queue, Process Decision |
| `inspectionAPI` | Issue, Return, My Inspections |
| `penaltyAPI` | My Penalties, Summary, Appeals |

---

## 🧪 Testing Different Roles

The app automatically routes you to the correct portal based on your role:

| Role | Portal |
|---|---|
| `STUDENT` | Student tabs (Home, Equipment, Requests, Penalties, Profile) |
| `SYSTEMADMIN` | Admin tabs (Dashboard, Profile) |
| `DEPARTMENTADMIN` / `HEADOFDEPARTMENT` | Dept Admin tabs (Home, Equipment, Requests, Staff, Profile) |
| `TECHNICALOFFICER` | TO tabs (Home, Issue, Returns, Profile) |
| `LECTURER` / `APPOINTEDLECTURER` / `INSTRUCTOR` | Lecturer tabs (Approvals, Profile) |

---

## 🐛 Troubleshooting

### "Network Error" on physical device
- Ensure your phone and computer are on the **same WiFi network**
- Update `API_BASE_URL` in `src/lib/api.js` to your machine's local IP
- Check firewall isn't blocking port 8080

### "Request failed with status 401"
- Your JWT token may have expired
- The app auto-refreshes tokens, but if the refresh also fails, you'll be logged out
- Try logging in again

### Metro bundler issues
```bash
# Clear cache and restart
npx expo start --clear
```

### Dependency issues
```bash
# Reset node_modules
rm -rf node_modules
npm install
```

---

## 📦 Building for Production

### Expo Application Services (EAS)

```bash
# Install EAS CLI
npm install -g eas-cli

# Login to Expo account
eas login

# Build for Android (APK)
eas build --platform android --profile preview

# Build for iOS (requires Apple Developer account)
eas build --platform ios --profile preview
```

### Local Development Build

```bash
# Android
npx expo run:android

# iOS (macOS only, requires Xcode)
npx expo run:ios
```

---

## 📄 License

Part of the EQuipHub project — University Equipment Management System.
