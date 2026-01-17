package model;

import java.util.Date;

public class Task {
    private int id;
    private String title;
    private String description;
    private Date deadline;
    private boolean completed;
    private int courseId;
    private int lecturerId;
    private String lecturerName;
    private String courseName;

    public Task() {}

    public Task(int id, String title, String description, Date deadline,
                boolean completed, int courseId, int lecturerId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.completed = completed;
        this.courseId = courseId;
        this.lecturerId = lecturerId;
    }

    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }

    public String getTitle() { 
        return title; 
    }
    public void setTitle(String title) { 
        this.title = title; 
    }

    public String getDescription() { 
        return description; 
    }
    public void setDescription(String description) { 
        this.description = description; 
    }

    public Date getDeadline() { 
        return deadline; 
    }
    public void setDeadline(Date deadline) { 
        this.deadline = deadline; 
    }

    public boolean isCompleted() { 
        return completed; 
    }
    public void setCompleted(boolean completed) { 
        this.completed = completed; 
    }

    public int getCourseId() { 
        return courseId; 
    }
    public void setCourseId(int courseId) { 
        this.courseId = courseId; 
    }

    public int getLecturerId() { 
        return lecturerId; 
    }
    public void setLecturerId(int lecturerId) { 
        this.lecturerId = lecturerId; 
    }
    
    public String getLecturerName() {
    return lecturerName;
    }
    
    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getCourseName() {
        return courseName;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
