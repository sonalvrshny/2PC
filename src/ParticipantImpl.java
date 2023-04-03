import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines the implementation for the replica servers used in
 * the two-phase commit protocol
 */
public class ParticipantImpl extends UnicastRemoteObject implements Participant {
    private static final Logger ServerLog = Logger.getLogger(CoordinatorImpl.class.getName());
    private final KeyValueStore keyValueStore = new KeyValueStoreImpl();
    private Coordinator coordinator;
    private final int participantId;

    protected ParticipantImpl(int participantId) throws RemoteException {
        super();
        this.participantId = participantId;
    }

    /**
     * Method for sending a "ready" or "fail" message to the
     * coordinator when it requests for prepare to commit phase
     * of the 2PC
     * @param request request made by client
     * @return READY or FAIL
     * @throws RemoteException thrown when remote invocation fails
     */
    @Override
    public Acknowledgement prepare(Transaction request) throws RemoteException {
        return Acknowledgement.ACK_READY;
    }

    /**
     * Method for sending a "ready" or "fail" message to the
     * coordinator when it requests for commit phase
     * of the 2PC
     * @param request request made by client
     * @return READY or FAIL
     * @throws RemoteException thrown when remote invocation fails
     */
    @Override
    public Acknowledgement commit(Transaction request) throws RemoteException {
        try {
            if (Objects.equals(request.getRequest(), "DEL")) {
                keyValueStore.deleteFromKeyValue(request.getKey());
            } else {
                keyValueStore.putToKeyValue(request.getKey(), request.getValue());
            }
            return Acknowledgement.ACK_READY;
        } catch (Exception e) {
            return Acknowledgement.ACK_FAIL;
        }
    }

    @Override
    public void abort(Transaction request) throws RemoteException {

    }

    /**
     * Get the identification of the participant server
     * @return participant id
     * @throws RemoteException thrown when remote invocation fails
     */
    @Override
    public int getPartId() throws RemoteException {
        return participantId;
    }

    /**
     * Add coordinator to each of the servers, so it can be used
     * for the 2PC protocol
     * @param coordinator Coordinator used for 2PC
     * @throws RemoteException thrown when remote invocation fails
     */
    @Override
    public void addCoordinator(Coordinator coordinator) throws RemoteException {
        this.coordinator = coordinator;
    }

    /**
     * Method to accept the client request and send it to coordinator
     * to initiate the two-phase commit protocol
     * @param request GET, PUT or DEL request
     * @param key key to be used in the operation
     * @param value value to be used in the operation
     * @return "success" or "fail" depending on 2PC execution
     * @throws RemoteException thrown when remote invocation fails
     */
    @Override
    public String clientRequest(String request, String key, String value) throws RemoteException {
        Transaction transaction = new Transaction(States.INITIAL, key, value, request);
        if (request.equals("GET")) {
            ServerLog.log(Level.INFO, String.format("Server number %s completed GET request " +
                    "for key %s", participantId, key));
            return keyValueStore.getFromKeyValue(key);
        }
        if (request.equals("DEL") && !keyValueStore.containsKey(key)) {
            return "Invalid key";
        }
        else {
            boolean successOrFail = this.coordinator.initiate2PC(transaction);
            return successOrFail ? "success" : "fail";
        }
    }
}
