import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This client is used to prepopulate the key value store
 * through hardcoding key value pairs before allowing the users
 * to interact with the store.
 */
public class InitialClient {
    private static final Logger ClientLog = Logger.getLogger(Client.class.getName());

    /**
     * Connects to the key value store using the same
     * port number entered by the user while starting
     * the server
     * @param port port which user enters to connect to server
     */
    public void prepopulateStore(int port) {
        // create an array to store the server (participants)
        Participant[] participants = new Participant[5];
        // create an array for looking up registries
        Registry[] registries = new Registry[5];

        try {
            // look up registry for each server
            for (int i = 0; i < 5; i++) {
                registries[i] = LocateRegistry.getRegistry("localhost", port);
                try {
                    participants[i] = (Participant) registries[i].lookup("participant"+i);
                } catch (RemoteException | NotBoundException re) {
                    System.out.println("Registry look up failed for" + i);
                    throw new RuntimeException();
                }
            }
            // prepopulate the store with some values
            participants[0].clientRequest("PUT", "Sonal", "Boston");
            participants[1].clientRequest("PUT", "John", "New York");
            participants[2].clientRequest("PUT", "Jane", "Seattle");
            participants[3].clientRequest("PUT", "Max", "SF");
            participants[4].clientRequest("PUT", "Rohit", "Miami");
        } catch (RemoteException re) {
            System.out.println("Exception in remote invocation: " + re.getMessage());
        }
    }
}
