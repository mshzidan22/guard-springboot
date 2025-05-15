🛡️ Guard
Spring boot app that uses most common features of Spring security.
This app uses session based authentication.

![Uploading Guard Project.png…]()


✨ Features

🔐 Username/Password login with "Remember Me"

🔁 OTP login with email or mobile (with 60-second resend lockout)

🌍 Google OAuth2 Login

🧠 Input validation (email, phone number, password strength)

🚦 Rate-limiting per IP and identifier

🧪 Unit and integration tests for critical authentication flows

📝 Custom registration with validation (email, phone, password strength)

💡 Modular design for extensibility (e.g., adding SMS/email providers)

🧱 Modular design for adding new login methods (e.g., Apple ID)



💡 Tech Stack

Java 17+

Spring Boot (Web, Security, OAuth2 client)

Thymeleaf

JUnit, Mockito

Maven
