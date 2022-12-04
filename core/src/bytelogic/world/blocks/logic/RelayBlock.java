package bytelogic.world.blocks.logic;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import bytelogic.gen.*;
import bytelogic.ui.guide.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;

public class RelayBlock extends AcceptorLogicBlock{

    public RelayBlock(String name){
        super(name);
        doOutput = true;
    }

    @Override
    public void init(){
        if(blockShowcase == null){
            blockShowcase = new BlockShowcase(this, 5, 5, (world, isSwitch) -> {
                world.tile(0, 2).setBlock(inputBlock(isSwitch), Team.sharded, 0);

                for(int dy = 0; dy < 3; dy++){

                    Tile switchTile = world.tile(0, 1 + dy);
                    switchTile.setBlock(inputBlock(isSwitch), Team.sharded, 0);
                    for(int dx = 0; dx < 3; dx++){
                        Tile tile = world.tile(1 + dx, 1 + dy);
                        tile.setBlock(byteLogicBlocks.relay, Team.sharded, Mathf.mod(Mathf.num(dx == 2) * (1 - dy), 4));
                    }
                }
                world.tile(4, 2).setBlock(byteLogicBlocks.relay, Team.sharded, 0);
                return new Point2[0];
            });
        }
        super.init();
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
        public boolean canOutputSignal(int dir){
            return super.canOutputSignal(dir);
//            return dir == rotation && front() instanceof LogicBuild && (!(front().block instanceof RelayBlock) || front().front() != this);
        }

        @Override
        public void draw(){
            super.draw();
            Draw.color(signalColor());
            for(Building prox : proximity){
                if(prox instanceof ByteLogicBuildingc buildingc && buildingc.canOutputSignal(prox.relativeTo(this))){
                    Draw.rect(region, x, y, relativeTo(prox) * 90);
                }
            }
            Draw.color();
        }
    }
}
