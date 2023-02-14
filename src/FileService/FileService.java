package FileService;

import DataModels.Candidates;
import Users.Users;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileService {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Users readJsonUsers(){
        Path parsedPath = Paths.get("data/json/users.json");
        String fileContents;
        try {
            fileContents = Files.readString(parsedPath);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        return gson.fromJson(fileContents, Users.class);
    }

    public static void writeJsonUsers(Users users){
        String json = gson.toJson(users);
        Path parsedPath = Paths.get("data/json/users.json");
        try {
            byte[] bytes = json.getBytes();
            Files.write(parsedPath, bytes);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static Candidates readJsonCandidates(){
        Path parsedPath = Paths.get("data/json/candidates.json");
        String fileContents;
        try {
            fileContents = Files.readString(parsedPath);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        return gson.fromJson(fileContents, Candidates.class);
    }

    public static void writeJsonCandidates(Candidates candidates){
        String json = gson.toJson(candidates);
        Path parsedPath = Paths.get("data/json/candidates.json");
        try {
            byte[] bytes = json.getBytes();
            Files.write(parsedPath, bytes);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
