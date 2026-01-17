package model;

public class Lecturer {
    private int id;
    private int userId;
    private String name;

    public Lecturer() {}

    public Lecturer(int id, int userId, String name) {
        this.id = id;
        this.userId = userId;
        this.name = name;
    }

    // getter & setter
    public int getId() { 
        return id; 
    }

    public void setId(int id) { 
        this.id = id; 
    }

    public int getUserId() { 
        return userId; 
    }

    public void setUserId(int userId) { 
        this.userId = userId; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    @Override
    public String toString() {
        return name;
    }
}
