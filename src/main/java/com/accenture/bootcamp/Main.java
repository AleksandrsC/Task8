package com.accenture.bootcamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        int port = 2323;//TELNET
        if (args.length > 0)
            try {
                port = Integer.parseUnsignedInt(args[0]);
            } catch (Exception x) {
                log.error("failed to parse the port number, using " + port);
            }
        TelnetChatWorker.initialize(port);

    }
}
