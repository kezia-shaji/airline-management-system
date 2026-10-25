package com.airline.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class Flight {
    private final int id;
    private final String number, airline, source, destination;
    private final LocalDate departureDate, arrivalDate;
    private final LocalTime departureTime, arrivalTime;
    private final int totalSeats, availableSeats;
    private final BigDecimal price;
    private final String status;

    public Flight(int id, String number, String airline, String source, String destination,
                  LocalDate departureDate, LocalTime departureTime, LocalDate arrivalDate,
                  LocalTime arrivalTime, int totalSeats, int availableSeats,
                  BigDecimal price, String status) {
        this.id=id; this.number=number; this.airline=airline; this.source=source;
        this.destination=destination; this.departureDate=departureDate;
        this.departureTime=departureTime; this.arrivalDate=arrivalDate;
        this.arrivalTime=arrivalTime; this.totalSeats=totalSeats;
        this.availableSeats=availableSeats; this.price=price; this.status=status;
    }

    public int getId(){return id;}
    public String getNumber(){return number;}
    public String getAirline(){return airline;}
    public String getSource(){return source;}
    public String getDestination(){return destination;}
    public LocalDate getDepartureDate(){return departureDate;}
    public LocalTime getDepartureTime(){return departureTime;}
    public LocalDate getArrivalDate(){return arrivalDate;}
    public LocalTime getArrivalTime(){return arrivalTime;}
    public int getTotalSeats(){return totalSeats;}
    public int getAvailableSeats(){return availableSeats;}
    public BigDecimal getPrice(){return price;}
    public String getStatus(){return status;}

    @Override public String toString() {
        return number + " | " + source + " → " + destination + " | " +
                departureDate + " " + departureTime + " | ₹" + price + " | " +
                availableSeats + " seats";
    }
}
