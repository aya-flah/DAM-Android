# Development Login Workaround

## Problem
Google Sign-In error 10 (DEVELOPER_ERROR) occurs when the Android OAuth client is not properly configured with SHA-1/SHA-256 fingerprints for each developer's debug keystore.

## Temporary Solution
A **dev login** endpoint has been added to bypass OAuth for local testing.

---

## Backend Changes

### New Endpoint: `POST /auth/dev-login`
- **Request body:**
  ```json
  {
    "email": "your-email@example.com",
    "name": "Your Name"
  }
  ```
- **Response:** Same as social-login (authToken, providerId, user object)
- **Behavior:** Creates or finds a local user with provider='local'

### Files Modified
- `src/auth/dto/dev-login.dto.ts` (new)
- `src/auth/auth.service.ts` - added `devLogin()` method
- `src/auth/auth.controller.ts` - added `/auth/dev-login` route

---

## Android Changes

### API & Repository
- `AuthApiService.kt` - added `devLogin()` endpoint
- `AuthRepository.kt` - added `loginWithDevUser()` method
- `DevLoginRequest` data class for request payload

### UI
- `WelcomeScreen.kt` - added **"🛠️ Dev Login (Temp)"** button in login chooser dialog
  - Uses hardcoded credentials: `dev@pianokids.local` / `Dev User`
  - Works without any OAuth configuration

---

## How to Use

1. **Start backend:**
   ```powershell
   cd DAM-backend
   npm run start:dev
   ```

2. **Build & run Android app:**
   ```powershell
   cd DAM-Android
   .\gradlew.bat clean assembleDebug
   # Then run from Android Studio or:
   adb install app\build\outputs\apk\debug\app-debug.apk
   ```

3. **Login flow:**
   - Tap "Login" on Welcome screen
   - Tap **"🛠️ Dev Login (Temp)"** button
   - You'll be logged in as `dev@pianokids.local`

---

## Next Steps (To Fix Google Sign-In Permanently)

### 1. Get Your Debug Keystore Fingerprints
```powershell
keytool -list -v -alias androiddebugkey -keystore "$env:USERPROFILE\.android\debug.keystore" -storepass android -keypass android
```
Copy both **SHA-1** and **SHA-256** lines.

### 2. Configure Google Cloud Console
1. Go to [Google Cloud Console Credentials](https://console.cloud.google.com/apis/credentials)
2. Select the project with client ID: `24237641156-i9auj11eib39mmuv76vg3jpd3a4enfd5`
3. Create **Android OAuth Client** (if not exists):
   - Application type: Android
   - Package name: `com.pianokids.game`
   - SHA-1 certificate fingerprint: [paste from step 1]
   - SHA-256 certificate fingerprint: [paste from step 1]
4. Save and wait 1-2 minutes for propagation

### 3. Test Google Sign-In Again
```powershell
# Uninstall to clear cached auth state
adb uninstall com.pianokids.game

# Reinstall
.\gradlew.bat installDebug

# Try Google login - error 10 should be gone
```

### 4. For Each Developer
Repeat steps 1-3 for every developer PC (each has a different debug keystore SHA).

### 5. For Release Builds
Get the release keystore SHA and add it to the same Android OAuth client.

---

## Cleanup (After OAuth Works)

### Backend
Remove or guard the dev-login endpoint:
```typescript
// In auth.controller.ts
@Post('dev-login')
async devLogin(@Body() dto: DevLoginDto): Promise<AuthResponseDto> {
  if (process.env.NODE_ENV === 'production') {
    throw new UnauthorizedException('Dev login disabled in production');
  }
  return this.authService.devLogin(dto.email, dto.name);
}
```

### Android
Remove the dev login button from `WelcomeScreen.kt` (lines around the "🛠️ Dev Login" button).

---

## Notes
- The dev login creates real users in MongoDB with provider='local'
- These users persist; you can reuse the same dev account across sessions
- Backend still validates HMAC tokens normally for API calls
- This is **for development only** - do not deploy to production with dev login enabled
