package dao;

import model.Lecturer;
import config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LecturerDAOImpl implements LecturerDAO {

    @Override
    public boolean add(Lecturer lecturer) {
        String sql = "INSERT INTO lecturer(name, user_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturer.getName());
            ps.setInt(2, lecturer.getUserId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Lecturer lecturer) {
        String sql = "UPDATE lecturePak A SET name=? WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lecturer.getName());
            ps.setInt(2, lecturer.getId());
            ps.setInt(3, lecturer.getUserId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id, int userId) {
        String sql = "DELETE FROM lecturer WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Lecturer getById(int id, int userId) {
        String sql = "SELECT * FROM lecturer WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Lecturer l = new Lecturer();
                l.setId(rs.getInt("id"));
                l.setName(rs.getString("name"));
                l.setUserId(rs.getInt("user_id"));
                return l;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Lecturer> getAll(int userId) {
        List<Lecturer> lecturers = new ArrayList<>();
        String sql = "SELECT * FROM lecturer WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Lecturer l = new Lecturer();
                l.setId(rs.getInt("id"));
                l.setName(rs.getString("name"));
                l.setUserId(rs.getInt("user_id"));
                lecturers.add(l);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lecturers;
    }
}
