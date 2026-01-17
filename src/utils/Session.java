package utils;

import model.User;

public class Session {
    private static User currentUser;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static int getUserId() {
        if (currentUser == null) {
            throw new IllegalStateException("User belum login!");
        }
        return currentUser.getId();
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}
