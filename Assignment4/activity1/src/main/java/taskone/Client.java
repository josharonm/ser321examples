/**
  File: Client.java
  Author: Student in Fall 2020B, Josh McManus
  Description: Client class in package taskone.
*/

package taskone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import org.json.JSONObject;

/**
 * Class: Client
 * Description: Client tasks.
 */
public class Client {
    private static BufferedReader stdin;

    /**
     * Function JSONObject add().
     */
    public static JSONObject add() {
        String strToSend = null;
        JSONObject request = new JSONObject();
        request.put("selected", 1);
        try {
            System.out.print("Please input the string: ");
            strToSend = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        request.put("data", strToSend);
        return request;
    }

    /**
     * Function JSONObject clear().
     */
    public static JSONObject clear() {
        JSONObject request = new JSONObject();
        request.put("selected", 2);
        return request;
    }

    /**
     * Function JSONObject find().
     */
    public static JSONObject find() {
        String strToSend = null;
        JSONObject request = new JSONObject();
        request.put("selected", 3);
        try {
            System.out.print("Please input the string: ");
            strToSend = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        request.put("data", strToSend);
        return request;
    }

    /**
     * Function JSONObject display().
     */
    public static JSONObject display() {
        JSONObject request = new JSONObject();
        request.put("selected", 4);
        request.put("data", "");
        return request;
    }

    /**
     * Function JSONObject sort().
     */
    public static JSONObject sort() {
        JSONObject request = new JSONObject();
        request.put("selected", 5);
        request.put("data", "");
        return request;
    }

    /**
     * Function JSONObject prepend().
     */
    public static JSONObject prepend() {
        String strToSend;
        int index;
        JSONObject request = new JSONObject();
        request.put("selected", 6);
        while (true) {
            System.out.print("Please input the index: ");
            try {
                index = Integer.parseInt(stdin.readLine());
                break;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException ne) {
                System.out.println("Input is not a number, continue");
            }
        }
        while (true) {
            System.out.print("Please input the string: ");
            try {
                strToSend = stdin.readLine();
                if (strToSend.equals("")) {
                    System.out.println("String is empty, continue");
                } else break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        request.put("data", index + " " + strToSend);
        return request;
    }

    /**
     * Function JSONObject quit().
     */
    public static JSONObject quit() {
        JSONObject request = new JSONObject();
        request.put("selected", 0);
        request.put("data", ".");
        return request;
    }

    /**
     * Function main().
     */
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 8000;
        Socket sock = null;
        stdin = new BufferedReader(new InputStreamReader(System.in));
        try {
            if (args.length != 2) {
                // gradle runClient -Phost=localhost -Pport=8000 -q --console=plain
                System.out.println("Using default values: Host localhost Port 8000");
            }

            try {
                host = args[0];
            } catch (Exception e) {
                System.out.println("[Host] must be a string");
            }

            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.out.println("[Port] must be an integer");
            }

            OutputStream out = null;
            InputStream in = null;
            try {
                sock = new Socket(host, port);
                out = sock.getOutputStream();
                in = sock.getInputStream();
            } catch (Exception e) {
                System.out.println("Unable to connect to Server.");
                try {
                    if (sock != null) {sock.close();}
                    if (out != null) {out.close();}
                    if (in != null) {in.close();}
                    System.exit(0);
                } catch (Exception exception) {
                    System.out.println("Error returning resources.");
                }
            }
            Scanner input = new Scanner(System.in);
            int choice;
            do {
                System.out.println();
                System.out.println("Client Menu");
                System.out.println("Please select a valid option (1-5). 0 to disconnect the client");
                System.out.println("1. add <string> - adds a string to the list and display it");
                System.out.println("2. clear <> - clears the whole list");
                System.out.println("3. find <string> - display idx of string if found, else -1");
                System.out.println("4. display <> - display the list");
                System.out.println("5. sort <> - sort the list");
                System.out.println("6. prepend <int> <string> - prepends given string to string at idx");
                System.out.println("0. quit");
                System.out.println();
                choice = input.nextInt(); // what if not int? should error handle this
                JSONObject request = null;
                switch (choice) {
                    case (1):
                        request = add();
                        break;
                    case (2):
                        request = clear();
                        break;
                    case (3):
                        request = find();
                        break;
                    case (4):
                        request = display();
                        break;
                    case (5):
                        request = sort();
                        break;
                    case (6):
                        request = prepend();
                        break;
                    case (0):
                        request = quit();
                        break;
                    default:
                        System.out.println("Please select a valid option (0-6).");
                        break;
                }
                if (request != null) {
                    System.out.println(request);
                    byte[] responseBytes = null;
                    try {
                        NetworkUtils.send(out, JsonUtils.toByteArray(request));
                        responseBytes = NetworkUtils.receive(in);
                    } catch (Exception e) {
                        System.out.println("Invalid Server Connection. Closing Connection.");
                        sock.close();
                        out.close();
                        in.close();
                        System.exit(0);
                    }

                    JSONObject response = null;
                    try {
                        response = JsonUtils.fromByteArray(responseBytes);
                    } catch (Exception e) {
                        System.out.println("Invalid Response. Closing Connection.");
                        sock.close();
                        out.close();
                        in.close();
                        System.exit(0);
                    }

                    if (!response.isEmpty()) {
                        if (response.has("ok") && !response.getBoolean("ok")) {
                            System.out.println(response.getString("error"));
                        } else {
                            System.out.println();
                            System.out.println("The response from the server: ");
                            System.out.println("datatype: " + response.getString("type"));
                            System.out.println("data: " + response.getString("data"));
                            System.out.println();
                            String typeStr = response.getString("type");
                            if (typeStr.equals("quit")) {
                                sock.close();
                                out.close();
                                in.close();
                                System.exit(0);
                            }
                        }
                    }
                }
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}