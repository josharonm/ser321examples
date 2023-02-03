package udp;

/**
 * Server class provides logic to parse and send server messages to server using custom UDP protocol,
 * and game logic for picture guessing game.
 *
 * Base code provided by David Clements and updated by Josh McManus.
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.json.*;

@SuppressWarnings("VulnerableCodeUsages")
public class Server {
  /*
   * request: { "test": <test string> }
   *
   * response: { "img": <img byte array>, "intro": <intro string> }
   *
   * error response: { "error": <error string> xs}
   */

  public static JSONObject image() throws IOException {
    JSONObject json = new JSONObject();

    File file = new File("img/hi.png");
    if (!file.exists()) {
      System.err.println("Cannot find file: " + file.getAbsolutePath());
      System.exit(-1);
    }
    // Read in image
    BufferedImage img = ImageIO.read(file);
    byte[] bytes = null;
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      ImageIO.write(img, "png", out);
      bytes = out.toByteArray();
    } catch (Exception e) {
      System.err.println("Error writing image to byte array.");
    }
    if (bytes != null) {
      Base64.Encoder encoder = Base64.getEncoder();
      json.put("img", encoder.encodeToString(bytes));
      return json;
    }
    return error("Unable to save image to byte array");
  }

  public static JSONObject intro(String name) {
    JSONObject json = new JSONObject();
    if (name == null || name.trim().isEmpty()) {
      json.put("intro", "Hi! What is your name?");
    } else {
      json.put("intro", "Hi " + name + "! Here's your image!");
    }
    return json;
  }

  public static JSONObject error(String err) {
    JSONObject json = new JSONObject();
    json.put("error", err);
    return json;
  }

  public static void main(String[] args) {
    DatagramSocket sock = null;
    String name = null;
    int port = 8000;

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
      sock = new DatagramSocket(port);
      // NOTE: SINGLE-THREADED, only one connection at a time
      NetworkUtils.Tuple messageTuple = NetworkUtils.Receive(sock);
      while (true) {

        byte[] introOutput = JsonUtils.toByteArray(intro(name));
        NetworkUtils.Send(sock, messageTuple.Address, messageTuple.Port, introOutput);

        try {
          while (true) {
            messageTuple = NetworkUtils.Receive(sock);
            JSONObject message = JsonUtils.fromByteArray(messageTuple.Payload);
            JSONObject returnMessage = null;

            if (message.has("name")) {
              name = message.getString("name");
              introOutput = JsonUtils.toByteArray(intro(name));
              NetworkUtils.Send(sock, messageTuple.Address, messageTuple.Port, introOutput);
              returnMessage = image();
            }

            if (returnMessage != null) {
              byte[] output = JsonUtils.toByteArray(returnMessage);
              NetworkUtils.Send(sock, messageTuple.Address, messageTuple.Port, output);
              name = null;
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (sock != null) {
        sock.close();
      }
    }
  }
}