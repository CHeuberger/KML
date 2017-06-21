package cfh.kml.jaxb;

import java.awt.Color;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import cfh.kml.jaxb.adapters.ColorAdapter;

@XmlSeeAlso({LineStyle.class, PolyStyle.class})
public abstract class ColorStyle extends KmlObject {

    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color color;
    
    @XmlElement
    private ColorMode colorMode;
    
    protected ColorStyle() {
    }
    
    public ColorStyle setColor(Color color) {
        if (color == null) throw new IllegalArgumentException("null");
        this.color = color;
        return this;
    }
    
    public ColorStyle setColor(int red, int green, int blue, int alpha) {
        this.color = new Color(red, green, blue, alpha);
        return this;
    }
    
    public ColorStyle setColor(double red, double green, double blue, double alpha) {
        this.color = new Color((float) red, (float) green, (float) blue, (float) alpha);
        return this;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }
    
    public ColorMode getColorMode() {
        return colorMode;
    }
}
