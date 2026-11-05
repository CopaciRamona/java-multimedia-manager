package ro.ase.multimediamanager.exceptions;

/** Se foloseste cand o cale nu indica un director care poate fi folosit de aplicatie. */
public class InvalidFolderException extends Exception {

    /**
     * Creeaza exceptia cu mesajul care explica problema.
     *
     * @param message mesajul care va fi afisat pentru utilizator
     */
    public InvalidFolderException(String message) {
        super(message);
    }

    /**
     * Creeaza exceptia cu un mesaj si cu eroarea care a provocat-o.
     *
     * @param message mesajul care va fi afisat pentru utilizator
     * @param cause eroarea initiala
     */
    public InvalidFolderException(String message, Throwable cause) {
        super(message, cause);
    }
}
