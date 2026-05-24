# Elab Library Management System

This is a Java Swing library management app.

## Firestore seeding

To create the Firestore collections from this project, set the path to your Firebase service account JSON and run the bootstrap class:

```powershell
setx FIREBASE_SERVICE_ACCOUNT "C:\path\to\service-account.json"
cd "C:\Users\amany\Desktop\other\Elab2"
javac library_management_system\*.java
java -cp . library_management_system.FirebaseBootstrap
```

Keep the service-account JSON outside version control. The repo ignores `library_management_system/e-library-service-account.json` and `e-library-service-account.json` so the credential stays local to your machine.

After seeding, launch the app through the friendly launcher:

```powershell
RUN_APP.bat
```

If you prefer the command line, use:

```powershell
java -cp . library_management_system.AppLauncher
```

The first screen is a branded welcome screen, then login opens the main dashboard with live counts, quick actions, and CSV export shortcuts for books and issue records.
