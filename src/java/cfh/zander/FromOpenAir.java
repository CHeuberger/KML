package cfh.zander;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cfh.air.Airspace;
import cfh.air.OpenAirReader;
import cfh.air.PushbackLineReader;

public class FromOpenAir {
    
    private static final String version = "ZanderFromOpenAir v1.1 by CFH";
    

    private static final String PREF_DIRECTORY = "directory";
    private static final Preferences prefs = Preferences.userNodeForPackage(FromOpenAir.class);

    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);
        
        File dir = new File(prefs.get(PREF_DIRECTORY, "."));
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setDialogTitle(version + " - Open");
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
                

                List<Integer> inverted = new ArrayList<Integer>();
                line = reader.readLine();
                if (line.startsWith("##invert ")) {
                    String[] values = line.split("\\s+");
                    for (int i = 1; i < values.length; i++) {
                        try {
                            inverted.add(Integer.parseInt(values[i]));
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage());
                            return;
                        }
                    }
                }

                String name = file.getName();
                int i = name.lastIndexOf('.');
                if (i != -1) {
                    name = name.substring(0, i);
                }

                File save = new File(file.getParent(), name + ".AZ");
                if (save.exists()) {
                    int opt = JOptionPane.showConfirmDialog(null, "Overwrite " + save, "Confirm", JOptionPane.OK_CANCEL_OPTION);
                    if (opt != JOptionPane.OK_OPTION)
                        return;
                }
                List<Airspace> airspaces = new OpenAirReader().readAirspaces(reader);

                Writer writer = new FileWriter(save);
                try {
                    ZanderWriter zander = new ZanderWriter(
                            writer, 
                            "Converted from OpenAir format to AZ: " + file.getName(),
                            version);
                    zander.write(airspaces, inverted);
                    zander.finish();
                } finally {
                    writer.close();
                }

                String msg = "saved as " + save.getAbsolutePath();
                JOptionPane.showMessageDialog(null, msg);
                System.out.println(msg);

            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
