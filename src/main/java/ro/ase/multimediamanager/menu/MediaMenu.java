package ro.ase.multimediamanager.menu;

import ro.ase.multimediamanager.folders.MonitoredFolder;
import ro.ase.multimediamanager.exceptions.InvalidFolderException;
import ro.ase.multimediamanager.manager.MediaLibraryManager;
import ro.ase.multimediamanager.media.MediaFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/** Arata meniul si trimite comenzile catre managerul bibliotecii media. */
public class MediaMenu {

    private MediaLibraryManager mediaLibraryManager;
    private Scanner scanner;

    /**
     * Pregateste meniul si il leaga de manager si de tastatura.
     *
     * @param mediaLibraryManager se ocupa de lista directoarelor si de fisierele gasite
     * @param scanner citeste raspunsurile utilizatorului
     */
    public MediaMenu(MediaLibraryManager mediaLibraryManager, Scanner scanner) {
        this.mediaLibraryManager = mediaLibraryManager;
        this.scanner = scanner;
    }

    /**
     * Arata meniul pana cand utilizatorul alege sa iasa. La iesire, salveaza modificarile.
     *
     * @throws IOException daca modificarile nu pot fi salvate la iesire
     */
    public void run() throws IOException {
        boolean running = true;
        while (running) {
            printMenu();
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    showFolders();
                    break;
                case "2":
                    addFolder();
                    break;
                case "3":
                    removeFolder();
                    break;
                case "4":
                    showMediaFiles();
                    break;
                case "5":
                    showFolderReport();
                    break;
                case "6":
                    removeMediaFile();
                    break;
                case "0":
                    mediaLibraryManager.save();
                    System.out.println("Lista si modificarile au fost salvate. La revedere!");
                    running = false;
                    break;
                default:
                    System.out.println("Optiune invalida. Alege 0, 1, 2, 3, 4, 5 sau 6.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Manager de fisiere multimedia ===");
        System.out.println("1. Afiseaza directoarele monitorizate");
        System.out.println("2. Adauga un director");
        System.out.println("3. Scoate un director din lista");
        System.out.println("4. Afiseaza fisierele media");
        System.out.println("5. Genereaza rapoarte media");
        System.out.println("6. Scoate un fisier media din colectie");
        System.out.println("0. Salveaza si iesi");
        System.out.print("Alege o optiune: ");
    }

    private void showFolders() {
        ArrayList<MonitoredFolder> folders = mediaLibraryManager.getFolders();
        if (folders.isEmpty()) {
            System.out.println("Nu exista directoare monitorizate.");
            return;
        }

        for (int index = 0; index < folders.size(); index++) {
            MonitoredFolder folder = folders.get(index);
            String status;
            if (folder.isAccessible()) {
                status = "disponibil";
            } else {
                status = "indisponibil";
            }
            System.out.printf("%d. %s [%s]%n", index + 1, folder, status);
        }
    }

    private void showMediaFiles() {
        ArrayList<MonitoredFolder> folders = mediaLibraryManager.getFolders();
        if (folders.isEmpty()) {
            System.out.println("Adauga mai intai un director.");
            return;
        }

        showFolders();
        System.out.print("Alege numarul directorului: ");
        String folderInput = scanner.nextLine().trim();

        try {
            int folderIndex = Integer.parseInt(folderInput);
            if (folderIndex < 1 || folderIndex > folders.size()) {
                System.out.println("Numarul directorului nu este in lista.");
                return;
            }

            MonitoredFolder folder = folders.get(folderIndex - 1);
            System.out.print("Extensia (mp3, wav, jpg, png) sau Enter pentru toate: ");
            String extensionFilter = scanner.nextLine().trim();
            ArrayList<MediaFile> mediaFiles = mediaLibraryManager.scanFolder(folder);
            boolean foundFiles = false;

            for (MediaFile mediaFile : mediaFiles) {
                if (!extensionFilter.isEmpty()
                        && !mediaFile.getExtension().equalsIgnoreCase(extensionFilter)) {
                    continue;
                }

                if (!foundFiles) {
                    System.out.println("Fisiere media in " + folder + ":");
                }
                System.out.printf("- %s (%s)%n",
                        mediaFile.getName(), mediaFile.getExtension());
                foundFiles = true;
            }

            if (!foundFiles) {
                if (extensionFilter.isEmpty()) {
                    System.out.println("Nu s-au gasit fisiere media acceptate in acest director.");
                } else {
                    System.out.println("Nu s-au gasit fisiere cu extensia "
                            + extensionFilter + " in acest director.");
                }
            }
        } catch (NumberFormatException exception) {
            System.out.println("Introdu un numar valid.");
        } catch (IOException exception) {
            System.out.println("Nu am putut citi directorul: " + exception.getMessage());
        }
    }

    /** Arata optiunile pentru generarea rapoartelor. */
    private void showFolderReport() {
        ArrayList<MonitoredFolder> folders = mediaLibraryManager.getFolders();
        System.out.println("1. Raport general");
        System.out.println("2. Raport pentru un singur director");
        System.out.println("3. Fisiere grupate pe extensii");
        System.out.println("4. Istoricul elementelor scoase din colectie");
        System.out.print("Alege raportul: ");
        String reportOption = scanner.nextLine().trim();

        try {
            switch (reportOption) {
                case "1":
                    showSavedReport(mediaLibraryManager.createGeneralReport(),
                            "data/raport.txt");
                    break;
                case "2":
                    showSingleFolderReport(folders);
                    break;
                case "3":
                    showSavedReport(mediaLibraryManager.createExtensionReport(),
                            "data/raport_extensii.txt");
                    break;
                case "4":
                    showSavedReport(mediaLibraryManager.createDeletionReport(),
                            "data/raport_stergere.txt");
                    break;
                default:
                    System.out.println("Optiune invalida.");
            }
        } catch (IOException exception) {
            System.out.println("Nu am putut salva raportul: " + exception.getMessage());
        }
    }

    private void showSingleFolderReport(ArrayList<MonitoredFolder> folders)
            throws IOException {
        if (folders.isEmpty()) {
            System.out.println("Adauga mai intai un director.");
            return;
        }

        showFolders();
        System.out.print("Alege numarul directorului: ");
        String input = scanner.nextLine().trim();
        try {
            int index = Integer.parseInt(input);
            if (index < 1 || index > folders.size()) {
                System.out.println("Numarul directorului nu este in lista.");
                return;
            }

            showSavedReport(mediaLibraryManager.createFolderReport(folders.get(index - 1)),
                    "data/raport_director.txt");
        } catch (NumberFormatException exception) {
            System.out.println("Introdu un numar valid.");
        }
    }

    private void showSavedReport(ArrayList<String> reportLines, String filePath) {
        showReport(reportLines);
        System.out.println("Raportul a fost salvat in " + filePath + ".");
    }

    private void showReport(ArrayList<String> reportLines) {
        for (String line : reportLines) {
            System.out.println(line);
        }
    }

    private void addFolder() {
        System.out.print("Introdu calea directorului: ");
        String path = scanner.nextLine();
        try {
            boolean added = mediaLibraryManager.addFolder(path);
            if (added) {
                System.out.println("Directorul a fost adaugat.");
            } else {
                System.out.println("Directorul se afla deja in lista.");
            }
        } catch (InvalidFolderException exception) {
            System.out.println("Nu am putut adauga directorul: " + exception.getMessage());
        }
    }

    private void removeFolder() {
        showFolders();
        if (mediaLibraryManager.getFolders().isEmpty()) {
            return;
        }

        System.out.print("Introdu numarul directorului pe care vrei sa-l scoti din lista: ");
        String input = scanner.nextLine().trim();
        try {
            int index = Integer.parseInt(input);
            MonitoredFolder removed = mediaLibraryManager.removeFolder(index);
            System.out.println("Directorul a fost scos din lista: " + removed);
        } catch (NumberFormatException exception) {
            System.out.println("Introdu un numar valid.");
        } catch (IndexOutOfBoundsException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void removeMediaFile() {
        ArrayList<MonitoredFolder> folders = mediaLibraryManager.getFolders();
        if (folders.isEmpty()) {
            System.out.println("Adauga mai intai un director.");
            return;
        }

        showFolders();
        System.out.print("Alege numarul directorului: ");
        try {
            int folderIndex = Integer.parseInt(scanner.nextLine().trim());
            if (folderIndex < 1 || folderIndex > folders.size()) {
                System.out.println("Numarul directorului nu este in lista.");
                return;
            }

            MonitoredFolder folder = folders.get(folderIndex - 1);
            ArrayList<MediaFile> mediaFiles = mediaLibraryManager.scanFolder(folder);
            if (mediaFiles.isEmpty()) {
                System.out.println("Nu exista fisiere media in acest director.");
                return;
            }

            System.out.println("Fisierele media din " + folder + ":");
            for (int index = 0; index < mediaFiles.size(); index++) {
                MediaFile mediaFile = mediaFiles.get(index);
                System.out.printf("%d. %s (%s)%n", index + 1,
                        mediaFile.getName(), mediaFile.getExtension());
            }

            System.out.print("Alege numarul fisierului pe care vrei sa-l scoti: ");
            int fileIndex = Integer.parseInt(scanner.nextLine().trim());
            MediaFile removed = mediaLibraryManager.removeMediaFile(folder, fileIndex);
            System.out.println("Fisierul a fost scos din colectia aplicatiei: "
                    + removed.getName());
            System.out.println("Fisierul original de pe calculator nu a fost sters.");
            System.out.println("Modificarea se salveaza cand alegi 0. Salveaza si iesi.");
        } catch (NumberFormatException exception) {
            System.out.println("Introdu un numar valid.");
        } catch (IndexOutOfBoundsException exception) {
            System.out.println(exception.getMessage());
        } catch (IOException exception) {
            System.out.println("Nu am putut citi directorul: " + exception.getMessage());
        }
    }
}
