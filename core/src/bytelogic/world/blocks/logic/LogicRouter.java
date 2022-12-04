package bytelogic.world.blocks.logic;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.guide.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;

public class LogicRouter extends LogicBlock{
    public LogicRouter(String name){
        super(name);
    }

    @Override
    public void init(){
        if (blockShowcase==null){
            blockShowcase=new BlockShowcase(this,5,5,(world,isSwitch)->{
                world.tile(0, 2).setBlock(inputBlock(isSwitch), Team.sharded, 0);

                world.tile(1, 2).setBlock(this, Team.sharded, 0);
                for(int dx = 0; dx < 3; dx++){
                    for(int dy = 0; dy < 3; dy++){
                        Tile tile = world.tile(1 + dx, 1 + dy);
                        if(tile.build != null) continue;
                        tile.setBlock(byteLogicBlocks.relay, Team.sharded, 0);
                    }
                }
                return new Point2[]{Tmp.p1.set(1, 2)};
            });
        }
        super.init();

    }


    public class LogicRouterBuild extends LogicBuild{
        protected int[] sides = new int[4];

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            int i = relativeTo(otherBuilding.<Building>as());
            sides[i] -= 1;
            if(signal.compareWithZero() != 0) sides[i] = 2;
//                sides[i]=Mathf.clamp(sides[i]+Mathf.sign(signal!=0),0,2);
            sides[i] = Mathf.clamp(sides[i], 0, 2);
            return super.acceptSignal(otherBuilding, signal);
        }

        @Override
        public void beforeUpdateSignalState(){
            for(int i = 0; i < sides.length; i++){
                if(this.canOutputSignal(i)){
                    nearby(i).<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
                }
            }
        }


        @Override
        public boolean canOutputSignal(int dir){
            return super.canOutputSignal(dir) && sides[dir] == 0;
        }


        @Override
        public byte version(){
            return (byte)(super.version() + 0x20);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, (byte)(revision & 0xF));
            revision = (byte)(revision / 0x10);
            if(revision != 1) return;
            for(int i = 0; i < sides.length; i++){
                sides[i] = read.i();
            }
        }

        @Override
        public void customWrite(Writes write){
            for(int side : sides){
                write.i(side);
            }
        }

        @Override
        public void customRead(Reads read){
            for(int i = 0; i < sides.length; i++){
                sides[i] = read.i();
            }
        }

        @Override
        public short customVersion(){
            return 0;
        }
    }
}
