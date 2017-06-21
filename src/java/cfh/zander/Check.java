package cfh.zander;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

import cfh.air.Airspace;
import cfh.air.PushbackLineReader;
import cfh.air.Segment;

public class Check {

    private static final String PREF_DIRECTORY = "directory";
    private static final Preferences prefs = Preferences.userNodeForPackage(Check.class);

    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);
        
        File dir = new File(prefs.get(PREF_DIRECTORY, "."));
        JFileChooser chooser = new JFileChooser(dir);
        int option = chooser.showOpenDialog(null);
        if (option != JFileChooser.APPROVE_OPTION)
            return;
        
        File file = chooser.getSelectedFile();
        dir = chooser.getCurrentDirectory();
        prefs.put(PREF_DIRECTORY, dir.getAbsolutePath());
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

                List<Airspace> airspaces = new ZanderReader().readAirspaces(reader);

                System.out.println(airspaces.size() + " airspaces read");
               
                for (Airspace airspace : airspaces) {
                    System.out.println();
                    System.out.println(airspace.toString());
                    List<Segment> segments = airspace.getSegments();
                    for (Segment segment : segments) {
                        System.out.println("  " + segment);
                    }
                }

            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
