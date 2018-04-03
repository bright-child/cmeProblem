package org.jynergy.cme.server;

/*
 * Copyright (c) 2018.
 * Licensed under LPGL v3 (http://www.gnu.org/licenses/lgpl.txt) or Apache License v2.0 (http://www.apache.org/licenses/LICENSE-2.0)
 */

import org.jynergy.cme.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles all communication with a client {@link #clientName}
 */
public class ServerThread extends Thread implements Protocol {
    /**
     * The client name
     */
    private String clientName;

    /**
     * Sockec that handles connection with a client
     */
    private Socket socket;

    /**
     * Handles all writing to the clent
     */
    private PrintWriter printWriter;

    /**
     * Handles all reading from the client
     */
    private BufferedReader bufferedReader;

    /**
     * A reference to {@link ChatServer}
     */
    private ChatServer chatServer;

    /**
     * Handles the initialization of this obect
     *
     * @param socket {@link #socket}
     * @param printWriter  {@link @printWriter}
     * @param bufferedReader {@link #bufferedReader}
     * @param chatServer    {@link #chatServer}
     */
    public ServerThread( Socket socket,
                         PrintWriter printWriter,
                         BufferedReader bufferedReader,
                         ChatServer chatServer ){
        this.socket = socket;
        this.printWriter = printWriter;
        this.bufferedReader = bufferedReader;
        this.chatServer = chatServer;
    }

    /**
     * sets {@link #clientName}
     * @param clientName {@link #clientName}
     */
    public void setClientName( String clientName ) {
        this.clientName = clientName;
    }

    /**
     * @return {@link #printWriter}
     */
    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    /**
     * Handles {@link #MESSAGE} messages
     */
    public void run() {
        try{
            while ( !socket.isClosed() ) {
                String line = bufferedReader.readLine();
                if ( line != null ) {
                    switch ( line.charAt( 0 ) ) {

                        case MESSAGE:
                            line = line.substring( 1, line.length() );
                            int seperatorPosition = line.indexOf( SEPERATOR );
                            String whoTo = line.substring( 0, seperatorPosition );
                            line = line.substring( seperatorPosition + 1, line.length() );
                            synchronized ( chatServer.getServerSocket() ) {
                                if ( !whoTo.equals( ALL ) ) {
                                    line = MESSAGE + line;
                                    chatServer.getClientThreadHashMap().get( whoTo ).printWriter.println( line );
                                }
                                else{
                                    line = MESSAGE + line;
                                    for ( String name : chatServer.getClientThreadHashMap().keySet()){
                                        chatServer.getClientThreadHashMap().get( name ).printWriter.println( line );
                                    }
                                }
                            }
                            break;
                    }
                }
                else{
                    break;
                }
            }
        }
        catch ( IOException ioException ) {
            ioException.printStackTrace();
        }
        System.out.println( "ChatServer: disconnect " + clientName );
        chatServer.removeServerThread( clientName );
        chatServer.sendUserListToAll();
    }
}
