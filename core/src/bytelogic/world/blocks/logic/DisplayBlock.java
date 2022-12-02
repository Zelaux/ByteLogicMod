package bytelogic.world.blocks.logic;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.io.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import mindustry.io.*;

public class DisplayBlock extends AcceptorLogicBlock{

    public static long canvasSize = 8L;

    public DisplayBlock(String name){
        super(name);
        rotate = false;
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{Core.atlas.find(name)};
    }

    public class DisplayBuild extends AcceptorLogicBuild{
        Color drawColor = new Color(Color.white);

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            if(signal.type == SignalTypes.colorType){
                drawColor.set(signal.intNumber());
                return true;
            }
            return super.acceptSignal(otherBuilding, signal);
        }

        @Override
        public void draw(){
            super.draw();
            //TODO buffer
            Draw.rect(region, tile.drawx(), tile.drawy());

            float dw = 8f / 6f, dh = 8f / 6f, xs = dw, ys = dh;

            long w = canvasSize, h = canvasSize;
            long signal = currentSignal().number();
//            int jjj = 2 * ;

            Draw.color(drawColor);
            for(long i = 0; i < w * h; i++){
                long x = i % w;
                long y = i / w;

                if((signal & (1L << i)) != 0){
                    Fill.rect(tile.drawx() + x * xs - (w - 1) * xs / 2f, tile.drawy() + y * ys - (h - 1) * ys / 2f, dw, dh);
                }
            }
        }

        @Override
        public void customWrite(Writes write){
            TypeIO.writeColor(write, drawColor);
        }

        @Override
        public void customRead(Reads read){
            TypeIO.readColor(read, drawColor);
        }

        @Override
        public short customVersion(){
            return 1;
        }
    }
}
