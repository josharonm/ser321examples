package client;

import java.net.*;
import java.io.*;

import org.json.*;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

class SockBaseClient {

    public static void main (String args[]) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int port = 9099; // default port

        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        String host = args[0];
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }

        // Ask user for username
        System.out.println("Please provide your name for the server: ");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String strToSend = stdin.readLine();

        // Build the first request object just including the name
        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .setName(strToSend).build();
        Response response;
        try {
            // connect to the server
            serverSock = new Socket(host, port);

            // write to the server
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();

            op.writeDelimitedTo(out);

            // read from the server
            response = Response.parseDelimitedFrom(in);

            // print the server response greeting.
            System.out.println(response.getMessage());
            boolean gameStart = false;
            boolean tileNumber = true; //1 - True, 2 - False

            while (true) {
                op = null;

                if (gameStart) {
                    System.out.println("* \nPlease make a selection or type 'exit' to quit: \n");
                    strToSend = stdin.readLine();
                    if (strToSend.equalsIgnoreCase("exit")) { //quit game
                        op = Request.newBuilder()
                                .setOperationType(Request.OperationType.QUIT)
                                .build();
                    } else { // play
                        if (strToSend.isEmpty() || strToSend.length() > 2) {
                            System.out.println("Please enter a valid selection.");
                        } else {
                            //valid play
                            if (tileNumber) {
                                // tile 1
                                op = Request.newBuilder()
                                        .setOperationType(Request.OperationType.TILE1)
                                        .setTile(strToSend).build();
                                tileNumber = false;
                            } else {
                                // tile 2
                                op = Request.newBuilder()
                                        .setOperationType(Request.OperationType.TILE2)
                                        .setTile(strToSend).build();
                                tileNumber = true;
                            }

                        }
                    }
                } else {
                    System.out.println("* \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - quit the game\n");
                    strToSend = stdin.readLine();
                    switch (strToSend) {
                        case ("1") ->
                            //leaderboard
                                op = Request.newBuilder()
                                        .setOperationType(Request.OperationType.LEADER)
                                        .build();
                        case ("2") ->
                            //enter game
                                op = Request.newBuilder()
                                        .setOperationType(Request.OperationType.NEW)
                                        .build();
                        case ("3"), ("exit") ->
                            //quit game
                                op = Request.newBuilder()
                                        .setOperationType(Request.OperationType.QUIT)
                                        .build();
                        default -> System.out.println("Please select a valid option (1-3).");
                    }
                }

                if (op != null) {
                    op.writeDelimitedTo(out);
                    // read from the server
                    try {
                        response = Response.parseDelimitedFrom(in);
                    } catch (Exception e) {
                        System.out.println("Failed to Parse Server Input.");
                    }

                    // print the server response.
                    if (response.getResponseType().name().equalsIgnoreCase("GREETING") || response.getResponseType().name().equalsIgnoreCase("ERROR")) {
                        System.out.println(response.getMessage());
                        if (response.getResponseType().name().equalsIgnoreCase("ERROR")) {
                            tileNumber = !tileNumber;
                        }
                    } else if (response.getResponseType().name().equalsIgnoreCase("LEADER")) {
                        System.out.println("---LEADERBOARD---");
                        for (Entry lead: response.getLeaderList()){
                            System.out.println(lead.getName() + ": " + lead.getWins());
                        }
                    } else if (response.getResponseType().name().equalsIgnoreCase("PLAY")) {
                        gameStart = true;
                        boolean check = false;
                        if (!response.getSecond()) {
                            //first
                            System.out.println("First Tile");
                            if (response.getEval()) {
                                System.out.println("You found a match!");
                                System.out.println(response.getBoard());
                            } else {
                                System.out.println(response.getBoard());
                                System.out.println(response.getFlippedBoard());
                            }
                        } else {
                            //second
                            System.out.println("Second Tile");
                            System.out.println(response.getFlippedBoard());
                        }
                    } else if (response.getResponseType().name().equalsIgnoreCase("WON")) {
                        System.out.println("You Won!");
                        System.out.println(response.getBoard());
                        gameStart = false;
                    } else {
                        System.out.println(response.getMessage());
                        gameStart =  false;
                        if (in != null)   in.close();
                        if (out != null)  out.close();
                        serverSock.close();
                        System.exit(0);
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)   in.close();
            if (out != null)  out.close();
            if (serverSock != null) serverSock.close();
        }
    }
}


