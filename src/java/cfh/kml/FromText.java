package cfh.kml;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cfh.air.PushbackLineReader;
import cfh.kml.jaxb.AltitudeMode;
import cfh.kml.jaxb.Feature;
import cfh.kml.jaxb.Folder;
import cfh.kml.jaxb.Kml;
import cfh.kml.jaxb.LineString;
import cfh.kml.jaxb.Placemark;
import cfh.kml.jaxb.Point;


public class FromText {

    private static final String PREF_DIRECTORY = "directory";
    private static final Preferences prefs = Preferences.userNodeForPackage(FromText.class);

    public static void main(String[] args) {
        Component frame = null;
        
        File dir = new File(prefs.get(PREF_DIRECTORY, "."));
        JFileChooser chooser = new JFileChooser(dir);
        int option = chooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
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
                    
                    String name = file.getName();
                    int i = name.lastIndexOf('.');
                    if (i != -1) {
                        name = name.substring(0, i);
                    }
                    
                    Feature feature = readFeature(name, reader);
                    feature.setOpen(false);
                    Kml kml = new Kml();
                    kml.add(feature);

                    File save = new File(name + ".kml");
                    FileOutputStream output = new FileOutputStream(save);
                    try {
                        kml.marshalTo(output);
                    } finally {
                        try {
                            output.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        System.out.println("saved as " + save.getAbsolutePath());
                    }
                    
                    option = JOptionPane.showConfirmDialog(frame, "Open " + save, "Open", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        TestKml.openFile(save);
                    }
                } finally {
                    reader.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, ex.toString());
                return;
            }
        }
    }
    
    private static Feature readFeature(String name, PushbackLineReader reader) throws IOException {
        Folder result = new Folder(name);
        
        LineString path = new LineString();
        path.setTessellate(true);
        path.setAltitudeMode(AltitudeMode.CLAMP);
        
        Pattern pattern = Pattern.compile("\\s*([0-9.]+)\\s*,\\s*([0-9.]+)\\s*");
        Point start = null;
        Point end = null;
        String line;
        while ((line = reader.readLine()) != null) {
           if (line.trim().isEmpty())
               continue;
           
           Matcher matcher = pattern.matcher(line);
           if (!matcher.matches())
               throw new IOException("Invalid coordinates: " + line);
           
           double lat;
           double lon;
           try {
               lat = Double.parseDouble(matcher.group(1));
               lon = Double.parseDouble(matcher.group(2));
           } catch (NumberFormatException ex) {
               throw new IOException("Invalid number: " + line, ex);
           }
           path.add(lon, lat, 0);
           
           if (start == null) {
               start = new Point(lon, lat);
               start.setAltitudeMode(AltitudeMode.CLAMP);
           } else {
               end = new Point(lon, lat);
               end.setAltitudeMode(AltitudeMode.CLAMP);
           }
        }
        
        result.add(new Placemark("track", path));
        if (start != null) result.add(new Placemark("start", start));
        if (end != null) result.add(new Placemark("end", end));
        return result;
    }
}
