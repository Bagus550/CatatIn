package dao;

import model.User;

public interface UserDAO {
    boolean register(User user);
    boolean usernameExists(String username);
    User login(String username, String password);
}
