FuelSA - JavaFX Fuel Request App
--------------------------------

How to run (Maven):
1. Install JDK 17+ and Maven.
2. Copy `fuelapp.properties.example` to `fuelapp.properties` and fill in your BulkSMS
   API token (optional - leave blank to run without sending SMS).
   You can set the same names as environment variables instead.
3. From this project root run:
   mvn clean javafx:run

Demo mode (sample data to click through):
   mvn javafx:run -Pdemo
- In IntelliJ: add `--demo` as the program argument of the run configuration for app.Main
  (and reload the Maven project so the icon library is picked up).
- Uses a separate `fuel_app_demo.db` filled with 6 made-up customers, an admin account and
  28 fuel requests, so your real `fuel_app.db` is never touched. SMS is switched off.
- Demo logins:
    Customers: thandi, sipho, lerato, pieter, ayesha, kagiso - password: Demo@1234
    Admin:     admin - password: FuelSA-demo-2026
- Delete `fuel_app_demo.db` to start over with fresh demo data.

First run:
- The app creates `fuel_app.db` (SQLite) in the working directory.
- There is no default admin. Open "Admin Login", type a username and click LOGIN to
  create the admin account (password of 8+ characters and a phone number for SMS alerts).

Security notes:
- Never commit `fuelapp.properties`, `.env`, `*.db` or `data/` - they are git-ignored
  because they contain API credentials and customers' personal information.
- Passwords are stored as salted PBKDF2 hashes. Older plaintext passwords are upgraded
  automatically the first time the app starts.
