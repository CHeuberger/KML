package cfh.air;

import java.awt.Color;

public class Brush {

    private final Color color;
    
    public Brush(Color color) {
        this.color = color;
    }
    
    public boolean isTransparent() {
        return color == null;
    }
    
    public Color getColor() {
        return color;
    }
}
