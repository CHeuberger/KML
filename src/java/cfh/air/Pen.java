package cfh.air;

import java.awt.Color;

public class Pen {

    public enum Style {
        SOLID(0),
        DASH(1),
        NULL(5);
        
        private final int code;
        
        private Style(int code) {
            this.code = code;
        }
        
        public int getCode() {
            return code;
        }
        
        public static Style getStyle(int code) {
            for (Style style : values()) {
                if (style.getCode() == code)
                    return style;
            }
            throw new IllegalArgumentException("unrecognized style: " + code);
        }
    }
    
    private Style style;
    private final int width;
    private final Color color;
    
    public Pen(Style style, int width, Color color) {
        if (style == null) throw new IllegalArgumentException("null");
        if (width < 0) throw new IllegalArgumentException(String.valueOf(width));
        if (color == null) throw new IllegalArgumentException("null");
        
        this.style = style;
        this.width = width;
        this.color = color;
    }
    
    public Style getStyle() {
        return style;
    }
    
    public int getWidth() {
        return width;
    }
    
    public Color getColor() {
        return color;
    }
}
