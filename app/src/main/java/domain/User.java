package domain;

import androidx.annotation.NonNull;

public class User {
    private String login;;
    private long id;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {

        return "User{"+
                "login"+ login+'\''+
                ",id"+id+
                '}';
    }
}
