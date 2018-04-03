package org.jynergy.cme.server;

/*
 * Copyright (c) 2018.
 * Licensed under LPGL v3 (http://www.gnu.org/licenses/lgpl.txt) or Apache License v2.0 (http://www.apache.org/licenses/LICENSE-2.0)
 */

import org.jynergy.cme.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * A ChatServer for client/server application
 */
public class ChatServer implements Protocol{
    /**
     * {@link ServerSocket} to handle client connections
     */
    private ServerSocket  serverSocket;

    /**
     * holds the client user name and {@link ServerThread}
     */
    private HashMap<String,ServerThread> clientThreadHashMap =
                new HashMap<String, ServerThread>( );

    /**
     * Connects the {@link ServerSocket} on port number
     *
     * @param portNumber the portNumber to connect to
     */
    public ChatServer(int portNumber) {
        try {
            InetAddress inetAddress = InetAddress.getByName( null );
            serverSocket = new ServerSocket( portNumber, 20, inetAddress );
            System.out.println( "ChatServer: serverSocket localhost:" + portNumber );
            acceptClients();
        }
        catch ( IOException ioException ) {
            ioException.printStackTrace();
            System.err.println( "Could not open socket on port: " + portNumber );
        }
        if ( serverSocket != null || !serverSocket.isClosed() ){
           close();
        }
        System.exit( 0 );
    }


    /**
     * Connects {@link ServerSocket}
     *
     * @param portNumber on port number
     * @param serverName on server name
     */
    public ChatServer( int portNumber,
                       String serverName ) {
        try {
            InetAddress inetAddress = null;
            StringTokenizer stringTokenizer = new StringTokenizer( serverName, "." );
            if ( stringTokenizer.countTokens() == 3 ){
                byte[] byteArray = new byte[4];
                byteArray[0] = Byte.parseByte( stringTokenizer.nextToken() );
                byteArray[1] = Byte.parseByte( stringTokenizer.nextToken() );
                byteArray[2] = Byte.parseByte( stringTokenizer.nextToken() );
                byteArray[4] = Byte.parseByte( stringTokenizer.nextToken() );
                inetAddress = InetAddress.getByAddress( byteArray );
            }
            else{
                inetAddress = InetAddress.getByName( serverName );
            }

            System.out.println( "ChatServer: serverSocket " + inetAddress.getHostAddress() +":" + portNumber );
            serverSocket = new ServerSocket( portNumber, 20, inetAddress );

            acceptClients();
        }
        catch ( IOException ioException ) {
            ioException.printStackTrace();
            System.err.println( "Could not open socket on port: " + portNumber );
        }
        if ( serverSocket != null || !serverSocket.isClosed() ) {
            close();
        }
        System.exit( 0 );
    }

    /**
     * @return {@link #serverSocket}
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Handles initial setup
     * <UL>
     *     <LI>checks that to many users are about to logon</LI>
     *     <LI>checks that the client name has not been used</LI>
     *     <LI>Starts {@link ServerThread} and {@link #sendUserListToAll()}</LI>
     * </UL>
     */
    public void acceptClients(){
        while ( true ){
            try{
                Socket socket = serverSocket.accept();
                PrintWriter printWriter = new PrintWriter( socket.getOutputStream(), true );
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                ServerThread serverThread =
                    new ServerThread( socket, printWriter, bufferedReader, this );
                String clientName = bufferedReader.readLine();
                if ( clientThreadHashMap.size() == 10 ){
                    printWriter.println( TO_MANY_USERS );
                }
                else if ( clientThreadHashMap.get( clientName ) != null ){
                    printWriter.println( USER_EXISTS );
                }
                else {
                    System.out.println( "ChatServer: connect " + clientName );
                    clientThreadHashMap.put( clientName, serverThread );
                    serverThread.setClientName( clientName );
                    serverThread.start();
                    synchronized ( serverSocket ) {
                        sendUserListToAll();
                    }
                }
            }   catch ( IOException ioException ){
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Sends the user list to all clients
     */
    public void sendUserListToAll(){
        if ( clientThreadHashMap.size() > 0 ) {
            StringBuilder stringBuilder = new StringBuilder(  );
            stringBuilder.append( USER_LIST );
            boolean first = true;
            for ( String name : clientThreadHashMap.keySet() ) {
                if ( first ) {
                    stringBuilder.append( name );
                    first = false;
                }
                else {
                    stringBuilder.append( COMMA );
                    stringBuilder.append( name );
                }
            }
            String stringBuilderString = stringBuilder.toString();
            System.out.println( stringBuilderString );
            for ( String clientThreadName : clientThreadHashMap.keySet() ) {
                clientThreadHashMap.get( clientThreadName ).getPrintWriter().println( stringBuilderString );
            }
        }
    }

    /**
     * @return {@link #clientThreadHashMap}
     */
    public HashMap<String, ServerThread> getClientThreadHashMap() {
        return clientThreadHashMap;
    }

    /**
     * Removes the client name from {@link #clientThreadHashMap}
     *
     * @param clientName
     */
    public void removeServerThread( String clientName ){
        clientThreadHashMap.remove( clientName );
    }

    /**
     * closes {@link #serverSocket}
     */
    public void close(){
        try {
            serverSocket.close();
        } catch ( IOException ioException ){
            ioException.printStackTrace();
        }
    }

    /**
     * The main program to run the {@link ChatServer}
     *
     * @param args
     */
    public static void main( String[] args ){

        if ( args.length == 1 ){
            ChatServer server = new ChatServer( Integer.parseInt( args[0] ) );
        }
        else if ( args.length == 2 ){
            System.out.println( args[1] + " : " + args[0]);
            ChatServer server = new ChatServer( Integer.parseInt( args[0] ), args[1] );
        }
        else  {
            System.out.println( "Usage: java ChatServer port <server-name>" );
            System.exit( 1 );
        }
    }
}
