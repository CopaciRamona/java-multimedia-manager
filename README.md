# Java Multimedia Manager

Aplicatie Java cu meniu in consola pentru lista directoarelor monitorizate si pentru fisierele media gasite in ele.

## Ce poate face acum

- Incarca si salveaza lista directoarelor in data/directoare.txt.
- Adauga un director existent si accesibil.
- Scoate un director din lista fara sa-i stearga fisierele de pe disc.
- Scoate un fisier media din colectia aplicatiei fara sa-l stearga de pe disc.
- Pastreaza fisierele media scoase in `data/fisiere_scoase.txt` la iesirea din program.
- Arata daca un director salvat este disponibil.
- Cauta fisierele mp3, wav, jpg si png.
- Afiseaza toate fisierele gasite sau le filtreaza dupa extensie.
- Genereaza un raport general in data/raport.txt.
- Genereaza un raport pentru un singur director in data/raport_director.txt.
- Listeaza fisierele pe extensii in data/raport_extensii.txt.
- Calculeaza numarul si dimensiunea fisierelor pe extensii in data/raport_total_extensii.txt.
- Genereaza un raport cu directoarele si fisierele media scoase din colectie in `data/raport_stergere.txt`.

## Pornire

Deschide proiectul in IntelliJ IDEA ca proiect Maven si porneste clasa ro.ase.multimediamanager.Main.

Lista directoarelor si lista fisierelor media scoase se actualizeaza la iesirea din program. Contin cai locale si sunt ignorate de Git.
Rapoartele sunt generate la alegerea optiunii 5 si sunt ignorate de Git.

## Clase si pachete

- Main - porneste aplicatia.
- folders/MonitoredFolder - descrie si verifica un director monitorizat.
- media/MediaFile - descrie un fisier media gasit.
- manager/MediaLibraryManager - gestioneaza lista, salvarea si scanarea.
- menu/MediaMenu - afiseaza meniul si citeste optiunile.
- exceptions/InvalidFolderException - semnaleaza o cale invalida.
