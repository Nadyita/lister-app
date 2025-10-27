# Lister - Shopping List Android App

A modern Android app for shopping lists, written in Kotlin with Jetpack Compose.

## Features

### List Management
- Create and manage multiple lists
- Rename lists (long press on list)
- Delete lists
- Number of items per list displayed as badge

### Item Management
- Add items with name, amount, unit and category
- Autocomplete when entering item names
- Automatic category suggestions based on previous entries
- Edit items (long press on item)
- Check off items and move to "IN CART"
- Move items back (click on item in cart)
- Delete all items in cart at once (green FAB)

### Categories
- Items are grouped by categories
- Categories are collapsible (expand/collapse)
- Items without category are shown under "NO CATEGORY"

### Settings
- Configurable API Base URL
- Optional Bearer Token authentication
- Default: http://192.168.42.12/api

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material3
- **Navigation**: Navigation Compose
- **Network**: Retrofit + OkHttp
- **State Management**: ViewModel + StateFlow
- **Persistence**: DataStore (for settings)
- **Coroutines**: For asynchronous operations

## Project Structure

```
app/src/main/java/xyz/travitia/lister/
├── data/
│   ├── model/              # Data classes (DTOs)
│   ├── remote/             # API Service & Client
│   ├── repository/         # Repository Layer
│   └── preferences/        # DataStore Preferences
├── ui/
│   ├── components/         # Reusable UI Components
│   ├── screens/            # Screen Composables
│   ├── theme/              # Theme, Colors, Typography
│   ├── viewmodel/          # ViewModels
│   └── navigation/         # Navigation Routes
├── ListerApplication.kt    # Application Class
├── MainActivity.kt         # Main Activity
└── ViewModelFactories.kt   # ViewModel Factories
```

## API Integration

The app connects to a RESTful backend API. Configure the API Base URL in the app settings.

## Build & Installation

### Prerequisites
- Android Studio (Hedgehog or newer)
- Android SDK 26+ (Android 8.0 Oreo)
- JDK 17

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Release APK
```bash
./gradlew assembleRelease
```

**Note**: For signed release APKs, see [SIGNING.md](SIGNING.md) for setup instructions.

### Install Debug APK
```bash
./gradlew installDebug
```

## Download

Pre-built APK files are available in the [Releases](https://github.com/YOUR_USERNAME/lister-app/releases) section.

## Configuration

### API Settings
1. Open the app
2. Tap on the Settings icon
3. Enter the Base URL (must end with `/api`)
4. Optional: Enter a Bearer Token for authentication
5. Tap Save

The Bearer Token will be sent as an `Authorization: Bearer <token>` header with every API request.

## UI/UX Design

### Color Scheme
- **Primary Color**: Purple (#6200EA)
- **Secondary Color**: Gray (#9E9E9E) for "In Cart"
- **Success Color**: Green (#4CAF50) for Done button

### Interactions
- **Normal Click**: Open list / Check off item
- **Long Press**: Edit/Delete menu
- **FAB (+)**: Create new element
- **FAB (checkmark)**: Delete all items in cart

## Known Limitations

- No offline functionality
- Requires network connection to backend API

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the GNU Affero General Public License v3.0 (AGPL-3.0).
See the [LICENSE](LICENSE) file for details.

**Important**: The AGPL-3.0 license requires that if you modify this software and use it to provide a service over a
network, you must make the modified source code available to users of that service.