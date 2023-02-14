package Users;

import Server.Encryption;

import java.util.ArrayList;
import java.util.List;

public class Users {
    private final List<User> users = new ArrayList<>();

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
     * Найдет пользователя по email, и проверяет его id, перед проверкой декодирует.
     * Если параметры пустые, вернет false.
     * @param userEmail - для поиска пользователя
     * @param decodedId - для сравнения
     * @return - если пользователь с таким email есть, вернет true
     */
    public boolean checkIdByEmail(String userEmail, String decodedId){
        if(userEmail == null || decodedId == null) return false;
        for (User user: users) {
            if(user.getEmail().equals(userEmail)){
                return user.getId().equals(decodedId);
            }
        }
        return false;
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
            if(user.getEmail().equals(userEmail)){
                user.setId(id);
                break;
            }
        }
    }

    /**
     * Установит статус голоса найдя пользователя по email
     * @param userEmail - для поиска
     */
    public void setIsVotedStatusToUserWithEmail(String userEmail){
        for (User user: users) {
            if(user.getEmail().equals(userEmail)){
                user.setVoted(true);
                break;
            }
        }
    }

    /**
     * Метод проверяет, голосовал пользователь или нет. Если userEmail пустой, вернет true.
     * @param userEmail - для поиска пользователя
     * @return - возвращает false если не голосовал
     */
    public boolean checkVoidStatusByEmail(String userEmail){
        if(userEmail == null) return true;
        for (User user: users) {
            if(user.getEmail().equals(userEmail)){
                return user.isVoted();
            }
        }
        return true;
    }
    /**
     * Добавит нового пользователя
     * @param user - пользователь которого нужно добавить
     */
    public void addNewUser(User user){
        users.add(user);
    }

}
