package Users;

import javax.crypto.SecretKey;

public class User {
    private String name;
    private String surname;
    private String password;
    private String email;
    private String id;
    private boolean isVoted;


    public User() {
    }

    public User(String name, String surname, String password, String email, String id) {
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.email = email;
        this.id = id;
        isVoted = false;
    }
    public User(User user){
        name = user.getName();
        surname = user.getSurname();
        password = user.getPassword();
        email = user.getEmail();
        id = user.getId();
        isVoted = user.isVoted();
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public boolean isVoted() {
        return isVoted;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVoted(boolean voted) {
        isVoted = voted;
    }


    @Override
    public String toString() {
        return "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", id='" + id + '\'' +
                ", isVoted=" + isVoted +
                '}';
    }
}
