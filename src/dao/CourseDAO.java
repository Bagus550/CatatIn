package dao;

import model.Course;
import java.util.List;

public interface CourseDAO {

    boolean add(Course course);

    boolean update(Course course);

    boolean delete(int id, int userId);

    Course getById(int id, int userId);

    List<Course> getAll(int userId);
}
