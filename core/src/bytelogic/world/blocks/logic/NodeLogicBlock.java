package bytelogic.world.blocks.logic;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.guide.*;
import bytelogic.world.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.world.*;
import mma.type.pixmap.*;

import static mindustry.Vars.*;
import static mma.ModVars.fullName;

public class NodeLogicBlock extends LogicRouter{
    private static int lastPlaced = Pos.invalid;

    protected float range = 100f;

    public NodeLogicBlock(String name){
        super(name);
        configurable = true;
        rotate = false;
        doOutput = true;
        this.<Integer, NodeLogicBuild>config(Integer.class, (build, link) -> {
            build.link = link;
        });
        config(Point2.class, (NodeLogicBuild tile, Point2 point) -> {
            tile.link = Point2.pack(point.x + tile.tileX(), point.y + tile.tileY());
        });
        config(byte[].class, (NodeLogicBuild tile, byte[] bytes) -> {
            tile.isolatedSides = bytes[1];
            tile.link = Point2.pack(getInt(bytes, 2) + tile.tileX(), getInt(bytes, 2 + 4) + tile.tileY());
        });
    }

    private static int getInt(byte[] bytes, int offset){
        return bytes[offset] + bytes[offset + 1] << 8 + bytes[offset + 2] << 16 + bytes[offset + 3] << 24;
    }

    @Override
    public Pixmap generate(Pixmap icon, PixmapProcessor processor){

        Pixmap generated = super.generate(icon, processor);
        processor.save(generated, name);
        return generated;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Tile tile = world.tile(x, y);

        if(tile == null) return;

        Lines.stroke(1f);
        Drawf.circles(x * tilesize + offset, y * tilesize + offset, range, Color.white);

        Building last = world.build(lastPlaced);
        if(linkValid(tile, last)){
            Drawf.square(last.x, last.y, last.block().size * tilesize / 2f + 1f, Pal.place);
        }

        Draw.reset();
    }


    public boolean linkValid(NodeLogicBuild tile){
        return linkValid(tile, world.build(tile.link));
    }

    public boolean linkValid(Building tile, Building other){
        return linkValid(tile.tile, other);
    }

    public boolean linkValid(Tile tile, Building other){
        return other != null && other.block() instanceof NodeLogicBlock
                   && Mathf.within(tile.drawx(), tile.drawy(), other.x, other.y, range)
                   && (other.team == tile.team() || !(tile.build instanceof ByteLogicBuildingc)) && (other.<NodeLogicBuild>as().link != tile.pos());
    }

    @Override
    public void init(){
        clipSize = range;
        if(blockPreview == null){
            blockPreview = new BlockPreview(this, 5, 5, (world, isSwitch) -> {

                world.tile(0, 3).setBlock(inputBlock(isSwitch), Team.sharded, 1);

                world.tile(0, 4).setBlock(this, Team.sharded, 1);
                world.tile(0, 4).build.<NodeLogicBuild>as().link = world.tile(4, 0).pos();
                world.tile(4, 0).setBlock(this, Team.sharded, 1);

                world.tile(4, 1).setBlock(byteLogicBlocks.relay, Team.sharded, 1);
                world.tile(4, 2).setBlock(byteLogicBlocks.relay, Team.sharded, 2);
                world.tile(3, 2).setBlock(byteLogicBlocks.relay, Team.sharded, 1);
                world.tile(3, 3).setBlock(byteLogicBlocks.relay, Team.sharded, 2);
                world.tile(4, 3).setBlock(byteLogicBlocks.relay, Team.sharded, 1);
                world.tile(4, 4).setBlock(byteLogicBlocks.relay, Team.sharded, 2);
                return new Point2[]{Tmp.p1.set(0, 4), Tmp.p2.set(4, 0)};
            });
        }
        super.init();
    }

    public boolean overlaps(@Nullable Tile src, @Nullable Tile other){
        if(src == null || other == null) return true;
        return Intersector.overlaps(Tmp.cr1.set(src.worldx() + offset, src.worldy() + offset, range - tilesize), Tmp.r1.setSize(size * tilesize).setCenter(other.worldx() + offset, other.worldy() + offset));
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) -> overlaps(world.tile(point.x, point.y), world.tile(other.x, other.y)));
    }

    public class NodeLogicBuild extends LogicRouterBuild{
        public int link = Pos.invalid;

        @Override
        public void nextBuildings(IntSeq positions){
            if(!linkValid(this)){
                super.nextBuildings(positions);
                return;
            }
            positions.add(world.tile(link).array());
        }

        @Override
        public void draw(){
            super.draw();
            Draw.draw(Layer.power, () -> {

                Building link = world.build(this.link);
                if(linkValid(this, link)){
                    Draw.color(signalColor());
                    Draw.alpha(1f * Core.settings.getInt("lasersopacity") / 100f);
                    Drawf.laser(Core.atlas.find(fullName("logic-laser")), Core.atlas.find(fullName("logic-laser-end")), x, y, link.x, link.y, 0.25f);
                    Draw.reset();
                }
            });
        }

        @Override
        public boolean canOutputSignal(int dir){
            return super.canOutputSignal(dir) && !linkValid(this, world.build(link));
        }

        @Override
        public void beforeUpdateSignalState(){
            Building link = world.build(this.link);
            if(linkValid(this, link)){
                NodeLogicBuild other = link.as();
                other.acceptSignal(this, lastSignal);
                lastSignal.setZero();
            }else{
                for(int i = 0; i < 4 && doOutput; i++){
                    if(this.canOutputSignal(i)){
                        nearby(i).<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
                    }
                }
            }
        }

        @Override
        public void drawSelect(){
            if(!canDrawSelect()) return;

            Lines.stroke(1f);

            Drawf.circles(tile.drawx(), tile.drawy(), range, Color.white);
            Draw.reset();
        }

        @Override
        public void drawConfigure(){
            Draw.color(Color.white);

            Lines.stroke(1.5f);
            Drawf.square(x, y, block.size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f), Color.white);

            Drawf.circles(x, y, range, Color.white);

            Lines.stroke(1.5f);
            int radius = Mathf.round(range / tilesize);
            Tmp.p3.x = 0;
            Geometry.circle(tile.x, tile.y, radius, (x, y) -> {
                Building link = world.build(x, y);

                if(linkValid(this, link) && this.link == link.pos()){
                    Tmp.p3.x = 1;
                    Drawf.square(link.x, link.y, link.block().size * tilesize / 2f + 1f, Pal.place);
                }
            });
            if(Tmp.p3.x == 0){
                Tile link = world.tile(this.link);
                if(link != null) Drawf.square(link.drawx(), link.drawy(), link.block().size * tilesize / 2f + 1f, Pal.remove);
            }

            Draw.reset();
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(linkValid(this, other)){
                configure(this.link == other.pos() ? Pos.invalid : other.pos());
                return false;
            }

            return true;
        }

        @Override
        public void playerPlaced(Object config){

            Building build = world.build(lastPlaced);
            boolean hasConfig = false;
            if(config instanceof Point2 point){
                configure(point);
                hasConfig = true;
                lastPlaced = Pos.invalid;
            }else{
                lastPlaced = tile.pos();
            }
            if(!hasConfig && linkValid(this, build) && build instanceof NodeLogicBuild logicBuild && logicBuild.link == Pos.invalid){
                build.configure(tile.pos());
            }
        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            if(link == otherBuilding.pos()) return false;
            int i = relativeTo(otherBuilding.<Building>as());
            if(otherBuilding instanceof NodeLogicBuild b && b.link == pos()){
                ignoreSideChecking=true;
            }
            boolean acceptSignal = super.acceptSignal(otherBuilding, signal);
            if(otherBuilding instanceof NodeLogicBuild nodeLogicBuild && nodeLogicBuild.link == pos()){
                sides[i] = 0;
            }
            return acceptSignal;
        }

        @Override
        public byte[] config(){
            Point2 unpack = Point2.unpack(link);
            int x = unpack.x - tile.x;
            int y = unpack.y - tile.y;
            return new byte[]{
                0,
                isolatedSides,
                (byte)(x & 0xFF),
                (byte)(x >>> 8 & 0xFF),
                (byte)(x >>> 16 & 0xFF),
                (byte)(x >>> 24 & 0xFF),
                (byte)(y & 0xFF),
                (byte)(y >>> 8 & 0xFF),
                (byte)(y >>> 16 & 0xFF),
                (byte)(y >>> 24 & 0xFF),
            };
        }


        @Override
        public byte version(){
            return super.version();
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            revision = (byte)(revision / 0x10);
            if(revision == 2) return;
            link = read.i();
        }

        @Override
        public void customWrite(Writes write){
            write.i(link);
        }

        @Override
        public void customRead(Reads read){
            link = read.i();
        }

        @Override
        public short customVersion(){
            return 0;
        }
    }
}
