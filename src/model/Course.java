package model;

public class Course {
    private int id;
    private int userId;
    private String name;
    private int lecturerId;

    public Course() {}

    public Course(int id, int userId, String name, int lecturerId) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.lecturerId = lecturerId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLecturerId() { return lecturerId; }
    public void setLecturerId(int lecturerId) { this.lecturerId = lecturerId; }

    @Override
    public String toString() {
        return name;
    }
}
