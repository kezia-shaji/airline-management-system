package com.airline.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.airline.database.DBConnection;
import com.airline.model.Flight;

public class FlightService {

    private Flight map(ResultSet r) throws SQLException {
        return new Flight(
                r.getInt("flight_id"),
                r.getString("flight_number"),
                r.getString("airline_name"),
                r.getString("source"),
                r.getString("destination"),
                r.getDate("departure_date").toLocalDate(),
                r.getTime("departure_time").toLocalTime(),
                r.getDate("arrival_date").toLocalDate(),
                r.getTime("arrival_time").toLocalTime(),
                r.getInt("total_seats"),
                r.getInt("available_seats"),
                r.getBigDecimal("price"),
                r.getString("status")
        );
    }

    // Search flights by source, destination and departure date
    public List<Flight> search(
            String source,
            String destination,
            LocalDate date
    ) throws SQLException {

        String sql =
                "SELECT * FROM flights " +
                "WHERE source = ? " +
                "AND destination = ? " +
                "AND departure_date = ? " +
                "AND status = 'SCHEDULED' " +
                "ORDER BY departure_time";

        List<Flight> list = new ArrayList<>();

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)
        ) {

            ps.setString(1, source);
            ps.setString(2, destination);
            ps.setDate(3, Date.valueOf(date));

            try (ResultSet r = ps.executeQuery()) {

                while (r.next()) {
                    list.add(map(r));
                }
            }
        }

        return list;
    }

    // Get all flights
    public List<Flight> all() throws SQLException {

        List<Flight> list = new ArrayList<>();

        String sql =
                "SELECT * FROM flights " +
                "ORDER BY departure_date, departure_time";

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet r = ps.executeQuery()
        ) {

            while (r.next()) {
                list.add(map(r));
            }
        }

        return list;
    }

    // Add a new flight
    public void add(
            String no,
            String airline,
            String source,
            String dest,
            LocalDate dd,
            LocalTime dt,
            LocalDate ad,
            LocalTime at,
            int seats,
            BigDecimal price
    ) throws SQLException {

        String sql =
                "INSERT INTO flights(" +
                "flight_number, airline_name, source, destination, " +
                "departure_date, departure_time, arrival_date, arrival_time, " +
                "total_seats, available_seats, price" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps =
                        c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {

            ps.setString(1, no);
            ps.setString(2, airline);
            ps.setString(3, source);
            ps.setString(4, dest);

            ps.setDate(5, Date.valueOf(dd));
            ps.setTime(6, Time.valueOf(dt));

            ps.setDate(7, Date.valueOf(ad));
            ps.setTime(8, Time.valueOf(at));

            ps.setInt(9, seats);
            ps.setInt(10, seats);

            ps.setBigDecimal(11, price);

            ps.executeUpdate();

            // Get the newly created flight ID
            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (keys.next()) {
                    int flightId = keys.getInt(1);

                    // Generate seats automatically
                    generateSeats(c, flightId, seats);
                }
            }
        }
    }

    // Generate seats for a newly added flight
    private void generateSeats(
            Connection c,
            int flightId,
            int total
    ) throws SQLException {

        int perRow = 10;

        int rows =
                (int) Math.ceil(total / (double) perRow);

        String sql =
                "INSERT INTO seats(" +
                "flight_id, seat_number, seat_class" +
                ") VALUES (?, ?, ?)";

        try (PreparedStatement ps = c.prepareStatement(sql)) {

            int made = 0;

            for (int r = 1; r <= rows && made < total; r++) {

                for (int s = 1; s <= perRow && made < total; s++) {

                    ps.setInt(1, flightId);

                    // Example: A1, A2, A3... B1, B2...
                    String seatNumber =
                            ((char) ('A' + r - 1))
                            + String.valueOf(s);

                    ps.setString(2, seatNumber);

                    // First row = Business,
                    // remaining rows = Economy
                    if (r == 1) {
                        ps.setString(3, "BUSINESS");
                    } else {
                        ps.setString(3, "ECONOMY");
                    }

                    ps.addBatch();

                    made++;
                }
            }

            ps.executeBatch();
        }
    }
}