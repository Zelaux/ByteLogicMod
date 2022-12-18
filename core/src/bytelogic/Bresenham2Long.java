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
    /*public Seq<Point2> line(Point2 start, Point2 end){
        return line(start.x, start.y, end.x, end.y);
    }*/

    /**
     * Returns a list of {@link Point2} instances along the given line, at integer coordinates.
     * @param startX the start x coordinate of the line
     * @param startY the start y coordinate of the line
     * @param endX the end x coordinate of the line
     * @param endY the end y coordinate of the line
     * @return the list of points on the line at integer coordinates
     */
   /* public Seq<Point2> line(long startX, long startY, long endX, long endY){
        pool.freeAll(points);
        points.clear();
        return line(startX, startY, endX, endY, pool, points);
    }*/

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
    /*public Seq<Point2> line(long startX, long startY, long endX, long endY, Pool<Point2> pool, Seq<Point2> output){

        long w = endX - startX;
        long h = endY - startY;
        long dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
        if(w < 0){
            dx1 = -1;
            dx2 = -1;
        }else if(w > 0){
            dx1 = 1;
            dx2 = 1;
        }
        if(h < 0)
            dy1 = -1;
        else if(h > 0) dy1 = 1;
        long longest = Math.abs(w);
        long shortest = Math.abs(h);
        if(longest <= shortest){
            longest = Math.abs(h);
            shortest = Math.abs(w);
            if(h < 0)
                dy2 = -1;
            else if(h > 0) dy2 = 1;
            dx2 = 0;
        }
        long numerator = longest >> 1;
        for(long i = 0; i <= longest; i++){
            Point2 point = pool.obtain();
            point.set(startX, startY);
            output.add(point);
            numerator += shortest;
            if(numerator > longest){
                numerator -= longest;
                startX += dx1;
                startY += dy1;
            }else{
                startX += dx2;
                startY += dy2;
            }
        }
        return output;
    }*/
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
     *//*
    public Seq<Point2> lineNoDiagonal(long startX, long startY, long endX, long endY, Pool<Point2> pool, Seq<Point2> output){
        long xDist = Math.abs(endX - startX);
        long yDist = -Math.abs(endY - startY);
        long xStep = (startX < endX ? +1 : -1);
        long yStep = (startY < endY ? +1 : -1);
        long error = xDist + yDist;

        output.add(pool.obtain().set(startX, startY));

        while(startX != endX || startY != endY){

            if(2 * error - yDist > xDist - 2 * error){
                error += yDist;
                startX += xStep;
            }else{
                error += xDist;
                startY += yStep;
            }

            output.add(pool.obtain().set(startX, startY));
        }
        return output;
    }*/
  public   interface Longc2{
        void get(long x,long y);
    }
}

