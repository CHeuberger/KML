package cfh.air

import static java.lang.Math.*
import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestMatchers.closeTo
import static org.junit.Assert.assertThat

import java.util.List

import spock.lang.*


class ArcBSpecification extends Specification {
    
    static final double ERR = 1e-5

    def "ArcB"() {
        given: "some parameters"
        def center = new Point(cx, cy)
        def start = new Point(sx, sy)
        def end = new Point(ex, ey)
        def clockwise = dir

        when: "create arc with parameters"
        def arc = new ArcB(start, end, center, clockwise)

        then: "given parameters should be returned"
        with(arc) {
            getStart() equals start
            getEnd() equals end
            getCenter() equals center
            isClockwise() == clockwise
        }

        where:
        cx | cy || sx | sy || ex | ey || dir
         0 |  0 || 10 |  0 ||  0 | 10 || true
         5 |  5 || -5 | -5 || -5 | 10 || false
    }
    
    
    def "getPoints(0)"() {
        given: "an arc with parameters"
        def center = new Point(cx, cy)
        def start = new Point(sx, sy)
        def end = new Point(ex, ey)
        def clockwise = dir
        def arc = new ArcB(start, end, center, dir)

        when: "list of points with zero step is retriedved"
        def points = arc.getPoints(0)
        
        then: "start and end point should be returned"
        points.size() == 2
        points.get(0) equals start
        points.get(1) equals end

        where:
        cx | cy || sx | sy || ex | ey || dir
         0 |  0 || 10 |  0 ||  0 | 10 || true
         5 |  5 || -5 | -5 || -5 | 10 || false
    }
    
    
    def "getPoints(>0)"() {
        given: "an arc with parameters"
        def center = new Point(cx, cy)
        def start = new Point(sx, sy)
        def end = new Point(ex, ey)
        def clockwise = dir
        def arc = new ArcB(start, end, center, clockwise)
        def minR = min(center.distTo(start), center.distTo(end)) - ERR
        def maxR = max(center.distTo(start), center.distTo(end)) + ERR

        when: "list of points with #step degree step is retriedved"
        def points = arc.getPoints(step)
        
        then: "the corect number of points is returned"
        points.size() == count
        points.get(0) equals start
        points.get(points.size()-1) equals end
        ordered(points, center, dir ? step : -step)
        points.each {
            def r = center.distTo(it)
            assert minR < r & r < maxR
        }

        where:
        cx | cy || sx | sy || ex | ey || dir   || step | count
         0 |  0 ||  5 |  0 ||  0 | -5 || true  ||  10  | 9+1
         0 |  0 ||  5 |  0 ||  0 | -5 || false ||  10  | 27+1
         0 |  0 ||  5 |  0 ||  0 |  5 || true  ||  10  | 27+1
         0 |  0 ||  5 |  0 ||  0 |  5 || false ||  10  | 9+1
    }
    
    boolean ordered(List<Point> points, Point center, int step) {
        double radStep = toRadians(step)
        double prev = center.angleTo(points.get(0))
        for (int i = 1; i < points.size(); i++) {
            double angle = center.angleTo(points.get(i))
            double delta = angle - prev;
            if (signum(delta) != signum(radStep)) {
                delta += signum(radStep) * 2 * PI;
            }
            if (abs(delta-radStep) > ERR) return false;
            prev = angle
        }
        return true;
    }
}
