package bytelogic.tools.shapes;

import arc.math.geom.Vec2;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Square extends Poly<Square> {
    public Square() {
        super(4);
    }
    public Square(Vec2... points) {
        this();
        if (points.length!=4){
            throw new IllegalArgumentException("points must be tree for square");
        }
        setAll(points);
    }

    @Override
    protected Square getThis() {
        return this;
    }
}
