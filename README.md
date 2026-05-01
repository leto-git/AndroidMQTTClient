# MQTT Element - Lightweight Android MQTT Client

**MQTT Element** is a lightweight MQTT client for Android, built with **Kotlin** and 
**Jetpack Compose**. **Eclipse Paho** is used as the MQTT client library.

The application is designed for IoT developers to test MQTT connections and for beginners to learn
about the core elements of MQTT.

## Features

- **MQTT v3.1.1 & v5.0 Support:** Supports both MQTT v3.1.1 and v5.0 connections via Eclipse Paho integration. Some MQTT v5.0 features are not yet available!
- **Server Management:** Add, edit, and delete MQTT connection profiles.
- **Publish and Subscribe**: Publish messages and subscribe to different topics.
- **Logging**: View and copy the history of published and received messages as well as an MQTT event log.
- **Encrypted Storage:** All broker configurations including passwords are encrypted using **SQLCipher** with 256-bit AES encryption within a Room database.
- **Modern UI:** Built entirely with Jetpack Compose and Material 3 for a fluid user experience.

## Tech Stack

- **Language:** Kotlin
- **MQTT Protocol:** Eclipse Paho (v3 & v5)
- **Database:** Room Persistence Library
- **Security/Encryption:** SQLCipher (Zetetic)
- **UI Framework:** Jetpack Compose (Material 3)

## Academic Project

This application was developed as part of a university project. It is mainly designed to be used in labs accompanying IoT courses and to learn about the MQTT protocol.

## License & Legal

This project is licensed under the **Apache License 2.0**. For more details, see the [LICENSE](LICENSE) and [NOTICE](NOTICE) files.

### Credits
- **Eclipse Paho:** Providing MQTT client core functionality (EPL-2.0).
- **SQLCipher:** Providing transparent database encryption (BSD-style License). Copyright (c) Zetetic LLC.

## Getting Started

1. **Clone the repository:**
   git clone https://github.com/leto-git/android-mqtt-element.git
2. **Open in Android Studio**
   (This project was developed using Android Studio Narwhal (Canary)).
3. **Build & Run:**
   Sync the Gradle files and run the app on an emulator or physical device.

## Contributing

As this project was created for educational purposes, contributions, bug reports, and feature requests from fellow students and the community are highly welcome.
