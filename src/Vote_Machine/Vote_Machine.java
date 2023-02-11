package Vote_Machine;

import Server.BasicServer;

import java.io.IOException;

public class Vote_Machine extends BasicServer {
    protected Vote_Machine(String host, int port) throws IOException {
        super(host, port);
        registerGet("/sample", this::freemarkerCandidatesHandler);
    }


}
