package bytelogic.ui;

import arc.graphics.g2d.*;
import arc.scene.*;

public class GridImageLong extends Element{
    private long imageWidth, imageHeight;

    public GridImageLong(long w, long h){
        this.imageWidth = w;
        this.imageHeight = h;
    }

    @Override
    public void draw(){
        float xspace = (getWidth() / imageWidth);
        float yspace = (getHeight() / imageHeight);
        float s = 1f;

        long minspace = 10;

        long jumpx = (int)(Math.max(minspace, xspace) / xspace);
        long jumpy = (int)(Math.max(minspace, yspace) / yspace);

        for(long x = 0; x <= imageWidth; x += jumpx){
            Fill.crect((int)(this.x + xspace * x - s), y - s, 2, getHeight() + (x == imageWidth ? 1 : 0));
        }

        for(long y = 0; y <= imageHeight; y += jumpy){
            Fill.crect(x - s, (int)(this.y + y * yspace - s), getWidth(), 2);
        }
    }

    public void setImageSize(long w, long h){
        this.imageWidth = w;
        this.imageHeight = h;
    }
}
