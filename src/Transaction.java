/**
 * An object to define the client request made to the
 * key-value store replicas. State defines the current step in the
 * 2PC protocol. Request is the GET, PUT or DEL operation made
 * by the client with key and value.
 */
public class Transaction {
    private States state;
    private final String key;
    private final String value;
    private final String request;


    public Transaction(States state, String key, String value, String request) {
        this.state = state;
        this.key = key;
        this.value = value;
        this.request = request;
    }

    /**
     * Get the current state of the 2PC.
     * @return state of 2PC
     */
    public States getState() {
        return state;
    }

    /**
     * Set the state of the 2PC
     * @param state state of 2PC
     */
    public void setState(States state) {
        this.state = state;
    }

    /**
     * @return key used in client request
     */
    public String getKey() {
        return key;
    }

    /**
     * @return value used in client request
     */
    public String getValue() {
        return value;
    }

    /**
     * @return GET, PUT or DEL operation used in client request
     */
    public String getRequest() {
        return request;
    }
}