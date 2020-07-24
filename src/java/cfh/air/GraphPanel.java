package cfh.air;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GraphPanel extends JPanel {
    
    private static final Color SEGMENT_COLOR = Color.BLACK;
    private static final int GAP = 20;

    private List<Airspace> airspaces = null;
    private Rectangle2D boundingBox = null;
    
    
    void setAirspaces(Collection<Airspace> airspaces) {
        this.airspaces = (airspaces == null) ? null : new ArrayList<Airspace>(airspaces);
        calcLimits();
        repaint();
    }
    
    private void calcLimits() {
        Rectangle2D bound = null;
        if (airspaces != null) {
            for (Airspace airspace : airspaces) {
                if (airspace.getName() != null) {
                    for (Segment segment : airspace.getSegments()) {
                        if (bound == null) {
                            bound = segment.getBound();
                        } else {
                            bound.add(segment.getBound());
                        }
                    }
                }
            }
        }
        boundingBox = bound;
        System.out.println(bound); // TODO delete
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (airspaces != null) {
            if (boundingBox == null) {
                calcLimits();
            }
            Graphics2D gg = (Graphics2D) g.create();
            try {
                if (boundingBox == null) {
                    gg.drawString("empty ?", 40, 40);
                    return;
                }
                double scaleX = (getWidth()-2*GAP) / boundingBox.getWidth();
                double scaleY = (getHeight()-2*GAP) / boundingBox.getHeight();
                double scale = Math.min(scaleX, scaleY);
                gg.translate(GAP, GAP);
                gg.scale(scale, -scale);
                gg.translate(-boundingBox.getMinX(), -boundingBox.getMaxY());
                gg.setStroke(new BasicStroke((float) (1/scale)));
                
                for (Airspace airspace : airspaces) {
                    if (airspace.getName() != null) {
                        System.out.println(airspace.getName());
                        Point last = null;
                        gg.setColor(SEGMENT_COLOR);
                        List<Segment> segments = airspace.getSegments();
                        for (Segment segment : segments) {
                            last = segment.draw(gg, last);
                        }
                        if (last != null) {
                            Point p = segments.get(0).getPoints(0).get(0);
                            gg.draw(new Line2D.Double(last.getLongitude(), last.getLatitude(), p.getLongitude(), p.getLatitude()));
                        }
                    }
                }
            } finally {
                gg.dispose();
            }
        }
    }
}
