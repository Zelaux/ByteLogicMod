package bytelogic.world.blocks.logic;

//import io.anuke.annotations.Annotations.*;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.content.*;
import bytelogic.core.*;
import bytelogic.game.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.guide.*;
import bytelogic.world.meta.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import mma.*;
import mma.type.*;
import mma.type.pixmap.*;
import mma.utils.*;

import java.lang.reflect.*;

public abstract class LogicBlock extends Block implements LogicBlockI, ImageGenerator{

    /**
     * @return signal to send next frame.
     */
//    public abstract int signal(Tile tile);
    private static final EventSender blLogicTileTapped = new EventSender("bl-logic-tile-tapped");
    @Load("@baseName()")
    public TextureRegion base;
    @Load("@baseName()-top")
    public TextureRegion topMask;
    public String baseName = "logic-base";
    public LogicBlock originalMirror = null;
    public ByteLogicBlocks byteLogicBlocks;
    public BlockPreview blockPreview = null;
    protected boolean doOutput = true;
    public LogicBlock(String name){
        super(name);
        saveConfig=false;
        rotate = true;
        group = BlockGroup.logic;
        update = true;
//        entityType = LogicBuild::new;
//        controllable = false;
    }

    private static boolean canDrawSelect(LogicBuild logicBuild){
        return !SettingManager.enabledLogicNetSelection.get();
    }

    @Override
    public Pixmap generate(Pixmap icon, PixmapProcessor processor){
        if(region.found()) applyMask(region, processor);

        return ImageGenerator.super.generate(icon, processor);
    }

    protected Pixmap applyMask(TextureRegion region, PixmapProcessor processor){
        Pixmap targetPixmap = processor.get(region);
        Pixmap pixmap = processor.get(topMask);

        for(int x = 0; x < pixmap.width; x++){
            for(int y = 0; y < pixmap.height; y++){
                if(pixmap.getA(x, y) == 0 || !targetPixmap.in(x, y)) continue;
                targetPixmap.set(x, y,Color.clearRgba);
            }
        }
        processor.replaceAbsolute(region,targetPixmap);
        return targetPixmap;
    }

    @Override
    public void init(){

        super.init();
        if(blockPreview == null){
            blockPreview = new DefaultBlockPreview(this);
        }
    }

    public String realName(){
        if(originalMirror == null) return name;
        return originalMirror.name;
    }

    public String baseName(){
        if(ModVars.packSprites) return baseName;
        return minfo.mod.name + "-" + baseName;
    }

    @Override
    public void load(){
        super.load();
        if(originalMirror != null && !region.found()){
            if(originalMirror.region == null) originalMirror.load();
            region = originalMirror.region;
        }
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(BLStat.preview, table -> {
            table.row();
            table.table(innerTable -> {
                blockPreview.buildDemoPage(innerTable, false);
            }).grow();

        });
    }

    @Override
    protected void initBuilding(){
        //attempt to find the first declared class and use it as the entity type
        try{
            Class<?> current = getClass();

            if(current.isAnonymousClass()){
                current = current.getSuperclass();
            }

            subclass = current;

            while(buildType == null && Block.class.isAssignableFrom(current)){
                //first class that is subclass of Building
                Class<?> type = Structs.find(current.getDeclaredClasses(), t -> Building.class.isAssignableFrom(t) && !t.isInterface());
                if(type != null){
                    //these are inner classes, so they have an implicit parameter generated
                    Constructor<? extends Building> cons = (Constructor<? extends Building>)type.getDeclaredConstructor(type.getDeclaringClass());
                    buildType = () -> {
                        try{
                            return cons.newInstance(this);
                        }catch(Exception e){
                            Log.err("Cannot initialize building for " + name);
                            throw new RuntimeException(e);
                        }
                    };
                }

                //scan through every superclass looking for it
                current = current.getSuperclass();
            }

        }catch(Throwable ignored){
        }

        if(buildType == null){
            //assign default value
            buildType = Building::create;
        }
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("signal", (Building e) -> {
            ByteLogicBuildingc entity = e.as();
            return new Bar(
                () -> Core.bundle.format("block.signal", entity.currentSignal()),
                () -> {
                    return entity.currentSignal().barColor();
                },
                () -> 1f);
        });
    }

    @Override
    public TextureRegion[] icons(){
        return !ModVars.packSprites ? new TextureRegion[]{fullIcon} : new TextureRegion[]{base, region};
    }

    ;

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){
        TextureRegion back = base;
        Draw.rect(back, req.drawx(), req.drawy(),
            back.width * req.animScale * Draw.scl,
            back.height * req.animScale * Draw.scl,
            0);
        Draw.rect(region, req.drawx(), req.drawy(),
            region.width * req.animScale * Draw.scl,
            region.height * req.animScale * Draw.scl,
            !rotate ? 0 : req.rotation * 90);
    }

    protected Block inputBlock(boolean isSwitch){
        return isSwitch ? byteLogicBlocks.switchBlock : byteLogicBlocks.signalBlock;
    }

    public abstract class LogicBuild extends Building implements ByteLogicBuildingc, CustomSaveBuilding{
        protected  int index__bytelogicBuild=-1;
        @Override
        public void setIndex__byteLogicBuild(int index){
this.index__bytelogicBuild=index;
        }


        public final Signal lastSignal = new Signal();
        protected final Signal nextSignal = new Signal();

        @Override
        public void nextBuildings(IntSeq positions){

        }

        @Override
        public int tickAmount(){
            return 1;
        }

        @Override
        public void tapped(){
            blLogicTileTapped.setParameter("build", this);
            blLogicTileTapped.fire(true);
        }

        public boolean canDrawSelect(){
            return LogicBlock.canDrawSelect(this);
        }

        @Override
        public void beforeUpdateSignalState(){
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
        }

        public Tile frontTile(){
            return tile.nearby(Geometry.d4x(this.rotation), Geometry.d4y(this.rotation));
        }

        @Override
        public void draw(){

            Draw.rect(base, tile.drawx(), tile.drawy());

            Draw.color(signalColor());
//            super.draw(tile);
            super.draw();
            Draw.color();

        }

        protected Color signalColor(){
            return currentSignal().color();
        }

        @Override
        public Signal currentSignal(){
            return lastSignal;
        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            nextSignal.or(signal);
            return true;
        }

        @Override
        public void updateSignalState(){
            lastSignal.set(nextSignal);
            nextSignal.setZero();
        }

        @Override
        public void update(){
            super.update();

        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(2);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(revision == 0){
                Signal.valueOf(nextSignal, read.i());
                lastSignal.set(nextSignal);
            }else{
                int version = read.i();
                if(version == 2) return;
                Signal.valueOf(nextSignal, read.i());
                Signal.valueOf(lastSignal, read.i());
            }
        }

        @Override
        public byte version(){
            return 1;
        }


        @Override
        public void remove(){
            boolean wasAdded = added;
            super.remove();
            if(wasAdded != added){
                BLGroups.byteLogicBuild.removeIndex(this,index__bytelogicBuild);
            }
        }

        @Override
        public void add(){
            boolean wasAdded = added;
            super.add();
            if(wasAdded != added){
                index__bytelogicBuild=BLGroups.byteLogicBuild.addIndex(this);
            }
        }

        @Override
        public void customWrite(Writes write){
            nextSignal.write(write);
            lastSignal.write(write);
        }

        @Override
        public void customRead(Reads read){
            nextSignal.read(read);
            lastSignal.read(read);
        }

        @Override
        public short customVersion(){
            return 2;
        }

        public boolean canOutputSignal(int dir){
            Building nearby = nearby(dir);
            if(!(nearby instanceof ByteLogicBuildingc)) return false;
            if(nearby.block.rotate && nearby.front() == this) return false;
            return (!LogicBlock.this.rotate || rotation == dir) && LogicBlock.this.doOutput;
        }
    }
}
