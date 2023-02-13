package Users;

import java.util.ArrayList;
import java.util.List;

public class Users {
    private List<User> users = new ArrayList<>();

    /**
     * Метод для поиска пользователя в списке и возврата его копии
     * @param userEmail параметр содержит email по которому будет происходить сравнение при поиске
     * @return вернет копию найденного пользователя или null если пользователь не найден
     */
    public User getUserByEmail(String userEmail){
        User userForCopy = users.stream()
                .filter(user -> user.getEmail().equals(userEmail))
                .findFirst()
                .orElse(null);

        if(userForCopy == null) return null;

        return new User(userForCopy);
    }

    /**
     * Вернет true если найдет пользователя с таким же email, переданным в параметры.
     * @param userEmail - для сравнения в списке всех пользователей
     * @return - верент true или false
     */
    public boolean emailCheck(String userEmail){
        return users.stream()
                .anyMatch(user -> user.getEmail().equals(userEmail));
    }

    /**
     * Вернет true если найдет пользователя с таким же email и паролем, переданным в параметры.
     * @param userEmail- для сравнения в списке всех пользователей
     * @param userPassword- для сравнения в списке всех пользователей
     * @return - верент true или false
     */
    public boolean emailAndPasswordCheck(String userEmail, String userPassword){
        return  users.stream()
                .anyMatch(user -> user.getEmail().equals(userEmail) && user.getPassword().equals(userPassword));
    }

    /**
     *
     * @param userEmail - для сравнения в списке всех пользователей
     * @return вернет id найденного пользователя или пустую строку
     */
    public String getIdByEmail(String userEmail){
        for (User user: users) {
            if(user.getId().equals(userEmail)){
                return user.getId();
            }
        }
        return "";
    }

    /**
     * Произведет поиск пользователя по email и установит значение id
     * @param id - для записи значения в поле id
     * @param userEmail - для сравнения в списке всех пользователей
     */
    public void setIdByEmail(String id, String userEmail){
        for (User user: users) {
            if(user.getId().equals(userEmail)){
                user.setId(id);
                break;
            }
        }
    }

    /**
     * Добавит нового пользователя
     * @param user - пользователь которого нужно добавить
     */
    public void addNewUser(User user){
        users.add(user);
    }

}
