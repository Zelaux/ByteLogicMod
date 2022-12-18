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
        ObjectSet<Building> buildings = new ObjectSet<>();
        World prevWorld = Vars.world;
        Vars.world = world;
        GroupSaver.store(groups);
        world.tiles.eachTile(it -> {
            if(it.build != null && buildings.add(it.build)){
                it.build.remove();
                it.build.add();
//                buildings.add(it.build);
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
