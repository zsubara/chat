package server.auth;

import dao.to.UserTo;

import java.util.List;

public class Authenticator {

    public boolean validateCredentials(UserTo user, String pass) {
        return user == null || user.getPassword().equals(pass);
    }
}
