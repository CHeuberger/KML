package cfh.zander;

import static cfh.air.Altitude.Type.*;

import java.awt.Color;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cfh.air.Airspace;
import cfh.air.AirspaceType;
import cfh.air.Altitude;
import cfh.air.Point;
import cfh.air.PushbackLineReader;

public class ZanderReader {

    public List<Airspace> readAirspaces(PushbackLineReader reader) throws IOException {
        List<Airspace> airspaces = new ArrayList<Airspace>();
        Airspace airspace;
        airspace = readComment(reader);
        if (airspace != null) {
            airspaces.add(airspace);
        }
        while ((airspace = parseAirspace(reader)) != null) {
            airspaces.add(airspace);
        }
        return airspaces;
    }
    
    private Airspace readComment(PushbackLineReader reader) throws IOException {
        String line;
        StringBuilder comment = null;
        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0)
                continue;
            if (line.charAt(0) != '*') {
                reader.pushback(line);
                break;
            }
            if (comment == null) {
                comment = new StringBuilder();
            } else {
                comment.append("\n");
            }
            comment.append(line.substring(1));
        }

        Airspace airspace;
        if (comment != null) {
            airspace = new Airspace(0);
            airspace.setComment(comment.toString());
        } else {
            airspace = null;
        }
        return airspace;
    }

    private Airspace parseAirspace(PushbackLineReader reader) throws IOException {
        Airspace airspace = null;
        String line = null;
        
        while ((line = skipEmptyOrComment(reader)) != null) {
            try {
                if (line.startsWith("N ")) {
                    airspace = new Airspace(reader.getLineNumber());

                    String name;
                    if (line.length() >= 14) {
                        char ch = line.charAt(13);
                        switch (ch) {
                            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                            case 'R': case 'Q': case 'P':
                                AirspaceType type = AirspaceType.getType(line.substring(13));
                                airspace.setType(type);
                                name = line.substring(2, 13);
                                break;
                            default:
                                System.err.println(reader.getLineNumber() + ":unrecognized airspace type: " + ch + "(" + line + ")");
                                name = line.substring(2, 14);
                                break;
                        }
                    } else {
                        name = line.substring(2);
                    }
                    checkName(name);
                    airspace.setName(name);
                    
                    line = readLine(reader);
                    if (line == null) {
                        System.err.println(reader.getLineNumber() + ":EOF, expected upper limit");
                        return null;
                    }
                    airspace.setCeiling(parseAltitude(line));

                    line = readLine(reader);
                    if (line == null) {
                        System.err.println(reader.getLineNumber() + ":EOF, expected lower limit");
                        return null;
                    }
                    if (line.startsWith("N ")) {
                        reader.pushback(line);
                    }
                    airspace.setFloor(parseAltitude(line));
                    
                    line = reader.readLine();
                    if (line == null) {
                        System.err.println(reader.getLineNumber() + ":EOF, expected start");
                        return null;
                    }
                    if (!line.startsWith("S ") && !line.startsWith("I "))
                        throw new ParseException("expected line with S or I", 0);
                    Point point = parsePoint(line.substring(2));
                    airspace.add(point);

                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty())
                            break;
                        
                        if (line.startsWith("L ")) {
                            point = parsePoint(line.substring(2));
                            airspace.add(point);
                        } else if (line.startsWith("A")) {
                            Point start = point;
                            Point end = parsePoint(line.substring(2));
                            
                            line = reader.readLine();
                            if (line == null) {
                                System.err.println(reader.getLineNumber() + ":EOF, expected center of arc");
                                return null;
                            }
                            if (!line.startsWith("  "))
                                throw new ParseException(reader.getLineNumber() + ":invalid center of arc", 0);
                            Point center = parsePoint(line.substring(2));
                            
                            line = reader.readLine();
                            if (line == null) {
                                System.err.println(reader.getLineNumber() + ":EOF, expected radius of arc");
                                return null;
                            }
                            if (!line.startsWith("  "))
                                throw new ParseException(reader.getLineNumber() + ":invalid radius of arc", 0);
                            
                            // TODO add to airspace?
                        } else {
                            throw new ParseException(reader.getLineNumber() + ":unrecognized line: " + line, 0);
                        }
                    }
                    break;
                }
                System.err.println(reader.getLineNumber() + ":expected \"N\": " + line);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println(reader.getLineNumber() + ":reading: " + line);
                airspace = null;
            }
        }


        
//        while ((line = skipEmptyOrComment(reader)) != null) {
//            try {
//                String[] tokens = line.split(" +", 2);
//                String record = tokens[0];
//                String arg = tokens[1];
//                if (record.equalsIgnoreCase("AC")) {
//                    reader.pushback(line);
//                    break;
//                } else if (record.equalsIgnoreCase("AN")) {
//                    if (airspace.getName() != null) {
//                        reader.pushback(line);
//                        break;
//                    }
//                    airspace.setName(normalize(arg));
//                } else if (record.equalsIgnoreCase("AL")) {
//                    airspace.setFloor(parseAltitude(arg));
//                } else if (record.equalsIgnoreCase("AH")) {
//                    airspace.setCeiling(parseAltitude(arg));
//                } else if (record.equalsIgnoreCase("AT")) {
//                    // TODO
//                } else if (record.equalsIgnoreCase("V")) {
//                    tokens = arg.split(" *= *", 2);
//                    String variable = tokens[0];
//                    String value = tokens[1];
//                    if (variable.equalsIgnoreCase("D")) {
//                        if (value.equals("+")) {
//                            clockwise = true;
//                        } else if (value.equals("-")) {
//                            clockwise = false;
//                        } else {
//                            System.err.println(reader.getLineNumber() + ":unrecognized direction: " + line);
//                        }
//                    } else if (variable.equalsIgnoreCase("X")) {
//                        center = parsePoint(value);
//                    } else {
//                        // TODO ignore?
//                    }
//                } else if (record.equalsIgnoreCase("SB")) {
//                    Color color = parseColor(arg);
//                    airspace.setBrush(new Brush(color));
//                } else if (record.equalsIgnoreCase("SP")) {
//                    tokens = arg.split(" *, *", 3);
//                    Pen.Style style = Pen.Style.getStyle(Integer.parseInt(tokens[0]));
//                    int width = Integer.parseInt(tokens[1]);
//                    Color color = parseColor(tokens[2]);
//                    airspace.setPen(new Pen(style, width, color));
//                } else if (record.equalsIgnoreCase("DA")) {
//                    tokens = arg.split(" *, *", 3);
//                    double radius = Double.parseDouble(tokens[0]);
//                    double startAngle = Double.parseDouble(tokens[1]);
//                    double endAngle = Double.parseDouble(tokens[2]);
//                    airspace.add(new ArcA(radius, startAngle, endAngle, center, clockwise));
//                } else if (record.equalsIgnoreCase("DB")) {
//                    tokens = arg.split(" *, *", 2);
//                    Point start = parsePoint(tokens[0]);
//                    Point end = parsePoint(tokens[1]);
//                    airspace.add(new ArcB(start, end, center, clockwise));
//                } else if (record.equalsIgnoreCase("DC")) {
//                    double radius = Double.parseDouble(arg);
//                    airspace.add(new Circle(radius, center, clockwise));
//                } else if (record.equalsIgnoreCase("DP")) {
//                    airspace.add(parsePoint(arg));
//                } else {
//                    System.err.println(reader.getLineNumber() + ":unrecognized record: " + record);
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.err.println(reader.getLineNumber() + ":reading: " + line);
//            }
//        }

        return airspace;
    }

    private String readLine(PushbackLineReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0 || line.charAt(0) == '*')
                throw new IOException(reader.getLineNumber() + ":empty line or comment inside airspace: " + line);
            
            return line;
        }
        return null;
    }
    
    private String skipEmptyOrComment(PushbackLineReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().length() > 0 && line.charAt(0) != '*')
                return line;
        }
        return null;
    }
    
    private Altitude parseAltitude(String text) throws ParseException {
        Altitude.Type type;
        double factor = 0.3048;
        Matcher matcher;
        
        matcher = Pattern.compile("  (\\d{5}) (GND|MSL|FL)").matcher(text);
        if (matcher.matches()) {
            if (matcher.group(2).equals("GND")) {
                type = GND;
            } else if (matcher.group(2).equals("MSL")) {
                type = MSL;
            } else {
                type = FL;
            }
            
            try {
                double value = Double.parseDouble(matcher.group(1)) * factor;
                return new Altitude(type, value);
            } catch (NumberFormatException ex) {
                throw new ParseException(ex.getMessage(), 0);
            }
        } else {
            throw new ParseException("unrecognized altitude: " + text, 0);
        }
    }
    
    private Point parsePoint(String text) throws ParseException {
        Pattern p = Pattern.compile(" *(\\d\\d)(\\d\\d)(\\d\\d)([NnSs]) (\\d\\d\\d)(\\d\\d)(\\d\\d)([EeWw]).*");
        Matcher m = p.matcher(text);
        if (m.matches()) {
            double v1 = parseHms(m.group(1), m.group(2), m.group(3));
            char t1 = m.group(4).toUpperCase().charAt(0);
            double v2 = parseHms(m.group(5), m.group(6), m.group(7));
            char t2 = m.group(8).toUpperCase().charAt(0);
            switch (t1) {
                case 'N':
                    if (t2 == 'E') return new Point(v1, v2);
                    if (t2 == 'W') return new Point(v1, -v2);
                    break;
                case 'S':
                    if (t2 == 'E') return new Point(-v1, v2);
                    if (t2 == 'W') return new Point(-v1, -v2);
                    break;
            }
        }
        throw new ParseException(text, 0);
    }
    
    private double parseHms(String h, String m, String s) throws ParseException {
        try {
            return Integer.parseInt(h) + (Integer.parseInt(m) + Integer.parseInt(s)/60.0) / 60.0;
        } catch (NumberFormatException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }
    
    private Color parseColor(String text) throws ParseException {
        String[] tokens = text.split(" *, *", 4);
        if (tokens.length != 3 && tokens.length != 4)
            throw new ParseException("invalid r,g,b[,a] format: " + text, tokens.length);
        
        try {
            int red = Integer.parseInt(tokens[0]);
            int green = Integer.parseInt(tokens[1]);
            int blue = Integer.parseInt(tokens[2]);
            int alpha = tokens.length >= 4 ? Integer.parseInt(tokens[3]) : 255;
            if (red == -1 && green == -1 && blue == -1)
                return null;  // transparent
            else
                return new Color(red, green, blue, alpha);
        } catch (NumberFormatException ex) {
            throw new ParseException(ex.getMessage(), tokens.length);
        }
    }

    private void checkName(String text) throws ParseException {
        for (int i = 0; i < text.length(); i += 1) {
            char ch = text.charAt(i);
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ-/:#.0123456789* ".indexOf(ch) == -1)
                throw new ParseException("invalid name character '" + ch + "'", i);
        }
    }
}
