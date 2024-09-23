package bytelogic;

import arc.func.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.pooling.*;

public class Bresenham2Long{
    private final Seq<Point2> points = new Seq<>();
    private final Pool<Point2> pool = Pools.get(Point2.class, Point2::new);

    /**
     * Iterates through a list of {@link Point2} instances along the given line, at integer coordinates.
     * @param startX the start x coordinate of the line
     * @param startY the start y coordinate of the line
     * @param endX the end x coordinate of the line
     * @param endY the end y coordinate of the line
     */
    public static void line(long startX, long startY, long endX, long endY, Longc2 consumer){
        long dx = Math.abs(endX - startX);
        long dy = Math.abs(endY - startY);

        long sx = startX < endX ? 1 : -1;
        long sy = startY < endY ? 1 : -1;

        long err = dx - dy;
        long e2;
        while(true){
            consumer.get(startX, startY);
            if(startX == endX && startY == endY) break;

            e2 = 2 * err;
            if(e2 > -dy){
                err = err - dy;
                startX = startX + sx;
            }

            if(e2 < dx){
                err = err + dx;
                startY = startY + sy;
            }
        }
    }

    /**
     * Returns a list of {@link Point2} instances along the given line, at integer coordinates.
     * @param start the start of the line
     * @param end the end of the line
     * @return the list of points on the line at integer coordinates
     */

    /**
     * Returns a list of {@link Point2} instances along the given line, at integer coordinates.
     * @param startX the start x coordinate of the line
     * @param startY the start y coordinate of the line
     * @param endX the end x coordinate of the line
     * @param endY the end y coordinate of the line
     * @return the list of points on the line at integer coordinates
     */

    /**
     * Returns a list of {@link Point2} instances along the given line, at integer coordinates.
     * @param startX the start x coordinate of the line
     * @param startY the start y coordinate of the line
     * @param endX the end x coordinate of the line
     * @param endY the end y coordinate of the line
     * @param pool the pool from which Point2 instances are fetched
     * @param output the output array, will be cleared in this method
     * @return the list of points on the line at integer coordinates
     */
    /*
    *//**
     * Returns a list of {@link Point2} instances along the given line at integer coordinates, with no diagonals.
     * @param startX the start x coordinate of the line
     * @param startY the start y coordinate of the line
     * @param endX the end x coordinate of the line
     * @param endY the end y coordinate of the line
     * @param pool the pool from which Point2 instances are fetched
     * @param output the output array, will be cleared in this method
     * @return the list of points on the line at integer coordinates
     */
    public   interface Longc2{
        void get(long x,long y);
    }
}

