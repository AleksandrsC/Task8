package com.accenture.bootcamp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simple telnet chat class
 *
 */
public class TelnetChatWorker implements Runnable {
    private final Logger log = LoggerFactory.getLogger(TelnetChatWorker.class);
    private final Socket socket;
    PrintWriter out;
    private final ConcurrentLinkedQueue<String> messagesReceived=new ConcurrentLinkedQueue<>();


    private static final  ConcurrentHashMap<String,String> nicknames=new ConcurrentHashMap<>();
    private static List<TelnetChatWorker> workers =new ArrayList<TelnetChatWorker>();

    private TelnetChatWorker(Socket sock) {
        socket = sock;
    }

    /**
     * Initializes the server.
     * Possible could be in its own separate class, don't know if unfortunate method placement
     * or making workers package-private
     * or smth. else
     * is a better option
     * @param port port the workers are listening tp
     */
    public static void initialize(int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                   /*
                    a bit of a fragility in the code here, don't try smth. like
                    try(Socket s=server.accept()){
                        TelnetChatWorker worker=new TelnetChatWorker(s);
                        ...
                    the socket would be closed immediately.
                    Have no idea what's that about...
                    */
                    TelnetChatWorker worker=new TelnetChatWorker(server.accept());
                    workers.add(worker);
                    new Thread(worker).start(); //tell during the demo.
                    LoggerFactory.getLogger(TelnetChatWorker.class).info("server thread init complete.");

            }
        } catch (Exception x) {
            LoggerFactory.getLogger(TelnetChatWorker.class).error("server thread init error:", x);
        }

    }

    private void broadcastMessage(String message){
        for(TelnetChatWorker worker: workers) {
            worker.messagesReceived.add(message);
            worker.sendMessages();
        }
    }
    private void sendMessages(){
        try{
            String next=messagesReceived.poll();
            while(next!=null){
                out.println(next);
                next=messagesReceived.poll();
            }
            out.flush();
        } catch (Exception e) {
            log.error("server thread error:", e);
        }
    }

    /**
     * @see Thread#run()
     */
    @Override
    public void run() {
        log.info("thread started.");
        try (InputStream is = (socket.getInputStream());) {
            out=new PrintWriter(socket.getOutputStream());
            out.println("Welcome to TELNET demo chat. Type a message starting with 'Bye' to exit.");
            out.println("Type '/nick <word>' to rename yourself.");
            out.flush();
            Scanner scn = new Scanner(is);
            String idString=""+socket.getInetAddress() + ":" + socket.getPort();
            scn.useDelimiter("\\n");
            while (scn.hasNext()) {
                String nick="["+nicknames.getOrDefault(idString,idString)+"] ";
                String chat=scn.next();
                broadcastMessage(nick + chat);
                if(chat.startsWith("Bye")){
                    break;
                }else if(chat.startsWith("/nick ")){
                    log.info("nick change req.");
                    Scanner nickScanner=new Scanner(chat);
                    nickScanner.next(); // /nick itself
                    String newNick=nickScanner.next();
                    log.info("ID:"+idString+"nick:"+newNick);
                    nicknames.put(idString, newNick);
                    log.info("nicknames:"+nicknames);
                }
            }
        } catch (IOException e) {
            log.error("server thread error:", e);
        }
        out.close();
        workers.remove(this);
    }//run
}
