package ro.ase.multimediamanager;

import ro.ase.multimediamanager.manager.MediaLibraryManager;
import ro.ase.multimediamanager.menu.MediaMenu;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/** Porneste aplicatia de gestionare a fisierelor multimedia. */
public class Main {

    /**
     * Pregateste managerul si porneste meniul aplicatiei.
     *
     * @param args argumente primite la pornire; aplicatia nu le foloseste
     */
    public static void main(String[] args) {
        try {
            MediaLibraryManager mediaLibraryManager = new MediaLibraryManager(
                    new File("data", "directoare.txt"));
            MediaMenu app = new MediaMenu(mediaLibraryManager, new Scanner(System.in));
            app.run();
        } catch (IOException exception) {
            System.err.println("Lista locatiilor nu a putut fi incarcata sau salvata: "
                    + exception.getMessage());
        }
    }
}
