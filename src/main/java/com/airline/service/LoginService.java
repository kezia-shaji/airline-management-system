package com.airline.service;

import com.airline.database.DBConnection;
import com.airline.model.User;

import java.sql.*;

public class LoginService {
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, role FROM users WHERE username=? AND password=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new User(rs.getInt("user_id"),
                        rs.getString("username"), rs.getString("role"));
            }
        }
        return null;
    }

    public int getPassengerId(int userId) throws SQLException {
        String sql = "SELECT passenger_id FROM passengers WHERE user_id=?";
        try (Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()) { return rs.next()?rs.getInt(1):-1; }
        }
    }

    public void register(String username, String password, String first, String last,
                         String email, String phone) throws SQLException {
        String userSql="INSERT INTO users(username,password,role) VALUES(?,?, 'PASSENGER')";
        String passengerSql="INSERT INTO passengers(user_id,first_name,last_name,email,phone) VALUES(?,?,?,?,?)";
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try(PreparedStatement up=c.prepareStatement(userSql,Statement.RETURN_GENERATED_KEYS);
                PreparedStatement pp=c.prepareStatement(passengerSql)) {
                up.setString(1,username); up.setString(2,password); up.executeUpdate();
                try(ResultSet keys=up.getGeneratedKeys()) {
                    if(!keys.next()) throw new SQLException("Could not create user.");
                    int userId=keys.getInt(1);
                    pp.setInt(1,userId); pp.setString(2,first); pp.setString(3,last);
                    pp.setString(4,email); pp.setString(5,phone); pp.executeUpdate();
                }
                c.commit();
            } catch(SQLException e) { c.rollback(); throw e; }
        }
    }
}
