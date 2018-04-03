package org.jynergy.cme.client;

/*
 * Copyright (c) 2018.
 * Licensed under LPGL v3 (http://www.gnu.org/licenses/lgpl.txt) or Apache License v2.0 (http://www.apache.org/licenses/LICENSE-2.0)
 */

import org.jynergy.cme.protocol.Protocol;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A chat Client for a client server application
 */
public class ChatClient extends JFrame implements Runnable, Protocol
{
    private final static String LINE_SEPARATOR = System.lineSeparator();

    private Socket socket;
    private String clientName;
    private JTextArea jTextArea;
    private JTextField jTextField;
    private JButton jButton;
    private JComboBox jComboBox;
    private PrintWriter printWriter;

    public ChatClient( String clientName,
                       String serverName,
                       int serverPort ) {
        super( "Chat Client: " + clientName );
        
        createView();
        initializeThread( clientName, serverName, serverPort );
        SwingUtilities.invokeLater( () ->  setVisible( true ));
    }

    /**
     * Initializes this thread
     *
     * @param clientName {@link #clientName}
     * @param serverName name to connect to
     * @param serverPort port to connect to
     */
    public void initializeThread( String clientName,
                                  String serverName,
                                  int serverPort){
        System.out.println( serverName + ":" + serverPort);
        this.clientName = clientName;
        try {
            socket = new Socket( serverName, serverPort );
            System.out.println( "Connected: " + socket );
        }
        catch ( UnknownHostException unknownHostException ) {
            System.err.println( "Host unknown: " + unknownHostException.getMessage() );
            System.exit( 1 );
        }
        catch ( IOException ioException ) {
            System.err.println( "Unexpected exception: " + ioException.getMessage() );
            System.exit( 1 );
        }
        Thread thread = new Thread( this );
        thread.start();
    }

    /**
     * Creates the GUI for thsi chat application
     */
    public void createView(){
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        setSize( 500, 300 );
        setLocationRelativeTo( null );

        JPanel mainPanel = new JPanel( new BorderLayout(  ) );
        getContentPane().add( mainPanel );

        jTextArea = new JTextArea(  );
        jTextArea.setEnabled( false );

        jComboBox = new JComboBox<>( );
        jComboBox.addItem( ALL );
        jTextField = new JTextField();

        jButton = new JButton( "Send");
        jButton.setEnabled( false );
        jButton.addActionListener( l -> sendMessage() );

        JPanel sendingPanel = new JPanel( new BorderLayout(  ) );
        sendingPanel.add( new JLabel( "Who Sending to: " ) );
        sendingPanel.add( jComboBox, BorderLayout.WEST );
        sendingPanel.add( jTextField, BorderLayout.CENTER );
        sendingPanel.add( jButton, BorderLayout.EAST );

        JScrollPane jScrollPane = new JScrollPane( jTextArea );
        jScrollPane.setBorder( BorderFactory.createTitledBorder( "Chat Output" ));

        mainPanel.add( jScrollPane, BorderLayout.CENTER );
        mainPanel.add( sendingPanel, BorderLayout.SOUTH );
    }

    /**
     * Populates {@link #jComboBox}
     *
     * @param nameList {@link ArrayList} of names
     */
    public void populateJComboBox( ArrayList<String> nameList ){
        if ( jComboBox.getItemCount() > 0 ){
            jComboBox.removeAllItems();
        }
        jComboBox.addItem( Protocol.ALL );
        for (String name : nameList ){
            jComboBox.addItem( name );
        }
        
        jButton.setEnabled( true );
    }

    /**
     * Adds the text to {@link #jTextArea}
     *
     * @param line line to add
     */
    public void addTextToJTextArea( String line ){
        line = line + LINE_SEPARATOR;
        jTextArea.append( line );
    }

    /**
     * Sends the message from {@link #jTextField} to the server
     */
    public void sendMessage(){
        String message = jTextField.getText().trim();
        jTextField.setText( "" );
        if ( message != null & message.length() > 0 ){
            StringBuilder stringBuilder = new StringBuilder(  );
            stringBuilder.append( Protocol.MESSAGE );
            stringBuilder.append( jComboBox.getSelectedItem() );
            stringBuilder.append( Protocol.SEPERATOR );
            stringBuilder.append( clientName );
            stringBuilder.append( " : " );
            stringBuilder.append( message );
            try {
                printWriter.println( stringBuilder.toString() );
            } catch ( Exception e ){
                if ( ! socket.isClosed() ){
                    try {
                        socket.close();
                    }
                    catch ( IOException e2 ) {
                        e2.printStackTrace();
                    }
                }
                System.err.println( "error writing to socket" );
                e.printStackTrace();
                System.exit( 1 );
            }
        }
    }

    /**
     * listesn to the thread and process the input
     */
    public void run(){
        try{
            printWriter = new PrintWriter( socket.getOutputStream(), true );
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            printWriter.println( clientName );
            while ( ! socket.isClosed() ){
                String line = bufferedReader.readLine();
                if ( line != null ) {
                    switch ( line.charAt( 0 ) ) {
                        case USER_EXISTS:
                            System.err.println( "The user " + clientName + " exists" );
                            System.exit( 1 );
                            break;
                        case TO_MANY_USERS:
                            System.err.println( "Ten people are already logged on" );
                            System.exit( 1 );
                            break;
                        case USER_LIST:
                            final ArrayList<String> nameList = createList( line.substring( 1, line.length() ) );
                            SwingUtilities.invokeLater( () ->  populateJComboBox( nameList ));
                            break;
                        case MESSAGE:
                            final String message  = line.substring( 1, line.length() );
                            SwingUtilities.invokeLater( () -> addTextToJTextArea( message ) );
                            break;
                    }
                }
                else{
                    break;
                }
            }
        }
        catch ( IOException ioException ){
            ioException.printStackTrace();
        }
        if ( !socket.isClosed() ) {
            try {
                socket.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
        System.exit( 0 );
    }

    /**
     * Creates an {@link ArrayList} of name
     *
     * @param line comma deliminated string
     *
     * @return {@link ArrayList} of name
     */
    private ArrayList<String> createList( String line ){
        ArrayList<String> nameList = new ArrayList<String>();
        StringTokenizer stringTokenizer =  new StringTokenizer( line, "," );
        while ( stringTokenizer.hasMoreElements() ) {
            nameList.add( stringTokenizer.nextToken() );
        }

        return nameList;
    }

    /**
     * Reads to command line and initiates the correct constructor or errors out
     * @param args
     */
    public static void main( String[] args ) {
        if ( args.length != 3 ) {
            System.err.println( "Usage ChatClient clientName host portNumber" );
            System.exit( 1 );
        }
        else {
            ChatClient client = new ChatClient( args[0], args[ 1], Integer.parseInt( args[2] ) );
        }
    }
}
