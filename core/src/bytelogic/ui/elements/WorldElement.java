package bytelogic.ui.elements;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import bytelogic.gen.*;
import bytelogic.tools.*;
import bytelogic.ui.fragments.*;
import kotlin.*;
import kotlin.jvm.functions.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import org.jetbrains.annotations.*;

public class WorldElement extends Table{
    public final WorldLogicContext context;
    public final Seq<TileSelection> tileSelections = new Seq<>();
    public final WorldFragment worldFragment;
    private final Seq<Function0<Unit>> updateWaiters = new Seq<>();
    public Tile selectedTile = null;
    public float selectionThickness = 4f;
    public Cons<Tile> tileClickListener = it -> {
    };
    BorderImage selectionImage = new BorderImage(){{
        borderColor = new Color();
    }};

    public WorldElement(WorldLogicContext context){
        this(context, 32);
    }

    public WorldElement(WorldLogicContext context, float tileScale){
        this.context = context;

        worldFragment = new WorldFragment(context);
        worldFragment.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                worldFragment.elementToWorld(Tmp.v1.set(x, y), Tmp.p1);
                Tile tile = context.world.tile(Tmp.p1.x, Tmp.p1.y);
                if(tile == null) return super.touchDown(event, x, y, pointer, button);
                TileSelection selection = tileSelections.find(it -> tile.x == (short)it.x && tile.y == (short)it.y);
                if(selection == null || selection.clickListener == null || !selection.enabled){
                    tileClickListener.get(tile);
                    return true;
                }
                selection.clickListener.run();
               /* if(selectedTile == tile){
                    selectedTile = null;
                }else{
                    selectedTile = tile;
                }*/
                return true;
//                return super.touchDown(event, x, y, pointer, button);
            }
        });
        worldFragment.onWorldUpdate(() -> {
            for(ByteLogicBuildingc build : BLGroups.byteLogicBuild){
                build.beforeUpdateSignalState();
            }
            for(ByteLogicBuildingc build : BLGroups.byteLogicBuild){
                build.updateSignalState();
            }

            updateWaiters.each(Function0::invoke);
            updateWaiters.clear();
        });
        stack(worldFragment, new Table(it -> {
            it.touchable = Touchable.disabled;
            it.fill((x, y, width, height) -> {
                for(TileSelection selection : tileSelections){
                    if(!selection.enabled) continue;
                    float dx = selection.x * width / context.world.width() + x;
                    float dy = selection.y * height / context.world.height() + y;
                    float rectSize = worldFragment.localTileSize();
                    float stroke = selectionThickness;
                    float hs = stroke / 2f;

                    Draw.color(selection.color);
                    Lines.stroke(stroke);
                    Lines.rect(dx, dy, rectSize, rectSize, hs, hs);

                }
            });

            it.add(selectionImage).visible(() -> selectedTile != null).update(image -> {

                image.setSize(worldFragment.localTileSize() + selectionThickness * 2f);
                image.thickness = selectionThickness;
                image.borderColor.set(Pal.accent).a *= Mathf.num(selectedTile != null);
                image.color.set(0x334FFF00);
                if(selectedTile == null) return;
                worldFragment.worldToElement(selectedTile.x, selectedTile.y, Tmp.v1);
                worldFragment.localToStageCoordinates(Tmp.v1)
                    .sub(selectionThickness, selectionThickness);
                it.stageToLocalCoordinates(Tmp.v1);

                image.setPosition(Tmp.v1.x, Tmp.v1.y, Align.bottomLeft);
            });
        })).size(tileScale * context.world.width(), tileScale * context.world.height());
    }

    @Override
    public void act(float delta){
        super.act(delta);
    }

    public void onNextUpdate(@NotNull Function0<Unit> function){
        updateWaiters.add(function);
    }

    public static class TileSelection{
        public int x, y;
        public Runnable clickListener;
        public boolean enabled = true;
        public Color color = new Color(0xFFFFFFFF);

        public TileSelection(int x, int y, Color color){
            this.x = x;
            this.y = y;
            this.color.set(color);
        }

        public TileSelection(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
}
