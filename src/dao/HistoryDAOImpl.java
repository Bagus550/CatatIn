package dao;

import config.DatabaseConnection;
import model.HistoryRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAOImpl implements HistoryDAO {

    private Connection conn;

    public HistoryDAOImpl() {
        conn = DatabaseConnection.getConnection();
    }

    @Override
    public boolean add(HistoryRecord record) {
        String sql = "INSERT INTO history(user_id, task_id, task_title, action, action_at) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, record.getUserId());

            if (record.getTaskId() != null) {
                ps.setInt(2, record.getTaskId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setString(3, record.getTaskTitle());
            ps.setString(4, record.getAction());
            ps.setTimestamp(5, new Timestamp(record.getActionAt().getTime()));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<HistoryRecord> getAllByUser(int userId) {
        List<HistoryRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM history WHERE user_id=? ORDER BY action_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                HistoryRecord r = new HistoryRecord();
                r.setId(rs.getInt("id"));
                r.setTaskId(rs.getInt("task_id"));
                r.setTaskTitle(rs.getString("task_title"));
                r.setAction(rs.getString("action"));

                Timestamp ts = rs.getTimestamp("action_at");
                r.setActionAt(ts != null ? new java.util.Date(ts.getTime()) : null);

                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    @Override
    public boolean deleteAllByUser(int userId) {
        String sql = "DELETE FROM history WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
