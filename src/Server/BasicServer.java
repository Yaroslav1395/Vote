package Server;

import FileService.Freemarker;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    protected final Map<String, RouteHandler> getRoutes() {
        return routes;
    }

    /**
     * метод определяет какой обработчик вызывать если в адресе корень.
     *
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
     * следующие методы создают ключ для мап, по ключу которой получаем обработчик
     */
    //Принимает объект HttpExchange, который содержит метод, адрес, тело запроса.
    //Если адрес запроса кончается / и размер его больше 1, то уберет /.
    //Если адрес содержит точку, то возьмет из адреса все что после нее,
    //если нет, то оставит путь как есть и создаст ключ вернув его как строку
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
    //Метод возвращает готовый ключ, проверяя входные данные на корректность пути
    protected static String makeKey(String method, String route) {
        route = ensureStartsWithSlash(route);
        return String.format("%s %s", method.toUpperCase(), route);
    }
    //Метод необходим для проверки, правильный путь передали в метод или нет.
    //Проверит, начинается ли путь с точки, если да то вернет строку без изменений.
    //Если путь не начинается с точки, то проверит, начинается ли с /, если да, то вернет путь.
    //Если нет, то добавит в начало пути /.
    private static String ensureStartsWithSlash(String route){
        if (route.startsWith("."))
            return route;
        return route.startsWith("/") ? route : "/" + route;
    }


    /**
     * следующие методы нужны для добавления в мап ключа и обработчика
     * registerGet отвечает за Get запросы
     * registerPost отвечает за обработку Post запроса
     */
    //метод создаст ключ и новую запись в мап, добавив ключ и обработчик
    protected final void registerGenericHandler(String method, String route, RouteHandler handler) {
        getRoutes().put(makeKey(method, route), handler);
    }
    protected final void registerGet(String route, RouteHandler handler) {
        registerGenericHandler("GET", route, handler);
    }
    protected final void registerPost(String route, RouteHandler handler) {
        registerGenericHandler("POST", route, handler);
    }


    /**
     * Следующие методы необходимы для отправки ответа в случае если в запросе содержится путь к конкретному
     * файлу.
     */
    //поместит в мап расширение файла и обработчик который отправит файл
    protected final void registerFileHandler(String fileExt, ContentType type) {
        registerGet(fileExt, exchange -> sendFile(exchange, makeFilePath(exchange), type));
    }
    //Метод принимает HttpExchange, путь к файлу, и тип передаваемого файла.
    //Преобразует файл найденный по адресу в поток байт.
    protected final void sendFile(HttpExchange exchange, Path pathToFile, ContentType contentType) {
        try {
            //если такого пути нет ответом на запрос будет 404
            if (Files.notExists(pathToFile)) {
                respond404(exchange);
                return;
            }
            //преобразует файл который, найдет по адресу в байты
            var data = Files.readAllBytes(pathToFile);
            sendByteData(exchange, ResponseCodes.OK, contentType, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //метод отправляет данные клиенту принимая файл преобразованный в байты
    //и указывает тип передаваемых данных принимая contentType(хранится в перечислении)
    protected final void sendByteData(HttpExchange exchange, ResponseCodes responseCode,
                                      ContentType contentType, byte[] data) throws IOException {
        //получаем исходящий поток для отправки
        try (var output = exchange.getResponseBody()) {
            setContentType(exchange, contentType);
            exchange.sendResponseHeaders(responseCode.getCode(), 0);
            output.write(data);
            output.flush();
        }
    }
    //примет объект HttpExchange и получит из него адрес запроса
    private static void setContentType(HttpExchange exchange, ContentType type) {
        exchange.getResponseHeaders().set("Content-Type", String.valueOf(type));
    }
    //преобразует строку в путь добавив к пути директорию
    protected Path makeFilePath(String... s) {
        return Path.of(dataDir, s);
    }
    //примет объект HttpExchange и получит из него адрес запроса
    private Path makeFilePath(HttpExchange exchange) {
        return makeFilePath(exchange.getRequestURI().getPath());
    }


    private void handleIndexIncomingServerRequests(HttpExchange exchange) {
        var route = getRoutes().getOrDefault(makeKey(exchange), this::respond404);
        route.handle(exchange);
    }


    /**
     * следующие методы обрабатывают ошибки 404 и переадресацию
     */
    private void respond404(HttpExchange exchange) {
        try {
            var data = "404 Not found".getBytes();
            sendByteData(exchange, ResponseCodes.NOT_FOUND, ContentType.TEXT_PLAIN, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
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
     * предназначен для получения Query параметров в виде строки
     */
    protected String getQueryParams(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        return Objects.nonNull(query) ? query : "";
    }


    protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
        try {
            // загружаем шаблон из файла по имени.
            // шаблон должен находится по пути, указанном в конфигурации
            Template temp = freemarker.getTemplate(templateFile);

            // freemarker записывает преобразованный шаблон в объект класса writer
            // а наш сервер отправляет клиенту массивы байт
            // по этому нам надо сделать "мост" между этими двумя системами

            // создаём поток который сохраняет всё, что в него будет записано в байтовый массив
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // создаём объект, который умеет писать в поток и который подходит для freemarker
            try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {

                // обрабатываем шаблон заполняя его данными из модели
                // и записываем результат в объект "записи"
                temp.process(dataModel, writer);
                writer.flush();

                // получаем байтовый поток
                var data = stream.toByteArray();

                // отправляем результат клиенту
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
