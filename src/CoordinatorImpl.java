import logger.Logging;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;

/**
 * Coordinator implementation which maintains a list
 * of all the servers to which client connects to. Operations
 * are performed following two-phase commit protocol.
 */
public class CoordinatorImpl extends UnicastRemoteObject implements Coordinator {
    private static final Logger ServerLog = Logger.getLogger(CoordinatorImpl.class.getName());
    private final Participant[] participantList = new Participant[5];
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final Boolean[] votes = {false, false, false, false, false};

    protected CoordinatorImpl() throws RemoteException {
        super();
    }

    /**
     * Helper method to start the 'prepare to commit' step of
     * two-phase commit protocol.
     * @param request request made by the client
     * @return boolean value depending on success/failure of 2PC
     */
    private boolean broadcastPrepare(Transaction request) {
        ServerLog.log(Level.INFO, String.format("Coordinator broadcasting to all servers to prepare to commit for %s " +
                "operation. Key: %s, Value: %s", request.getRequest(), request.getKey(), request.getValue()));
        return execute2PC(request);
    }

    /**
     * Helper method to start the 'commit' step of
     * two-phase commit protocol.
     * @param request request made by the client
     * @return boolean value depending on success/failure of 2PC
     */
    private boolean broadcastCommit(Transaction request) {
        ServerLog.log(Level.INFO, String.format("Coordinator broadcasting to all servers to commit for %s " +
                "operation. Key: %s, Value: %s", request.getRequest(), request.getKey(), request.getValue()));
        return execute2PC(request);
    }

    private void broadcastAbort(Transaction request) {

    }

    /**
     * Executes the 2PC protocol depending on the current state.
     * Initial state prepares all servers to commit, Commit state
     * tells all servers to perform the commit.
     * @param request request made by the client
     * @return boolean value depending on success/failure of 2PC
     */
    private boolean execute2PC(Transaction request) {
        // used for running the operation concurrently
        List<Future<String>> futures = new ArrayList<>();

        // if the request state is COMMIT, tell all servers to perform commit
        if (request.getState() == States.COMMIT) {
            for (Participant participant : participantList) {
                Future<String> future = executorService.submit(() -> {
                    try {
                        Acknowledgement ack = participant.commit(request);
                        ServerLog.log(Level.INFO, String.format("Received ready commit acknowledgement from " +
                                "participant %s", participant.getPartId()));
                        return ack.toString();
                    } catch (Exception e) {
                        broadcastAbort(request);
                        return Acknowledgement.ACK_FAIL.toString();
                    }
                });
                futures.add(future);
            }
        }

        // if the request state is INITIAL, tell all servers to prepare to commit
        if (request.getState() == States.INITIAL) {
            for (Participant participant : participantList) {
                Future<String> future = executorService.submit(() -> {
                    try {
                        Acknowledgement ack = participant.prepare(request);
                        ServerLog.log(Level.INFO, String.format("Received ready prepare acknowledgement from " +
                                "participant %s", participant.getPartId()));
                        return ack.toString();
                    } catch (Exception e) {
                        broadcastAbort(request);
                        return "down";
                    }
                });
                futures.add(future);
            }
        }

        int index = 0;
        try {
            for (Future<String> future : futures) {
                if (future.get().equals(Acknowledgement.ACK_READY.toString())) {
                    votes[index] = true;
                    index++;
                } else {
                    Thread.sleep(1000);
                    if (future.get().equals(Acknowledgement.ACK_READY.toString())) {
                        votes[index] = true;
                        index++;
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            ServerLog.log(Level.WARNING, "At least one server is down");
            throw new RuntimeException(e);
        }
        if (allVotesTrue(votes)) {
            ServerLog.log(Level.INFO, "Received ready votes from all participants");
            setVotesFalse(votes);
            return true;
        }
        else {
            ServerLog.log(Level.INFO, "At least one of the server participants failed");
            setVotesFalse(votes);
            return false;
        }
    }

    /**
     * Add a server as a participant of the 2PC protocol.
     * @param participant one of the replica servers
     * @param participantNum identification of the replica server
     * @throws RemoteException thrown when remote invocation fails
     */
    @Override
    public void addParticipant(Participant participant, int participantNum) throws RemoteException {
        ServerLog.log(Level.INFO, "Added server number " + participant.getPartId());
        this.participantList[participantNum] = participant;
    }

    // helper methods for 2PC
    private boolean allVotesTrue(Boolean[] votes) {
        return !Arrays.asList(votes).contains(false);
    }

    private void setVotesFalse(Boolean[] votes) {
        Arrays.fill(votes, false);
    }

    /**
     * Initiate the 2PC protocol with a transaction request. This
     * request is the operation requested by the client and the key-value
     * pair required for the operations.
     * @param transaction input request by client
     * @return true or false depending on 2PC output
     * @throws RemoteException thrown when remote invocation fails
     */
    @Override
    public boolean initiate2PC(Transaction transaction) throws RemoteException {
        ServerLog.log(Level.INFO, "Initiating 2PC");
        if (!broadcastPrepare(transaction)) {
            ServerLog.log(Level.INFO, "Broadcast prepare failed");
            return false;
        }
        ServerLog.log(Level.INFO, "Prepare phase of 2PC complete");
        transaction.setState(States.COMMIT);
        if (!broadcastCommit(transaction)) {
            ServerLog.log(Level.INFO, "Broadcast commit failed");
            return false;
        }
        ServerLog.log(Level.INFO, "Commit phase of 2PC complete");
        ServerLog.log(Level.INFO, String.format("%s request has been completed", transaction.getRequest()));
        return true;
    }

    public static void main(String[] args) throws IOException {
        // disable logging to console log
        ServerLog.setUseParentHandlers(false);

        // create file for logging and set its formatting
        FileHandler serverLogHandler = new FileHandler("ServerLog");
        Logging logging = new Logging(serverLogHandler);
        logging.formatLogging();

        ServerLog.addHandler(serverLogHandler);

        // accept port number
        int port = 0;
        try {
            port = parseInt(args[0]);
        } catch (IllegalArgumentException iae) {
            System.out.println(iae.getMessage());
            ServerLog.log(Level.INFO, iae.getMessage());
        }

        Registry registry = LocateRegistry.createRegistry(port);
        Participant[] participants = new Participant[5];

        // bind the servers to the rmi registry
        try {
            Coordinator coordinator = new CoordinatorImpl();
            registry.rebind("Coordinator", coordinator);
            System.out.println("Starting the Coordinator...");
            ServerLog.log(Level.INFO, "Starting the Coordinator...");
            for (int i = 0; i < 5; i++) {
                participants[i] = new ParticipantImpl(i + 1);
                participants[i].addCoordinator(coordinator);
                registry.rebind("participant"+i, participants[i]);
                coordinator.addParticipant(participants[i], i);
                System.out.printf("Server number %s has been added.\n", i + 1);
            }
        } catch (RemoteException re) {
            ServerLog.log(Level.WARNING, "Error in binding servers: " + re.getMessage());
            System.out.println("Error in binding servers: " + re.getMessage());
        }
        InitialClient initialClient = new InitialClient();
        initialClient.prepopulateStore(port);
    }
}
