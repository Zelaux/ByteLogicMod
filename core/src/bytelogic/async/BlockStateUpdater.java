package bytelogic.async;

import arc.util.*;
import bytelogic.gen.*;
import mindustry.async.*;

public class BlockStateUpdater implements AsyncProcess {
    public float timer = 0;
    public static final int updatesPerSecond = 60;
    public static final float deltaScale = updatesPerSecond / Time.toSeconds;
    ;

    @Override
    public void begin() {
        timer += Time.delta * deltaScale;
    }

    @Override
    public void reset() {
        timer = 0;
    }

    @Override
    public void process() {

        while (timer >= 1f) {
            for (ByteLogicBuildingc build : BLGroups.byteLogicBuild) {
                build.transportSignalState();
            }
            for (ByteLogicBuildingc build : BLGroups.byteLogicBuild) {
                build.swapingSignalState();
            }
            timer -= 1f;
        }
    }

    @Override
    public void end() {

        for (ByteLogicBuildingc build : BLGroups.byteLogicBuild) {
            build.updateDisplaySignalState();
        }
        //        AsyncProcess.super.end();
    }
}
