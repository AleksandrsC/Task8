package com.accenture.bootcamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    static Logger log= LoggerFactory.getLogger(Main.class);
    public static void main(String[] args){
        System.out.println("things work");
        log.info("logging works too");
        TelnetChatWorker.initialize(3456);

    }
}
