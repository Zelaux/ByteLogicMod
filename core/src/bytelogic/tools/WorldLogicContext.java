package bytelogic.tools;

import arc.struct.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.gen.*;

public class WorldLogicContext{
    public final World world;
    private EntityGroup[] groups = null;

    public WorldLogicContext(World world){
        this.world = world;
    }

    public void inContext(ContextRunnable runnable){
        Seq<Building> buildings = new Seq<>();
        World prevWorld = Vars.world;
        Vars.world = world;
        GroupSaver.store(groups);
        world.tiles.eachTile(it -> {
            if(it.build != null && !buildings.contains(it.build)){
                it.build.remove();
                it.build.add();
                buildings.add(it.build);
            }
            ;
        });
        runnable.run();

        Vars.world = prevWorld;
        groups = GroupSaver.restore();
    }

    public interface ContextRunnable{
        void run();
    }
}
