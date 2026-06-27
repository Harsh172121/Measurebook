# Kalanidhan MeasureBook

**Kalanidhan MeasureBook** is a professional Android application designed for ladies' tailors to manage customer information and measurement records efficiently. Built with modern Android technologies, it provides a seamless experience for tracking custom clothing orders with precision and ease.

<div align="center">
    <img src="app/src/main/res/drawable/img_app_logo.jpg" alt="Kalanidhan Logo" width="200" height="200" style="border-radius: 50%" />
</div>

---

## Features

- **Dashboard Insights**: Get a quick overview of your business with statistics for total customers, measurement records, and recent activity.
- **Customer Management**: 
    - Create, edit, and organize customer profiles.
    - **Instant Search**: Find customers in milliseconds using a prefix-matching Trie algorithm.
- **Precision Measurements**:
    - Specialized fields for various categories like Blouse and Punjabi Dress.
    - Track detailed measurements (Inches) with support for custom notes.
- **Professional PDF Export**: Generate and share professional-looking PDF measurement sheets with shop branding.
- **Multi-language Support**: Fully localized in English and Gujarati (ગુજરાતી).
- **Dynamic Theming**: Choose between Light Mode, Dark Mode, or let it follow your System settings.
- **Backup & Restore**: Export your entire database to a JSON file for backup and easily import it whenever needed.
- **Shop Branding**: Customize the app with your shop's name, owner details, and contact information for the PDF exports.

---

## Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
- **Design System**: [Material Design 3 (M3)](https://m3.material.io/) for a premium, clean look.
- **Programming Language**: [Kotlin](https://kotlinlang.org/) with [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html).
- **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room) (SQLite) for offline-first data management.
- **Architecture**: MVVM (Model-View-ViewModel) pattern for clean separation of concerns.
- **Local Persistence**: SharedPreferences for lightweight settings.
- **PDF Generation**: Native Android `PdfDocument` API.
- **Data Serialization**: [Moshi](https://github.com/square/moshi) for JSON handling.

---

## Getting Started

### Prerequisites
- [Android Studio Ladybug](https://developer.android.com/studio) or newer.
- Android SDK 24 (Android 7.0) or higher.

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Harsh172121/Measurebook.git
   ```

2. **Open in Android Studio**:
   - Launch Android Studio.
   - Select **Open** and navigate to the cloned project directory.

3. **Configure API Keys (Optional)**:
   The project uses a secrets plugin. Create a `.env` file in the root directory:
   ```env
   GEMINI_API_KEY=your_api_key_here
   ```
   *(Note: See `.env.example` for reference)*

4. **Build and Run**:
   - Sync Gradle files.
   - Connect an Android device or start an emulator.
   - Click **Run** (`Shift + F10`).

---

## Project Structure

- `app/src/main/java/com/example/data/`: Room database entities, DAOs, and repository.
- `app/src/main/java/com/example/ui/`: Compose UI screens, ViewModels, and theme definitions.
- `app/src/main/java/com/example/util/`: Utility classes for PDF generation, localization, and search algorithms (Trie).
- `app/src/main/res/`: Android resources including layouts, drawables, and XML configurations.

---

## Screenshots

| Dashboard | Customer List | Measurement Add |
| :---: | :---: | :---: |
| ![Dashboard](https://via.placeholder.com/200x400?text=Dashboard) | ![Customer List](https://via.placeholder.com/200x400?text=Customer+List) | ![Add Measurement](https://via.placeholder.com/200x400?text=Add+Measurement) |

---

## Contributing

Contributions are welcome! If you'd like to improve Kalanidhan MeasureBook, feel free to fork the repository and submit a pull request.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

Distributed under the MIT License. See `LICENSE` for more information.

---

## Contact

**Kalanidhan Ladies Tailor**  
Developed for precision and efficiency in tailoring.

Project Link: [https://github.com/Harsh172121/Measurebook](https://github.com/Harsh172121/Measurebook)
