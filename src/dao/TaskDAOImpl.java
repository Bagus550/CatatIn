package dao;

import config.DatabaseConnection;
import model.Task;
import model.HistoryRecord;
import utils.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAOImpl implements TaskDAO {

    private Connection conn;

    public TaskDAOImpl() {
        this.conn = DatabaseConnection.getConnection();
    }

    @Override
    public boolean addTask(Task task) {
        String sql = "INSERT INTO task (user_id, title, description, "
                + "deadline, lecturer_id, course_id, is_done) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, Session.getUser().getId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            if (task.getDeadline() != null) {
                ps.setTimestamp(4, new Timestamp(task.getDeadline().getTime()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            ps.setInt(5, task.getLecturerId());
            ps.setInt(6, task.getCourseId());
            ps.setInt(7, task.isCompleted() ? 1 : 0);

            int affected = ps.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int taskId = rs.getInt(1);
                        HistoryRecord record = new HistoryRecord();
                        record.setUserId(Session.getUser().getId());
                        record.setTaskId(taskId);
                        record.setTaskTitle(task.getTitle());
                        record.setAction("Tambah Tugas");
                        record.setActionAt(new java.util.Date());
                        new HistoryDAOImpl().add(record);
                    }
                }
            }

            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateTask(Task task) {
        String sql = "UPDATE task SET title=?, description=?, deadline=?, "
                + "lecturer_id=?, course_id=?, is_done=? "
                + "WHERE id=? AND user_id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());

            if (task.getDeadline() != null) {
                ps.setTimestamp(3, new Timestamp(task.getDeadline().getTime()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }

            ps.setInt(4, task.getLecturerId());
            ps.setInt(5, task.getCourseId());
            ps.setInt(6, task.isCompleted() ? 1 : 0);
            ps.setInt(7, task.getId());
            ps.setInt(8, Session.getUser().getId());

            int updated = ps.executeUpdate();

            if (updated > 0) {
                HistoryRecord record = new HistoryRecord();
                record.setUserId(Session.getUser().getId());
                record.setTaskId(task.getId());
                record.setTaskTitle(task.getTitle());
                record.setAction("Update Tugas");
                record.setActionAt(new java.util.Date());

                HistoryDAO historyDAO = new HistoryDAOImpl();
                historyDAO.add(record);
            }

            return updated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateTaskStatus(int taskId, boolean completed) {
        String sql = "UPDATE task SET is_done=? WHERE id=? AND user_id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, completed ? 1 : 0);
            ps.setInt(2, taskId);
            ps.setInt(3, Session.getUser().getId());

            int updated = ps.executeUpdate();

            if (updated > 0) {
                Task task = getTaskById(taskId);

                HistoryRecord record = new HistoryRecord();
                record.setUserId(Session.getUser().getId());
                record.setTaskId(taskId);
                record.setTaskTitle(task != null ? task.getTitle() : null);
                record.setAction(completed ? "Tandai Selesai" : "Tandai Belum Selesai");
                record.setActionAt(new java.util.Date());

                HistoryDAO historyDAO = new HistoryDAOImpl();
                historyDAO.add(record);
            }

            return updated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteTask(int taskId) {
        Task task = getTaskById(taskId);

        String sql = "DELETE FROM task WHERE id=? AND user_id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, Session.getUser().getId());

            int deleted = ps.executeUpdate();

            if (deleted > 0 && task != null) {
                HistoryRecord record = new HistoryRecord();
                record.setUserId(Session.getUser().getId());
                record.setTaskId(taskId);
                record.setTaskTitle(task.getTitle());
                record.setAction("Hapus Tugas");
                record.setActionAt(new java.util.Date());

                HistoryDAO historyDAO = new HistoryDAOImpl();
                historyDAO.add(record);
            }

            return deleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Task getTaskById(int id) {
        String sql = "SELECT * FROM task WHERE id=? AND user_id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, Session.getUser().getId());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));

                Timestamp ts = rs.getTimestamp("deadline");
                task.setDeadline(ts != null ? new java.util.Date(ts.getTime()) : null);

                task.setCompleted(rs.getInt("is_done") != 0);
                task.setLecturerId(rs.getInt("lecturer_id"));
                task.setCourseId(rs.getInt("course_id"));

                return task;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
public List<Task> getAllTasks() {
    List<Task> list = new ArrayList<>();
    String sql = "SELECT * FROM task WHERE user_id=? ORDER BY deadline";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, Session.getUser().getId());
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));

                Timestamp ts = rs.getTimestamp("deadline");
                task.setDeadline(ts != null ? new java.util.Date(ts.getTime()) : null);

                task.setCompleted(rs.getInt("is_done") != 0);
                task.setLecturerId(rs.getInt("lecturer_id"));
                task.setCourseId(rs.getInt("course_id"));

                list.add(task);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return list;
}

}
