package dao;

import model.Task;
import java.util.List;

public interface TaskDAO {

    boolean addTask(Task task);

    boolean updateTask(Task task);

    boolean deleteTask(int taskId);

    boolean updateTaskStatus(int taskId, boolean completed);

    Task getTaskById(int taskId);

    List<Task> getAllTasks();
}
