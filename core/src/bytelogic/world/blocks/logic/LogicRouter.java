package bytelogic.world.blocks.logic;

import arc.math.*;
import arc.util.io.*;
import bytelogic.gen.*;
import mindustry.gen.*;

public class LogicRouter extends LogicBlock{
    public LogicRouter(String name){
        super(name);
    }

    public class LogicRouterBuild extends LogicBuild{
     protected    int[] sides = new int[4];

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, int signal){
            int i = relativeTo(otherBuilding.<Building>as());
            sides[i] -= 1;
            if(signal != 0) sides[i] = 2;
//                sides[i]=Mathf.clamp(sides[i]+Mathf.sign(signal!=0),0,2);
            sides[i] = Mathf.clamp(sides[i], 0, 2);
            return super.acceptSignal(otherBuilding, signal);
        }

        @Override
        public void beforeUpdateSignalState(){
            for(int i = 0; i < sides.length; i++){
                if(output(i)){
                    nearby(i).<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
                }
            }
        }

        @Override
        public byte version(){
            return (byte)(super.version() +0x10);
        }


        @Override
        public boolean output(int dir){
            return super.output(dir) && sides[dir] == 0;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            for(int side : sides){
                write.i(side);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, (byte)(revision&0xF));
            revision = (byte)(revision / 0x10);
            if (revision==0)return;
            for(int i = 0; i < sides.length; i++){
                sides[i]=read.i();
            }
        }
    }
}
