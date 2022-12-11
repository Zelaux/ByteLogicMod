package bytelogic.tools;

import arc.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.geom.*;
import arc.struct.*;
import bytelogic.annotations.BLAnnotations.*;
import bytelogic.gen.BLIcons.*;
import bytelogic.gen.*;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mma.graphics.*;
import org.jetbrains.annotations.*;

import static mindustry.Vars.tilesize;

public class LogicNet{
    static final Bits exists = new Bits();
    static final LongSeq buildCache = new LongSeq();
    static final LongSeq lastVisitedFirst = new LongSeq();
    static final LongSeq lastVisitedSecond = new LongSeq();
    private static final int[] sideMasks = {
        LogicNetBuildSignalDirections.bitMaskRight,
        LogicNetBuildSignalDirections.bitMaskTop,
        LogicNetBuildSignalDirections.bitMaskLeft,
        LogicNetBuildSignalDirections.bitMaskBottom
    };
    private static final IntSeq tmpSeq = new IntSeq(4);
    static boolean isFirstNext;
    static Seq<String> numberToString = new Seq<>();
    static IntSeq buildingIndexToCacheIndex = new IntSeq();

    @Nullable
    public static ByteLogicBuildingc first(){
        if(buildCache.size == 0) return null;
        long data = buildCache.get(0);
        int x = LogicNetBuildData.x(data);
        int y = LogicNetBuildData.y(data);
        return Vars.world.build(x, y).as();
    }

    public static void set(@NotNull ByteLogicBuildingc building){
        reset();
        registerBuild(building, 0, 0);
    }

    public static void reset(){
        buildCache.clear();
        lastVisitedFirst.clear();
        lastVisitedSecond.clear();
        exists.clear();
        for(int i = 0; i < buildingIndexToCacheIndex.size; i++){
            buildingIndexToCacheIndex.set(i, -1);
        }
        if(Vars.world != null){
            for(int i = 0; i < Vars.world.width() * Vars.world.height() - buildingIndexToCacheIndex.size; i++){
                buildingIndexToCacheIndex.add(-1);
            }
        }
        isFirstNext = true;
    }

    public static void draw(){
        for(int i = 0; i < buildCache.size; i++){
            long data = buildCache.get(i);
            int x = LogicNetBuildData.x(data);
            int y = LogicNetBuildData.y(data);
            int number = LogicNetBuildData.number(data);
            int signalDirections = LogicNetBuildData.signalDirections(data);

            for(int j = numberToString.size; j <= number; j++){
                numberToString.add(j + "");
            }
            Tile tile = Vars.world.tile(x, y);
            Draw.color(Pal.accent,1f);
//            Draw.color(number % 2 == 0 ? Pal.removeBack : Pal.heal, (i + .5f) / buildCache.size);
//            Fill.rect(tile.worldx(), tile.worldy(), tilesize, tilesize);

            for(int j = 0; j < sideMasks.length; j++){
                if((signalDirections & sideMasks[j]) == 0) continue;
                Point2 offset = Geometry.d4((j) % 4);
                float scale = tilesize / 2f;
                Draw.rect(Regions.nextBlockArrow32, tile.worldx() + offset.x * scale, tile.worldy() + offset.y * scale, j * 90 + 180);
            }
            ADrawf.drawText(tile.worldx()-tilesize/4f, tile.worldy()-tilesize/4f,0.125f, numberToString.get(number));
        }
    }

    public static void update(){
//        if(!Core.input.keyTap(KeyCode.z)) return;
        LongSeq lastVisited;
        if(isFirstNext){
            lastVisited = lastVisitedFirst;
        }else{
            lastVisited = lastVisitedSecond;
        }
        isFirstNext = !isFirstNext;
        for(int i = 0; i < lastVisited.size; i++){
            long data = lastVisited.get(i);
            int x = LogicNetBuildData.x(data);
            int y = LogicNetBuildData.y(data);
            int number = LogicNetBuildData.number(data);

            ByteLogicBuildingc build = Vars.world.build(x, y).as();
            tmpSeq.clear();
            build.nextBuildings(tmpSeq);
            for(int i0 = 0; i0 < tmpSeq.size; i0++){
                Tile nextTile = Vars.world.tiles.geti(tmpSeq.get(i0));
                if(!(nextTile.build instanceof ByteLogicBuildingc otherBuild)) continue;
                Building other = otherBuild.as();
                byte parentDir = build.relativeTo(other);
                /*if(!build.canOutputSignal(parentDir)){
                    if(buildingc.canOutputSignal((parentDir + 2) % 4)){

                    }
                    continue;
                }*/

                if(exists.get(other.tile.array())){
                    int j = buildingIndexToCacheIndex.get(other.tile.array());
                    long logicNetBuildData = buildCache.get(j);
                    buildCache.set(j,
                        LogicNetBuildData.signalDirections(logicNetBuildData,
                            LogicNetBuildData.signalDirections(logicNetBuildData) |
                                sideMasks[other.tile.absoluteRelativeTo(build.tile().x, build.tile().y)]
                        )
                    );
                    continue;
                }
                registerBuild(other.as(), number + build.tickAmount(), sideMasks[(parentDir + 2) % 4]);
            }
        }
        lastVisited.clear();
    }

    private static void registerBuild(ByteLogicBuildingc building, int number, int incomeDirection){
        long buildData = buildData(building, number, incomeDirection);
        exists.set(building.tile().array());
        buildingIndexToCacheIndex.set(building.tile().array(), buildCache.size);
        buildCache.add(buildData);
        if(isFirstNext){
            lastVisitedFirst.add(buildData);
        }else{
            lastVisitedSecond.add(buildData);
        }
    }

    static long buildData(ByteLogicBuildingc building, int number, int incomeDirection){
        return LogicNetBuildData.get(building.tile().x, building.tile().y, (short)number, incomeDirection);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    @Struct
    @RemoveFromCompilation
    class LogicNetBuildDataStruct{
        @StructField(20)
        int x;
        @StructField(20)
        int y;
        @StructField(20)
        short number;
        @StructField(4)
        int signalDirections;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    @Struct
    @RemoveFromCompilation
    class LogicNetBuildSignalDirectionsStruct{
        @StructField(1)
        int right;
        @StructField(1)
        int top;
        @StructField(1)
        int left;
        @StructField(1)
        int bottom;
    }
}
