# EMI V2 System for Smart Televisions (Skyworth Models)

## Overview

**EMI V2** is the latest installment security system developed specifically for **Walton Smart Televisions powered by Skyworth main boards**. It is designed to prevent unauthorized use before sale and ensure installment payments are enforced after delivery.

This version introduces **auto-unlock via portal update** and **pre-sale locking** to enhance security and streamline operations.

## 📌 Target Models

> ⚠️ **This EMI V2 system is exclusively developed for Skyworth-based Walton TV models.**  
> Integration with other platforms (e.g., Vestel, Changhong) is planned for future updates.

---

## 🔑 Key Features

### 🔒 Pre-Sale Lock (Anti-Theft)
- **Purpose**: Prevents theft or unauthorized use before delivery.
- **Function**: Skyworth-based TVs remain locked until activated via EMI Portal.
- **Benefit**: Ensures safe inventory management and transport.

### 🔁 Auto Unlock After EMI Portal Update
- **Real-Time Sync**: Automatically unlocks the TV once the customer's payment is recorded in the portal.
- **No Manual Input**: Skyworth TV fetches status remotely and performs unlocking.
- **Secure Matching**: Uses device serial or unique ID for validation.

### 🔐 EMI Lock on Payment Failure
- System automatically **locks the TV** if the customer fails to make an installment payment by the due date.
- TV will remain locked until the payment is confirmed via the EMI portal.

---

## ⚙️ Architecture

- **Device-Side**: Runs as a system service in the Skyworth Android framework.
- **Server-Side**: Connected with Walton EMI Portal backend.
- **Communication**: Periodic secure requests to the server from the TV to fetch EMI status.

---

## 📂 Components

- `skyworth_emi_service/` – Lock logic for Skyworth Android TVs
- `emi_portal_api/` – Secure interface between portal and device
- `emi_lock_screen/` – Full-screen UI to block usage on locked state
- `device_binding/` – Skyworth model registration and verification module

---

## ✅ Use Case Summary

| Situation                    | Behavior                                 |
|-----------------------------|-------------------------------------------|
| New Skyworth TV, unsold     | Locked until EMI portal activation        |
| Payment missed              | TV gets locked remotely                   |
| Payment updated in portal   | TV auto-unlocks within minutes            |
| Reset attempt               | Lock remains (cannot bypass from Android) |

---

## 🔐 Security Features

- MAC or Serial-based TV identity binding
- Obfuscated system service to prevent tampering
- Token-authenticated API communication

---

## 🧭 Roadmap

- On-TV EMI dashboard for customer reminders
- Offline fallback handling
- OTA deployment for field models
- Integration with Walton sales app for dealers

---

## 🏢 Developed By

**Walton Television Software Team**  
Target Platform: **Skyworth Android TV Models**  
Project Lead: **Toukir Sheikh**  
Internal Contact: `alam37880@waltonplc.com`
