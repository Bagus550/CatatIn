package dao;

import model.HistoryRecord;
import java.util.List;

public interface HistoryDAO {

    boolean add(HistoryRecord record);

    List<HistoryRecord> getAllByUser(int userId);

    boolean deleteAllByUser(int userId);
}
