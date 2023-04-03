import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Defines the implementation for the replica servers used in
 * the two-phase commit protocol
 */
public interface Participant extends Remote {

    /**
     * Method for sending a "ready" or "fail" message to the
     * coordinator when it requests for prepare to commit phase
     * of the 2PC
     * @param request request made by client
     * @return READY or FAIL
     * @throws RemoteException thrown when remote invocation fails
     */
    Acknowledgement prepare(Transaction request) throws RemoteException;

    /**
     * Method for sending a "ready" or "fail" message to the
     * coordinator when it requests for commit phase
     * of the 2PC
     * @param request request made by client
     * @return READY or FAIL
     * @throws RemoteException thrown when remote invocation fails
     */
    Acknowledgement commit(Transaction request) throws RemoteException;

    void abort(Transaction request) throws RemoteException;

    /**
     * Get the identification of the participant server
     * @return participant id
     * @throws RemoteException thrown when remote invocation fails
     */
    int getPartId() throws RemoteException;

    /**
     * Add coordinator to each of the servers, so it can be used
     * for the 2PC protocol
     * @param coordinator Coordinator used for 2PC
     * @throws RemoteException thrown when remote invocation fails
     */
    void addCoordinator(Coordinator coordinator) throws RemoteException;

    /**
     * Method to accept the client request and send it to coordinator
     * to initiate the two-phase commit protocol
     * @param request GET, PUT or DEL request
     * @param key key to be used in the operation
     * @param value value to be used in the operation
     * @return "success" or "fail" depending on 2PC execution
     * @throws RemoteException thrown when remote invocation fails
     */
    String clientRequest(String request, String key, String value) throws RemoteException;
}
