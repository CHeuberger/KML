package cfh.air;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public interface Segment {

    /**
     * @param step degrees, 0: center for circle, start for arcs
     * @return
     */
    public List<Point> getPoints(int step);
    
    public Rectangle2D getBound();
    
    public Point draw(Graphics2D gg, Point last);
}
