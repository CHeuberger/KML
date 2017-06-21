package cfh.air;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public interface Segment {
    
    public static final double MILES_PER_DEGREE = 60.0311975044;

    /**
     * @param step degrees, 
     *        if 0 return: center for circle or end points for arcs
     * @return
     */
    public List<Point> getPoints(int step);
    
    public Rectangle2D getBound();
    
    @Deprecated
    public Point draw(Graphics2D gg, Point last);
}
