package ua.procamp.util;

/**
 * @author Danil Kuznetsov (kuznetsov.danil.v@gmail.com)
 */
public class OptimisticLockingException extends RuntimeException {
    public OptimisticLockingException(String message) {
        super(message);
    }
}
