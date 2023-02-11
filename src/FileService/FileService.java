package FileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileService {
    //перед созданием экземпляра необходимо подключить библиотеку
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();//позволяет привести в читабельный вид

    public static Library readJsonFile(){
        Path parsedPath = Paths.get("data/gson/library.json");//преобразует String путь в Path путь
        String fileContents;
        try {
            fileContents = Files.readString(parsedPath);//Класс Files предоставляет возможность считать файлы
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        //библиотека Gson дает возможность записать данные в класс
        //gson.fromJson(строка с данными из класса, Класс в который нужно записать)
        return gson.fromJson(fileContents, Library.class);
    }

    public static void writeJson(Library library){
        String json = gson.toJson(library);//метод преобразует класс в строку для записи в json
        Path parsedPath = Paths.get("data/gson/library.json");//преобразует String путь в Path путь
        try {
            byte[] bytes = json.getBytes();//для записи необходимо получить битовое значение строки
            Files.write(parsedPath, bytes);//передаем путь к файлу типом данных Path и битовое значение строки
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
