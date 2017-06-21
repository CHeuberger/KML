package cfh.kml.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cfh.kml.jaxb.Coord;
import cfh.kml.jaxb.Coordinates;

public class CoordinatesAdapter extends XmlAdapter<String, Coordinates> {


    @Override
    public String marshal(Coordinates coordinates) throws Exception {
        StringBuilder builder = new StringBuilder();
        if (coordinates != null && coordinates.getCoordinates() != null) {
            for (Coord coord : coordinates.getCoordinates()) {
                builder.append("\n              ").append(coord.toKmlString());
            }
            builder.append("\n            ");
        }
        return builder.toString();
    }

    @Override
    public Coordinates unmarshal(String text) throws Exception {
        Coordinates coordinates = new Coordinates();
        if (text != null) {
            for (String token : text.split("\\s")) {
                coordinates.add(new Coord(token));
            }
        }
        return coordinates;
    }
}
