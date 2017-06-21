package cfh.air;

import static java.nio.file.StandardWatchEventKinds.*;
import static javax.swing.JOptionPane.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;


public class Graph {
    
    private static final String version = "GraphOpenAir v1.0 by CFH";
    
    private static final String PREF_DIRECTORY = "directory";

    
    public static void main(String[] args) {
        new Graph(args);
    }
    
    
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());
   
    private JFrame frame;
    private GraphPanel graph = new GraphPanel();
    
    private final WatchService watcher;
    private WatchKey watchKey = null;
    private File watchFile = null;
    
    
    private Graph(String[] args) {
        Locale.setDefault(Locale.Category.FORMAT, Locale.ROOT);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initGUI();
            }
        });
        
        WatchService ws;
        try {
            ws = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            ex.printStackTrace();
            showMessageDialog(null, ex.getMessage(), ex.getClass().getSimpleName(), WARNING_MESSAGE);
            ws = null;
        }
        watcher = ws;
        
        if (watcher != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    watchLoop();
                }
            });
            thread.setName("Watcher Thread");
            thread.setDaemon(true);
            thread.start();
        }
    }
    
    private void initGUI() {
        JMenuItem open = new JMenuItem("Open");
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });
        
        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        
        JMenu file = new JMenu("File");
        file.add(open);
        file.addSeparator();
        file.add(quit);
        
        JMenuBar menubar = new JMenuBar();
        menubar.add(file);

        graph = new GraphPanel();
        
        frame = new JFrame();
        frame.setJMenuBar(menubar);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(graph);
        frame.setSize(800, 600);
        frame.validate();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        openFile();
    }
    
    private void openFile() {
        File dir = new File(prefs.get(PREF_DIRECTORY, "."));
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setDialogTitle(version + " - Open");
        int option = chooser.showOpenDialog(null);
        if (option != JFileChooser.APPROVE_OPTION)
            return;
        
        File file = chooser.getSelectedFile();
        dir = chooser.getCurrentDirectory();
        prefs.put(PREF_DIRECTORY, dir.getAbsolutePath());
        
        readAirspaces(file);
        
        if (watcher != null) {
            if (watchKey != null) {
                watchKey.cancel();
                watchKey = null;
            }
            watchFile = file;
            try {
                watchKey = dir.toPath().register(watcher, ENTRY_MODIFY);
            } catch (IOException ex) {
                ex.printStackTrace();
                showMessageDialog(frame, ex.getMessage(), ex.getClass().getSimpleName(), ERROR_MESSAGE);
                watchFile = null;
                watchKey = null;
            }
        }
    }

    private void readAirspaces(File file) {
        try {
            PushbackLineReader reader = new PushbackLineReader(
                new InputStreamReader(new FileInputStream(file)));
            try {
                String line = reader.readLine();
                if (line.length() >= 2) {
                    int start = 256*line.charAt(0) + line.charAt(1);
                    if (start == '\uFFFE' || start == '\uFEFF') {
                        reader.close();
                        reader = new PushbackLineReader(
                            new InputStreamReader(new FileInputStream(file), "UTF-16"));
                    } else {
                        reader.pushback(line);
                    }
                }

                String name = file.getName();
                int i = name.lastIndexOf('.');
                if (i != -1) {
                    name = name.substring(0, i);
                }

                List<Airspace> airspaces = new OpenAirReader().readAirspaces(reader);
                System.out.printf("%d airspaces read from %s\n", airspaces.size(), file);
                graph.setAirspaces(airspaces);
            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void watchLoop() {
        while (watcher != null && !Thread.interrupted()) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                break;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() == ENTRY_MODIFY && watchFile != null) {
                    Path path = (Path) event.context();
                    if (watchFile.toPath().endsWith(path)) {
                        readAirspaces(watchFile);
                    }
                }
            }
            key.reset();
        }
    }
}
