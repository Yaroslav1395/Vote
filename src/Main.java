import Vote_Machine.Vote_Machine;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try{
            new Vote_Machine("localhost", 9889).start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}