
<p align="center">
  <img src="https://i.ibb.co/YFQ3gwnS/575970602-1315469567260441-1313972598450601892-n.jpg" width="200" height="200"> 
</p>



# TuneIsland (Android Client)

> A magical game for tablets that teaches kids the joy of playing the piano. This repository contains the Android version of the game.

TuneIsland is an interactive mobile game designed to make learning piano fun and accessible for children. Through a captivating game world, customizable avatars, and an interactive piano keyboard, kids can embark on a musical journey, learning notes and songs as they play.

---

## ✨ Features

*   **Avatar Customization:** Personalize your character to make your musical adventure unique.
*   **Interactive Piano Keyboard:** A fully functional virtual piano to practice and play along with lessons.
*   **Engaging Game World:** Explore different levels and challenges that teach musical concepts in a fun way.

## 🛠️ Technologies & Libraries

This project is the Android client for TuneIsland, built entirely with modern Android development technologies.

*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) for building the native UI.
*   **Language:** [Kotlin](https://kotlinlang.org/) (with Coroutines for asynchronous operations).
*   **Architecture:** MVVM (Model-View-ViewModel).
*   **Navigation:** [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation).
*   **Networking:** [Retrofit](https://square.github.io/retrofit/) for API communication with our backend.
*   **Image Loading:** [Coil](https://coil-kt.github.io/coil/) for efficient image loading.
*   **Animations:** [Lottie](https://airbnb.io/lottie/) for rich, engaging animations.
*   **Authentication:** Google Sign-In & Facebook Login.
*   **Data Persistence:** [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for lightweight local data storage.

The complete TuneIsland ecosystem also includes:
*   **iOS Client:** Built with SwiftUI.
*   **Backend Server:** Built with NestJS.

## ⚙️ Setup and Installation

To run the TuneIsland Android client, you must first have the backend server running.

### 1. Set Up the Backend

Clone and run the backend project from its repository.
*   **Backend Repository:** [https://github.com/amelmediouni2001/DAM-backend.git](url)

Follow the instructions in the backend's `README.md` file to get it running.

### 2. Clone the Android Project

Clone this repository to your local machine:

```
bash git clone https://github.com/aya-flah/DAM-Android.git
cd TuneIsland-Android
```



### 3. Configure the Backend IP Address

Before running the app, you must configure the IP address of your running backend server.
1.  Open the project in Android Studio.
2.  Navigate to the following file: `app/src/main/java/com/pianokids/game/api/RetrofitClient.kt`.
3.  Inside this file, find the line that defines the `BASE_URL` and change the IP address to match the local IP address of the machine running your backend server.

    **If using an Android Emulator:**
     The default address to connect to your local machine (localhost) is 10.0.2.2. Your `BASE_URL` should look like this:
        ```
         private const val BASE_URL = "http://10.0.2.2:3000/"
        ```

    **If using a physical Android device:**
      Ensure your device and the computer running the backend are on the same Wi-Fi network.
      Find your computer's local IP address `cmd ipconfig` (e.g.and use that. Your `BASE_URL` would then be:
        ```
        private const val BASE_URL = "http://192.168.1.10:3000/"
        ```
        
### 4. Run the Application

Build and run the project on an Android tablet or emulator through Android Studio.

## 🚀 Usage

Once the backend is running and the IP address is correctly configured in the Android project, simply run the application from Android Studio. The game will start, and you can begin exploring the world of TuneIsland, create your avatar, and start learning to play the piano!

## 🤝 How to Contribute

Currently, the project is maintained by a small team, with each member responsible for a specific part of the ecosystem (Android, iOS, Backend). While we are not actively seeking major contributions, we are open to suggestions and bug fixes.

If you'd like to contribute, please feel free to:

1.  Fork the project.
2.  Create a new branch for your feature or fix (`git checkout -b feature/MyNewFeature` or `fix/MyBugFix`).
3.  Commit your changes (`git commit -m 'Add some new feature'`).
4.  Push to your branch (`git push origin feature/MyNewFeature`).
5.  Open a Pull Request.

## 📄 License

This project is licensed under the **MIT License**. See the `LICENSE.md` file for full details.

---
_This README was last updated on November 17, 2025._
