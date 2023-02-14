import DataModels.Candidate;
import DataModels.Candidates;
import FileService.FileService;
import Server.BasicServer;
import Server.Encryption;
import Server.RouteHandler;
import Vote_Machine.Vote_Machine;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try{
            new Vote_Machine("localhost", 9889).start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}