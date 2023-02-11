/**
  File: Server.java
  Author: Student in Fall 2020B, Josh McManus
  Description: Server class in package taskone.
*/

package taskone;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class: Server
 * Description: Server tasks.
 */
class Server {

    public static void main(String[] args) throws Exception {
        int port = 8000;
        StringList strings = new StringList();

        if (args.length != 1) {
            // gradle runServer -Pport=8000 -q --console=plain
            System.out.println("Using default values: Port 8000");
        } else {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
                System.out.println("[Port] must be an integer");
                System.exit(2);
            }
        }

        ServerSocket server = new ServerSocket(port);
        System.out.println("Server Started...");
        while (true) {
            System.out.println("Accepting a Request...");
            Socket sock = server.accept();

            Performer performer = new Performer(sock, strings);
            performer.doPerform();
            try {
                System.out.println("Close Socket of Client.");
                sock.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
