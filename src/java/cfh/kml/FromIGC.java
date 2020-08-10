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


public class FromIGC {

    private static final String PREF_DIRECTORY = "directory";
    private static final Preferences prefs = Preferences.userNodeForPackage(FromIGC.class);

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
        
        LineString baro = new LineString();
        baro.setAltitudeMode(AltitudeMode.ABSOLUTE);
        
        LineString gps = new LineString();
        gps.setAltitudeMode(AltitudeMode.ABSOLUTE);
        
        LineString ground = new LineString();
        ground.setTessellate(true);
        ground.setAltitudeMode(AltitudeMode.CLAMP);
        
        String date = null;    // HFDTE090820
        String id = null;      // HFGIDGLIDERID:D-7314
        
        // B1413184851882N00913074EA002550033100311
        //  hhmmss
        //        nnmmmmmN
        //                eeemmmmmE
        //                         Aaaaaaggggg
        Pattern pattern = Pattern.compile("B(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{5})([NnSs])(\\d{3})(\\d{5})([EeWw])A(\\d{5})(\\d{5}).*");
        Point start = null;
        String startTime = null;
        Point end = null;
        String line;
        while ((line = reader.readLine()) != null) {
           if (line.trim().isEmpty())
               continue;
           if (line.length() >= 11 && line.startsWith("HFDTE") && line.charAt(5) != 'D') {
               date = line.substring(5);
           }
           if (line.startsWith("HFGIDGLIDERID:")) {
               id = line.substring(14);
           }
           Matcher matcher = pattern.matcher(line);
           if (!matcher.matches())
               continue;
           
           String time;
           double lat;
           double lon;
           int baroAlt;
           int gpsAlt;
           try {
               time = matcher.group(1) + ":" + matcher.group(2) + ":" + matcher.group(3);
               lat = Integer.parseInt(matcher.group(4)) + Integer.parseInt(matcher.group(5))/ 60000.0;
               if (matcher.group(6).equalsIgnoreCase("S")) {
                   lat = -lat;
               }
               lon = Integer.parseInt(matcher.group(7)) + Integer.parseInt(matcher.group(8)) / 60000.0;
               if (matcher.group(9).equalsIgnoreCase("W")) {
                   lon = -lon;
               }
               baroAlt = Integer.parseInt(matcher.group(10));
               gpsAlt = Integer.parseInt(matcher.group(11));
           } catch (NumberFormatException ex) {
               throw new IOException("Invalid number: " + line, ex);
           }
           baro.add(lon, lat, baroAlt);
           gps.add(lon, lat, gpsAlt);
           ground.add(lon, lat, 0);
           
           if (start == null) {
               start = new Point(lon, lat);
               start.setAltitudeMode(AltitudeMode.CLAMP);
               startTime = time;
           } else {
               end = new Point(lon, lat);
               end.setAltitudeMode(AltitudeMode.CLAMP);
           }
        }
        
        if (start != null) result.add(new Placemark("start", start));
        if (end != null) result.add(new Placemark("end", end));
        result.add(new Placemark("normal", baro));
        result.add(new Placemark("GPS", gps));
        result.add(new Placemark("ground", ground).setVisibility(false));
        String description = "";
        if (id != null) description += "ID: " + id + "\n";
        if (date != null) {
            description += String.format("Date: %s/%s/%s", date.substring(0, 2), date.substring(2, 4), date.substring(4, 6));
            if (startTime != null) description += " " + startTime;
        }
        result.setDescription(description);
        return result;
    }
}
