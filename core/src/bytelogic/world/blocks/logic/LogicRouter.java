package bytelogic.world.blocks.logic;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.annotations.BLAnnotations.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.guide.*;
import mindustry.annotations.Annotations.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mma.annotations.ModAnnotations.*;
import mma.type.*;
import mma.type.pixmap.*;

public class LogicRouter extends LogicBlock implements ImageGenerator{

    protected static final int[] sideMasks = {LogicRouterSides.bitMaskRight, LogicRouterSides.bitMaskTop, LogicRouterSides.bitMaskLeft, LogicRouterSides.bitMaskBottom};
    @ALoad(value = "@-center", fallback = {"@realName()-center", "@byteLogicBlocks.signalRouter.realName()-center"})
    public TextureRegion centerRegion;
    @ALoad(value = "@-rotate", fallback = {"@realName()-rotate", "@byteLogicBlocks.signalRouter.realName()-rotate"})
    public TextureRegion rotateRegion;

    public LogicRouter(String name){
        super(name);

        configurable = true;
        this.<Byte, LogicRouterBuild>config(Byte.class, (build, value) -> {
            build.isolatedSides = value;
        });
    }

    @Override
    public Pixmap generate(Pixmap icon, PixmapProcessor processor){
        icon = super.generate(icon, processor);


        Pixmap center = applyMask(centerRegion,processor);
        Pixmap rotate = applyMask(rotateRegion,processor);


        Pixmap base = processor.get(this.base);
        icon.draw(base);
        icon.draw(center, true);
        for(int i = 0; i < 4; i++){
            icon.draw(rotate, true);
            PixmapProcessor.rotatePixmap(rotate, 1);
        }
        return icon;
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{base};
    }

    @Override
    public void init(){
        if(blockPreview == null){
            blockPreview = new BlockPreview(this, 5, 5, (world, isSwitch) -> {
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
        protected byte isolatedSides = LogicRouterSides.get(false, false, false, false);

        @Override
        public void draw(){
            Draw.rect(base, tile.drawx(), tile.drawy());

            Draw.color(signalColor());
            Draw.rect(centerRegion, x, y);
            for(int i = 0; i < sideMasks.length; i++){
                if((isolatedSides & sideMasks[i]) != 0) continue;
                Draw.rect(rotateRegion, x, y, i * 90);
            }

            Draw.color();
            drawTeamTop();
        }
protected boolean ignoreSideChecking=false;
        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            if (!ignoreSideChecking){
                int sideIndex = relativeTo(otherBuilding.<Building>as());
                if((isolatedSides & sideMasks[sideIndex]) != 0) return false;
                sides[sideIndex] -= 1;
                if(signal.compareWithZero() != 0) sides[sideIndex] = 2;
//                sides[sideIndex]=Mathf.clamp(sides[sideIndex]+Mathf.sign(signal!=0),0,2);
                sides[sideIndex] = Mathf.clamp(sides[sideIndex], 0, 2);
            }
            ignoreSideChecking=false;
            return super.acceptSignal(otherBuilding, signal);
        }

        @Override
        public void nextBuildings(IntSeq positions){
            for(int i = 0; i < 4; i++){
                if(canOutputSignal(i)){
                    Tile nearby = tile.nearby(i);
                    if(nearby != null) positions.add(nearby.array());
                }
            }
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
        public void updateTableAlign(Table table){
            Vec2 pos = Core.input.mouseScreen(x, y);
//            table.setSize(size * Vars.tilesize * 3);
            table.setPosition(pos.x, pos.y, Align.center);
        }

        @Override
        public void buildConfiguration(Table table){
            table.add();
            float size = 48f;
//            Color disabledColor = Color.valueOf("f25555");
//            Color enabledColor = Color.lime;
            ImageButtonStyle buttonStyle = Styles.squareTogglei;

            table.button(Icon.up, buttonStyle, () -> {
                configure(LogicRouterSides.top(isolatedSides, !LogicRouterSides.top(isolatedSides)));
            }).size(size).checked((b) -> !LogicRouterSides.top(isolatedSides));//.checked(!LogicRouterSides.top(isolatedSides));
            table.add().row();
            table.button(Icon.left, buttonStyle, () -> {
                configure(LogicRouterSides.left(isolatedSides, !LogicRouterSides.left(isolatedSides)));
            }).size(size).checked((b) -> !LogicRouterSides.left(isolatedSides));//.checked(!LogicRouterSides.left(isolatedSides));
            table.add();
            table.button(Icon.right, buttonStyle, () -> {
                configure(LogicRouterSides.right(isolatedSides, !LogicRouterSides.right(isolatedSides)));
            }).size(size).checked((b) -> !LogicRouterSides.right(isolatedSides));//.checked(!LogicRouterSides.right(isolatedSides));
            table.row();
            table.add();
            table.button(Icon.down, buttonStyle, () -> {
                configure(LogicRouterSides.bottom(isolatedSides, !LogicRouterSides.bottom(isolatedSides)));
            }).size(size).checked((b) -> !LogicRouterSides.bottom(isolatedSides));//.checked(!LogicRouterSides.bottom(isolatedSides));
            table.add();
        }

        @Override
        public boolean canOutputSignal(int dir){
            return super.canOutputSignal(dir) && sides[dir] == 0 && (isolatedSides & (1 << dir)) == 0;
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
        public Object config(){
            return isolatedSides;
        }

        @Override
        public void customWrite(Writes write){
            for(int side : sides){
                write.i(side);
            }
            write.b(isolatedSides);
        }

        @Override
        public void customRead(Reads read){
            for(int i = 0; i < sides.length; i++){
                sides[i] = read.i();
            }
            isolatedSides = read.b();
        }

        @Override
        public short customVersion(){
            return 1;
        }
    }
}

@Struct
@RemoveFromCompilation
class LogicRouterSidesStruct{
    boolean right;
    boolean top;
    boolean left;
    boolean bottom;
}