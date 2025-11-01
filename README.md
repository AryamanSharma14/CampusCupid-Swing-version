<div align="center">
  <img src="https://img.shields.io/badge/Java-Swing-blue" alt="Java Swing" />
  <h1>CampusCupid Swing Version</h1>
  <p>A modern Java Swing application for campus matchmaking.</p>
</div>

---

## 🚀 Features

- **Login & Registration**: Secure user authentication
- **Profile Setup**: Personalize your profile
- **Preferences**: Set gender, age range, and interests
- **Swipe Interface**: Discover and match with others
- **Chats Panel**: Connect and chat with matches

---

## 🖥️ How to Run

1. **Install Java** (JDK 8 or higher)
2. SQLite JDBC is included under `lib/sqlite-jdbc.jar` (downloaded automatically).
3. **Compile all Java files** (Windows PowerShell):
   ```powershell
   javac -cp ".;lib/sqlite-jdbc.jar;lib/slf4j-api.jar;lib/slf4j-simple.jar" *.java
   ```
4. **Run the application** (Windows PowerShell):
   ```powershell
   java -cp ".;lib/sqlite-jdbc.jar;lib/slf4j-api.jar;lib/slf4j-simple.jar" MainWindow
   ```
   The app will create a local SQLite file `campuscupid.db` in the project folder.

---

## 🌐 Multi‑laptop demo (super simple)

This repo includes a tiny HTTP server so multiple laptops can register, swipe, match, and chat together.

1) On ONE laptop (the "host"), start the server:
   ```powershell
   java -cp ".;lib/sqlite-jdbc.jar;lib/slf4j-api.jar;lib/slf4j-simple.jar" ServerMain
   ```
   - By default it runs on port 8080 and uses the same `campuscupid.db` file.
   - If 8080 is busy, the server now auto-picks the first free port in 8080–8090 and logs it, e.g. `Server running on http://localhost:8081`.
   - You can also force a port explicitly:
     - Via system property: `java -Dport=8099 -cp ".;lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" ServerMain`
     - Or environment variable (before running): `set PORT=8099`
   - Optional demo data: seed ~60 users only if requested:
      - `java -Dseed=true -cp ".;lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" ServerMain`
      - Or set `SEED=1` before running

2) On EVERY laptop (including the host), start the app and enter the server URL when prompted:
   - Example on host: `http://localhost:8080` (or whatever the server printed)
   - Example on other laptops (replace with host IP): `http://192.168.1.23:8080`

3) Present: register different accounts, set profiles/preferences, swipe to match, and chat. All data is shared via the server.

Notes
- This is for demo/classroom use. It skips complex auth, uses simple endpoints and local DB.
- For production, switch to a deployed server and a cloud DB (Postgres/MySQL) and add proper auth (tokens/HTTPS).

---

## 📁 Project Structure

```
CampusCupidSwing/
├── ChatsPanel.java
├── LoginPanel.java
├── MainWindow.java
├── PreferencesPanel.java
├── ProfilePanel.java
├── RegistrationPanel.java
├── SwipePanel.java
└── README.md
```

- `MainWindow.java` — Main application window
- `LoginPanel.java` — Login screen
- `RegistrationPanel.java` — Registration screen
- `ProfilePanel.java` — Profile setup
- `PreferencesPanel.java` — User preferences
- `SwipePanel.java` — Swipe functionality
- `ChatsPanel.java` — Chat interface

---

## �️ Database

SQLite database `campuscupid.db` is created automatically with tables:

- `users(id, email, password_hash, name, created_at)`
- `profiles(user_id, bio, interests, hobbies, occupation)`
- `preferences(user_id, gender_pref, age_pref, interests_pref)`
- `swipes(id, user_id, target_user_id, liked, created_at)`
- `matches(id, user1_id, user2_id, created_at)`
- `messages(id, from_user_id, to_user_id, body, created_at)`

Passwords are hashed using SHA-256 with an app salt for demo purposes. For production use, replace with bcrypt/Argon2.

---

## �💡 Contributing

Pull requests and issues are welcome! Feel free to fork and improve the project.

---
