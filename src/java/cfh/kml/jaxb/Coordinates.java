package cfh.kml.jaxb;

import java.util.LinkedList;
import java.util.List;



public class Coordinates {

    private final LinkedList<Coord> coordinates = new LinkedList<Coord>();
    
    public Coordinates() {
    }
    
    public Coordinates(Coord coord) {
        if (coord == null) throw new IllegalArgumentException("null");
        coordinates.add(coord);
    }
    
    public Coordinates(double longitude, double latitude) {
        coordinates.add(new Coord(latitude, longitude));
    }
    
    public Coordinates(double longitude, double latitude, double altitude) {
        coordinates.add(new Coord(latitude, longitude, altitude));
    }
    
    public Coordinates add(Coord coord) {
        if (coord == null) throw new IllegalArgumentException("null");
        coordinates.add(coord);
        return this;
    }
    
    public Coordinates close() {
        Coord first = coordinates.getFirst();
        if (coordinates.getLast() != first) {
            coordinates.addLast(first);
        }
        return this;
    }
    
    public List<Coord> getCoordinates() {
        return coordinates;
    }
    
    public int size() {
        return coordinates.size();
    }
}
