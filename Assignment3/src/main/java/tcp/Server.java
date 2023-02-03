package tcp;

/**
 * Server class provides logic to parse and send server messages to server using custom TCP protocol,
 * and game logic for picture guessing game.
 *
 * Base code provided by David Clements and updated by Josh McManus.
 */

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Random;

import javax.imageio.ImageIO;

import org.json.*;

@SuppressWarnings("VulnerableCodeUsages")
public class Server {
  /*
   * request: { "name": <name string>, "other": <other string> }
   * 
   * response: { "img": <img byte array>, "lead": <leaderboard string>, "intro": <intro string>,
   * "gameState": <current game state string>, "score": <score int>, "fail": <lose boolean>,
   * "win": <win boolean> }
   * 
   * error response: { "error": <error string>}
   */

  public static JSONObject intro(String name) {
    JSONObject json = new JSONObject();
    if (name == null || name.trim().isEmpty()) {
      json.put("intro", "Hi! What is your name?");
//      json = image(1, json);
    } else {
      json.put("intro", "Hi " + name + "! Please enter \"lead\" to see the leaderboard, \"city\" or \"country\" to start a new game, or \"quit\" to quit!");
    }
    return json;
  }

  public static void updateLead(String name, int score) {
    File file = new File("lead/lead.txt");
    BufferedReader br = null;
    FileWriter fileWriter = null;
    if (!file.exists()) {
      System.err.println("Cannot find file: " + file.getAbsolutePath());
      try {
        file.createNewFile();
      } catch (IOException e) {
        System.err.println("Cannot create new leaderboard file.");
      }
    }
    try {
      br = new BufferedReader(new FileReader(file));
      File file1 = null;
      try{
        file1 = new File("lead/tmp.txt");
      } catch (Exception e) {
        System.err.println("Unable to find tmp file.");
      }
      if (!file1.exists()) {
        System.err.println("Cannot find file: " + file1.getAbsolutePath());
        try {
          file1.createNewFile();
        } catch (IOException e) {
          System.err.println("Cannot create new tmp file.");
        }
      }

      if (file.exists() && file1.exists()) {
        fileWriter = new FileWriter("lead/tmp.txt");
        String st;

        while ((st = br.readLine()) != null) {
          if (!st.toLowerCase().contains(name.toLowerCase())) {
            fileWriter.append(st).append("\n"); // is this going to create too many new lines?
          }
        }
        // update score
        fileWriter.append(name).append(": ").append(String.valueOf(score)).append("\n");
        file1.renameTo(file);
      }

    } catch (FileNotFoundException e) {
      System.err.println("Failed to create BufferedReader.");
    } catch (IOException e) {
      System.err.println("Failed to read Leaderboard.");
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          System.err.println("BufferedReader is already closed.");
        }
      }
      if (fileWriter != null) {
        try {
          fileWriter.flush();
          fileWriter.close();
        } catch (IOException e) {
          System.err.println("FileWriter is already closed.");
        }
      }
    }
  }

  public static JSONObject lead() {
    JSONObject json = new JSONObject();
    File file = new File("lead/lead.txt");
    if (!file.exists()) {
        System.err.println("Cannot find file: " + file.getAbsolutePath());
        json = error("Can't find leaderboard.");
    } else {
      try {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        StringBuilder payload = new StringBuilder();

        while ((st = br.readLine()) != null) {
          payload.append(st);
          payload.append("\n");
        }

        json.put("lead", payload);
      } catch (FileNotFoundException e) {
        System.err.println("Failed to create BufferedReader.");
      } catch (IOException e) {
        System.err.println("Failed to read Leaderboard.");
      }
    }
    json.put("intro", "Please enter \"lead\" to see the leaderboard, \"city\" or \"country\" to start a new game, or \"quit\" to quit!");
    return json;
  }

  public static String randPath(int selection) {
    Random rand = new Random();
    String value = null;
    if (selection == 3) {
      //country
      int random = rand.nextInt(3);
      if (random == 0) {
        value = "germany";
      } else if (random == 1) {
        value = "ireland";
      } else {
        value = "southafrica";
      }
    } else if (selection == 4) {
      //city
      int random = rand.nextInt(4);
      if (random == 0) {
        value = "berlin";
      } else if (random == 1) {
        value = "paris";
      } else if (random == 2) {
        value = "phoenix";
      } else {
        value = "rome";
      }
    } else {
      System.err.println("Invalid image selection.");
    }

    return value;
  }

  public static JSONObject image(int selection, JSONObject json) {
    String ans = null;
    String path = "img/";

    switch (selection) {
      case (1) ->
        //hi
              path += "hi.png";
      case (2) ->
        //fail
              path += "lose.png";
      case (3) -> {
        //country
        ans = randPath(3);
        path += "country/";
        path += ans;
        path += ".png";
      }
      case (4) -> {
        //city
        ans = randPath(4);
        path += "city/";
        path += ans;
        path += ".png";
      }
      case (5) -> path += "win.png";
      default -> //invalid
              System.err.println("Invalid image selection.");
    }

    File file = new File(path);
    if (!file.exists()) {
      System.err.println("Cannot find file: " + file.getAbsolutePath());
    }
    // Read in image
    BufferedImage img = null;
    try {
      img = ImageIO.read(file);
    } catch (IOException e) {
      System.err.println("Unable to read image file.");
    }
    byte[] bytes = null;
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      if (img != null) {
        ImageIO.write(img, "png", out);
        bytes = out.toByteArray();
      }
    } catch (IOException e) {
      System.err.println("Error writing image to bytes.");
    }
    if (bytes != null) {
      Base64.Encoder encoder = Base64.getEncoder();
      json.put("img", encoder.encodeToString(bytes));
      if (ans != null && !ans.trim().isEmpty()) {
        json.put("ans", ans);
      }
      return json;
    }
    return error("Unable to save image to byte array");
  }

  public static JSONObject error(String err) {
    JSONObject json = new JSONObject();
    json.put("error", err);
    return json;
  }

  public static void main(String[] args) throws IOException {
    ServerSocket serv = null;
    int port = 8000;
    String name = null;
    boolean gameStarted = false;
    boolean fail = false;
    boolean win = false;
    String ans = null;
    String gameState = null;
    int score = 0;

    if (args.length != 1) {
      System.out.println("Expected arguments: <port(int)>");
      System.out.println("Using default argument.");
    } else {
      try {
        port = Integer.parseInt(args[0]);
      } catch (Exception e) {
        System.out.println("Invalid [Port]");
        System.out.println("Default Port: 8000");
      }
    }

    try {
      serv = new ServerSocket(port);
      // NOTE: SINGLE-THREADED, only one connection at a time
      while (true) {
        try (Socket sock = serv.accept()) {
          // blocking wait
          OutputStream out = sock.getOutputStream();
          InputStream in = sock.getInputStream();

          byte[] introOutput = JsonUtils.toByteArray(intro(name));
          NetworkUtils.Send(out, introOutput);

          while (true) {
            JSONObject returnMessage = null;
            byte[] messageBytes = NetworkUtils.Receive(in);
            JSONObject message = JsonUtils.fromByteArray(messageBytes);

            if (message.has("name")) {
              name = message.getString("name");
              introOutput = JsonUtils.toByteArray(intro(name));
              NetworkUtils.Send(out, introOutput);
            } else if (message.has("other")) {
              try {
                String choice = message.getString("other");
                switch (choice) {
                  case ("quit") -> {
                    //quit, write to leaderboard
                    if (gameStarted) {
                      gameStarted = false;
                      fail = false;
                      win = false;
                      ans = null;
                      gameState = null;
                      score = 0;
                      returnMessage = intro(name);
                    } else {
                      name = null;
                      sock.close();
                    }
                  }
                  case ("lead") ->
                    //return leaderboard
                          returnMessage = lead();
                  default -> {
                    if (!gameStarted) {
                      if (choice.equalsIgnoreCase("country")) {
                        // return country
                        returnMessage = image(3, new JSONObject());
                        gameStarted = true;
                      } else if (choice.equalsIgnoreCase("city")) {
                        // return city
                        returnMessage = image(4, new JSONObject());
                        gameStarted = true;
                      } else {
                        returnMessage = error("Invalid selection: " + choice + " is not an option");
                      }

                      if (returnMessage.has("ans")) {
                        ans = returnMessage.getString("ans");
                        returnMessage.remove("ans");
                        gameState = ans.replaceAll("(?i)[a-z]", "_");
                        returnMessage.put("gameState", gameState);
                        returnMessage.put("score", score);
                        System.out.println("The answer is: " + ans);
                      }
                    } else {
                      returnMessage = new JSONObject();
                      // make a guess
                      if (!choice.trim().isEmpty()) {
                        if (choice.length() == 1) {
                          // letter
                          String newGameState = "";
                          System.out.println(ans);
                          for (int i = 0; i < ans.length(); i++) {
                            if (ans.charAt(i) == choice.charAt(0)) {
                              newGameState += choice.charAt(0);
                              score++;
                            } else if (gameState.charAt(i) != '_') {
                              newGameState += ans.charAt(i);
                            } else {
                              newGameState += "_";
                            }
                          }

                          if (gameState.equals(newGameState)) {
                            score -= 1;
                          } else {
                            gameState = newGameState;
                          }
                          if (gameState.equals(ans)) {
                            System.out.println("Correct! You win!");
                            win = true;
                          }
                        } else {
                          // word
                          if (choice.equalsIgnoreCase("quit")) {
                            sock.close();
                          } else {
                            if (ans.equalsIgnoreCase(choice)) {
                              System.out.println("Correct! You win!");
                              win = true;
                              score += 5;
                            } else {
                              score -= 5;
                            }
                          }
                        }
                      }

                      returnMessage.put("score", score);
                      returnMessage.put("gameState", gameState);
                      if (score < 1) fail = true;
                      if (win) {
                        updateLead(name, score);
                      }
                      if (fail || win) {
                        returnMessage = intro(name);
                        if (fail) {
                          returnMessage.put("fail", true);
                        } else {
                          returnMessage.put("win", true);
                        }
                        gameStarted = false;
                        fail = false;
                        win = false;
                        ans = null;
                        gameState = null;
                        score = 0;
                      }
                    }
                  }
                }
              } catch (Exception e) {
                System.err.println("Invalid input choice.");
                returnMessage = error("Invalid message received");
              }
            } else {
              returnMessage = error("Invalid message received");
            }

            // we are converting the JSON object we have to a byte[]
            if (returnMessage != null) {
              byte[] output = JsonUtils.toByteArray(returnMessage);
              NetworkUtils.Send(out, output);
            }
          }
        } catch (Exception e) {
          System.out.println("Client disconnect");
          name = null;
          gameStarted = false;
          fail = false;
          win = false;
          ans = null;
          gameState = null;
          score = 0;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (serv != null) {
        serv.close();
      }
    }
  }
}