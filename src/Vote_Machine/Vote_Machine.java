package Vote_Machine;

import FileService.FileService;
import Server.*;
import Users.*;
import com.sun.net.httpserver.HttpExchange;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static Server.Cookie.setCookie;

public class Vote_Machine extends BasicServer {
    public Vote_Machine(String host, int port) throws IOException {
        super(host, port);
        registerGet("/candidates", this::freemarkerCandidatesHandler);
        registerGet("/registration", this::registrationGet);
        registerGet("/login", this::loginGet);
        registerGet("/failedRegistration", this::failedRegistrationGet);
        registerPost("/registration", this::registrationPost);
        registerPost("/login", this::loginPost);
    }

    /**
     * Методы ниже указывают как обрабатывать запросы с методом GET.
     * Создает путь к файлу, который необходимо передать.
     * И передает данные методом sendFile с указанием типа передаваемых данных.
     */
    private void registrationGet(HttpExchange exchange){
        Path path = makeFilePath("/html/registration.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }
    private void loginGet(HttpExchange exchange){
        Path path = makeFilePath("/html/login.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }
    private void failedRegistrationGet(HttpExchange exchange){
        Path path = makeFilePath("/html/failedRegistration.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }
    /**
     * Методы ниже указывают как обрабатывать запросы с методом POST.
     * Создает путь к файлу, который необходимо передать.
     * И передает данные методом sendFile с указанием типа передаваемых данных.
     */
    /**
     * При получении POST запроса страницы регистрации, метод прочитает из базы всех пользователей, обработает тело запроса, получив
     * все данные из формы, проверит есть ли пользователь с таким же email, если нет, то создаст нового пользователя используя
     * данные из формы и установит куки, перенаправив пользователя на страницу голосования. Либо редирект на failedRegistration
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     */
    private void registrationPost(HttpExchange exchange){
        Users users = FileService.readJsonFile();

        String registrationInfo = getBody(exchange);
        Map<String, String> parsedInfo = Utils.parseUrlEncoded(registrationInfo, "&");

        if(!users.emailCheck(parsedInfo.get("email"))){

            String id = parsedInfo.get("email") + parsedInfo.get("name");
            String encryptedId = Encryption.encrypt(id, Encryption.key);

            setCookie(exchange, Cookie.make("id", encryptedId, 500));
            setCookie(exchange, Cookie.make("email", parsedInfo.get("email"), 500));

            users.addNewUser(new User(
                                parsedInfo.get("name"),
                                parsedInfo.get("surname"),
                                parsedInfo.get("password"),
                                parsedInfo.get("email"),
                                encryptedId)
            );

            renderTemplate(exchange, "candidates.html", getCandidatesModel());

            FileService.writeJson(users);

        }else {
            //здесь должна быть страница с помощью для регистрации
            redirect303(exchange, "/failedRegistration");
        }
    }

    /**
     * При получении POST запроса страницы авторизации, считаются данные из базы. Из тела запроса выделяются данные пользователя.
     * Производится проверка пароля и email. Если пользователь проходит проверку, то устанавливаются куки и производится переход
     * на страницу candidates.html. Если нет, то страница авторизации перезагрузится.
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     */
    private void loginPost(HttpExchange exchange){
        Users users = FileService.readJsonFile();

        String loginInfo = getBody(exchange);
        Map<String, String> parsedInfo = Utils.parseUrlEncoded(loginInfo, "&");

        if(users.emailAndPasswordCheck(parsedInfo.get("email"), parsedInfo.get("password"))){

            String encryptedId = Encryption.encrypt(users.getIdByEmail(parsedInfo.get("email")), Encryption.key);

            setCookie(exchange, Cookie.make("id", encryptedId, 500));
            setCookie(exchange, Cookie.make("email", parsedInfo.get("email"), 500));
            renderTemplate(exchange, "candidates.html", getCandidatesModel());

            users.setIdByEmail(encryptedId, parsedInfo.get("email"));
            FileService.writeJson(users);
        }else {
            //здесь должна быть страница с помощью при авторизации
            loginGet(exchange);
        }
    }



    private void freemarkerCandidatesHandler(HttpExchange exchange){
        renderTemplate(exchange, "candidates.html", getCandidatesModel());
    }
    private String getCandidatesModel(){
        return null;
    }

}
