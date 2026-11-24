package bank.model;

/** Business-rule or validation failure that should be shown to the user. */
public class BankException extends Exception {
    public BankException(String message) { super(message); }
    public BankException(String message, Throwable cause) { super(message, cause); }
}
