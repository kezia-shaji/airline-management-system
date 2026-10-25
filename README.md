# Airline Management System

Java Swing + MySQL + JDBC + Groq API desktop project.

## Requirements
- JDK 21
- Maven 3.9+
- MySQL Server 8.4 LTS
- MySQL Workbench (recommended)
- VS Code with Extension Pack for Java

## 1. Create the database
Open MySQL Workbench and run:
`src/main/java/...` is not where the SQL is. Run the file:
`database/schema.sql`

It creates:
users, passengers, flights, seats, bookings, e_tickets, notifications, destinations.

## 2. Configure database
The application defaults to:
- URL: jdbc:mysql://localhost:3306/airline_management_system
- user: root
- password: empty

If your MySQL root password is not empty, set environment variables.

macOS/Linux:
```bash
export DB_USER="root"
export DB_PASSWORD="YOUR_MYSQL_PASSWORD"
```

Optional:
```bash
export DB_URL="jdbc:mysql://localhost:3306/airline_management_system?useSSL=false&serverTimezone=UTC"
```

## 3. Groq API
The Smart Travel Companion reads the destination from the passenger's latest confirmed booking and sends it to Groq.

Set the API key only in your environment:
```bash
export GROQ_API_KEY="YOUR_GROQ_API_KEY"
```

Do NOT put the key in GitHub.

## 4. Run
From the project root:
```bash
mvn clean compile
mvn exec:java
```

Or open the Maven project in VS Code and run `com.airline.Main`.

## Demo accounts
Admin:
- username: admin
- password: admin123

Passenger:
- username: kezia
- password: kezia123

## Main modules
- Login / registration
- Role-based admin/passenger dashboard
- Flight management
- Passenger management
- Flight search
- Seat availability and selection
- Booking
- Cancellation
- E-ticket
- Notification Centre
- Smart Travel Companion using Groq API
- Basic admin statistics

## Security note
The demo schema uses plain-text passwords to keep the academic prototype simple. Before a final production-quality version, hash passwords with a strong password-hashing algorithm.
