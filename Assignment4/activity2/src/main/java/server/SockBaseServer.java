package server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

class SockBaseServer extends Thread {
    static String logFilename = "logs.txt";
    static String leadFilename = "lead.txt";

    ServerSocket serv = null;
    InputStream in = null;
    OutputStream out = null;
    Socket clientSocket;
    int port = 9099; // default port
    Game game;


    public SockBaseServer(Socket sock, Game game){
        this.clientSocket = sock;
        this.game = game;
        try {
            this.in = clientSocket.getInputStream();
            this.out = clientSocket.getOutputStream();
        } catch (Exception e){
            System.out.println("Error in constructor: " + e);
        }
    }

    // Handles the communication right now it just accepts one input and then is done you should make sure the server stays open
    // can handle multiple requests and does not crash when the server crashes
    // you can use this server as based or start a new one if you prefer. 
    public void run() {
        String name = null;
        System.out.println("Ready...");
        boolean quit = false;
        ArrayList<String> oldTiles = new ArrayList<>();
        while (!quit) {
            try {
                // read the proto object and put into new object
                Request op = null;
                try {
                    op = Request.parseDelimitedFrom(in);
                } catch (Exception e) {
                    System.out.println("Error parsing Client Input.");
                    System.exit(1);
                }

                Response response = null;

                if (op != null) {
                    // if the operation is NAME (so the beginning then say there is a comment and greet the client)
                    if (op.getOperationType() == Request.OperationType.NAME) {
                        // get name from proto object
                        name = op.getName();

                        // writing connect message to the log with name and CONNECT
                        writeToLog(name, Message.CONNECT);
                        System.out.println("Got a connection and a name: " + name);
                        response = Response.newBuilder()
                                .setResponseType(Response.ResponseType.GREETING)
                                .setMessage("Hello " + name + " and welcome. Welcome to a simple game of Memory.")
                                .build();
                    } else if (op.getOperationType() == Request.OperationType.LEADER) {
                        // return leaderboard
                        System.out.println("Returning Leaderboard.");
                        response = readLeaderFile().build();
                    } else if (op.getOperationType() == Request.OperationType.NEW) {
                        // new game
                        if (game.getWon()) {
                            game.newGame(); // starting a new game
                            System.out.println(game.showBoard());
                        }

                        // build a simple response for PLAY as answer to NEW
                        response = Response.newBuilder()
                                .setResponseType(Response.ResponseType.PLAY)
                                .setBoard(game.getBoard()) // gets the hidden board
                                .setEval(false)
                                .setSecond(false)
                                .build();
                    } else if (op.getOperationType() == Request.OperationType.TILE1) {
                        //first tile
                        String tile1 = op.getTile();
                        if (tile1.charAt(0) >= 'a' && tile1.charAt(0) <= game.getLastRow() && tile1.charAt(1) >= '1' && tile1.charAt(1) <= game.getLastCol()) {
                            // convert coordinates
                            int row = 0;
                            int col = Character.getNumericValue(tile1.charAt(1));

                            if (tile1.charAt(0) == 'a') {
                                row = 1;
                            } else if (tile1.charAt(0) == 'b') {
                                row=2;
                            } else if (tile1.charAt(0) == 'c') {
                                row = 3;
                            } else if (tile1.charAt(0) == 'd') {
                                row = 4;
                            }

                            col = col * 2;

                            oldTiles.add((Integer.toString(row) + col));

                            response = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.PLAY)
                                    .setFlippedBoard(game.tempFlipTiles(row, col)) // gets the hidden board
                                    .setEval(false)
                                    .setSecond(true)
                                    .build();
                        } else {
                            response = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.ERROR)
                                    .setMessage("Invalid Selection.")
                                    .build();
                        }
                    } else if (op.getOperationType() == Request.OperationType.TILE2) {
                        if (op.getTile().charAt(0) >= 'a' && op.getTile().charAt(0) <= game.getLastRow() && op.getTile().charAt(1) >= '1' && op.getTile().charAt(1) <= game.getLastCol()) {
                            boolean match = false;

                            // convert coordinates
                            int row = 0;
                            int col = Character.getNumericValue(op.getTile().charAt(1));

                            if (op.getTile().charAt(0) == 'a') {
                                row = 1;
                            } else if (op.getTile().charAt(0) == 'b') {
                                row=2;
                            } else if (op.getTile().charAt(0) == 'c') {
                                row = 3;
                            } else if (op.getTile().charAt(0) == 'd') {
                                row = 4;
                            }

                            col = col * 2;

                            String tile2 = (Integer.toString(row) + col);
                            String tile1 = null;

                            if (!(oldTiles.size() > 0)) {
                                oldTiles.add("00");
                                tile1 = oldTiles.get(0);
                            } else {
                                tile1 = oldTiles.get(oldTiles.size() - 1);
                            }

                            if (game.getTile(Character.getNumericValue(tile1.charAt(0)), Character.getNumericValue(tile1.charAt(1))) == game.getTile(Character.getNumericValue(tile2.charAt(0)), Character.getNumericValue(tile2.charAt(1)))) {
                                System.out.println("Client got a match!");
                                game.replaceCharacters(Character.getNumericValue(tile1.charAt(0)), Character.getNumericValue(tile1.charAt(1)), Character.getNumericValue(tile2.charAt(0)), Character.getNumericValue(tile2.charAt(1)));
                                game.checkWin();
                                if (game.getWon()) {
                                    //you won!
                                    System.out.println("Client Won!");
                                    writeToLead(name);
                                    response = Response.newBuilder()
                                            .setResponseType(Response.ResponseType.WON)
                                            .setBoard(game.getBoard()) // gets the hidden board
                                            .setEval(true)
                                            .setSecond(false)
                                            .build();
                                } else {
                                    System.out.println("Client got a match, but didn't win.");
                                    // you got a match but didn't win
                                    response = Response.newBuilder()
                                            .setResponseType(Response.ResponseType.PLAY)
                                            .setBoard(game.getBoard()) // gets the hidden board
                                            .setEval(true)
                                            .setSecond(false)
                                            .build();
                                }
                            } else {
                                System.out.println("Not a match.");
                                // not a match
                                response = Response.newBuilder()
                                        .setResponseType(Response.ResponseType.PLAY)
                                        .setFlippedBoard(game.tempFlipTiles(Character.getNumericValue(tile1.charAt(0)),Character.getNumericValue(tile1.charAt(1)),Character.getNumericValue(tile2.charAt(0)), Character.getNumericValue(tile2.charAt(1)))) // gets the hidden board
                                        .setEval(false)
                                        .setSecond(false)
                                        .build();
                            }
                        } else {
                            response = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.ERROR)
                                    .setMessage("Invalid Selection.")
                                    .build();
                        }
                    } else if (op.getOperationType() == Request.OperationType.QUIT) {
                        quit = true;
                        response = Response.newBuilder()
                                .setResponseType(Response.ResponseType.BYE)
                                .setMessage("Goodbye " + name + "!")
                                .build();
                    }
                }

                if (response != null) {
                    response.writeDelimitedTo(out);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    if (out != null) out.close();
                    if (in != null)   in.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Failed to Close Resources.");;
                }
            }
        }
    }


    /**
     * Writing a new entry to our log
     * @param name - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect)
     */
    public synchronized static void writeToLog(String name, Message message){
        try {
            // read old log file 
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();
            System.out.println(date);

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date + ": " +  name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"

            for (String log: logsObj.getLogList()){

                System.out.println(log);
            }

            // write to log file
            logsObj.writeTo(output);
        }catch(Exception e){
            System.out.println("Issue while trying to save log file.");
        }
    }

    /**
     * Writing a new entry to our leaderboard
     * @param name - Name of the person logging in
     */
    public synchronized static void writeToLead(String name){
        try {
            // read old log file
            System.out.println("Writing lead!");
            File file = new File(leadFilename);
            Scanner scanner = new Scanner(file);
            ArrayList<String> temp = new ArrayList<>();

            // create temp string array
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                temp.add(line);
            }

            StringBuilder s = new StringBuilder();

            if (temp.size() == 0) {
                // first entry
                temp.add(name.trim()+":1:1");
                s.append(temp.get(0)).append("\n");
            } else {
                // iterate through checking for matching name, if found iterate wins
                for (int i1 = 0; i1 < temp.size(); i1++) {
                    String[] components = temp.get(i1).split(":", 3);
                    if (!components[0].trim().isEmpty()) {
                        if (components[0].trim().equalsIgnoreCase(name)) {
                            // iterate wins
                            components[1] = Integer.toString(Integer.parseInt(components[1].trim()) + 1);
                            String newLine = components[0].trim() + ":" + components[1].trim() + ":" + components[2].trim();
                            temp.set(i1, newLine);
                            System.out.println("Found a match!");
                        } else {
                            temp.add(name.trim() + ":1:1");
                            System.out.println("Didn't find a match!");
                        }
                    }
                }


                for (String value : temp) {
                    for (String v2 : temp) {
                        if (Objects.equals(value, v2)) {
                            temp.remove(value);
                        }
                    }
                    if (temp.contains(value)) {
                        s.append(value).append("\n");
                    }
                }
            }


            try {
                FileWriter writer = new FileWriter(leadFilename, false);
                writer.write(String.valueOf(s));
                writer.flush();
                writer.close();
            } catch (Exception e) {
                System.out.println("Unable to print to leaderboard.");
            }
        }catch(Exception e){
            System.out.println("Issue while trying to save leaderboard.");
        }
    }

    /**
     * Reading the current log file
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public synchronized static Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }

    /**
     * Reading the current leader file
     * @return Entry.Builder a builder of an entry from protobuf
     */
    public synchronized static Response.Builder readLeaderFile() {
        Response.Builder res = null;
        try {
            // just read the file and put what is in it into the logs object
            File file = new File(leadFilename);
            Scanner scanner = new Scanner(file);
            ArrayList<String> temp = new ArrayList<>();

            // create temp string array
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                temp.add(line);
            }

            res = Response.newBuilder()
                    .setResponseType(Response.ResponseType.LEADER);

            // iterate through checking for matching name, if found iterate wins
            for (String s : temp) {
                Entry.Builder entry = Entry.newBuilder();
                String[] components = s.split(":", 3);
                if (!components[0].trim().isEmpty() && !components[1].trim().isEmpty() && !components[2].trim().isEmpty()) {
                    entry.setName(components[0].trim());
                    entry.setWins(Integer.parseInt(components[1].trim()));
                    entry.setLogins(Integer.parseInt(components[2].trim()));
                    res.addLeader(entry);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(leadFilename + ": File not found. Failed to update leaderboard.");
        }
        return res;
    }


    public static void main (String[] args) throws Exception {
        Game game = new Game();
        game.newGame();
        System.out.println(game.showBoard());

        if (args.length != 1) {
            System.out.println("Expected arguments: <port(int)>. Using default port 9099.");
        }
        int port = 9099; // default port
        Socket clientSocket;
        ServerSocket serv = null;

        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer. Using default port 9099.");
        }
        try {
            serv = new ServerSocket(port);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        while (true) {
            clientSocket = serv.accept();
            SockBaseServer server = new SockBaseServer(clientSocket, game);
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Close Socket of Client.");
                clientSocket.close();
            }
        }
    }
}

