package bytelogic.world;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.util.Log;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;

public class AlwaysDrawBlock {
    static {
        Log.info("Hello world");
        Events.run(EventType.Trigger.draw, () -> {
            for (Building building : Groups.build) {
                if (building instanceof AlwaysDrawBuild) {
                    float prev = Draw.z();
                    Draw.z(Layer.block);
                    ((AlwaysDrawBuild) building).alwaysDraw();
                    Draw.z(prev);
                }
            }
        });
    }

    public static void register() {
    }

    public static interface AlwaysDrawBuild {

        void alwaysDraw();
    }
}
