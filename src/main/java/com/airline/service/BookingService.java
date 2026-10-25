package com.airline.service;

import com.airline.database.DBConnection;

import java.sql.*;
import java.util.*;

public class BookingService {
    public List<String[]> availableSeats(int flightId) throws SQLException {
        String sql="SELECT seat_id,seat_number,seat_class FROM seats WHERE flight_id=? AND status='AVAILABLE' ORDER BY seat_id";
        List<String[]> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,flightId);
            try(ResultSet r=ps.executeQuery()){while(r.next())list.add(new String[]{String.valueOf(r.getInt(1)),r.getString(2),r.getString(3)});}
        }
        return list;
    }

    public int book(int passengerId,int flightId,int seatId) throws SQLException {
        String seatSql="SELECT status FROM seats WHERE seat_id=? AND flight_id=? FOR UPDATE";
        String flightSql="SELECT price,available_seats FROM flights WHERE flight_id=? FOR UPDATE";
        String insert="INSERT INTO bookings(passenger_id,flight_id,seat_id,amount) VALUES(?,?,?,?)";
        String seatUpdate="UPDATE seats SET status='BOOKED' WHERE seat_id=?";
        String flightUpdate="UPDATE flights SET available_seats=available_seats-1 WHERE flight_id=?";
        String ticket="INSERT INTO e_tickets(booking_id,ticket_number,ticket_status) VALUES(?, ?, 'ACTIVE')";
        String notification="INSERT INTO notifications(user_id,title,message,notification_type) SELECT p.user_id,?,?, 'BOOKING' FROM passengers p WHERE p.passenger_id=?";
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                try(PreparedStatement ps=c.prepareStatement(seatSql)){ps.setInt(1,seatId);ps.setInt(2,flightId);
                    try(ResultSet r=ps.executeQuery()){if(!r.next()||!"AVAILABLE".equals(r.getString(1)))throw new SQLException("Seat is no longer available.");}}
                double price;
                try(PreparedStatement ps=c.prepareStatement(flightSql)){ps.setInt(1,flightId);try(ResultSet r=ps.executeQuery()){
                    if(!r.next()||r.getInt("available_seats")<=0)throw new SQLException("Flight is full.");
                    price=r.getDouble("price");
                }}
                int bookingId;
                try(PreparedStatement ps=c.prepareStatement(insert,Statement.RETURN_GENERATED_KEYS)){
                    ps.setInt(1,passengerId);ps.setInt(2,flightId);ps.setInt(3,seatId);ps.setDouble(4,price);ps.executeUpdate();
                    try(ResultSet k=ps.getGeneratedKeys()){if(!k.next())throw new SQLException("Booking creation failed.");bookingId=k.getInt(1);}
                }
                try(PreparedStatement ps=c.prepareStatement(seatUpdate)){ps.setInt(1,seatId);ps.executeUpdate();}
                try(PreparedStatement ps=c.prepareStatement(flightUpdate)){ps.setInt(1,flightId);ps.executeUpdate();}
                String ticketNo="TKT"+System.currentTimeMillis();
                try(PreparedStatement ps=c.prepareStatement(ticket)){ps.setInt(1,bookingId);ps.setString(2,ticketNo);ps.executeUpdate();}
                try(PreparedStatement ps=c.prepareStatement(notification)){
                    ps.setString(1,"Booking Confirmed");ps.setString(2,"Your booking #"+bookingId+" has been confirmed. Ticket: "+ticketNo);ps.setInt(3,passengerId);ps.executeUpdate();
                }
                c.commit(); return bookingId;
            }catch(SQLException e){c.rollback();throw e;}
        }
    }

    public List<String[]> myBookings(int passengerId) throws SQLException {
        String sql="SELECT b.booking_id,f.flight_number,f.source,f.destination,f.departure_date,f.departure_time,s.seat_number,b.amount,b.booking_status,COALESCE(t.ticket_number,'') FROM bookings b JOIN flights f ON b.flight_id=f.flight_id JOIN seats s ON b.seat_id=s.seat_id LEFT JOIN e_tickets t ON b.booking_id=t.booking_id WHERE b.passenger_id=? ORDER BY b.booking_date DESC";
        List<String[]> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,passengerId);try(ResultSet r=ps.executeQuery()){while(r.next())list.add(new String[]{
                String.valueOf(r.getInt(1)),r.getString(2),r.getString(3),r.getString(4),
                r.getDate(5).toString(),r.getTime(6).toString(),r.getString(7),
                r.getBigDecimal(8).toString(),r.getString(9),r.getString(10)});}}
        return list;
    }

    public void cancel(int passengerId,int bookingId) throws SQLException {
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                int seatId,flightId;
                String q="SELECT seat_id,flight_id,booking_status FROM bookings WHERE booking_id=? AND passenger_id=? FOR UPDATE";
                try(PreparedStatement ps=c.prepareStatement(q)){ps.setInt(1,bookingId);ps.setInt(2,passengerId);try(ResultSet r=ps.executeQuery()){
                    if(!r.next())throw new SQLException("Booking not found.");
                    if(!"CONFIRMED".equals(r.getString(3)))throw new SQLException("Booking is already cancelled.");
                    seatId=r.getInt(1);flightId=r.getInt(2);
                }}
                try(PreparedStatement ps=c.prepareStatement("UPDATE bookings SET booking_status='CANCELLED' WHERE booking_id=?")){ps.setInt(1,bookingId);ps.executeUpdate();}
                try(PreparedStatement ps=c.prepareStatement("UPDATE seats SET status='AVAILABLE' WHERE seat_id=?")){ps.setInt(1,seatId);ps.executeUpdate();}
                try(PreparedStatement ps=c.prepareStatement("UPDATE flights SET available_seats=available_seats+1 WHERE flight_id=?")){ps.setInt(1,flightId);ps.executeUpdate();}
                try(PreparedStatement ps=c.prepareStatement("UPDATE e_tickets SET ticket_status='CANCELLED' WHERE booking_id=?")){ps.setInt(1,bookingId);ps.executeUpdate();}
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO notifications(user_id,title,message,notification_type) SELECT p.user_id,'Booking Cancelled',CONCAT('Booking #',?,' has been cancelled.'),'CANCELLATION' FROM passengers p WHERE p.passenger_id=?")){
                    ps.setInt(1,bookingId);ps.setInt(2,passengerId);ps.executeUpdate();}
                c.commit();
            }catch(SQLException e){c.rollback();throw e;}
        }
    }

    public String ticket(int passengerId,int bookingId) throws SQLException {
        String sql="SELECT b.booking_id,t.ticket_number,t.issue_date,p.first_name,p.last_name,f.flight_number,f.airline_name,f.source,f.destination,f.departure_date,f.departure_time,f.arrival_date,f.arrival_time,s.seat_number,b.amount,b.booking_status FROM bookings b JOIN passengers p ON b.passenger_id=p.passenger_id JOIN flights f ON b.flight_id=f.flight_id JOIN seats s ON b.seat_id=s.seat_id LEFT JOIN e_tickets t ON b.booking_id=t.booking_id WHERE b.booking_id=? AND b.passenger_id=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,bookingId);ps.setInt(2,passengerId);
            try(ResultSet r=ps.executeQuery()){if(!r.next())throw new SQLException("Ticket not found.");
                return "================ E-TICKET ================\n"+
                        "Ticket No : "+r.getString(2)+"\nBooking ID: "+r.getInt(1)+
                        "\nPassenger : "+r.getString(4)+" "+r.getString(5)+
                        "\n--------------------------------------------\nFlight    : "+r.getString(6)+" ("+r.getString(7)+")"+
                        "\nRoute     : "+r.getString(8)+" → "+r.getString(9)+
                        "\nDeparture : "+r.getDate(10)+" "+r.getTime(11)+
                        "\nArrival   : "+r.getDate(12)+" "+r.getTime(13)+
                        "\nSeat      : "+r.getString(14)+"\nAmount    : ₹"+r.getBigDecimal(15)+
                        "\nStatus    : "+r.getString(16)+"\n============================================";
            }
        }
    }
}
