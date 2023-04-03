import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Coordinator class for implementing the 2PC protocol.
 * The methods involve adding servers as participants to
 * the protocol and initiating 2PC.
 */
public interface Coordinator extends Remote {
    /**
     * Add a server as a participant of the 2PC protocol.
     * @param participant one of the replica servers
     * @param participantNum identification of the replica server
     * @throws RemoteException thrown when remote invocation fails
     */
    void addParticipant(Participant participant, int participantNum) throws RemoteException;

    /**
     * Initiate the 2PC protocol with a transaction request. This
     * request is the operation requested by the client and the key-value
     * pair required for the operations.
     * @param transaction input request by client
     * @return true or false depending on 2PC output
     * @throws RemoteException thrown when remote invocation fails
     */
    boolean initiate2PC(Transaction transaction) throws RemoteException;

}
