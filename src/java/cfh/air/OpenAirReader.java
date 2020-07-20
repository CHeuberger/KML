package cfh.air;

import static cfh.air.Altitude.Type.*;

import java.awt.Color;
import java.io.IOException;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAirReader {

    /*
     * OpenAir:
     *     comment*
     *     airspace*
     * 
     * comment:
     *     '*' text
     *     
     * airspace:
     *     AC type text?    // airspace class, comment
     *     AN text          // name
     *     data*         
     *     |
     *     AC type text?    // airspace class, comment
     *     data*         
     *     |
     *     AN text          // name
     *     data*         
     * 
     * data:
     *     AL altitude               |  // floor
     *     AH altitude               |  // ceiling
     *     AT test                   |  // ignored (TODO)
     *     V D = dir                 |  // direcition for DA and DB
     *     V X = coord               |  // coordinate for DA, DB and DC
     *     SB color                  |  // brush color
     *     SP style, num, color      |  // pen style, width, color
     *     DA double, double, double |  // arc radius [nm], start and end angle [degrees]
     *     DB coord, coord           |  // arc start, end
     *     DC double                 |  // circle radius [nm]
     *     DP coord                     // point
     * 
     * type:
     *     R    |  // restricted
     *     Q    |  // danger
     *     P    |  // prohibited
     *     A    |  // class A
     *     B    |  // class B
     *     C    |  // class C
     *     D    |  // class D
     *     E    |  // class E
     *     F    |  // class F
     *     G    |  // class G
     *     GP   |  // glider prohibited
     *     GSEC |  // glider sector
     *     CTR  |  // ctr
     *     W    |  // wave
     *     TMZ  |  // tmz
     *     RMZ  |  // rmz
     * 
     * altitude:
     *     FL num         |
     *     [num] unit ref
     * 
     * unit:
     *     [ft] |
     *     m    |
     *     km
     * 
     * ref:
     *     AGL   | GND | SFC |  // above ground
     *     [MSL]                // mean sea level
     * 
     * dir:
     *   - |  // clockwise
     *   +    // counterclockwise
     * 
     * coord:
     *     num[:num[:num]] {'N'|'S'} num[:num[:num]] {'E'|'W'} |
     *     num[:num[:num]] {'E'|'W'} num[:num[:num]] {'N'|'S'}
     * 
     * style:
     *     0 |  // solid
     *     1 |  // dash
     *     5 |  // no border
     * 
     * color:
     *     red, green, blue[, alpha] |
     *     -1, -1, -1[, num]            // transparent
     * 
     */
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
        
        Point center = null;
        boolean clockwise = true;

        String line;
        
        while ((line = skipEmpty(reader)) != null) {
            String[] tokens = line.split(" +", 3);
            if (tokens[0].equalsIgnoreCase("AC")) {
                airspace = new Airspace(reader.getLineNumber());
                AirspaceType type = AirspaceType.getType(tokens[1]);
                if (type == null) {
                    System.err.println(reader.getLineNumber() + ":unrecognized \"AC\": " + line);
                } else {
                    airspace.setType(type);
                }
                if (tokens.length > 2) {
                    airspace.setComment(tokens[2]);
                }
                break;
            }
            System.err.println(reader.getLineNumber() + ":expected \"AC\": " + line);
        }
        if (airspace == null)
            return null;
        
        while ((line = skipEmpty(reader)) != null) {
            try {
                String[] tokens = line.split(" +", 2);
                String record = tokens[0];
                String arg = tokens[1];
                if (record.equalsIgnoreCase("AC")) {
                    reader.pushback(line);
                    break;
                } else if (record.equalsIgnoreCase("AN")) {
                    if (airspace.getName() != null) {
                        reader.pushback(line);
                        break;
                    }
                    airspace.setName(normalize(arg));
                } else if (record.equalsIgnoreCase("AL")) {
                    airspace.setFloor(parseAltitude(arg));
                } else if (record.equalsIgnoreCase("AH")) {
                    airspace.setCeiling(parseAltitude(arg));
                } else if (record.equalsIgnoreCase("AT")) {
                    // TODO
                } else if (record.equalsIgnoreCase("V")) {
                    tokens = arg.split(" *= *", 2);
                    String variable = tokens[0];
                    String value = tokens[1];
                    if (variable.equalsIgnoreCase("D")) {
                        if (value.equals("+")) {
                            clockwise = true;
                        } else if (value.equals("-")) {
                            clockwise = false;
                        } else {
                            System.err.println(reader.getLineNumber() + ":unrecognized direction: " + line);
                        }
                    } else if (variable.equalsIgnoreCase("X")) {
                        center = parsePoint(value);
                    } else {
                        // TODO ignore?
                    }
                } else if (record.equalsIgnoreCase("SB")) {
                    Color color = parseColor(arg);
                    airspace.setBrush(new Brush(color));
                } else if (record.equalsIgnoreCase("SP")) {
                    tokens = arg.split(" *, *", 3);
                    Pen.Style style = Pen.Style.getStyle(Integer.parseInt(tokens[0]));
                    int width = Integer.parseInt(tokens[1]);
                    Color color = parseColor(tokens[2]);
                    airspace.setPen(new Pen(style, width, color));
                } else if (record.equalsIgnoreCase("DA")) {
                    tokens = arg.split(" *, *", 3);
                    double radius = Double.parseDouble(tokens[0]);
                    double startAngle = Double.parseDouble(tokens[1]);
                    double endAngle = Double.parseDouble(tokens[2]);
                    airspace.add(new ArcA(radius, startAngle, endAngle, center, clockwise));
                } else if (record.equalsIgnoreCase("DB")) {
                    tokens = arg.split(" *, *", 2);
                    Point start = parsePoint(tokens[0]);
                    Point end = parsePoint(tokens[1]);
                    airspace.add(new ArcB(start, end, center, clockwise));
                } else if (record.equalsIgnoreCase("DC")) {
                    double radius = Double.parseDouble(arg);
                    airspace.add(new Circle(radius, center, clockwise));
                } else if (record.equalsIgnoreCase("DP")) {
                    airspace.add(parsePoint(arg));
                } else {
                    System.err.println(reader.getLineNumber() + ":unrecognized record: " + record);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println(reader.getLineNumber() + ":reading: " + line);
            }
        }

        if (airspace.getName() == null) {
            System.err.println(airspace.getLineNumber() + ":airspace without name");
        }
        return airspace;
    }

    private String skipEmpty(PushbackLineReader reader) throws IOException {
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
        matcher = Pattern.compile("FL *+(\\d++).*+").matcher(text);
        if (matcher.matches()) {
            text = matcher.group(1);
            type = FL;
            factor *= 100;
        } else {
            matcher = Pattern.compile("(\\d*+(?:\\.\\d*+)?+) *+(m|km|ft|) *+(AGL|GND|SFC|MSL|) *+").matcher(text);
            if (matcher.matches()) {
                text = matcher.group(1);
                
                if (matcher.group(2).equals("m")) {
                    factor = 1;
                } else if (matcher.group(2).equals("km")) {
                    factor = 1000;
                }
                
                if (matcher.group(3).equals("AGL") || matcher.group(3).equals("GND") || matcher.group(3).equals("SFC")) {
                    type = GND;
                } else {
                    type = MSL;
                }
            } else {
                throw new ParseException("unrecognized altitude: " + text, 0);
            }
        }
        
        try {
            double value = text.isEmpty() ? 0 : factor * Double.parseDouble(text);
            return new Altitude(type, value);
        } catch (NumberFormatException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }
    
    private Point parsePoint(String text) throws ParseException {
        Pattern p = Pattern.compile(" *(\\d+(?::\\d+){0,2}(?:\\.\\d*+)?) *([NnSsEeWw]) +" +
        		"(\\d+(?::\\d+){0,2}(?:\\.\\d*+)?) *([NnSsEeWw]).*");
        Matcher m = p.matcher(text);
        if (m.matches()) {
            double v1 = parseHms(m.group(1));
            char t1 = m.group(2).toUpperCase().charAt(0);
            double v2 = parseHms(m.group(3));
            char t2 = m.group(4).toUpperCase().charAt(0);
            switch (t1) {
                case 'N':
                    if (t2 == 'E') return new Point(v1, v2);
                    if (t2 == 'W') return new Point(v1, -v2);
                    break;
                case 'E':
                    if (t2 == 'N') return new Point(v2, v1);
                    if (t2 == 'S') return new Point(-v2, v1);
                    break;
                case 'S':
                    if (t2 == 'E') return new Point(-v1, v2);
                    if (t2 == 'W') return new Point(-v1, -v2);
                    break;
                case 'W':
                    if (t2 == 'N') return new Point(v2, -v1);
                    if (t2 == 'S') return new Point(-v2, -v1);
                    break;
            }
        }
        throw new ParseException(text, 0);
    }
    
    private double parseHms(String text) throws ParseException {
        String[] tokens = text.split(":",3);
        try {
            switch (tokens.length) {
                case 1:
                    return Double.parseDouble(tokens[0]);
                case 2:
                    return Integer.parseInt(tokens[0]) + Double.parseDouble(tokens[1])/60.0;
                case 3:
                    return Integer.parseInt(tokens[0]) + 
                    (Integer.parseInt(tokens[1]) + Double.parseDouble(tokens[2])/60.0) / 60.0;
                default:
                    throw new ParseException("invalid hh:mm:ss format: " + text, tokens.length);
            }
        } catch (NumberFormatException ex) {
            throw new ParseException(ex.getMessage(), tokens.length);
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

    private String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }
}
