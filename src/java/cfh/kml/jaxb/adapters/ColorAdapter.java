package cfh.kml.jaxb.adapters;

import java.awt.Color;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ColorAdapter extends XmlAdapter<String, Color> {

    @Override
    public String marshal(Color c) throws Exception {
        return String.format("%02x%02x%02x%02x", c.getAlpha(), c.getBlue(), c.getGreen(), c.getRed());
    }

    @Override
    public Color unmarshal(String text) throws Exception {
        int a = Integer.parseInt(text.substring(0, 2), 16);
        int b = Integer.parseInt(text.substring(2, 4), 16);
        int g = Integer.parseInt(text.substring(4, 6), 16);
        int r = Integer.parseInt(text.substring(6, 8), 16);
        return new Color(r, g, b, a);
    }
}
