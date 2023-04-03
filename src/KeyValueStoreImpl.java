import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementing the key value store through the
 * get, put and delete operations
 */
public class KeyValueStoreImpl extends UnicastRemoteObject implements KeyValueStore {
    protected ConcurrentHashMap<String, String> keyValueStore = new ConcurrentHashMap<>();

    /**
     * Explicit constructor to declare RemoteException
     */
    public KeyValueStoreImpl() throws RemoteException {
        super();
    }

    /**
     * Get value from the key value store based on
     * key provided. If key is not present, an exception
     * is thrown
     *
     * @param key input for which value is required
     * @return value of key from store
     */
    @Override
    public String getFromKeyValue(String key) throws RemoteException {
//        ServerLog.log(Level.INFO, String.format("Client made a GET request for key %s", key));
        if (!keyValueStore.containsKey(key)) {
            throw new IllegalArgumentException(
                    "This key is not present in key value store");
        }
        return keyValueStore.get(key);
    }

    /**
     * Put key and value into the store. If key is already
     * present, the value is updated
     *
     * @param key   input to store
     * @param value input to store
     */
    @Override
    public void putToKeyValue(String key, String value) throws RemoteException {
//        ServerLog.log(Level.INFO, String.format("Client made a PUT request for key %s and value %s", key, value));
        keyValueStore.put(key, value);
    }

    /**
     * Delete key and value entry from store based
     * on key provided. If key is not present, an exception
     * is thrown
     *
     * @param key input for which key-value have to be removed
     */
    @Override
    public void deleteFromKeyValue(String key) throws RemoteException {
//        ServerLog.log(Level.INFO, String.format("Client made a DELETE request for key %s", key));
        if (!keyValueStore.containsKey(key)) {
            throw new IllegalArgumentException(
                    "This key is not present in key value store");
        }
        keyValueStore.remove(key);
    }

    /**
     * Check if key is present in store or not
     *
     * @param key input key
     * @return true or false depending on if key present in store
     * @throws RemoteException thrown when remote method invocation fails
     */
    @Override
    public boolean containsKey(String key) throws RemoteException {
        return keyValueStore.containsKey(key);
    }
}
