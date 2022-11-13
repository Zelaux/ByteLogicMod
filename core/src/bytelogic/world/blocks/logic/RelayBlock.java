package bytelogic.world.blocks.logic;

import arc.graphics.*;
import arc.graphics.g2d.*;
import bytelogic.gen.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class RelayBlock extends AcceptorLogicBlock{

    public RelayBlock(String name){
        super(name);
        doOutput = true;
    }

    public class RelayBuild extends AcceptorLogicBuild{

/*        @Override
        public int signal(){
            int max = 0;
            for(Building other : proximity()){
                if(front() == other) continue;
//                if((!(other instanceof RelayBuild) || other.front() != this) && back() != other) continue;
                if(other instanceof RelayBuild && other.front() == this) continue;
                if(!(other instanceof RelayBuild) && (!other.block.rotate || other.front() != this))
                    max |= getSignal(this, other);
            }
            return max;
        }*/

        @Override
        public boolean output(int dir){
            return super.output(dir);
//            return dir == rotation && front() instanceof LogicBuild && (!(front().block instanceof RelayBlock) || front().front() != this);
        }

        @Override
        public void draw(){
            super.draw();
            Draw.color(signalColor());
            for(Building prox : proximity){
                if(prox instanceof ByteLogicBuildingc buildingc && buildingc.output(prox.relativeTo(this))){
                    Draw.rect(region, x, y, relativeTo(prox) * 90);
                }
            }
            Draw.color();
        }
    }
}
