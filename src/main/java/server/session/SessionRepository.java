package server.session;

import dao.to.UserTo;

public interface SessionRepository {

    void add(UserTo user);

    UserTo get(String userName);

    void remove(String userName);

    Boolean contains(String userName);
}
