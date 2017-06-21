package cfh.kml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import cfh.kml.jaxb.AltitudeMode;
import cfh.kml.jaxb.Coord;
import cfh.kml.jaxb.Document;
import cfh.kml.jaxb.Folder;
import cfh.kml.jaxb.Kml;
import cfh.kml.jaxb.LinearRing;
import cfh.kml.jaxb.MultiGeometry;
import cfh.kml.jaxb.Placemark;
import cfh.kml.jaxb.Point;
import cfh.kml.jaxb.PolyStyle;
import cfh.kml.jaxb.Polygon;
import cfh.kml.jaxb.Style;

public class TestKml {

    public static void main(String[] args) {
        Folder folder;
        LinearRing ring;
        Placemark mark;
        
        Coord east = new Coord(48.863564, 9.229200, 271);
        Coord north = new Coord(48.864978, 9.220300, 280);
        Coord west = new Coord(48.864797, 9.220233, 280);
        Coord south = new Coord(48.863397, 9.229142, 271);
        
        Kml kml = new Kml();
        
        Document document = new Document();
        document.setName("Test").setId("test");
        kml.add(document);
        
        
        ring = new LinearRing().add(east).add(north).add(west).add(south).close();
        
        mark = new Placemark("Linear Ring", ring);
        mark.setVisibility(false);
        document.add(mark);
        
        
        PolyStyle polyStyle = new PolyStyle();
        polyStyle.setColor(1.0, 0.0, 0.0, 0.2);
        polyStyle.setFill(true);
        polyStyle.setOutline(false);
        Style style = new Style("TestStyle").setPolyStyle(polyStyle);
        document.add(style);
        URI uri;
            try {
                uri = new URI("#TestStyle");
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
                return;
            }
        
        Polygon polygon = new Polygon();
        polygon.setAltitudeMode(AltitudeMode.ABSOLUTE);
        document.add(new Placemark("vertical", polygon).setStyleUrl(uri));
        
        ring = new LinearRing();
        ring.add(east.relative(0, 0, 100)).add(east.relative(0, 0, 200));
        ring.add(west.relative(0, 0, 200)).add(west.relative(0, 0, 100)).close();
        polygon.setOuterBoundaryIs(ring);
        
        ring = new LinearRing();
        ring.add(east.relative(0, 0, 120)).add(east.relative(0, 0, 140));
        ring.add(west.relative(0, 0, 140)).add(west.relative(0, 0, 120)).close();
        polygon.addInnerBoundaryIs(ring);
        
        ring = new LinearRing();
        ring.add(east.relative(0, 0, 160)).add(east.relative(0, 0, 180));
        ring.add(west.relative(0, 0, 180)).add(west.relative(0, 0, 160)).close();
        polygon.addInnerBoundaryIs(ring);
        
        
        folder = new Folder("Points");
        document.add(folder);
        
        mark = new Placemark("East", new Point(east));
        folder.add(mark);
        
        mark = new Placemark("North", new Point(north));
        folder.add(mark);
        
        mark = new Placemark("West", new Point(west));
        folder.add(mark);
        
        mark = new Placemark("South", new Point(south));
        folder.add(mark);

        folder.setVisibility(false);
        
        
        folder = new Folder("Multi");
        document.add(folder);
        
        mark = new Placemark("Multi", 
                new MultiGeometry(new Point(east), new Point(north), new Point(west), new Point(south)));
        folder.add(mark);
        
        try {
            kml.marshalTo(System.out);
            java.io.File file = new java.io.File("test.kml");
            FileOutputStream output = new FileOutputStream(file);
            try {
                kml.marshalTo(output);
            } finally {
                try {
                    output.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            
            int option = JOptionPane.showConfirmDialog(null, "Open " + file, "Open", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                openFile(file);
            }
        } catch (JAXBException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public static void openFile(java.io.File file) {
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", file.getAbsolutePath());
        try {
            final Process process = builder.start();
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    process.destroy();
                }
            }, 20000);
            new Redirector(process.getInputStream(), System.out);
            new Redirector(process.getErrorStream(), System.err);
            process.waitFor();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    private static class Redirector implements Runnable {
        private final InputStream input;
        private final PrintStream output;
        
        public Redirector(InputStream inputStream, PrintStream outputStream) {
            input = inputStream;
            output = outputStream;
            Thread thread = new Thread(this, "Redirector");
            thread.setDaemon(true);
            thread.start();
        }

        @Override
        public void run() {
            byte[] buffer = new byte[512];
            int len;
            try {
                while ((len = input.read(buffer)) > 0) {
                    output.write(buffer, 0, len);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
