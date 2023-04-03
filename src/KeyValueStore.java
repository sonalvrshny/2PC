import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Used to maintain a key value store by implementing
 * the GET, PUT, DELETE operations. These methods are used
 * by the server to update the key value store based on
 * the request sent by the client
 */
public interface KeyValueStore extends Remote {
    /**
     * Get value from the key value store based on
     * key provided
     * @param key input for which value is required
     * @return value of key from store
     * @throws RemoteException thrown when remote method invocation fails
     */
    String getFromKeyValue(String key) throws RemoteException;

    /**
     * Put key and value into the store
     * @param key input to store
     * @param value input to store
     * @throws RemoteException thrown when remote method invocation fails
     */
    void putToKeyValue(String key, String value) throws RemoteException;

    /**
     * Delete key and value entry from store based
     * on key provided
     * @param key input for which key-value have to be removed
     * @throws RemoteException thrown when remote method invocation fails
     */
    void deleteFromKeyValue(String key) throws RemoteException;

    /**
     * Check if key is present in store or not
     * @param key input key
     * @return true or false depending on if key present in store
     * @throws RemoteException thrown when remote method invocation fails
     */
    boolean containsKey(String key) throws RemoteException;
}
