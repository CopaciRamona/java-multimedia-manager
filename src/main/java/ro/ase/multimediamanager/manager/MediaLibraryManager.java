package ro.ase.multimediamanager.manager;

import ro.ase.multimediamanager.exceptions.InvalidFolderException;
import ro.ase.multimediamanager.folders.MonitoredFolder;
import ro.ase.multimediamanager.media.MediaFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

/** Tine lista directoarelor urmarite si cauta fisierele media din ele. */
public class MediaLibraryManager {

    private enum MediaExtension {
        MP3("mp3"), WAV("wav"), JPG("jpg"), PNG("png");

        private final String text;

        MediaExtension(String text) {
            this.text = text;
        }
    }

    private static final MediaExtension[] EXTENSIONS = MediaExtension.values();

    private File foldersFile;
    private ArrayList<MonitoredFolder> folders;
    private ArrayList<String> removedFolders;
    private HashMap<File, MediaFile> removedMediaFiles;

    /**
     * Incarca lista directoarelor salvate in fisier.
     *
     * @param foldersFile fisierul in care se tine lista directoarelor
     * @throws IOException daca fisierul nu poate fi citit
     */
    public MediaLibraryManager(File foldersFile) throws IOException {
        this.foldersFile = foldersFile.getAbsoluteFile();
        this.folders = loadFolders();
        this.removedFolders = new ArrayList<>();
        this.removedMediaFiles = loadRemovedMediaFiles();
    }

    /**
     * Intoarce directoarele urmarite, fara sa dea acces direct la lista managerului.
     *
     * @return o copie a listei de directoare
     */
    public ArrayList<MonitoredFolder> getFolders() {
        return new ArrayList<>(folders);
    }

    /**
     * Verifica directorul si il adauga daca nu este deja in lista.
     *
     * @param rawPath calea scrisa de utilizator
     * @return true daca directorul a fost adaugat; false daca era deja in lista
     * @throws InvalidFolderException daca calea nu indica un director valid si accesibil
     */
    public boolean addFolder(String rawPath) throws InvalidFolderException {
        MonitoredFolder folder = MonitoredFolder.fromUserInput(rawPath);
        if (folders.contains(folder)) {
            return false;
        }

        folders.add(folder);
        return true;
    }

    /**
     * Scoate din lista directorul ales in meniu.
     * Directorul real si fisierele din el raman pe calculator.
     *
     * @param oneBasedIndex numarul directorului din meniu, unde primul este 1
     * @return directorul scos din lista
     * @throws IndexOutOfBoundsException daca numarul nu corespunde unui director din lista
     */
    public MonitoredFolder removeFolder(int oneBasedIndex) {
        if (oneBasedIndex < 1 || oneBasedIndex > folders.size()) {
            throw new IndexOutOfBoundsException("Numarul directorului nu este in lista.");
        }

        int index = oneBasedIndex - 1;
        MonitoredFolder removedFolder = folders.remove(index);
        removedFolders.add(removedFolder + " | " + LocalDateTime.now());
        return removedFolder;
    }

    /**
     * Scoate un fisier din colectia aplicatiei si il noteaza pentru raport.
     * Fisierul original ramane pe calculator.
     *
     * @param folder directorul in care se afla fisierul
     * @param oneBasedIndex numarul fisierului din lista afisata, unde primul este 1
     * @return fisierul scos din colectie
     * @throws IOException daca directorul nu poate fi scanat
     * @throws IndexOutOfBoundsException daca numarul nu corespunde unui fisier din lista
     */
    public MediaFile removeMediaFile(MonitoredFolder folder, int oneBasedIndex)
            throws IOException {
        ArrayList<MediaFile> mediaFiles = scanFolder(folder);
        if (oneBasedIndex < 1 || oneBasedIndex > mediaFiles.size()) {
            throw new IndexOutOfBoundsException("Numarul fisierului nu este in lista.");
        }

        MediaFile removedFile = mediaFiles.get(oneBasedIndex - 1);
        removedMediaFiles.put(removedFile.getFile(), removedFile);
        return removedFile;
    }

    /**
     * Cauta fisierele cu extensiile acceptate si le omite pe cele scoase din colectie.
     * Nu verifica subdirectoarele.
     *
     * @param monitoredFolder directorul in care se cauta
     * @return fisierele media gasite
     * @throws IOException daca directorul nu poate fi citit
     */
    public ArrayList<MediaFile> scanFolder(MonitoredFolder monitoredFolder) throws IOException {
        File folder = monitoredFolder.getFolder();
        if (!folder.isDirectory() || !folder.canRead()) {
            throw new IOException("Nu se poate citi directorul: " + folder.getPath());
        }

        File[] entries = folder.listFiles();
        if (entries == null) {
            throw new IOException("Nu s-au putut afisa fisierele din: " + folder.getPath());
        }

        ArrayList<MediaFile> mediaFiles = new ArrayList<>();
        for (File entry : entries) {
            if (!entry.isFile()) {
                continue;
            }

            String extension = getExtension(entry.getName());
            if (!isSupportedExtension(extension)) {
                continue;
            }

            File entryPath = entry.getAbsoluteFile();
            if (!isRemovedMediaFile(entryPath)) {
                mediaFiles.add(new MediaFile(entryPath, extension));
            }
        }

        return mediaFiles;
    }

    /**
     * Numara fisierele din fiecare director si calculeaza numarul si dimensiunea lor pe extensii.
     * Salveaza raportul langa fisierul cu lista directoarelor.
     *
     * @return liniile raportului, pentru afisarea in consola
     * @throws IOException daca raportul nu poate fi salvat
     */
    public ArrayList<String> createGeneralReport() throws IOException {
        int[][] counts = new int[folders.size()][EXTENSIONS.length];
        int[] totals = new int[EXTENSIONS.length];
        long[] bytes = new long[EXTENSIONS.length];
        ArrayList<String> lines = new ArrayList<>();

        lines.add("RAPORT FISIERE MULTIMEDIA");
        lines.add("");
        lines.add("Numar de fisiere media pe director:");

        int total = 0;
        for (int i = 0; i < folders.size(); i++) {
            MonitoredFolder folder = folders.get(i);
            try {
                ArrayList<MediaFile> files = scanFolder(folder);
                lines.add("- " + folder + ": " + files.size() + " fisiere media");
                total += files.size();

                for (MediaFile file : files) {
                    lines.add("    " + file.getName() + " (." + file.getExtension() + ")");
                    int extIndex = getSupportedExtensionIndex(file.getExtension());
                    counts[i][extIndex]++;
                    totals[extIndex]++;
                    bytes[extIndex] += file.getFile().length();
                }
            } catch (IOException exception) {
                lines.add("Nu am putut scana " + folder + ": " + exception.getMessage());
            }
        }

        lines.add("");
        lines.add("Numar de fisiere pe director si extensie:");
        for (int i = 0; i < folders.size(); i++) {
            lines.add(folders.get(i) + ":");
            for (int j = 0; j < EXTENSIONS.length; j++) {
                lines.add("  ." + EXTENSIONS[j].text + ": " + counts[i][j]);
            }
        }

        lines.add("");
        lines.add("Statistici pe extensie:");
        for (int i = 0; i < EXTENSIONS.length; i++) {
            lines.add("- ." + EXTENSIONS[i].text + ": "
                    + totals[i] + " fisiere, " + bytes[i] + " bytes");
        }
        lines.add("Total: " + total + " fisiere media");

        saveReport(lines, "raport.txt");
        return lines;
    }

    /**
     * Creeaza un raport doar pentru directorul ales.
     *
     * @param folder directorul pentru care se face raportul
     * @return liniile raportului, pentru afisarea in consola
     * @throws IOException daca raportul nu poate fi salvat
     */
    public ArrayList<String> createFolderReport(MonitoredFolder folder) throws IOException {
        int[] counts = new int[EXTENSIONS.length];
        long[] bytes = new long[EXTENSIONS.length];
        ArrayList<String> lines = new ArrayList<>();

        lines.add("RAPORT FISIERE MULTIMEDIA");
        lines.add("Director: " + folder);
        lines.add("");

        try {
            ArrayList<MediaFile> files = scanFolder(folder);
            lines.add("Numar total de fisiere media: " + files.size());
            lines.add("Fisiere:");

            for (MediaFile file : files) {
                lines.add("- " + file.getName() + " (." + file.getExtension() + ")");
                int extIndex = getSupportedExtensionIndex(file.getExtension());
                counts[extIndex]++;
                bytes[extIndex] += file.getFile().length();
            }
        } catch (IOException exception) {
            lines.add("Nu am putut scana directorul: " + exception.getMessage());
        }

        lines.add("");
        lines.add("Statistici pe extensie:");
        for (int i = 0; i < EXTENSIONS.length; i++) {
            lines.add("- ." + EXTENSIONS[i].text + ": "
                    + counts[i] + " fisiere, " + bytes[i] + " bytes");
        }

        saveReport(lines, "raport_director.txt");
        return lines;
    }

    /**
     * Creeaza un raport cu fisierele gasite, grupate pe extensie.
     *
     * @return liniile raportului, pentru afisarea in consola
     * @throws IOException daca raportul nu poate fi salvat
     */
    public ArrayList<String> createExtensionReport() throws IOException {
        ArrayList<String> reportLines = new ArrayList<>();
        reportLines.add("RAPORT FISIERE PE EXTENSII");

        for (MediaExtension extension : EXTENSIONS) {
            reportLines.add("");
            reportLines.add("." + extension.text + ":");
            int fileCount = 0;

            for (MonitoredFolder folder : folders) {
                try {
                    ArrayList<MediaFile> mediaFiles = scanFolder(folder);
                    for (MediaFile mediaFile : mediaFiles) {
                        if (mediaFile.getExtension().equalsIgnoreCase(extension.text)) {
                            reportLines.add("- " + mediaFile.getFile());
                            fileCount++;
                        }
                    }
                } catch (IOException exception) {
                    reportLines.add("Nu am putut scana " + folder + ": "
                            + exception.getMessage());
                }
            }

            reportLines.add("Total: " + fileCount + " fisiere");
        }

        saveReport(reportLines, "raport_extensii.txt");
        return reportLines;
    }

    /**
     * Creeaza un raport cu directoarele scoase din lista in sesiunea curenta.
     *
     * @return liniile raportului, pentru afisarea in consola
     * @throws IOException daca raportul nu poate fi salvat
     */
    public ArrayList<String> createDeletionReport() throws IOException {
        ArrayList<String> reportLines = new ArrayList<>();
        reportLines.add("RAPORT ELEMENTE SCOASE DIN COLECTIE");

        reportLines.add("Directoare scoase din lista in sesiunea curenta:");
        if (removedFolders.isEmpty()) {
            reportLines.add("Nu au fost scoase directoare din lista in aceasta sesiune.");
        } else {
            for (String removedFolder : removedFolders) {
                reportLines.add("- " + removedFolder);
            }
        }

        reportLines.add("");
        reportLines.add("Fisiere media scoase din colectie:");
        if (removedMediaFiles.isEmpty()) {
            reportLines.add("Nu au fost scoase fisiere media din colectie.");
        } else {
            for (MediaFile removedFile : removedMediaFiles.values()) {
                reportLines.add("- " + removedFile.getFile());
            }
        }

        saveReport(reportLines, "raport_stergere.txt");
        return reportLines;
    }

    private void saveReport(ArrayList<String> reportLines, String fileName) throws IOException {
        File reportFile = new File("data", fileName);
        try (FileWriter writer = new FileWriter(reportFile, StandardCharsets.UTF_8)) {
            for (String line : reportLines) {
                writer.write(line + System.lineSeparator());
            }
        }
    }

    /**
     * Scrie lista directoarelor si lista fisierelor media scoase. Creeaza folderul parinte daca lipseste.
     *
     * @throws IOException daca fisierul nu poate fi scris
     */
    public void save() throws IOException {
        File dataFile = foldersFile;
        File parent = dataFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdir();
        }

        try (FileWriter writer = new FileWriter(dataFile, StandardCharsets.UTF_8)) {
            for (MonitoredFolder folder : folders) {
                writer.write(folder.getFolder().getAbsolutePath() + System.lineSeparator());
            }
        }

        File removedMediaFile = new File(parent, "fisiere_scoase.txt");
        try (FileWriter writer = new FileWriter(removedMediaFile, StandardCharsets.UTF_8)) {
            for (MediaFile removedFile : removedMediaFiles.values()) {
                writer.write(removedFile.getFile().getAbsolutePath() + System.lineSeparator());
            }
        }
    }

    private HashMap<File, MediaFile> loadRemovedMediaFiles() throws IOException {
        HashMap<File, MediaFile> loadedFiles = new HashMap<>();
        File parentFolder = foldersFile.getParentFile();
        File removedMediaFile = new File(parentFolder, "fisiere_scoase.txt");
        if (!removedMediaFile.exists()) {
            return loadedFiles;
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(removedMediaFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                try {
                    File storedFile = new File(line).getAbsoluteFile();
                    String extension = getExtension(storedFile.getName());
                    loadedFiles.put(storedFile,
                            new MediaFile(storedFile, extension));
                } catch (IllegalArgumentException exception) {
                    throw new IOException("Fisierul cu elemente scoase contine o cale invalida: "
                            + line, exception);
                }
            }
        }
        return loadedFiles;
    }

    private boolean isRemovedMediaFile(File file) {
        return removedMediaFiles.containsKey(file);
    }

    private ArrayList<MonitoredFolder> loadFolders() throws IOException {
        File dataFile = foldersFile;
        if (!dataFile.exists()) {
            File parent = dataFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdir();
            }
            dataFile.createNewFile();
            return new ArrayList<>();
        }

        ArrayList<MonitoredFolder> loadedFolders = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new FileReader(dataFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    loadedFolders.add(MonitoredFolder.fromStoredPath(line));
                } catch (InvalidFolderException exception) {
                    throw new IOException("Fisierul cu locatii contine o cale invalida: "
                            + line, exception);
                }
            }
        }
        return loadedFolders;
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    private int getSupportedExtensionIndex(String extension) {
        for (int index = 0; index < EXTENSIONS.length; index++) {
            if (EXTENSIONS[index].text.equalsIgnoreCase(extension)) {
                return index;
            }
        }
        return -1;
    }

    private boolean isSupportedExtension(String extension) {
        return getSupportedExtensionIndex(extension) >= 0;
    }
}
