a readme # ListenX App - Quick Reference Guide

**🎵 Song Recognition App** | **Status**: ✅ Ready to Use | **Build**: Successful 0 Errors

## 🚀 Get Started in 3 Steps

### Step 1: Build
```bash
cd /Users/sanskarlohani/StudioProjects/ListenX
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

### Step 2: Install
```bash
./gradlew installDebug
```

### Step 3: Launch
Click the app icon on your emulator/device, or:
```bash
adb shell am start -n com.sanskar.listenx/.MainActivity
```

## 📱 How to Use the App

1. **Skip Splash** - Waits 3 seconds automatically
2. **Home Screen** - See featured songs, click **Listen** button
3. **Recording** - Watch the waveform animate for 10 seconds
4. **Results** - See matched songs (demo data)
5. **Favorites** - Click ❤️ to save songs
6. **Share** - Click "Share" button to share

## 📂 Important Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | App entry point |
| `navigation/AppNavigation.kt` | All routes & screens |
| `viewmodel/SongRecognitionViewModel.kt` | State management |
| `util/MockDataProvider.kt` | Demo song data |
| `data/api/RetrofitClient.kt` | Backend API |

## 🔧 Connect Your Backend

Open `data/api/RetrofitClient.kt` and update:
```kotlin
private const val BASE_URL = "https://your-server.com/api/"
```

Your API should return:
```json
[
  {
    "title": "Song Title",
    "artist": "Artist Name",
    "youtubeId": "dQw4w9WgXcQ",
    "score": 95.2,
    "album": "Album Name"
  }
]
```

## ⚠️ If Something Goes Wrong

| Problem | Solution |
|---------|----------|
| Build fails | `./gradlew clean assembleDebug` |
| App crashes | Check permissions: `adb shell pm grant com.sanskar.listenx android.permission.RECORD_AUDIO` |
| No results | Verify mock data is working by checking `util/MockDataProvider.kt` |
| Permission error | Restart emulator or reinstall: `./gradlew installDebug` |

## 📊 Project Structure

```
ListenX/
├── MainActivity.kt             ← Start here
├── navigation/                 ← Navigation
│   ├── AppNavigation.kt       ← Screen hierarchy
│   └── Screen.kt              ← Routes
├── ui/
│   ├── screen/                ← Full screens
│   └── component/             ← Reusable parts
├── viewmodel/                 ← Business logic
├── data/                       ← API & models
└── util/                       ← Helpers & utilities
```

## 🎯 What Works Now

✅ App launches without crashing  
✅ Records audio for 10 seconds  
✅ Shows demo songs in results  
✅ Bottom navigation works  
✅ Favorites can be saved  
✅ Beautiful purple gradient theme  
✅ Smooth animations and transitions  

## ❌ Known Limitations

⚠️ Uses demo songs (connect backend to change)  
⚠️ Favorites reset on app restart (add Room DB for persistence)  
⚠️ No user login system (add if needed)  
⚠️ Images from YouTube (requires internet)  

## 💡 Next Steps

1. Test on device/emulator
2. Connect your API endpoint
3. Implement database for favorites
4. Add user authentication
5. Publish to Play Store

## 📞 Commands Cheat Sheet

```bash
# Build
./gradlew assembleDebug

# Install
./gradlew installDebug

# Clean build
./gradlew clean assembleDebug

# Run tests
./gradlew test

# View logs
adb logcat | grep listenx

# Grant permissions
adb shell pm grant com.sanskar.listenx android.permission.RECORD_AUDIO

# List devices
adb devices

# Uninstall
adb uninstall com.sanskar.listenx
```

## 📚 Documentation

- **FIX_SUMMARY.md** - What was fixed
- **TROUBLESHOOTING.md** - Common issues
- **IMPLEMENTATION_SUMMARY.md** - Complete technical details

---

**You're all set! Start coding! 🚀**

