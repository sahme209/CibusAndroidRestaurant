# Cibus Android Restaurant

Android app for Cibus restaurant partners. Part of the Cibus food delivery ecosystem.

## Overview

- **Role:** Restaurant Partner
- **Platform:** Android (Kotlin, Jetpack Compose)
- **API:** `https://api-vtadzgdqca-uc.a.run.app`

## Features

- Restaurant login
- Partnership application (Pakistan-compliant: NTN, PFA license, CNIC)
- Dashboard
- Incoming orders (accept / reject)
- Active orders
- Status updates (preparing, ready for pickup)
- Menu management
- Revenue summary
- Settings
- English + Roman Urdu support

## Build

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Integration

- **Auth:** `POST /restaurant/auth/sign-in`, `POST /restaurant/auth/apply`
- **Orders:** `GET /restaurants/:id/orders`
- **Accept/Reject:** `POST /orders/:id/accept`, `POST /orders/:id/reject`
- **Status:** `PATCH /orders/:id/status`
- **Menu:** `GET /restaurants/:id/menu`, `PATCH /restaurants/:id/menu`
- **Availability:** `PATCH /restaurants/:id/availability`

See [BACKEND_CONTRACT.md](../MD%20Files/BACKEND_CONTRACT.md) for full API.

## Ecosystem

- Customer: CibusAndroid, CibusIOS
- Rider: CibusAndroidRider, CibusIOSRider
- Restaurant: CibusAndroidRestaurant (this app), CibusIOSRestaurant
