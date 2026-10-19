FuelSA - JavaFX Fuel Request App
--------------------------------

How to run (Maven):
1. Install JDK 17+ and Maven.
2. Copy `fuelapp.properties.example` to `fuelapp.properties` and fill in your BulkSMS
   API token (optional - leave blank to run without sending SMS).
   You can set the same names as environment variables instead.
3. From this project root run:
   mvn clean javafx:run

First run:
- The app creates `fuel_app.db` (SQLite) in the working directory.
- There is no default admin. Open "Admin Login", type a username and click LOGIN to
  create the admin account (password of 8+ characters and a phone number for SMS alerts).

Security notes:
- Never commit `fuelapp.properties`, `.env`, `*.db` or `data/` - they are git-ignored
  because they contain API credentials and customers' personal information.
- Passwords are stored as salted PBKDF2 hashes. Older plaintext passwords are upgraded
  automatically the first time the app starts.
