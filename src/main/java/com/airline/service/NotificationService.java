package com.airline.service;

import com.airline.database.DBConnection;
import java.sql.*;
import java.util.*;

public class NotificationService {
    public List<String[]> get(int userId) throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="SELECT notification_id,title,message,notification_type,is_read,created_at FROM notifications WHERE user_id=? ORDER BY created_at DESC";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,userId);try(ResultSet r=ps.executeQuery()){while(r.next())out.add(new String[]{
                String.valueOf(r.getInt(1)),r.getString(2),r.getString(3),r.getString(4),
                r.getBoolean(5)?"Read":"Unread",r.getTimestamp(6).toString()});}}
        return out;
    }

    public void markAllRead(int userId) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE notifications SET is_read=TRUE WHERE user_id=?")){
            ps.setInt(1,userId);ps.executeUpdate();
        }
    }
}
