package za.co.hydra24.db;

/** A row is still referenced by another, or refers to one that does not exist. */
public class ForeignKeyViolationException extends RuntimeException {

    public ForeignKeyViolationException(Throwable cause) {
        super(cause);
    }
}
