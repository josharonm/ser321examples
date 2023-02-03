package udp;

/**
 * Client class provides logic to parse and send client messages to server using custom UDP protocol.
 *
 * Base code provided by David Clements and updated by Josh McManus.
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
   * request: { "test": <test string> }
   *
   * response: { "img": <img byte array>, "intro": <intro string> }
   *
   * error response: { "error": <error string> xs}
   */

  public static void main(String[] args) {
    DatagramSocket sock;
    try {

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

      InetAddress address = InetAddress.getByName(host);
      sock = new DatagramSocket();

      Scanner input = new Scanner(System.in);

      JSONObject baseRequest = new JSONObject().put("test", "test");
      NetworkUtils.Send(sock, address, port, JsonUtils.toByteArray(baseRequest));

      NetworkUtils.Tuple responseTuple = NetworkUtils.Receive(sock);
      JSONObject response = JsonUtils.fromByteArray(responseTuple.Payload);

      if (response.has("intro")) {
        System.out.println(response.getString("intro"));
      }
      String name = input.next();
      JSONObject request = new JSONObject().put("name", name);
      System.out.println(request.get("name"));
      NetworkUtils.Send(sock, address, port, JsonUtils.toByteArray(request));
      responseTuple = NetworkUtils.Receive(sock);
      response = JsonUtils.fromByteArray(responseTuple.Payload);
      if (response.has("intro")) {
        System.out.println(response.getString("intro"));
      }

      do {
        responseTuple = NetworkUtils.Receive(sock);
        response = JsonUtils.fromByteArray(responseTuple.Payload);
        if (response.has("error")) {
          System.out.println(response.getString("error"));
        } else {
          if (response.has("img")) {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] bytes = decoder.decode(response.getString("img"));
            ImageIcon icon = null;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
              BufferedImage image = ImageIO.read(bais);
              icon = new ImageIcon(image);
            } catch (Exception e) {
              System.err.println("Error reading image.");
            }
            if (icon != null) {
              JFrame frame = new JFrame();
              JLabel label = new JLabel();
              label.setIcon(icon);
              frame.add(label);
              frame.setSize(icon.getIconWidth(), icon.getIconHeight());
              frame.show();
              sock.close();
              break;
            }
          }
        }
      } while (true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}