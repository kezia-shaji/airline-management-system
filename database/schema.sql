CREATE DATABASE IF NOT EXISTS airline_management_system;
USE airline_management_system;

DROP PROCEDURE IF EXISTS generate_seats;
DROP TABLE IF EXISTS e_tickets;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS destinations;
DROP TABLE IF EXISTS passengers;
DROP TABLE IF EXISTS flights;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','PASSENGER') NOT NULL DEFAULT 'PASSENGER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE passengers (
    passenger_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    date_of_birth DATE,
    gender ENUM('MALE','FEMALE','OTHER'),
    passport_no VARCHAR(30) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_passenger_user FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE flights (
    flight_id INT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL UNIQUE,
    airline_name VARCHAR(100) NOT NULL,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure_date DATE NOT NULL,
    departure_time TIME NOT NULL,
    arrival_date DATE NOT NULL,
    arrival_time TIME NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status ENUM('SCHEDULED','DELAYED','CANCELLED','COMPLETED') DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (total_seats > 0),
    CHECK (available_seats >= 0 AND available_seats <= total_seats),
    CHECK (price >= 0)
);

CREATE TABLE seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    flight_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    seat_class ENUM('ECONOMY','BUSINESS') DEFAULT 'ECONOMY',
    status ENUM('AVAILABLE','BOOKED') DEFAULT 'AVAILABLE',
    UNIQUE (flight_id, seat_number),
    CONSTRAINT fk_seat_flight FOREIGN KEY (flight_id) REFERENCES flights(flight_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    passenger_id INT NOT NULL,
    flight_id INT NOT NULL,
    seat_id INT NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    booking_status ENUM('CONFIRMED','CANCELLED') DEFAULT 'CONFIRMED',
    amount DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_booking_passenger FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id),
    CONSTRAINT fk_booking_flight FOREIGN KEY (flight_id) REFERENCES flights(flight_id),
    CONSTRAINT fk_booking_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
);

CREATE TABLE e_tickets (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE,
    ticket_number VARCHAR(30) NOT NULL UNIQUE,
    issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    qr_code TEXT,
    ticket_status ENUM('ACTIVE','CANCELLED') DEFAULT 'ACTIVE',
    CONSTRAINT fk_ticket_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE
);

CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    notification_type ENUM('BOOKING','CANCELLATION','FLIGHT','REMINDER','GENERAL') DEFAULT 'GENERAL',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE destinations (
    destination_id INT AUTO_INCREMENT PRIMARY KEY,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    airport_code VARCHAR(10) NOT NULL UNIQUE,
    description TEXT,
    local_currency VARCHAR(30),
    UNIQUE(city, country)
);

INSERT INTO users(username,password,role) VALUES
('admin','admin123','ADMIN'),
('kezia','kezia123','PASSENGER'),
('nanditha','nanditha123','PASSENGER'),
('sheikha','sheikha123','PASSENGER'),
('surya','surya123','PASSENGER');

INSERT INTO passengers(user_id,first_name,last_name,email,phone,date_of_birth,gender,passport_no) VALUES
(2,'Kezia','Shaji','kezia@example.com','9876543210','2006-05-15','FEMALE','P1234567'),
(3,'Nanditha','S','nanditha@example.com','9876543211','2006-07-20','FEMALE','P1234568'),
(4,'Sheikha','Nizam','sheikha@example.com','9876543212','2006-03-12','FEMALE','P1234569'),
(5,'Suryadev','TP','surya@example.com','9876543213','2006-09-10','MALE','P1234570');

INSERT INTO flights
(flight_number,airline_name,source,destination,departure_date,departure_time,arrival_date,arrival_time,total_seats,available_seats,price,status)
VALUES
('AI101','Air India','Kochi','Bangalore','2026-10-15','08:30:00','2026-10-15','09:40:00',40,40,4500,'SCHEDULED'),
('AI205','Air India','Kochi','Delhi','2026-10-18','10:00:00','2026-10-18','13:15:00',50,50,7200,'SCHEDULED'),
('6E310','IndiGo','Kochi','Mumbai','2026-10-20','14:00:00','2026-10-20','16:05:00',40,40,5800,'SCHEDULED'),
('6E420','IndiGo','Kochi','Chennai','2026-10-22','07:15:00','2026-10-22','08:35:00',40,40,3900,'SCHEDULED');

INSERT INTO destinations(city,country,airport_code,description,local_currency) VALUES
('Bangalore','India','BLR','Technology hub known for parks, food and cultural attractions.','INR'),
('Delhi','India','DEL','Capital city known for historical monuments and food.','INR'),
('Mumbai','India','BOM','Major metropolitan city known for the Gateway of India and beaches.','INR'),
('Chennai','India','MAA','Coastal city known for temples, beaches and South Indian culture.','INR'),
('Kochi','India','COK','Kerala destination known for Fort Kochi and backwaters.','INR');

DROP PROCEDURE IF EXISTS generate_seats;

DELIMITER //
CREATE PROCEDURE generate_seats(IN p_flight_id INT, IN p_rows INT, IN p_per_row INT)
BEGIN
    DECLARE r INT DEFAULT 1;
    DECLARE s INT DEFAULT 1;
    DECLARE letter CHAR(1);
    WHILE r <= p_rows DO
        SET letter = CHAR(64 + r);
        SET s = 1;
        WHILE s <= p_per_row DO
            INSERT INTO seats(flight_id,seat_number,seat_class,status)
            VALUES(p_flight_id,CONCAT(letter,s),
                   IF(r=1,'BUSINESS','ECONOMY'),'AVAILABLE');
            SET s = s + 1;
        END WHILE;
        SET r = r + 1;
    END WHILE;
END//
DELIMITER ;

CALL generate_seats(1,4,10);
CALL generate_seats(2,5,10);
CALL generate_seats(3,4,10);
CALL generate_seats(4,4,10);

DROP PROCEDURE generate_seats;
