/**
  File: Performer.java
  Author: Student in Fall 2020B, Josh McManus
  Description: Performer class in package taskone.
*/

package taskone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.json.JSONObject;

/**
 * Class: Performer 
 * Description: Threaded Performer for server tasks.
 */
@SuppressWarnings("VulnerableCodeUsages")
class Performer {

    private StringList state;
    private Socket conn;

    public Performer(Socket sock, StringList strings) {
        this.conn = sock;
        this.state = strings;
    }

    public JSONObject add(String str) {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "1");
        state.add(str);
        json.put("data", state.toString());
        return json;
    }

    public JSONObject clear() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "2");
        state = new StringList();
        json.put("data", state.toString());
        return json;
    }

    public JSONObject find(String str) {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "3");
        if (state.size() > 0) {
            json.put("data", Integer.toString(state.contains(str)));
        } else {
            json.put("data", -1);
        }
        return json;
    }

    public JSONObject display() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "4");
        json.put("data", state.toString());
        return json;
    }

    public JSONObject sort() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "5");
        state.sort();
        json.put("data", state);
        return json;
    }

    public JSONObject prepend(int i, String str) {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "6");
        state.prepend(i, str);
        json.put("data", state);
        return json;
    }

    public static JSONObject error(String err) {
        JSONObject json = new JSONObject();
        json.put("ok", false);
        json.put("message", err);
        return json;
    }

    public void doPerform() {
        boolean quit = false;
        OutputStream out;
        InputStream in;
        try {
            out = conn.getOutputStream();
            in = conn.getInputStream();
            System.out.println("Server connected to client.");
            while (!quit) {
                byte[] messageBytes = tasktwo.NetworkUtils.receive(in);
                JSONObject message = null;
                JSONObject returnMessage = null;
                int choice = 0;
                try {
                    message = tasktwo.JsonUtils.fromByteArray(messageBytes);
                    returnMessage = new JSONObject();
                    choice = message.getInt("selected");
                } catch (Exception e) {
                    System.out.println("Invalid Message.");
                }
                    switch (choice) {
                        case (1):
                            //add
                            String inStr = (String) message.get("data");
                            returnMessage = add(inStr);
                            break;
                        case (2):
                            //clear
                            returnMessage = clear();
                            break;
                        case (3):
                            //find
                            inStr = (String) message.get("data");
                            returnMessage = find(inStr);
                            break;
                        case (4):
                            //display
                            returnMessage = display();
                            break;
                        case (5):
                            //sort
                            returnMessage = sort();
                            break;
                        case (6):
                            //prepend
                            inStr = (String) message.get("data");
                            String[] values = inStr.split(" ");
                            returnMessage = prepend(Integer.parseInt(values[0]), values[1]);
                            break;
                        case (0):
                            //quit
                            quit = true;
                            break;
                        default:
                            returnMessage = error("Invalid selection: " + choice 
                                    + " is not an option");
                            break;
                    }
                // we are converting the JSON object we have to a byte[]
                if (returnMessage != null && !returnMessage.isEmpty()) {
                    byte[] output = JsonUtils.toByteArray(returnMessage);
                    NetworkUtils.send(out, output);
                }
            }
            // close the resource
            System.out.println("Close the Resources of Client.");
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
