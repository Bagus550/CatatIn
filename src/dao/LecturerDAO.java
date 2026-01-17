package dao;

import model.Lecturer;
import java.util.List;

public interface LecturerDAO {

    boolean add(Lecturer lecturer);

    boolean update(Lecturer lecturer);

    boolean delete(int id, int userId);

    Lecturer getById(int id, int userId);

    List<Lecturer> getAll(int userId);

}
