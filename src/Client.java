import logger.Logging;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;


/**
 * Client sends requests to the servers to GET,
 * PUT or DELETE from the key value store. The communication
 * between the clients and servers is through RMI.
 */
public class Client {
    private static final Logger ClientLog = Logger.getLogger(Client.class.getName());

    public static void main(String[] args) throws IOException, NotBoundException {
        // disable logging to console log
        ClientLog.setUseParentHandlers(false);

        // create file for logging and set its formatting
        FileHandler clientLogHandler = new FileHandler("ClientLog");
        Logging logging = new Logging(clientLogHandler);
        logging.formatLogging();

        ClientLog.addHandler(clientLogHandler);

        ClientLog.log(Level.INFO, "Client main called");

        Scanner scanner = new Scanner(System.in);
        int serverCount = 5;

        // accept port number
        int port = 0;
        try {
            port = parseInt(args[0]);
        } catch (IllegalArgumentException iae) {
            System.out.println(iae.getMessage());
            ClientLog.log(Level.INFO, iae.getMessage());
        }

        // create an array to store the server (participants)
        Participant[] participants = new Participant[serverCount];
        // create an array for looking up registries
        Registry[] registries = new Registry[serverCount];

        // look up registry for each server
        for (int i = 0; i < serverCount; i++) {
            registries[i] = LocateRegistry.getRegistry("localhost", port);
            try {
                participants[i] = (Participant) registries[i].lookup("participant"+i);
                ClientLog.log(Level.INFO, String.format("Request can be made to server number %s", i + 1));
            } catch (RemoteException re) {
                System.out.println("Registry look up failed for" + i);
                throw new RuntimeException();
            }
        }


        String inputOperation = "";
        String server_number_input;

        try {
            // continue accepting response until user enter q/Q
            while (!inputOperation.equalsIgnoreCase("Q")) {
                // get input from user until user enters q/Q to quit
                System.out.println("Enter q to quit");

                boolean server_flag = true;
                int server_number = 0;
                while (server_flag){
                    System.out.println("Enter server to be requested from (1-5)");
                    server_number_input = scanner.nextLine().trim();
                    if (server_number_input.equalsIgnoreCase("Q")) {
                        break;
                    }
                    try {
                        Integer.parseInt(server_number_input);
                    } catch (NumberFormatException nfe) {
                        System.out.println("Please enter a server number between 1 and 5");
                        continue;
                    }
                    if (Integer.parseInt(server_number_input) < 1
                            || Integer.parseInt(server_number_input) > 5) {
                        System.out.println("Please enter a server number between 1 and 5");
                    } else {
                        server_number = Integer.parseInt(server_number_input) - 1;
                        server_flag = false;
                    }
                }
                ClientLog.log(Level.INFO, String.format("Client requested from server number: %s", server_number));

                System.out.println("Enter operation GET/PUT/DEL: ");
                inputOperation = scanner.nextLine();
                String reply;

                ClientLog.log(Level.INFO, String.format("Client sent a request %s", inputOperation));
                switch (inputOperation.toUpperCase().trim()) {
                    // get value from key value store
                    case "GET":
                        System.out.println("Enter key to get value from store: ");
                        String getKey = scanner.nextLine();
                        String getResponse;
                        try {
                            getResponse = participants[server_number].clientRequest("GET", getKey, null);
                            reply = String.format("GET request successful. The value for '%s' is '%s'", getKey, getResponse);
                        } catch (IllegalArgumentException iae) {
                            reply = iae.getMessage();
                        }
                        System.out.println(reply);
                        ClientLog.log(Level.INFO, reply);
                        break;
                    // put key-value to store
                    case "PUT":
                        System.out.println("Enter key and value to put into store: ");
                        System.out.print("Enter key: ");
                        String putKey = scanner.nextLine();
                        System.out.print("Enter value: ");
                        String putValue = scanner.nextLine();
                        if (participants[server_number].clientRequest("PUT", putKey, putValue).equals("success")) {
                            reply = String.format("PUT request successful. Key '%s' and value '%s' updated in store", putKey, putValue);
                        }
                        else {
                            reply = "PUT request unsuccessful. Server failed";
                        }

                        System.out.println(reply);
                        ClientLog.log(Level.INFO, reply);
                        break;
                    // delete key from store
                    case "DEL":
                        System.out.println("Enter key to remove from store: ");
                        System.out.print("Enter key: ");
                        String delKey = scanner.nextLine();
                        String response = participants[server_number].clientRequest("DEL", delKey, null);
                        if (response.equals("Invalid key")) {
                            reply = "This key is not present in key value store";
                        } else {
                            reply = String.format("DELETE request successful. The key '%s' has been removed from " +
                                    "the store", delKey);
                        }
                        System.out.println(reply);
                        ClientLog.log(Level.INFO, reply);
                        break;
                    // exit process
                    case "Q":
                        System.out.println("Quitting...");
                        ClientLog.log(Level.INFO, "Closing the client");
                        break;
                    default:
                        System.out.println("This is not a valid operation");
                        ClientLog.log(Level.INFO, "Invalid operation");
                        break;
                }
            }
        } catch (RemoteException re) {
            System.out.println("Exception in remote invocation: " + re.getMessage());
        }
    }
}
