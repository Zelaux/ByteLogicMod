package bytelogic.ui.dialogs;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mma.ui.tiledStructures.*;

public class ByteLogicGateCanvas extends TiledStructuresCanvas{
    public ByteLogicGateCanvas(TiledStructuresDialog tiledStructuresDialog){
        super(tiledStructuresDialog);
    }
    public void setPressed(boolean pressed){
        Reflect.set(TiledStructuresCanvas.class,this,"pressed",pressed);
    }
    public void setVisualPressed(long visualPressed){
        Reflect.set(TiledStructuresCanvas.class,this,"visualPressed",visualPressed);
    }
    public int queryX(Vec2 pos){
        return Mathf.round(floatQueryX(pos) / unitSize);
    }

    public float floatQueryX(Vec2 pos){
        return pos.x - query.width() * unitSize / 2f;
    }

    public int queryY(Vec2 pos){
        return Mathf.round(floatQueryY(pos) / unitSize);
    }

    public float floatQueryY(Vec2 pos){
        return pos.y - query.height() * unitSize / 2f;
    }
}
