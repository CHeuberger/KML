package cfh.kml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Convert {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        final double START = 364.0;
        Pattern coordLine = Pattern.compile("(?i)\\s*<coordinates>[0-9.]+,[0-9.]+,([0-9.]+)</coordinates>\\s*");
        Pattern coordValues = Pattern.compile("\\s*[0-9.]+,[0-9.]+,([0-9.]+)\\s*");
        
        JFileChooser fc = new JFileChooser("D:\\tmp");
        int option = fc.showOpenDialog(null);
        if (option != fc.APPROVE_OPTION)
            return;
        
        File ifile = fc.getSelectedFile();
        File ofile = new File(ifile.getParentFile(), "correct.kml");
        LineNumberReader inp;
        try {
            inp = new LineNumberReader(new FileReader(ifile));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex);
            return;
        }
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(ofile));
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex);
            return;
        }
        try {
            boolean folder = false;
            boolean presalt = false;
            boolean gnssalt = false;
            boolean coordinates = false;
            String line;
            while ((line = inp.readLine()) != null) {
                
                if (!folder) {
                    folder = line.matches("(?i)\\s*<Folder>\\s*");
                } else {
                    folder = false;
                    if (line.matches("(?i)\\s*<name>PRESALTTRK</name>\\s*")) {
                        presalt = true;
                        gnssalt = false;
                        coordinates = false;
                    } else if (line.matches("(?i)\\s*<name>GNSSALTTRK</name>\\s*")) {
                        presalt = false;
                        gnssalt = true;
                        coordinates = false;
                    }
                }
                
                if (presalt || gnssalt) {
                    if (!coordinates) {
                        Matcher m = coordLine.matcher(line);
                        if (m.matches()) {
                            try {
                                double h = Double.parseDouble(m.group(1));
                                if (presalt) {
                                    h += START - 295.0;
                                } else {
                                    h += START - 407.0;
                                }
                                line = String.format("%s%7.6f%s", line.substring(0, m.start(1)), h, line.substring(m.end(1)));
                            } catch (NumberFormatException ex) {
                                System.err.printf("parsing line %d: \"%s\"", inp.getLineNumber(), m.group(1));
                                ex.printStackTrace();
                            }
                        } else {
                            coordinates = line.matches("(?i)\\s*<coordinates>\\s*");
                        }
                    } else {
                        Matcher m = coordValues.matcher(line);
                        if (m.matches()) {
                            try {
                                double h = Double.parseDouble(m.group(1));
                                if (presalt) {
                                    h += START - 295.0;
                                } else {
                                    h += START - 407.0;
                                }
                                line = String.format("%s%7.6f%s", line.substring(0, m.start(1)), h, line.substring(m.end(1)));
                            } catch (NumberFormatException ex) {
                                System.err.printf("parsing line %d: \"%s\"", inp.getLineNumber(), m.group(1));
                                ex.printStackTrace();
                            }
                        } else {
                            if (line.matches("(?i)\\s*</coordinates>\\s*")) {
                                coordinates = false;
                            }
                        }
                    }
                }
                out.write(line);
                out.newLine();
            }
            inp.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
