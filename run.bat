@echo off
echo Starting HealthKeeper Application...
echo.
echo The application is starting, please wait...
echo.
echo To stop the application, press Ctrl+C and then Y to confirm
echo.
echo ===================================================
echo.
java -jar target/java_personal_health_assistant-0.0.1-SNAPSHOT.jar
echo.
echo ===================================================
echo.
echo Application has stopped. Press any key to close this window...
pause > nul 