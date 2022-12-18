package bytelogic.ui.fragments;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.struct.*;
import arc.util.*;
import bytelogic.tools.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class WorldFragment extends Element{
    private static final int resolution = 32;
    private static final int padding = 2;
    public final WorldLogicContext worldContext;
    private final BlockRenderer blocks;
    private final Seq<Runnable> worldUpdateListeners = new Seq<>();
    private final Seq<Runnable> worldDrawListeners = new Seq<>();
    private final SortedSpriteBatch sortedSpriteBatch = new SortedSpriteBatch();
    private FrameBuffer shadowBuffer = new FrameBuffer(maxSchematicSize + padding + 8, maxSchematicSize + padding + 8);
    private FrameBuffer buffer = new FrameBuffer(1, 1);
    private boolean shouldInvalidateFloor;

    public WorldFragment(WorldLogicContext worldContext){
        ObjectMap<Object, Seq<Cons<?>>> events = Reflect.get(Events.class, "events");
        ObjectMap<Object, Seq<Cons<?>>> copy = events.copy();
        events.clear();
        blocks = new BlockRenderer();
        worldContext.inContext(() -> {
            Events.fire(new WorldLoadEvent());
        });
        invalidateFloor();
        events.clear();
        events.putAll(copy);


        this.worldContext = worldContext;
    }

    public void invalidateFloor(){
        shouldInvalidateFloor = true;
    }

    public Vec2 worldUnitToElement(Vec2 position){
        position.scl(width / worldContext.world.unitWidth());
        return position;
    }

    public Vec2 worldToElement(int x, int y, Vec2 output){
        output.set(
            x * width / worldContext.world.width(),
            y * height / worldContext.world.height()
        );
        return output;
//        position.scl(width / worldContext.world.unitWidth());
    }

    public Vec2 elementToWorldUnit(Vec2 position){
        position.scl(worldContext.world.unitWidth() / width);
        return position;
    }

    public Point2 elementToWorld(Vec2 position, Point2 output){
        output.set(
            (int)(position.x * worldContext.world.width() / width),
            (int)(position.y * worldContext.world.height() / height)
        );
        return output;
//        position.scl(width / worldContext.world.unitWidth());
    }

    public float localTileSize(){
        return width / worldContext.world.width();
    }

    @Override
    public void draw(){

//        Draw.color(Color.green);
//        Fill.crect(x, y, width, height);
        Core.app.post(this::drawWorldInBuffer);

        Tmp.tr1.set(buffer.getTexture());
        Draw.color();
        Draw.rect(Tmp.tr1, x + width / 2f, y + height / 2f, width, -height);

    }

    private void drawWorldInBuffer(){
        Batch previousBatch = Core.batch;
        Core.batch = sortedSpriteBatch;

//        Draw.blend();
//        Draw.reset();

//        Tmp.m3.set(Core.camera.mat);000001111100010010110000
        Tmp.r1.set(Core.camera.position.x, Core.camera.position.y, Core.camera.width, Core.camera.height);
        Tmp.m1.set(Draw.proj());
        Tmp.m2.set(Draw.trans());
//        Draw.trans().idt();


        Draw.flush();
        worldContext.inContext(() -> {

            Core.camera.resize(world.unitWidth(), world.unitHeight());
            Core.camera.position.set(Core.camera.width / 2f - tilesize / 2f, Core.camera.height / 2f - tilesize / 2f);
            Core.camera.update();

            buffer.resize(world.width() * resolution, world.height() * resolution);
            buffer.begin();

            Draw.proj(Core.camera);

//            oldDraw();
            blocks.checkChanges();
            blocks.floor.checkChanges();
            blocks.processBlocks();

//            Draw.proj().setOrtho(-tilesize * .5f, -tilesize * .5f, world.unitWidth(), world.unitHeight());
            Draw.sort(true);
            //scale each plan to fit world
//            Draw.trans().scale(resolution / tilesize, resolution / tilesize).translate(tilesize * 1.5f, tilesize * 1.5f);

            Draw.draw(Layer.floor, blocks.floor::drawFloor);
            Draw.draw(Layer.block - 1, blocks::drawShadows);
            Draw.flush();
            blocks.drawBlocks();

            worldDrawListeners.each(Runnable::run);
            //draw blocks
            /*worldContext.world.tiles.eachTile(tile -> {
                tile.block().drawBase(tile);
            });*/
        });


//        plans.each(req -> req.block.drawPlanConfigTop(req, plans));

        Draw.reset();
        Draw.flush();
        Draw.sort(false);
        Draw.trans().idt();

        buffer.end();
        Draw.proj(Tmp.m1);
        Draw.trans(Tmp.m2);

        Core.camera.resize(Tmp.r1.width, Tmp.r1.height);
        Core.camera.position.set(Tmp.r1.x, Tmp.r1.y);
        Core.camera.update();
        Core.batch = previousBatch;
    }

    @Override
    public void act(float delta){
        super.act(delta);

        worldContext.inContext(() -> {
            Groups.update();
            if(shouldInvalidateFloor){
                shouldInvalidateFloor = false;
                world.tiles.eachTile(blocks.floor::recacheTile);
            }
            worldUpdateListeners.each(Runnable::run);
        });
    }

    public void onWorldUpdate(Runnable runnable){
        worldUpdateListeners.add(runnable);
    }

    public void onWorldDraw(Runnable runnable){
        worldDrawListeners.add(runnable);
    }
}
