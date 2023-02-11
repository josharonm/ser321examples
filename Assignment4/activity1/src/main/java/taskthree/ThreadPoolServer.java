/**
 File: ThreadPoolServer.java
 Author: Student in Fall 2020B, Josh McManus
 Description: ThreadPoolServer class in package taskthree.
 */

package taskthree;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Class: ThreadPoolServer
 * Description: ThreadPoolServer tasks.
 */
class ThreadPoolServer {

    public static void main(String[] args) throws Exception {
        int port = 8000;
        int pool = 3;
        StringList strings = new StringList();

        if (args.length != 2) {
            // gradle runServer -Pport=8000 -q --console=plain
            System.out.println("Using default values: Port 8000 Thread Pool 3");
        } else {
            try {
                port = Integer.parseInt(args[0]);
                pool = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.out.println("[Port] and [Pool] must be an integer");
                System.exit(2);
            }
        }

        ServerSocket server = new ServerSocket(port);
        System.out.println("ThreadPoolServer Started...");
        System.out.println("Pool size is: " + pool);
        Executor executor = Executors.newFixedThreadPool(pool);
        while (true) {
            System.out.println("Accepting a Request...");
            Socket sock = server.accept();

            Performer performer = new Performer(sock, strings);
            System.out.println("Performer Created, Starting Performer");
            try {
                executor.execute(performer);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Close Socket of Client.");
                sock.close();
            }
        }
    }
}
