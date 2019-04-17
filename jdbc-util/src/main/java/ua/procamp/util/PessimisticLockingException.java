package ua.procamp.util;

/**
 * @author Danil Kuznetsov (kuznetsov.danil.v@gmail.com)
 */
public class PessimisticLockingException extends RuntimeException {
    public PessimisticLockingException(String message) {
        super(message);
    }
}
