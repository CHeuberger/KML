package cfh.kml;

import static java.lang.Math.*;

import static cfh.air.Altitude.Type.GND;
import static cfh.kml.jaxb.AltitudeMode.ABSOLUTE;
import static cfh.kml.jaxb.AltitudeMode.CLAMP;
import static cfh.kml.jaxb.AltitudeMode.RELATIVE;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cfh.air.Airspace;
import cfh.air.AirspaceType;
import cfh.air.Altitude;
import cfh.air.Brush;
import cfh.air.Pen;
import cfh.air.Point;
import cfh.air.Segment;
import cfh.kml.jaxb.AltitudeMode;
import cfh.kml.jaxb.Coord;
import cfh.kml.jaxb.Document;
import cfh.kml.jaxb.Feature;
import cfh.kml.jaxb.Folder;
import cfh.kml.jaxb.Geometry;
import cfh.kml.jaxb.LineStyle;
import cfh.kml.jaxb.LinearRing;
import cfh.kml.jaxb.MultiGeometry;
import cfh.kml.jaxb.Placemark;
import cfh.kml.jaxb.PolyStyle;
import cfh.kml.jaxb.Polygon;
import cfh.kml.jaxb.Style;

public class AirspaceConverter {

    /** step in degrees for curves */
    private static final int STEP = 10;
    private static final String DESC_INDENT = "\n                ";
    private static final int SPACE_SEAM = 200;

    public Document convertAirspaces(List<Airspace> airspaces, String name) {
        if (airspaces == null) throw new IllegalArgumentException("null");
        
        Document document = new Document(name);

        Folder ground = new Folder("GROUND");
        for (Airspace airspace : airspaces) {
            if (airspace.getLineNumber() == 0 && airspace.getType() == null)
                continue;
            if (airspace.getName() != null && !airspace.getSegments().isEmpty() && airspace.getFloor() != null) {
                ground.add(createGround(airspace));
            }
        }
        document.add(ground);
        
        Map<AirspaceType, Airspace> templates = new HashMap<AirspaceType, Airspace>(); 
        for (Airspace airspace : airspaces) {
            if (airspace.getLineNumber() == 0 && airspace.getType() == null)
                continue;
            if (airspace.getName() == null || airspace.getSegments().isEmpty()) {
                Style template = createTemplate(airspace);
                document.add(template);
                templates.put(airspace.getType(), airspace);
                
                template = createGroundTemplate(airspace);
                if (template != null) {
                    document.add(template);
                }
            }
        }
        for (Airspace airspace : airspaces) {
            if (airspace.getLineNumber() == 0 && airspace.getType() == null)
                continue;
            if (airspace.getName() != null && !airspace.getSegments().isEmpty()) {
                document.add(createAirspace(airspace, templates));
            }
        }
        return document;
    }
    
    public Feature createAirspace(Airspace airspace, Map<AirspaceType, Airspace> templates) {
        if (airspace == null) throw new IllegalArgumentException("null");
        
        try {
            Placemark placemark;
            Folder folder = new Folder(airspace.getName());
            
            Altitude ceiling = airspace.getCeiling();
            Altitude floor = airspace.getFloor();
            
            String description = createDescription(airspace);
            List<Point> points = getPoints(airspace.getSegments());
            
            Style style = createStyle(airspace);
            URI styleUrl = null;
            if (airspace.getType() != null) {
                try {
                    styleUrl = new URI("#style_" + airspace.getType().getCode());
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
            
            if (ceiling != null) {
                Geometry geometry;
                
                geometry = createPolygon(points, ceiling);
                placemark = new Placemark(airspace.getName() + "-Top", geometry);
                placemark.setDescription(description);
                placemark.setSnippet("");
                if (style != null) {
                    placemark.setStyleSelector(style);
                } else if (styleUrl != null) {
                    placemark.setStyleUrl(styleUrl);
                }
                folder.add(placemark);
                
                if (floor != null) {
                    geometry = createSide(airspace, points, floor, ceiling);
                    if (geometry == null) {
                        System.err.println("unable to create side: " + airspace);
                    } else {
                        placemark = new Placemark(airspace.getName() + "-Side", geometry);
                        placemark.setDescription(description);
                        placemark.setSnippet("");
                        if (style != null) {
                            placemark.setStyleSelector(style);
                        } else if (styleUrl != null) {
                            placemark.setStyleUrl(styleUrl);
                        }
                        folder.add(placemark);
                    }
                }
            }
            
            if (floor != null) {
                Geometry geometry = createPolygon(points, floor);
                placemark = new Placemark(airspace.getName() + "-Bottom", geometry);
                placemark.setDescription(description);
                placemark.setSnippet("");
                if (style != null) {
                    placemark.setStyleSelector(style);
                } else if (styleUrl != null) {
                    placemark.setStyleUrl(styleUrl);
                }
                folder.add(placemark);
            }
            
            return folder;
        } catch (RuntimeException ex) {
            System.err.println(airspace);
            throw ex;
        }
    }
    
    public Feature createGround(Airspace airspace) {
        if (airspace == null) throw new IllegalArgumentException("null");
        
        Altitude floor = airspace.getFloor();
        if (floor == null) return null;
        
        try {
            String description = createDescription(airspace);
            List<Point> points = getPoints(airspace.getSegments());
            
            Placemark placemark = new Placemark(airspace.getName() + "-Ground", createRing(points, null));
            placemark.setDescription(description);
            placemark.setSnippet("");
            if (airspace.getType() != null) {
                try {
                    placemark.setStyleUrl(new URI("#ground_" + airspace.getType().getCode()));
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
            placemark.setVisibility(false);
            
            return placemark;
        } catch (RuntimeException ex) {
            System.err.println(airspace);
            throw ex;
        }
    }

    private Style createTemplate(Airspace airspace) {
        Style template;
        template = createStyle(airspace);
        if (template == null) {
            template = new Style();
        }
        template.setId("style_" + airspace.getType().getCode());
        return template;
    }

    private Style createGroundTemplate(Airspace airspace) {
        Style template = null;
        Pen pen = airspace.getPen();
        if (pen != null) {
            Color color = pen.getColor();
            if (color != null) {
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()/2);
                airspace.setPen(new Pen(pen.getStyle(), pen.getWidth(), color));
                template = createStyle(airspace);
                if (template != null) {
                    template.setId("ground_" + airspace.getType().getCode());
                }
            }
        }
        return template;
    }
    
    private LinearRing createRing(List<Point> points, Altitude altitude) {
        assert points != null;
        
        LinearRing ring = new LinearRing();
        double alt;
        if (altitude == null) {
            alt = 0;
        } else if (altitude.getValue() == 0) {
            alt = 0;
            ring.setAltitudeMode(CLAMP).setTessellate(true);
            ring.setAltitudeMode(CLAMP).setTessellate(true);
        } else if (altitude.getType() == GND) {
            alt = altitude.getValue();
            ring.setAltitudeMode(RELATIVE);
        } else {
            alt = altitude.getValue();
            ring.setAltitudeMode(ABSOLUTE);
        }
        
        for (Point point : points) {
            Coord coord = new Coord(point.getLatitude(), point.getLongitude(), alt);
            ring.add(coord);
        }
        return ring;
    }
    
    private Polygon createPolygon(List<Point> points, Altitude altitude) {
        Polygon polygon = new Polygon();
        polygon.setOuterBoundaryIs(createRing(points, altitude));
        if (altitude == null) {
            // empty
        } else if (altitude.getValue() == 0) {
            polygon.setAltitudeMode(CLAMP).setTessellate(true);
        } else if (altitude.getType() == GND) {
            polygon.setAltitudeMode(RELATIVE);
        } else {
            polygon.setAltitudeMode(ABSOLUTE);
        }
        return polygon;
    }
    
    private Geometry createSide(Airspace airspace, List<Point> points, Altitude floor, Altitude ceiling) {
        if (points.size() < 3)
            return null;

        if (floor.getValue() == 0) {
            Polygon polygon = createPolygon(points, ceiling);
            polygon.setExtrude(true);
            return polygon;
        } 

        MultiGeometry geometry = new MultiGeometry();
        if (floor.getType() == GND) {
            if (ceiling.getType() == GND) {
                addSides(geometry, RELATIVE, points, floor.getValue(), ceiling.getValue());
            } else {
                addSides(geometry, RELATIVE, points, floor.getValue(), floor.getValue()+SPACE_SEAM);
                addSides(geometry, ABSOLUTE, points, ceiling.getValue()-SPACE_SEAM, ceiling.getValue());
                System.err.println("Space with mixed limits: " + airspace);
            }
        } else {
            if (ceiling.getType() != GND) {
                addSides(geometry, ABSOLUTE, points, floor.getValue(), ceiling.getValue());
            } else {
                addSides(geometry, ABSOLUTE, points, floor.getValue(), floor.getValue()+SPACE_SEAM);
                addSides(geometry, RELATIVE, points, ceiling.getValue()-SPACE_SEAM, ceiling.getValue());
                System.err.println("Space with mixed limits: " + airspace);
            }
        }
        return geometry;
    }
    
    private void addSides(MultiGeometry geometry, AltitudeMode mode, List<Point> points, double floor, double ceiling) {
        Point last = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            Point point = points.get(i);
            LinearRing ring = new LinearRing();
            ring.add(new Coord(last.getLatitude(), last.getLongitude(), floor));
            ring.add(new Coord(point.getLatitude(), point.getLongitude(), floor));
            ring.add(new Coord(point.getLatitude(), point.getLongitude(), ceiling));
            ring.add(new Coord(last.getLatitude(), last.getLongitude(), ceiling));
            ring.close();
            geometry.add(new Polygon().setOuterBoundaryIs(ring).setAltitudeMode(mode).setTessellate(mode == CLAMP));
            last = point;
        }
    }

    private String createDescription(Airspace airspace) {
        Altitude ceiling = airspace.getCeiling();
        Altitude floor = airspace.getFloor();
        
        StringBuilder builder = new StringBuilder();
        builder.append(DESC_INDENT).append("    Name: ").append(airspace.getName()); 
        builder.append(DESC_INDENT).append("    Class: ").append(airspace.getType()); 
        if (airspace.getComment() != null) {
            builder.append(" ").append(airspace.getComment());
        }
        if (ceiling != null) {
            builder.append(DESC_INDENT).append("    Ceiling: ").append(ceiling.toDisplayValue()); 
        }
        if (airspace.getFloor() != null) {
            builder.append(DESC_INDENT).append("    Floor: ").append(floor.toDisplayValue());
        }
        builder.append(DESC_INDENT).append("    Line: ").append(airspace.getLineNumber());
        builder.append(DESC_INDENT);
        String description = builder.toString();
        return description;
    }

    private Style createStyle(Airspace airspace) {
        Style style = null;
        Pen pen = airspace.getPen();
        if (pen != null) {
            LineStyle line = new LineStyle();
            line.setWidth(pen.getWidth()).setColor(pen.getColor());
            style = new Style().setLineStyle(line);
        }
        
        Brush brush = airspace.getBrush();
        if (brush != null) {
            PolyStyle poly = new PolyStyle();
            if (brush.isTransparent()) {
                poly.setFill(false);
            } else {
                poly.setColor(brush.getColor());
            }
            if (pen != null && pen.getStyle() == Pen.Style.NULL) {
                poly.setOutline(false);
            }
            if (style == null) {
                style = new Style();
            }
            style.setPolyStyle(poly);
        }
        
        return style;
    }

    private List<Point> getPoints(List<Segment> segments) {
        List<Point> points = new ArrayList<Point>();
        for (Segment segment : segments) {
            points.addAll(segment.getPoints(STEP));
        }
        points.add(points.get(0));
        
        int size = points.size();
        if (size < 4)
            return points;
        
        double lat = 0;
        double lon = 0;
        for (int i = 0; i < size; i++) {
            lat += points.get(i).getLatitude();
            lon += points.get(i).getLongitude();
        }
        lat /= size;
        lon /= size;
        Point center = new Point(lat, lon);

        double total;
        total = calcAngle(points, center);
        if (-0.2 < total && total < 0.2) {
            Point p = points.get(0);
            lat = center.getLatitude() - p.getLatitude();
            lon = center.getLongitude() - p.getLongitude();
            double r = sqrt(lat*lat + lon*lon);
            lat /= r;
            lon /= r;
            center = new Point(p.getLatitude()-0.005*lat, p.getLongitude()-0.005*lon);
            total = calcAngle(points, center);
        }
        
        if (total > 2*PI-0.2) {
            Collections.reverse(points);
            total = -total;
        }
        
        return points;
    }

    private double calcAngle(List<Point> points, Point center) {
        double total = 0;
        double last = center.angleTo(points.get(0));
        for (int i = 1; i < points.size(); i++) {
            double angle = center.angleTo(points.get(i));
            double delta = angle - last;
            while (delta < -PI) delta += 2 * PI;
            while (delta > PI) delta -= 2 * PI;
            total += delta;
            last = angle;
        }
        return total;
    }
}
