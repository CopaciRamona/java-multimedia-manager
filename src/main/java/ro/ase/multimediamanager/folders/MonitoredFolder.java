package ro.ase.multimediamanager.folders;

import ro.ase.multimediamanager.exceptions.InvalidFolderException;

import java.io.File;

/** Reprezinta un director pe care aplicatia il urmareste. */
public class MonitoredFolder {

    private File folder;

    private MonitoredFolder(File folder) {
        this.folder = folder;
    }

    /**
     * Verifica directorul introdus si il pregateste pentru lista de directoare urmarite.
     *
     * @param rawPath calea scrisa de utilizator
     * @return directorul pregatit pentru folosire
     * @throws InvalidFolderException daca calea este goala, invalida, nu indica un director
     *                                sau directorul nu poate fi citit
     */
    public static MonitoredFolder fromUserInput(String rawPath)
            throws InvalidFolderException {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            throw new InvalidFolderException("Calea directorului nu poate fi goala.");
        }

        File folder;
        try {
            folder = new File(rawPath.trim()).getAbsoluteFile();
        } catch (IllegalArgumentException exception) {
            throw new InvalidFolderException("Calea introdusa nu este valida.", exception);
        }

        if (!folder.exists()) {
            throw new InvalidFolderException("Directorul nu exista: " + folder.getPath());
        }
        if (!folder.isDirectory()) {
            throw new InvalidFolderException("Calea indicata nu este un director: "
                    + folder.getPath());
        }
        if (!folder.canRead()) {
            throw new InvalidFolderException("Directorul nu poate fi citit: "
                    + folder.getPath());
        }

        return new MonitoredFolder(folder);
    }

    /**
     * Reface intrarea pentru o cale salvata. Directorul poate sa nu mai fie disponibil acum.
     *
     * @param rawPath calea citita din fisier
     * @return directorul citit din fisier
     * @throws InvalidFolderException daca in fisier calea este goala sau invalida
     */
    public static MonitoredFolder fromStoredPath(String rawPath)
            throws InvalidFolderException {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            throw new InvalidFolderException("Fisierul cu locatii contine o cale goala.");
        }

        try {
            File folder = new File(rawPath.trim()).getAbsoluteFile();
            return new MonitoredFolder(folder);
        } catch (IllegalArgumentException exception) {
            throw new InvalidFolderException("Fisierul cu locatii contine o cale invalida.",
                    exception);
        }
    }

    /**
     * Intoarce directorul.
     *
     * @return directorul urmarit
     */
    public File getFolder() {
        return folder;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MonitoredFolder)) {
            return false;
        }
        MonitoredFolder other = (MonitoredFolder) object;
        return folder.equals(other.folder);
    }

    @Override
    public int hashCode() {
        return folder.hashCode();
    }

    /**
     * Verifica daca directorul exista si poate fi citit in acest moment.
     *
     * @return true daca directorul este disponibil pentru citire
     */
    public boolean isAccessible() {
        return folder.isDirectory() && folder.canRead();
    }

    /**
     * Intoarce calea directorului ca text.
     *
     * @return calea directorului
     */
    @Override
    public String toString() {
        return folder.getPath();
    }
}
