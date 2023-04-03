/**
 * Used to define if replica servers are ready for
 * prepare and commit phase or if they have failed.
 */
public enum Acknowledgement {
    ACK_READY,
    ACK_FAIL
}
