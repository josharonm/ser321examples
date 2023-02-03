package tcp;

/**
 * Client class provides logic to parse and send client messages to server using custom TCP protocol.
 *
 * Base code provided by David Clements and updated by Josh McManus.
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.json.*;

@SuppressWarnings("VulnerableCodeUsages")
public class Client {
  /*
   * request: { "name": <name string>, "other": <other string> }
   *
   * response: { "img": <img byte array>, "lead": <leaderboard string>, "intro": <intro string>,
   * "gameState": <current game state string>, "score": <score int>, "fail": <lose boolean>,
   * "win": <win boolean> }
   *
   * error response: { "error": <error string> }
   */

  public static void main(String[] args) {
    Socket sock;
    boolean gameStarted = false;
    JFrame frame = null;

    // default values
    String host = "localhost";
    int port = 8000;

    if (args.length != 2) {
      System.out.println("Expected arguments: <host(String)> <port(int)>");
      System.out.println("Using default argument(s).");
    }
    try {
      host = args[0];
    } catch (Exception e) {
      System.out.println("Invalid [Host]");
      System.out.println("Default Host: localhost");
    }
    try {
      port = Integer.parseInt(args[1]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port] must be integer");
      System.out.println("Default Port: 8000");
    }

    try {
      sock = new Socket(host, port);
      OutputStream out = sock.getOutputStream();
      InputStream in = sock.getInputStream();

      Scanner input = new Scanner(System.in);
      String choice;

      byte[] responseBytes = NetworkUtils.Receive(in);
      JSONObject response = JsonUtils.fromByteArray(responseBytes);
      if (response.has("intro")) {
        System.out.println(response.getString("intro"));
      }
      String name = input.next();
      JSONObject request = new JSONObject().put("name", name);
      NetworkUtils.Send(out, JsonUtils.toByteArray(request));
      responseBytes = NetworkUtils.Receive(in);
      response = JsonUtils.fromByteArray(responseBytes);
      if (response.has("intro")) {
        System.out.println(response.getString("intro"));
      }

        do {
          choice = input.next();
          request = null;

          switch (choice) {
            case ("quit") -> {
              if (!gameStarted) {
                out.close();
                in.close();
                sock.close();
                System.exit(0);
              } else {
                request = new JSONObject().put("other", choice);
                frame.setVisible(false);
                frame.dispose();
              }
            }
            case ("lead") -> request = new JSONObject().put("other", choice);
            case ("country"), ("city") -> {
              request = new JSONObject().put("other", choice);
              gameStarted = true;
            }
            default -> {
              if (gameStarted) {
                request = new JSONObject().put("other", choice);
              } else {
                System.out.println("Invalid input.");
              }
            }
          }

          if (request != null) {
            NetworkUtils.Send(out, JsonUtils.toByteArray(request));
            responseBytes = NetworkUtils.Receive(in);
            try {
              response = JsonUtils.fromByteArray(responseBytes);
            } catch (Exception e) {
              System.out.println("Invalid response from server.");
              response = new JSONObject();
            }
            if (response.has("error")) {
              System.out.println(response.getString("error"));
            } else {
              if (response.has("win") && response.getBoolean("win")) {
                System.out.println("You win!\n");
                gameStarted = false;
                if (frame != null) {
                  frame.setVisible(false);
                  frame.dispose();
                }
              } else if (response.has("fail") && response.getBoolean("fail")) {
                System.out.println("You lose!\n");
                gameStarted = false;
                if (frame != null) {
                  frame.setVisible(false);
                  frame.dispose();
                }
              }
              if (response.has("lead")) {
                System.out.println("---LEADERBOARD---\n");
                System.out.println(response.getString("lead"));
                System.out.println();
              }
              if (response.has("img")) {
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] bytes = decoder.decode(response.getString("img"));
                ImageIcon icon = null;
                try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                  BufferedImage image = ImageIO.read(bais);
                  icon = new ImageIcon(image);
                } catch (Exception e) {
                  System.err.println(e);
                }

                if (icon != null) {
                  frame = new JFrame();
                  JLabel label = new JLabel();
                  label.setIcon(icon);
                  frame.add(label);
                  frame.setSize(icon.getIconWidth(), icon.getIconHeight());
                  frame.show();
                }
              }
              if (gameStarted && response.has("gameState") && response.has("score")) {
                System.out.println("Current Score: " + response.getInt("score"));
                System.out.println("Current Game State: " + response.getString("gameState"));
              }

              if (response.has("intro")) {
                System.out.println(response.getString("intro"));
              }
            }
          }
        } while (true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}