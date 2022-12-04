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
    private FrameBuffer shadowBuffer = new FrameBuffer(maxSchematicSize + padding + 8, maxSchematicSize + padding + 8);
    private FrameBuffer buffer = new FrameBuffer(1, 1);

    public WorldFragment(WorldLogicContext worldContext){
        ObjectMap<Object, Seq<Cons<?>>> events = Reflect.get(Events.class, "events");
        ObjectMap<Object, Seq<Cons<?>>> copy = events.copy();
        events.clear();
        blocks = new BlockRenderer();
        worldContext.inContext(() -> {
            Events.fire(new WorldLoadEvent());
        });

        events.clear();
        events.putAll(copy);


        this.worldContext = worldContext;
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

        Draw.color(Color.black);
        Fill.crect(x, y, width, height);
        drawWorldInBuffer();
        Tmp.tr1.set(buffer.getTexture());

        Draw.rect(Tmp.tr1, x + width / 2f, y + height / 2f, width, -height);

    }

    private void drawWorldInBuffer(){
        Draw.blend();
        Draw.reset();

        Tmp.m3.set(Core.camera.mat);
        Tmp.r1.set(Core.camera.position.x, Core.camera.position.y, Core.camera.width, Core.camera.height);
        Core.camera.resize(worldContext.world.unitWidth(), worldContext.world.unitHeight());
        Core.camera.position.set(Core.camera.width / 2f - tilesize / 2f, Core.camera.height / 2f - tilesize / 2f);
        Core.camera.update();


        Tmp.m1.set(Draw.proj());
        Tmp.m2.set(Draw.trans());
        buffer.resize(worldContext.world.width() * resolution, worldContext.world.height() * resolution);
        buffer.begin(Color.clear);

        worldContext.inContext(() -> {


//            oldDraw();
            blocks.checkChanges();
            blocks.floor.checkChanges();
            blocks.processBlocks();

            Draw.proj().setOrtho(-tilesize * .5f, -tilesize * .5f, worldContext.world.unitWidth(), worldContext.world.unitHeight());
            Draw.flush();
            //scale each plan to fit world
//            Draw.trans().scale(resolution / tilesize, resolution / tilesize).translate(tilesize * 1.5f, tilesize * 1.5f);
            blocks.floor.drawFloor();
            Draw.color(0f, 0f, 0f, 1f);
            blocks.drawShadows();
            Draw.color();
            blocks.drawBlocks();

            worldUpdateListeners.each(Runnable::run);
            //draw blocks
            /*worldContext.world.tiles.eachTile(tile -> {
                tile.block().drawBase(tile);
            });*/
        });


//        plans.each(req -> req.block.drawPlanConfigTop(req, plans));

        Draw.flush();
        Draw.trans().idt();

        buffer.end();

        Draw.proj(Tmp.m1);
        Draw.trans(Tmp.m2);

        Core.camera.resize(Tmp.r1.width, Tmp.r1.height);
        Core.camera.position.set(Tmp.r1.x, Tmp.r1.y);
        Core.camera.update();

    }

    @Override
    public void act(float delta){
        super.act(delta);

        worldContext.inContext(() -> {
            Groups.update();
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
