package chronometre;


import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.*;


public class App {

    public static void main(String[] args) throws AWTException, IOException, InterruptedException {




        /**
         * Cette partie gère l'icône qui va s'afficher dans le tray menu.
         * Il s'agit du menu windows situé à droite de la barre des tâches
         **/
        // Vérifie que le système supporte les tray icons
        if (!SystemTray.isSupported()) {
            System.err.println("System tray feature is not supported");
            return;
        }

        // Recupère l'objet SystemTray
        SystemTray tray = SystemTray.getSystemTray();

        // Créer une instance de TrayIcon
        Image image = ImageIO.read(App.class.getResourceAsStream("/unicorn.png"));
        TrayIcon trayIcon = new TrayIcon(image, "Genuine Coder", null);
        trayIcon.setImageAutoSize(true);

        // Créer le menu accessible lorsque l'on clic-doit sur le tray-icon
        PopupMenu rootMenu = new PopupMenu();

        // Créer l'option "Quitter"
        MenuItem leave = new MenuItem("Quitter");

        // Ajoute l'option au menu de l'icône
        rootMenu.add(leave);

        // Attach to trayIcon
        trayIcon.setPopupMenu(rootMenu);

        // Permet de quitter l'application
        leave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Attache l'icône au system tray
        tray.add(trayIcon);

        /**
         * Cette partie permet de détecter les modifications dans le dossier ou se situe le fichier
         *
         */

        // WatchService permet de détecter des modifications dans un dossier
        WatchService watchService = FileSystems.getDefault().newWatchService();

        // Permet de lire le fichier JSON
        JSONParser parser = new JSONParser();

        // Permet d'intéragir avec l'OS
        Robot robot = new Robot();

        // On saisi le répertoire qu'on souhaite surveiller
        Path path = Paths.get(System.getProperty("user.home") + "/Desktop/Chronometre");

        // On défini sur quelles actions watchService va réagir (création et modification)
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println("Type d'évènement:" + event.kind() + ". Fichier affecté: " + event.context() + ".");


                /**
                 * Cette partie décode le fichier mis à jour et simule la saisie au clavier
                 */
                try {


                    File targetDirectory = new File(System.getProperty("user.home") + "/Desktop/Chronometre");
                    FileFilter fileFilter = new WildcardFileFilter("*.json");
                    File[] files = targetDirectory.listFiles(fileFilter);


                    // Décode le fichier JSON

                    JSONObject jsonObject = (JSONObject) parser.
                            parse(new FileReader(files[0].getAbsolutePath()));

                    // Récupère la valeur correspondant à la clef "Text"
                    JSONArray jsonArray = (JSONArray) jsonObject.get("Records");
                    JSONObject jsonValue = (JSONObject) jsonArray.get(0);
                    String value = (String) jsonValue.get("Text");
                    System.out.println("Recupération du numéro :" + value);


                    // Sépare l'ensemble de la chaine de caractère en sous-chaine de caractère : AA55C396 -> [A,A,5,5,C,3,9,6]
                    String[] substrings = value.split("");

                    // Simule l'appui sur la touche MAJ afin d'utiliser les caractères numériques du clavier
                    robot.keyPress(KeyEvent.VK_SHIFT);

                    // Pour chaque élément du tableau on récupère le code qui correspond à la touche sur la clavier et on simule un appui sur cette touche
                    for (String ch : substrings) {
                        int keyCode = KeyStroke.getKeyStroke(ch).getKeyCode();
                        robot.keyPress(keyCode);
                        robot.keyRelease(keyCode);
                    }
                    // On relâche la touche MAJ
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            key.reset();
        }

        watchService.close();


    }

}