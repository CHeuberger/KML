package cfh.air;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Airspace {

    private final int lineNumber;
    private AirspaceType type;
    private String comment;
    private String name;
    private Altitude floor;
    private Altitude ceiling;
    private Pen pen;
    private Brush brush;
    
    private final List<Segment> segments = new ArrayList<Segment>();
    
    public Airspace(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public Airspace setType(AirspaceType type) {
        if (type == null) throw new IllegalArgumentException("null");
        this.type = type;
        return this;
    }
    
    public AirspaceType getType() {
        return type;
    }
    
    public void setComment(String comment) {
        if (comment == null) throw new IllegalArgumentException("null");
        this.comment = comment;
    }
    
    public String getComment() {
        return comment;
    }
    
    public Airspace setName(String name) {
        if (name == null) throw new IllegalArgumentException("null");
        this.name = name;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public Airspace setFloor(Altitude floor) {
        if (floor == null) throw new IllegalArgumentException("null");
        this.floor = floor;
        return this;
    }
    
    public Altitude getFloor() {
        return floor;
    }
    
    public Airspace setCeiling(Altitude ceiling) {
        if (ceiling == null) throw new IllegalArgumentException("null");
        this.ceiling = ceiling;
        return this;
    }
    
    public Altitude getCeiling() {
        return ceiling;
    }
    
    public Airspace setPen(Pen pen) {
        if (pen == null) throw new IllegalArgumentException("null");
        this.pen = pen;
        return this;
    }
    
    public Pen getPen() {
        return pen;
    }
    
    public Airspace setBrush(Brush brush) {
        if (brush == null) throw new IllegalArgumentException("null");
        this.brush = brush;
        return this;
    }
    
    public Brush getBrush() {
        return brush;
    }
    
    public Airspace add(Segment segment) {
        if (segment == null) throw new IllegalArgumentException("null");
        segments.add(segment);
        return this;
    }
    
    public List<Segment> getSegments() {
        return Collections.unmodifiableList(segments);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        builder.append(":").append(lineNumber);
        builder.append("[").append(name).append(",");
        builder.append(type).append(",");
        builder.append(floor).append(",");
        builder.append(ceiling).append("]");
        return builder.toString();
    }
}
