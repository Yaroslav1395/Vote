package Server;

import FileService.Freemarker;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public class BasicServer {
    private final HttpServer server;
    private final String dataDir = "data";
    private final static Configuration freemarker = Freemarker.initFreeMarker();

    //routes хранит путь запроса и обработчик, который выполнит действия
    public static final Map<String, RouteHandler> routes = new HashMap<>();
    protected BasicServer(String host, int port) throws IOException {
        server = createServer(host, port);
        registerAddressRequestWithHandlers();
    }

    private static HttpServer createServer(String host, int post) throws IOException{
        var format = "Starting server on http://%s:%s/%n";
        System.out.printf(format, host, post);

        var addressSocket = new InetSocketAddress(host, post);
        return HttpServer.create(addressSocket, 50);
    }

    /**
     * метод устанавливает контекст для сервера, по которому сервер будет определять, какой
     * файл нужно отправить на запрос. Так же, регистрирует основные пути, для передачи файлов
     * сss, html, jpeg, pnd и обработчик для корневого запроса /.
     */
    private void registerAddressRequestWithHandlers() {
        server.createContext("/", this::handleIndexIncomingServerRequests);

        registerGet("/", exchange -> sendFile(exchange, makeFilePath("index.html"), ContentType.TEXT_HTML));

        registerFileHandler(".css", ContentType.TEXT_CSS);
        registerFileHandler(".html", ContentType.TEXT_HTML);
        registerFileHandler(".jpeg", ContentType.IMAGE_JPEG);
        registerFileHandler(".png", ContentType.IMAGE_PNG);

    }


    /**
     * Принимает адрес запроса и проверяет, начинается ли запрос с точки. Если да, то вернет адрес запроса,
     * если нет, то проверит, начинается ли запрос с /. Если нет, то добавит к адресу запроса /. Метод необходим
     * для проверки корректности адреса запроса.
     * @param route - адрес запроса
     * @return - вернет строку
     */
    private static String ensureStartsWithSlash(String route){
        if (route.startsWith("."))
            return route;
        return route.startsWith("/") ? route : "/" + route;
    }
    /**
     * Принимает в качестве параметров метод запроса и адрес запроса, преобразуя их в
     * строку, которая будет использоваться в качестве ключа.
     * @param method - строковое представление метода, с которым клиент запрашивает данные
     * @param route - адрес, по которому клиент запрашивает данные
     * @return - вернет строку в виде - пример: Метод(GET/POST) адрес(/image.jpeg)
     */
    protected static String makeKey(String method, String route) {
        route = ensureStartsWithSlash(route);
        return String.format("%s %s", method.toUpperCase(), route);
    }
    /**
     * Из объекта HttpExchange получает метод запроса и адрес запроса. Если в конце адреса запроса имеется
     * / и его длина больше 1 (для проверки не просит ли клиент корневой файл), то уберет с конца /. Затем
     * получит индекс ".". Если индекс "." не равен -1 - то есть "." есть, то обрежет адрес запроса от точки до
     * конца, если нет, то оставит запрос как есть.
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     * @return - вернет строку в виде Метод(GET/POST) адрес(/image.jpeg)
     */
    private static String makeKey(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        int index = path.lastIndexOf(".");
        String extOrPath = index != -1 ? path.substring(index).toLowerCase() : path;
        return makeKey(method, extOrPath);
    }

    /**
     * @return - возвращает мап, хранящий в качестве ключа Метод(GET/POST) адрес(/image.jpeg) и
     * обработчик для данного ключа
     */
    protected final Map<String, RouteHandler> getRoutes() {
        return routes;
    }
    /**
     * Приняв параметры добавит в мап, хранящую в качестве ключа Метод(GET/POST) адрес(/image.jpeg) и
     * обработчик для данного ключа, новую запись.
     * @param method - строковое представление метода, с которым клиент запрашивает данные
     * @param route - адрес запроса
     * @param handler - обработчик
     */
    //метод создаст ключ и новую запись в мап, добавив ключ и обработчик
    protected final void registerGenericHandler(String method, String route, RouteHandler handler) {
        getRoutes().put(makeKey(method, route), handler);
    }

    /**
     * Создаст ключ в виде GET адрес, и установит обработчик в качестве значения
     * @param route - адрес запроса
     * @param handler - обработчик
     */
    protected final void registerGet(String route, RouteHandler handler) {
        registerGenericHandler("GET", route, handler);
    }
    /**
     * Создаст ключ в виде POST адрес, и установит обработчик в качестве значения
     * @param route - адрес запроса
     * @param handler - обработчик
     */
    protected final void registerPost(String route, RouteHandler handler) {
        registerGenericHandler("POST", route, handler);
    }
    /**
     * Через объект HttpExchange получим доступ к заголовкам ответа и установим Content-Type
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     * @param type - заголовок Content-Type обозначающий тип передаваемого контента
     */
    private static void setContentType(HttpExchange exchange, ContentType type) {
        exchange.getResponseHeaders().set("Content-Type", String.valueOf(type));
    }
    /**
     * Метод откроет исходящий поток доступный у объекта HttpExchange.
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     * @param responseCode - код для клиента, обозначающий статус ответа.
     * @param contentType - тип передаваемого контента, указывающий клиенту, какой тип данных он будет принимать
     * @param data - поток байт, который отправится в теле ответа
     * @throws IOException - ошибка возникает, если невозможно получить тело ответа
     */
    protected final void sendByteData(HttpExchange exchange, ResponseCodes responseCode,
                                      ContentType contentType, byte[] data) throws IOException {
        try (var output = exchange.getResponseBody()) {
            setContentType(exchange, contentType);
            exchange.sendResponseHeaders(responseCode.getCode(), 0);
            output.write(data);
            output.flush();
        }
    }
    /**
     * Метод проверит, существует ли файл по пути который передан в качестве параметра. Если пути нет, то отправит клиенту
     * ответ с кодом 404. Если файл существует, то преобразует его в поток байт. Затем вызовет метод sendByteData()
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     * @param pathToFile - путь к файлу который необходимо передать (файл по данному пути преобразется в байты)
     * @param contentType - тип передаваемого контента (передается в метод sendByteData()).
     */
    protected final void sendFile(HttpExchange exchange, Path pathToFile, ContentType contentType) {
        try {
            if (Files.notExists(pathToFile)) {
                respond404(exchange);
                return;
            }

            var data = Files.readAllBytes(pathToFile);

            sendByteData(exchange, ResponseCodes.OK, contentType, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Поле dataDir хранит путь к папке, где содержатся все файлы html.
     * @param s адрес к файлу, который берется из запроса
     * @return возвращает путь к файлу
     */
    protected Path makeFilePath(String... s) {
        return Path.of(dataDir, s);
    }

    /**
     * Взяв из запроса, адрес по которому обращается клиент и корневой каталог, создает путь к файлу.
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     * @return - возвращает адрес файла, совместив корневой каталог и адрес запроса
     */
    //примет объект HttpExchange и получит из него адрес запроса
    private Path makeFilePath(HttpExchange exchange) {
        return makeFilePath(exchange.getRequestURI().getPath());
    }
    /**
     * Создаст новую запись в мапе, в виде ключа (GET .расширение файла) и установит обработчик,
     * который отправит данные.
     * @param fileExt - расширение файла
     * @param type - тип передаваемых данных
     */
    protected final void registerFileHandler(String fileExt, ContentType type) {
        registerGet(fileExt, exchange -> sendFile(exchange, makeFilePath(exchange), type));
    }

    /**
     * Метод создает ключ из запроса для поиска обработчика. Если такой обработчик есть, то вызовется
     * метод обработки, если нет, то отправит сообщение 404.
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     */
    private void handleIndexIncomingServerRequests(HttpExchange exchange) {
        var route = getRoutes().getOrDefault(makeKey(exchange), this::respond404);
        route.handle(exchange);
    }
    /**
     * Получит из тела запроса входящий поток. Из входящего потока преобразует поток сырых байт
     * в символы. Через BufferedReader считает построчно и соединит все в одну строку.
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     * @return вернет тело запроса в виде строки.
     */
    protected String getBody(HttpExchange exchange){
        InputStream input = exchange.getRequestBody();
        InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(isr)){
            return reader.lines().collect(joining(""));
        }catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }
    /**
     * В случае если по ключу в мапе ничего не найдено, то отправит ошибку 404
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     */
    private void respond404(HttpExchange exchange) {
        try {
            var data = "404 Not found".getBytes();
            sendByteData(exchange, ResponseCodes.NOT_FOUND, ContentType.TEXT_PLAIN, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод устанавливает по какому пути необходимо осуществить переход.
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     * @param path- путь, по которому будет произведен редирект.
     */
    protected void redirect303(HttpExchange exchange, String path){
        try {
            exchange.getResponseHeaders().add("Location", path);
            exchange.sendResponseHeaders(303, 0);
            exchange.getResponseBody().close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Получает из HttpExchange Query параметрами и если они есть вернет их
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     * @return венет строку с Query параметрами
     */
    protected String getQueryParams(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        return Objects.nonNull(query) ? query : "";
    }

    /**
     * Метод загрузит шаблон из файла переданного в параметры. Создает поток который, сохраняет всё, что в него
     * будет записано в байтовый массив. Создаем поток, который умеет записывать. Обрабатываем шаблон заполняя
     * его данными. Отправляем клиенту.
     * @param exchange - объект HttpExchange хранящий заголовки, методы, и тело(запроса, ответа).
     * @param templateFile - название файла, который нужно обработать.
     * @param dataModel - объект из которого будут браться данные для заполнения.
     */
    protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
        try {

            Template temp = freemarker.getTemplate(templateFile);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
                temp.process(dataModel, writer);
                writer.flush();
                var data = stream.toByteArray();
                sendByteData(exchange, ResponseCodes.OK, ContentType.TEXT_HTML, data);
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }
    public final void start() {
        server.start();
    }
}
