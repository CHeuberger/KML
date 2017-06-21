package cfh.kml;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cfh.air.Airspace;
import cfh.air.OpenAirReader;
import cfh.air.PushbackLineReader;
import cfh.kml.jaxb.Feature;
import cfh.kml.jaxb.Kml;

public class FromOpenAir {

    private static final String PREF_DIRECTORY = "directory";
    private static final Preferences prefs = Preferences.userNodeForPackage(FromOpenAir.class);

    public static void main(String[] args) {
        Component frame = null;
        
//        {
//            char[] buff = new char[128];
//            InputStreamReader inp = 
//                new InputStreamReader(new ByteArrayInputStream(new byte[] {0x47, (byte)0xD6, 0x50, 0x50}));
//            int len;
//            try {
//                len = inp.read(buff);
//                System.out.println(new String(buff, 0, len));
//                if (true) return;
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
        
//        {
//            System.out.println(DatatypeConverter.printHexBinary("G�PP".getBytes()));
//            if (true) return;
//        }
        
//        {
//            String text = "G�PP";
//            text = Normalizer.normalize(text, Normalizer.Form.NFD);
//            text = text.replaceAll("[^\\p{ASCII}]", "");
//            byte[] buff = text.getBytes();
//            System.out.println(DatatypeConverter.printHexBinary(buff));
//            try {
//                System.out.println(new String(buff, "ISO-8859-1"));
//            } catch (UnsupportedEncodingException ex) {
//                ex.printStackTrace();
//            }
//            if (true) return;
//        }
        
        
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
                    
                    List<Airspace> airspaces = new OpenAirReader().readAirspaces(reader);
                    Feature test = new AirspaceConverter().convertAirspaces(airspaces, name);
                    test.setOpen(true);
                    Kml kml = new Kml();
                    kml.add(test);

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
}
