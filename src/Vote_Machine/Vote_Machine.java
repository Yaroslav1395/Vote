package Vote_Machine;

import Server.BasicServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class Vote_Machine extends BasicServer {
    public Vote_Machine(String host, int port) throws IOException {
        super(host, port);
        registerGet("/candidates", this::freemarkerCandidatesHandler);
    }

    private void freemarkerCandidatesHandler(HttpExchange exchange){
        renderTemplate(exchange, "candidates.html", getCandidatesModel());
    }
    private String getCandidatesModel(){
        return null;
    }

}
