package cfh.air;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
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
    
    
    private Graph(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initGUI();
            }
        });
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
}
