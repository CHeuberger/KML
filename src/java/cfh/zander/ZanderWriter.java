package cfh.zander;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import cfh.air.Airspace;
import cfh.air.AirspaceType;
import cfh.air.Altitude;
import cfh.air.ArcA;
import cfh.air.ArcB;
import cfh.air.Circle;
import cfh.air.Point;
import cfh.air.Segment;

public class ZanderWriter {
    
    /** step in degrees for curves */
    private static final int STEP = 10;

    private static final String NL = "\r\n";
    private static final String SEP = "------------------------------------------------------------------------------";
    private static final double DELTA = 0.001;
    
    private volatile int number;

    private final Writer writer;
    
    public ZanderWriter(Writer writer, String... comments) throws IOException {
        if (writer == null) throw new IllegalArgumentException("null");
        this.writer = writer;
        
        if (comments != null) {
            for (String comment : comments) {
                comment(comment);
            }
        }
        line();
    }
    
    public void write(Collection<Airspace> airspaces) throws IOException {
        System.out.printf("%5s  %5s  %-35s  %5s  %s%n", "NR", "LINE", "NAME", "SIZE", "C");
        number = 1;
        for (Airspace airspace : airspaces) {
            if (airspace.getLineNumber() == 0 && airspace.getType() == null) {
                comment(airspace.getComment());
            } else {
                write(airspace);
            }
        }
    }
    
    public void finish() throws IOException {
        line();
    }
    
    public void write(Airspace airspace) throws IOException {
        String name = airspace.getName();
        if (name != null && name.length() > 35) {
            name = name.substring(0, 35);
        }
        System.out.printf("%5d  %5d  %-35s  %5d", number, airspace.getLineNumber(), name, airspace.getSegments().size());
        
        if (airspace.getName() == null || airspace.getSegments().isEmpty()) {
            System.out.println("     IGNORED");
            return;
        }
        
        if (airspace.getSegments().size() > 512) {
            String msg = "Airspace " + airspace.getName() + " (" + airspace.getLineNumber() + ") has too many points!";
            JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("     REJECTED");
            return;
        }
        
        header(airspace);
        segments(airspace);
        line();
        number += 1;

        System.out.println();
    }

    
    /*
     * N = name 12 characters (last character may be type of airspace)
     * followed by upper level in ft with altitude type GND, MSL, FL (FL130=00000 FL, UNL=99999 MSL)
     * followed by lower level in ft with altitude type GND, MSL, FL (FL130=00000 FL, SFC=00000 GND)
     */
    private void header(Airspace airspace) throws IOException {
        if (airspace.getName() == null || airspace.getSegments().isEmpty())
            return;
        comment(SEP);
        comment(airspace.getName() + " (" + Integer.toString(airspace.getLineNumber()) + ")");
        if (airspace.getComment() != null) {
            comment(airspace.getComment());
        }
        line("N ", normName(airspace.getName()), airspaceCode(airspace.getType()));
        line("  ", normAltitude(airspace.getCeiling(), true));
        line("  ", normAltitude(airspace.getFloor(), false));
    }

    /*
     * S = start point with degrees, minutes, seconds
     *     ('S' used for airspaces drawn clockwise, 'I' used for airspaces drawn counterclockwise)
     * L = end point of line
     */
    private void segments(Airspace airspace) throws IOException {
        List<Segment> segments = new ArrayList<Segment>(airspace.getSegments());
        boolean clockwise = isClockwise(segments);
        if (clockwise != isClockwise2(segments)) {
            System.err.printf("%d: inconsistent direction of airspace: %s", airspace.getLineNumber(), airspace.getName());
        }
        String code = clockwise ? "S " : "I ";
        System.out.printf("  %1s", code);
//code = "S ";
//if (!clockwise) {
//    Collections.reverse(segments);
//}
        Point start = null;
        boolean close = false;
        for (Segment segment : segments) {
            Point first;
            if (segment instanceof Point) {
                Point point = (Point) segment;
                first = point;
                line(code, normPoint(point));
                close = start != null && first.distTo(start) > DELTA;
            } else if (segment instanceof ArcA) {
                ArcA arc = (ArcA) segment;
                List<Point> points = arc.getPoints(0);
//if (!clockwise) {
//    first = points.get(1);
//    line(code, normPoint(first));
//    line("A ", normPoint(points.get(0)));
//    line("  ", normPoint(arc.getCenter()));
//    line("  ", normRadius(arc.getRadius(), !arc.isClockwise()), " NM");
//    close = start != null && points.get(0).distTo(start) > DELTA;
//} else {
                first = points.get(0);
                line(code, normPoint(first));
                line("A ", normPoint(points.get(1)));
                line("  ", normPoint(arc.getCenter()));
                line("  ", normRadius(arc.getRadius(), arc.isClockwise()), " NM");
                close = start != null && points.get(1).distTo(start) > DELTA;
//}
            } else if (segment instanceof ArcB) {
                ArcB arc = (ArcB) segment;
//if (!clockwise) {
//    first = arc.getEnd();
//    line(code, normPoint(first));
//    line("A ", normPoint(arc.getStart()));
//    line("  ", normPoint(arc.getCenter()));
//    line("  ", normRadius(arc.getRadius(), !arc.isClockwise()), " NM");
//    close = start != null && arc.getStart().distTo(start) > DELTA;
//} else {
                first = arc.getStart();
                line(code, normPoint(first));
                line("A ", normPoint(arc.getEnd()));
                line("  ", normPoint(arc.getCenter()));
                line("  ", normRadius(arc.getRadius(), arc.isClockwise()), " NM");
                close = start != null && arc.getEnd().distTo(start) > DELTA;
//}
            } else if (segment instanceof Circle) {
                if (start != null) {
                    System.err.println("circle must be alone");
                    continue;
                } else {
                    Circle circle = (Circle) segment;
                    line("C ", normPoint(circle.getCenter()));
                    line("  ", normRadius(circle.getRadius(), true));
                    close = false;
                    break;
                }
            } else {
                System.err.println("unrecognized segment " + segment);
                continue;
            }
            if (start == null) {
                start = first;
                code = "L ";
            }
        }
        if (close) {
            line(code, normPoint(start));
        }
    }

    private void comment(String comment) throws IOException {
        String[] lines = comment.split("\\n", -1);
        for (String line : lines) {
            line("*", line);
        }
    }
    
    private void line(String... texts) throws IOException {
        for (String text : texts) {
            writer.append(text);
        }
        writer.append(NL);
    }
    
    /*
     * ABCDEFGHIJKLMNOPQRSTUVWXYZ-/:#.0123456789* and space  (no tabs allowed!)
     */
    private String normName(String name) {
        name = name.toUpperCase().replaceAll("[^-/:#.* ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789]", "");
        
        if (name.length() > 10) {
            name = name.substring(0, 10);
        }
        while (name.length() < 11) {
            name += " ";
        }

        return name;
    }
    
    private String airspaceCode(AirspaceType type) {
        switch (type) {
            case RESTRICTED:
            case DANGER:
            case PROHIBITED:
            case CLASS_A:
            case CLASS_B:
            case CLASS_C:
            case CLASS_D:
            case CLASS_E:
            case CLASS_F:
                return type.getCode();
            case CLASS_G:
            case CTR:
            case GLIDER_PROHIBITED:
            case GLIDER_SECTOR:
            case RMZ:
            case TMZ:
            case WAVE:
            default: 
                return "";
        }
    }
    
    private String normAltitude(Altitude altitude, boolean upper) {
        if (altitude == null)
            return upper ? "99999 MSL" : "00000 GND";
        
        return String.format("%05d %s", altitude.getValueFeet(), altitude.getType());
    }
    
    private String normPoint(Point point) {
        return normLatitude(point.getLatitude()) + " " + normLongitude(point.getLongitude());
    }
    
    private String normLatitude(double latitude) {
        int val = normLatLong(latitude);
        return String.format("%06d%s", Math.abs(val), val<0 ? "S" : "N");
    }
    
    private String normLongitude(double latitude) {
        int val = normLatLong(latitude);
        return String.format("%07d%s", Math.abs(val), val<0 ? "W" : "E");
    }
    
    private int normLatLong(double coord) {
        int second = (int) Math.round(coord * 3600.0);
        int minute = (second / 60) % 60;
        int degree = second / 3600;
        second %= 60;
        return degree*10000 + minute*100 + second;
    }
    
    private String normRadius(double radius, boolean clockwise) {
        return String.format("%s%07.3f", clockwise ? "+" : "-", radius);
    }
    
    private boolean isClockwise(List<Segment> segments) {
        List<Point> points = new ArrayList<Point>();
        for (Segment segment : segments) {
            points.addAll(segment.getPoints(STEP));
        }
        Point prev = points.get(points.size()-1);
        double a = 0.0;
        for (Point point : points) {
            if (prev != null) {
                a += point.getLongitude()*prev.getLatitude() - prev.getLongitude()*point.getLatitude();
            }
            prev = point;
        }
        return a >= 0;
    }
    
    private boolean isClockwise2(List<Segment> segments) {
        List<Point> points = new ArrayList<Point>();
        for (Segment segment : segments) {
            points.addAll(segment.getPoints(STEP));
        }
        Point prev = points.get(points.size()-1);
        double a = 0;
        for (Point point : points) {
            if (prev != null) {
                a += (point.getLongitude()-prev.getLongitude())*(point.getLatitude() + prev.getLatitude());
            }
            prev = point;
        }
        return a >= 0;
    }
}
