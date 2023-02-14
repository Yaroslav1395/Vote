package Vote_Machine;

import DataModels.Candidate;
import DataModels.Candidates;
import FileService.FileService;
import Server.*;
import Users.*;
import com.sun.net.httpserver.HttpExchange;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static Server.Cookie.getCookies;
import static Server.Cookie.setCookie;

public class Vote_Machine extends BasicServer {
    public Vote_Machine(String host, int port) throws IOException {
        super(host, port);
        registerGet("/candidates", this::candidatesGet);
        registerGet("/registration", this::registrationGet);
        registerGet("/login", this::loginGet);
        registerGet("/votes", this::votesGet);
        registerGet("/thankyou", this::thankYouGet);
        registerGet("/failedRegistration", this::failedRegistrationGet);
        registerPost("/registration", this::registrationPost);
        registerPost("/login", this::loginPost);
        registerPost("/candidates", this::candidatesPost);
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
     * Обработчик запроса candidates, проверит, совпадает ли значение куки из запроса, со значением
     * установленным у пользователя. Поиск пользователя производится по email.
     * Если значение совпадает, то установит статус пользователя в объекте candidates, что позволит
     * отображать в шаблоне действия для авторизованных пользователей
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     */
    private void candidatesGet(HttpExchange exchange){
        Candidates candidates = FileService.readJsonCandidates();
        Users users = FileService.readJsonUsers();

        String cookiesAsString = getCookies(exchange);

        Map<String, String> parsedCookies = Cookie.parse(cookiesAsString);

        String encodedId = null;
        String userEmail = null;

        if(parsedCookies.get("id") != null){
            encodedId = Encryption.decrypt(parsedCookies.get("id"), Encryption.key);
            userEmail = parsedCookies.get("email");
        }

        if(users.checkIdByEmail(userEmail, encodedId) && !users.checkVoidStatusByEmail(userEmail)){
            candidates.setUserStatus(true);
        }

        renderTemplate(exchange, "/candidates.html", candidates);
    }

    /**
     * Метод из куки получает id кандидата, после чего отправляет страницу с отображением кандидата, за которого проголосовали
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     */
    private void thankYouGet(HttpExchange exchange){
        Candidates candidates = FileService.readJsonCandidates();

        String cookiesAsString = getCookies(exchange);

        Map<String, String> parsedCookies = Cookie.parse(cookiesAsString);

        int candidateIndex = Integer.parseInt(parsedCookies.get("candidateId"));

        candidates.takeCandidatesPercentage();

        renderTemplate(exchange, "/thankyou.html", candidates.getCandidateByIndex(candidateIndex));
    }

    /**
     * Отобразит страницу по запросу votes, передав туда список с участниками
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     */
    private void votesGet(HttpExchange exchange){
        Candidates candidates = FileService.readJsonCandidates();
        candidates.takeCandidatesPercentage();
        candidates.sortCandidatesByVotes();
        List<Candidate> candidateList = candidates.getCandidateList();
        candidateList.forEach(System.out::println);
        renderTemplate(exchange, "/votes.html", candidates);
    }

    /**
     * При получении POST запроса страницы регистрации, метод прочитает из базы всех пользователей, обработает тело запроса, получив
     * все данные из формы, проверит есть ли пользователь с таким же email, если нет, то создаст нового пользователя используя
     * данные из формы и установит куки, перенаправив пользователя на страницу голосования. Либо редирект на failedRegistration
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     */
    private void registrationPost(HttpExchange exchange){
        Users users = FileService.readJsonUsers();

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
                                id)
            );

            redirect303(exchange, "/candidates");

            FileService.writeJsonUsers(users);

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
        Users users = FileService.readJsonUsers();

        String loginInfo = getBody(exchange);
        Map<String, String> parsedInfo = Utils.parseUrlEncoded(loginInfo, "&");

        if(users.emailAndPasswordCheck(parsedInfo.get("email"), parsedInfo.get("password"))){
            String id = parsedInfo.get("email") + parsedInfo.get("password");
            String encryptedId = Encryption.encrypt(id, Encryption.key);

            setCookie(exchange, Cookie.make("id", encryptedId, 500));
            setCookie(exchange, Cookie.make("email", parsedInfo.get("email"), 500));

            users.setIdByEmail(id, parsedInfo.get("email"));
            FileService.writeJsonUsers(users);

            redirect303(exchange, "/candidates");
        }else {
            //здесь должна быть страница с помощью при авторизации
            loginGet(exchange);
        }
    }

    /**
     * При ополчении POST запроса со страницы candidates, из тела запроса выделится индекс кандитата, и прибавится
     * один балл. По значению email из Кука найдется пользователь, и изменится статус его голоса. Осуществится переход
     * на страницу с отображением кандидата за которого проголосовали.
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     */
    private void candidatesPost(HttpExchange exchange){
        Users users = FileService.readJsonUsers();
        Candidates candidates = FileService.readJsonCandidates();

        String loginInfo = getBody(exchange);
        Map<String, String> parsedInfo = Utils.parseUrlEncoded(loginInfo, "&");

        String cookiesAsString = getCookies(exchange);
        Map<String, String> parsedCookies = Cookie.parse(cookiesAsString);

        if(!users.checkVoidStatusByEmail(parsedCookies.get("email"))){
            candidates.plusPointToCandidateWithIndex(Integer.parseInt(parsedInfo.get("candidateId")));
            users.setIsVotedStatusToUserWithEmail(parsedCookies.get("email"));
            FileService.writeJsonCandidates(candidates);
            FileService.writeJsonUsers(users);
        }

        setCookie(exchange, Cookie.make("candidateId", parsedInfo.get("candidateId"), 50));

        redirect303(exchange, "/thankyou");
    }



}
