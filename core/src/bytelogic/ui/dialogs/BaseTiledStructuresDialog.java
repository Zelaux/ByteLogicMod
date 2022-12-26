package bytelogic.ui.dialogs;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Log.*;
import bytelogic.type.*;
import kotlin.jvm.internal.Ref.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.ui.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.*;
import mma.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.*;
import zelaux.arclib.ui.components.*;
import zelaux.arclib.ui.utils.*;

import java.lang.annotation.*;
import java.util.*;

import static mindustry.Vars.mobile;

public class BaseTiledStructuresDialog<T extends TiledStructure<?>&TiledStructureWithGroup> extends TiledStructuresDialog{


    final Seq<Prov<T>> allGates;
    public Element selectionTable = new Element(){
        boolean selecting;

        float prevX, prevY;

        {
            fillParent = true;
            addListener(new InputListener(){


                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    localToStageCoordinates(Tmp.v1.set(x, y));
                    prevX = Tmp.v1.x;
                    prevY = Tmp.v1.y;
                    selecting = true;
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                    localToStageCoordinates(Tmp.v1.set(x, y));
                    canvas.tilemap.stageToLocalCoordinates(Tmp.v1);

                    int x1 = canvas.roundCords(Tmp.v1.x / TiledStructuresCanvas.unitSize);
                    int y1 = canvas.roundCords(Tmp.v1.y / TiledStructuresCanvas.unitSize);

                    Tmp.v1.set(prevX, prevY);
                    canvas.tilemap.stageToLocalCoordinates(Tmp.v1);

                    int x0 = canvas.roundCords(Tmp.v1.x / TiledStructuresCanvas.unitSize);
                    int y0 = canvas.roundCords(Tmp.v1.y / TiledStructuresCanvas.unitSize);
                    remove();
                    setSelection(x0, y0, x1, y1);
                    selecting = false;
                }
            });
            hidden(() -> remove());
        }

        @Override
        public void act(float delta){
            setWidth(parent.getWidth());
            setHeight(parent.getHeight());
            super.act(delta);
        }

        @Override
        public void draw(){
            if(selecting){
                Vec2 mouse = stageToLocalCoordinates(Core.scene.screenToStageCoordinates(Core.input.mouse()));
                Vec2 startPosition = stageToLocalCoordinates(Tmp.v1.set(prevX, prevY));

                float x = Math.min(mouse.x, startPosition.x);
                float y = Math.min(mouse.y, startPosition.y);
                float width = Math.abs(mouse.x - startPosition.x);
                float height = Math.abs(mouse.y - startPosition.y);
                Draw.color(Pal.accent, 0.5f);
                Fill.crect(x, y, width, height);
                Draw.color();
            }
        }
    };

    public BaseTiledStructuresDialog(String title, Class<T> initClass, Seq<Prov<T>> allGates){
        super(title, initClass);
        settings.updateStructuresAfterConfig = false;
        this.allGates = allGates;
        Objects.requireNonNull(allGates);
        setupUI(initClass);
    }

    private void setSelection(int x0, int y0, int x1, int y1){
        this.canvas.setSelection(Math.min(x0, x1), Math.min(y0, y1), Math.abs(x0 - x1), Math.abs(y0 - y1));
    }

    @Override
    protected void setupUI(Class<? extends TiledStructure> initClass){
        if(allGates == null) return;
        TiledStructures tmpStructures = new TiledStructures(allGates.as());
        clear();
        margin(0f);
//        WidgetGroup canvasGroup;
        Image backgroundImage = new Image(Styles.black5);
        add(new Stack(
                backgroundImage,
//            canvasGroup = new WidgetGroup(canvas = new TiledStructuresCanvas(this)),
                canvas = new TiledStructuresCanvas(this),
                new Table(){
                    {
                        canvas.update(() -> {
                            canvas.originX = canvas.getWidth() / 2f;
                            canvas.originY = canvas.getHeight() / 2f;
                        });
                        canvas.setTransform(true);
                        buttons.defaults().size(160f, 64f).pad(2f);
                        buttons.button("@back", Icon.left, () -> hide());
//                buttons.button("@add", Icon.add, () -> getProvider(initClass).get(new TypeInfo(initClass), canvas::query));

                        if(mobile){
                            buttons.button("@cancel", Icon.cancel, canvas::stopQuery).disabled(b -> !canvas.isQuerying());
                            buttons.button("@ok", Icon.ok, canvas::placeQuery).disabled(b -> !canvas.isQuerying());
                        }

                        setFillParent(true);
                        margin(3f);

                        add(titleTable).growX().fillY();
                        row().table(contentTable -> {
                            BooleanRef collaped = new BooleanRef();
                            HorizontalCollapser collapser = new HorizontalCollapser(t -> {
                                t.pane(p -> buildGateSelection(p)).scrollX(false).left();
                            }, false);
                            contentTable.add(collapser);
                            contentTable.button(Icon.list, () -> collaped.element = !collaped.element).padTop(30f).size(48f).left().top();
                            collapser.setCollapsed(true, () -> collaped.element);

                            contentTable.add().grow();
                            contentTable.table(buttons -> {
                                buttons.background(Tex.pane);

                                setupSideButtons(buttons, tmpStructures);
                                buttons.top();
                            }).top();
                        }).grow();
                        row().add(buttons).fill();
                        addCloseListener();
                    }

                }
            ){
                @Override
                public Element hit(float x, float y, boolean touchable){
                    Element hit = super.hit(x, y, touchable);
                    if(hit == backgroundImage) return canvas;
                    return hit;
                }
            }
        ).grow().pad(0f).margin(0f);
    }

    private void setupSideButtons(Table buttons, TiledStructures tmpStructures){
        buttons.defaults().size(48f);

        buttons.button("+", () -> {
            float scale = (1f / 4) + 1;
            canvas.scaleX *= scale;
            canvas.scaleY *= scale;
        });
        buttons.button("-", () -> {
            float scale = (-1f / 4) + 1;
            canvas.scaleX *= scale;
            canvas.scaleY *= scale;
        });
        buttons.row();

        buttons.button(Icon.file, Styles.squareTogglei, () -> {
            if(!selectionTable.remove()){
                Core.scene.add(selectionTable);
            }
        }).checked(it -> selectionTable.parent != null);
        buttons.row();
        buttons.button(Icon.copy, Styles.squarei, () -> {
            for(StructureTile tile : canvas.selection.list()){
                tmpStructures.all.add(tile.obj);
            }
            LogLevel level = Log.level;
            Log.level = LogLevel.none;
            JsonIO.read(TiledStructures.class, tmpStructures, JsonIO.write(tmpStructures));
            Log.level = level;
            canvas.stopQuery();
            for(TiledStructure tiledStructure : tmpStructures.all){
                canvas.getQuery().list().add(tiledStructure);
                canvas.queryTilemap.createTile(tiledStructure);
            }
            canvas.getQuery().calculateSize();
            tmpStructures.clear();
            prevPair.element=null;
        }).disabled(it -> canvas.selection.isEmpty());
        buttons.button(Icon.trash, Styles.squarei, () -> {
            for(StructureTile tile : canvas.selection.list()){
                tile.remove();
            }
            canvas.selection.clear();
            canvas.updateStructures();
        }).disabled(it -> canvas.selection.isEmpty());
        buttons.row();

        buttons.button(Icon.save, Styles.squarei, () -> {

            for(StructureTile tile : canvas.selection.list()){
                tmpStructures.all.add(tile.obj);
            }
        }).disabled(it -> canvas.selection.isEmpty());
    }

    ObjectRef<GatePair<T>> prevPair = new ObjectRef<>();
    private void buildGateSelection(Table p){
        p.background(Tex.button);
        p.marginRight(14f);

        ObjectMap<GroupOfTiledStructure, Seq<GatePair<T>>> keyMap = new ObjectMap<>();
        for(Prov<T> gate : allGates){
            T logicGate = gate.get();
            keyMap.get(logicGate.group(), Seq::new).add(new GatePair<>(logicGate, gate));
        }
        Seq<GroupOfTiledStructure> groups = keyMap.keys().toSeq().sort();

        for(GroupOfTiledStructure group : groups){
            Seq<GatePair<T>> gates = keyMap.get(group);
            p.table(title -> {
                Separators.horizontalSeparator(title, Pal.accent);
                title.add(group.localized());
                Separators.horizontalSeparator(title, Pal.accent);
            }).growX().row();
            p.table(items -> {
                items.defaults().size(195f, 56f);
                int i = 0;
                for(GatePair<T> pair : gates){

                    TiledStructureGroup query = canvas.getQuery();
//                    Seq<TiledStructure<?>> list = query.list();
                    items.button(pair.structure.typeName(), Styles.flatTogglet, () -> {
                            if(query.any() && prevPair.element == pair){
                                T obj = pair.structureProvider.get();
                                if(obj instanceof ConfigGroupStructure groupStructure){
                                    groupStructure.updateConfig(query.list().size);
                                }
                                canvas.addQuery(obj);
                                int index = query.list().size - 1;
                                for(TiledStructure<?> structure : query.list()){
                                    if(structure instanceof ConfigGroupStructure groupStructure){
                                        groupStructure.updateConfig(index);
                                    }
                                    index--;
                                }
                            }else{
                                T obj = pair.structureProvider.get();
                                if(obj instanceof ConfigGroupStructure groupStructure){
                                    groupStructure.updateConfig(0);
                                }
                                canvas.beginQuery(obj);
                            }
                            prevPair.element = pair;
                        }).checked(it -> prevPair.element == pair && query.any())
                        .with(Table::left).get().getLabelCell().growX().left().padLeft(5f).labelAlign(Align.left);

                    if(++i % 2 == 0) items.row();
                }
            }).fillX();
            p.row();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface DoNotAddRow{

    }

    static class GatePair<T extends TiledStructure<?>>{
        public final Prov<T> structureProvider;
        public final T structure;

        public GatePair(T structure, Prov<T> structureProvider){
            this.structureProvider = structureProvider;
            this.structure = structure;
        }
    }
}
