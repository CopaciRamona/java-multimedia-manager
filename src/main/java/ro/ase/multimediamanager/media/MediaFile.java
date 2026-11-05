package ro.ase.multimediamanager.media;

import java.io.File;

/** Retine fisierul media si extensia lui. */
public class MediaFile {

    private File file;
    private String extension;

    /**
     * Creeaza descrierea unui fisier media gasit.
     *
     * @param file fisierul media
     * @param extension extensia fisierului, fara punct
     */
    public MediaFile(File file, String extension) {
        this.file = file;
        this.extension = extension;
    }

    /**
     * Intoarce fisierul media.
     *
     * @return fisierul media
     */
    public File getFile() {
        return file;
    }

    /**
     * Intoarce numele fisierului, inclusiv extensia.
     *
     * @return numele fisierului
     */
    public String getName() {
        return file.getName();
    }

    /**
     * Intoarce extensia fisierului, fara punct.
     *
     * @return extensia, de exemplu mp3 sau jpg
     */
    public String getExtension() {
        return extension;
    }
}
