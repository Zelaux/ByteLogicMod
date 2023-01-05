package bytelogic.ui.dialogs;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.*;
import arc.struct.*;
import arc.util.*;
import bytelogic.*;
import bytelogic.schematics.*;
import bytelogic.type.*;
import bytelogic.type.ConnectionSettings.*;
import bytelogic.type.ConnectionSettings.WireDescriptor.*;
import kotlin.jvm.internal.Ref.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.*;
import mma.ui.tiledStructures.TiledStructuresCanvas.*;
import mma.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.*;
import mma.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.StructureTile.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.*;
import zelaux.arclib.ui.listeners.*;
import zelaux.arclib.ui.tooltips.*;
import zelaux.arclib.ui.utils.*;

import static mindustry.Vars.mobile;
import static mma.ui.tiledStructures.TiledStructuresCanvas.*;

public class ByteLogicSchematicEditDialog extends TiledStructuresDialog{

    public final Seq<TiledStructure<?>> structures;
    private final ConnectingState connecting = new ConnectingState();
    private final Seq<MockConnector> mockInputs = new Seq<>();
    private final Seq<MockConnector> mockOutputs = new Seq<>();
    @Nullable
    public ByteLogicSchematicsDialog schematicsDialog;
    LongMap<TiledStructure<?>> structureMap = new LongMap<>();
    private ByteLogicSchematic schematic;

    public ByteLogicSchematicEditDialog(Seq<TiledStructure<?>> structures, ByteLogicGateProvider provider){
        this(createSchematic(structures, provider), null);
    }

    public ByteLogicSchematicEditDialog(ByteLogicSchematic schematic, @Nullable ByteLogicSchematicsDialog dialog){
        super("byte-logic-schematic-save", null);
        this.schematic = schematic;
        this.schematicsDialog = dialog;
        for(TiledStructure structure : schematic.structures){
            structureMap.put(Pack.longInt(structure.editorX, structure.editorY), structure);
        }
        this.structures = schematic.structures.as();
        addCloseListener();
        setupUI(null);
    }

    @NotNull
    private static ByteLogicSchematic createSchematic(Seq<TiledStructure<?>> structures, ByteLogicGateProvider provider){
        TiledStructureGroup tmpGroup = new TiledStructureGroup();
        tmpGroup.list().addAll(structures);
        tmpGroup.calculateSize();
        return new ByteLogicSchematic(structures.as(), new StringMap(), tmpGroup.width(), tmpGroup.height(), provider);
    }

    public static void clearTiles(Seq<StructureTile> tiles){
        clearTiles(tiles, null, false);
    }

    private static void clearTiles(Seq<StructureTile> tiles, @Nullable ByteLogicSchematicEditDialog self, boolean enableConnectors){
        for(StructureTile tile : tiles){
            Table mainTable = tile.find("center-table");
            mainTable.removeCaptureListener(tile.mover);
            mainTable.find("remove-button").remove();
            Element editButton = mainTable.find("edit-button");
            if(editButton != null) editButton.remove();


            for(Connector connector : Seq.with(tile.conChildren).addAll(tile.conParent)){
                connector.getCaptureListeners().pop();
                if(enableConnectors){
                    connector.addCaptureListener(new ConnectorCaptureListener(self, connector));
                    UIUtils.replaceClickListener(connector, new ButtonClickListener(connector){
                        Connector connecting(){
                            if(self == null) return null;
                            return Reflect.get(StructureTilemap.class, self.canvas.tilemap, "connecting");
                        }

                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                            if(connecting() == null){
                                return super.touchDown(event, x, y, pointer, button);
                            }else{
                                return false;
                            }
                        }

                        @Override
                        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                            if(connecting() == null){
                                super.touchUp(event, x, y, pointer, button);
                            }
                        }

                        @Override
                        public void touchDragged(InputEvent event, float x, float y, int pointer){
                            if(connecting() == null) super.touchDragged(event, x, y, pointer);
                        }

                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
                            if(connecting() == null) super.enter(event, x, y, pointer, fromActor);
                        }
                    });
                }else{
                    UIUtils.replaceClickListener(connector, new ButtonClickListener(connector){

                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                            return false;
                        }

                        @Override
                        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                        }

                        @Override
                        public void touchDragged(InputEvent event, float x, float y, int pointer){
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void setupUI(Class<? extends TiledStructure> initClass){
        if(structures == null) return;
        cont.pane(this::setup).expand();
    }

    private void setup(Table cont){
        initCanvas();
        for(TiledStructure<?> structure : structures){

            canvas.tilemap.createTile(structure);
        }

        Seq<StructureTile> tiles = canvas.tilemap.getChildren().as();


        clearTiles(tiles, this, true);
        canvas.setTransform(true);

        float size = 10 * unitSize / Scl.scl();
        float scaleX = Math.min(1, size / (schematic.width * unitSize / Scl.scl()));
        float scaleY = Math.min(1, size / (schematic.height * unitSize / Scl.scl()));
        canvas.setScale(Math.min(scaleX, scaleY));
        canvas.update(() -> {
            canvas.originX = canvas.getWidth() / 2f;
            canvas.originY = canvas.getHeight() / 2f;
        });
        Seq<Runnable> connectorsReset = new Seq<>();
        setFillParent(true);

        cont.margin(30);

        cont.add("@schematic.tags").padRight(6f);
        cont.table(tags -> buildTags(schematic, tags, false)).maxWidth(400f).fillX().left().row();

        cont.margin(30).add("@name").padRight(6f);
        TextField nameField = cont.field(schematic.name(), null).size(400f, 55f).left().get();

        cont.row();

        cont.margin(30).add("@editor.description").padRight(6f);
        TextField descField = cont.area(schematic.description(), Styles.areaField, t -> {
        }).size(400f, 140f).left().get();

        Runnable accept = () -> {
            schematic.tags.put("name", nameField.getText());
            schematic.tags.put("description", descField.getText());
            saveSchem();
            hide();
        };

        buttons.defaults().size(120, 54).pad(4);

        keyDown(KeyCode.enter, () -> {
            if(!nameField.getText().isEmpty() && Core.scene.getKeyboardFocus() != descField){
                accept.run();
            }
        });
        keyDown(KeyCode.escape, this::hide);
        keyDown(KeyCode.back, this::hide);

        cont.row();
        cont.table(contInner -> {
            contInner.table(inputConnections -> {
                connectorsReset.add(connectionsBuilder(inputConnections, schematic.connectionSettings.inputWires, mockInputs, true));
            }).height(size);
            contInner.add(new Table(it -> it.add(canvas)){

                @Override
                public void draw(){
                    clipBegin(x, y, width, height);
                    setClip(true);
                    super.draw();
                    clipEnd();
                }

                @Override
                public Element hit(float x, float y, boolean touchable){
                    Element hit = super.hit(x, y, touchable);
                    if(!(hit instanceof Connector) && hit != null) return canvas;
                    return hit;
                }
            }).touchable(Touchable.enabled).size(size);

            contInner.table(outputConnections -> {
                connectorsReset.add(connectionsBuilder(outputConnections, schematic.connectionSettings.outputWires, mockOutputs, false));
            }).height(size);
            connectorsReset.each(Runnable::run);
            canvas.tilemap.setPosition(-schematic.width * unitSize + 5 * unitSize, -schematic.height * unitSize + 5 * unitSize);

            contInner.row();
            contInner.button("@add", () -> {
                schematic.connectionSettings.inputWires.add(new WireDescriptor());
                connectorsReset.get(0).run();
            });
            contInner.add();
            contInner.button("@add", () -> {
                schematic.connectionSettings.outputWires.add(new WireDescriptor());
                connectorsReset.get(1).run();
            });
            contInner.row();
        }).colspan(2);

//        addCloseButton();
        buttons.button("@cancel", this::hide);
        buttons.button("@save", accept).disabled(b -> nameField.getText().isEmpty());
    }

    private void initCanvas(){
        canvas = new ByteLogicGateCanvas(this){{
            getCaptureListeners().pop();
            addCaptureListener(new ElementGestureListener(){
                int pressPointer = -1;

                @Override
                public void pan(InputEvent event, float x, float y, float deltaX, float deltaY){
//                    if(tilemap.moving.any() || tilemap.connecting != null) return;
                    if(connecting.any()) return;
                    tilemap.x = Mathf.clamp(tilemap.x + deltaX, -bounds * unitSize + width, bounds * unitSize);
                    tilemap.y = Mathf.clamp(tilemap.y + deltaY, -bounds * unitSize + height, bounds * unitSize);
                }

                @Override
                public void tap(InputEvent event, float x, float y, int count, KeyCode button){
                    if(query.isEmpty()) return;

                    Vec2 pos = localToDescendantCoordinates(tilemap, Tmp.v1.set(x, y));
                    query.setPosition(
                        queryX(pos), queryY(pos)
                    );
//                queryX = queryX(pos);
                    //noinspection IntegerDivisionInFloatingPointContext
//                queryY = queryY(pos);

                    // In mobile, placing the query is done in a separate button.
                    if(!mobile) placeQuery();
                }

                @Override
                public void touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(pressPointer != -1) return;
                    pressPointer = pointer;
                    setPressed(true);
                    setVisualPressed(Time.millis() + 100);
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(pointer == pressPointer){
                        pressPointer = -1;
                        setPressed(false);
                    }
                }
            });
        }};

    }

    private void buildTags(ByteLogicSchematic schem, Table t, boolean name){
        final float tagh = 42f;
        t.clearChildren();
        t.left();
        Seq<String> tags = schematicsDialog == null ? Core.settings.getJson("schematic-tags", Seq.class, String.class, Seq::new) : schematicsDialog.tags;

        //sort by order in the main target array. the complexity of this is probably awful
        schem.labels.sort(s -> tags.indexOf(s));

        if(name) t.add("@schematic.tags").padRight(4);
        t.pane(s -> {
            s.left();
            s.defaults().pad(3).height(tagh);
            for(var tag : schem.labels){
                s.table(Tex.button, i -> {
                    i.add(tag).padRight(4).height(tagh).labelAlign(Align.center);
                    i.button(Icon.cancelSmall, Styles.emptyi, () -> {
                        removeTag(schem, tag);
                        buildTags(schem, t, name);
                    }).size(tagh).padRight(-9f).padLeft(-9f);
                });
            }

        }).fillX().left().height(tagh).scrollY(false);

        t.button(Icon.addSmall, () -> {
            var dialog = new BaseDialog("@schematic.addtag");
            dialog.addCloseButton();
            dialog.cont.pane(p -> resized(true, () -> {
                p.clearChildren();

                float sum = 0f;
                Table current = new Table().left();
                for(var tag : tags){
                    if(schem.labels.contains(tag)) continue;

                    var next = Elem.newButton(tag, () -> {
                        addTag(schem, tag);
                        buildTags(schem, t, name);
                        dialog.hide();
                    });
                    next.getLabel().setWrap(false);

                    next.pack();
                    float w = next.getPrefWidth() + Scl.scl(6f);

                    if(w + sum >= Core.graphics.getWidth() * (Core.graphics.isPortrait() ? 1f : 0.8f)){
                        p.add(current).row();
                        current = new Table();
                        current.left();
                        current.add(next).height(tagh).pad(2);
                        sum = 0;
                    }else{
                        current.add(next).height(tagh).pad(2);
                    }

                    sum += w;
                }

                if(sum > 0){
                    p.add(current).row();
                }

                Cons<String> handleTag = res -> {
                    dialog.hide();
                    addTag(schem, res);
                    buildTags(schem, t, name);
                };

                p.row();

                p.table(v -> {
                    v.left().defaults().fillX().height(tagh).pad(2);
                    v.button("@schematic.texttag", Icon.add, () -> schematicsDialog.showNewTag(handleTag)).wrapLabel(false).get().getLabelCell().padLeft(4);
                    v.button("@schematic.icontag", Icon.add, () -> schematicsDialog.showNewIconTag(handleTag)).wrapLabel(false).get().getLabelCell().padLeft(4);
                });
            }));
            dialog.show();
        }).size(tagh).tooltip("@schematic.addtag");
    }

    void addTag(ByteLogicSchematic s, String tag){
        s.labels.add(tag);
        s.save();
        if(schematicsDialog != null) schematicsDialog.tagsChanged();
    }

    void removeTag(ByteLogicSchematic s, String tag){
        s.labels.remove(tag);
        s.save();
        if(schematicsDialog != null) schematicsDialog.tagsChanged();
    }

    private void saveSchem(){
        if(schematic.file == null){
            BLVars.byteLogicSchematics.add(schematic);
        }else{
            schematic.save();
        }
        if(schematicsDialog != null){
            schematicsDialog.rebuild();
        }
    }

    @Override
    public void draw(){
        super.draw();
        canvas.localToAscendantCoordinates(this, Tmp.v1.setZero());
        canvas.localToAscendantCoordinates(this, Tmp.v2.set(canvas.getWidth(), canvas.getHeight()));
        drawWires();
        /*if(clipBegin(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x - Tmp.v1.x, Tmp.v2.y - Tmp.v1.y)){

            clipEnd();
        }*/
    }

    @NotNull
    private Runnable connectionsBuilder(Table table, Seq<WireDescriptor> wires, Seq<MockConnector> collectors, boolean isInput){
        ObjectRef<Runnable> runnable = new ObjectRef<>();
        runnable.element = () -> {
            table.defaults().growY();
            table.clearChildren();
            collectors.clear();
            for(int i = 0; i < wires.size; i++){
                WireDescriptor wireDescriptor = wires.get(i);

                MockConnector element = new MockConnector(!isInput, i, ConnectorStyle.defaultStyle());

                collectors.add(element);
                ObjectRef<String> tooltipText = new ObjectRef<>();
                if(isInput){

                    table.button(Icon.editSmall, Styles.cleari, () -> WireDescriptorEditDialog.showDialog(wireDescriptor));
                    table.button(Icon.trashSmall, Styles.cleari, () -> {
                        wires.remove(wireDescriptor);
                        runnable.element.run();
                    });
                    SideTooltips.mutableSideTooltip(table, Align.topRight, element, tooltipText);
                }else{
                    SideTooltips.mutableSideTooltip(table, Align.topRight, element, tooltipText);
                    table.button(Icon.editSmall, Styles.cleari, () -> WireDescriptorEditDialog.showDialog(wireDescriptor));
                    table.button(Icon.trashSmall, Styles.cleari, () -> {
                        wires.remove(wireDescriptor);
                        runnable.element.run();
                    });
                }
                element.update(() -> {
                    tooltipText.element = wireDescriptor.name;
                });
                table.row();
            }
        };
        return runnable.element;
    }

    private void drawWires(){
        Seq<StructureTile> tiles = canvas.tilemap.getChildren().as();
        for(int i = 0; i < mockInputs.size; i++){
            MockConnector conFrom = mockInputs.get(i);
            WireDescriptor wireDescriptor = schematic.connectionSettings.inputWires.get(conFrom.id);
            for(StructureDescriptor structureDescriptor : wireDescriptor.connectedStructures){
                StructureTile structureTile = tiles.find(it -> it.tx == structureDescriptor.x && it.ty == structureDescriptor.y);

                Connector conTo = structureTile.conParent[structureDescriptor.connectionIndex];
                Vec2
                    from = conFrom.localToAscendantCoordinates(this, Tmp.v1.set(conFrom.getWidth() / 2f, conFrom.getHeight() / 2f)),
                    to = conTo.localToAscendantCoordinates(this, Tmp.v2.set(conTo.getWidth() / 2f, conTo.getHeight() / 2f));
                drawCurve(Color.white, from.x, from.y, to.x, to.y, false);
            }
        }
        for(int i = 0; i < mockOutputs.size; i++){
            MockConnector conTo = mockOutputs.get(i);
            WireDescriptor wireDescriptor = schematic.connectionSettings.outputWires.get(conTo.id);
            for(StructureDescriptor structureDescriptor : wireDescriptor.connectedStructures){
                StructureTile structureTile = tiles.find(it -> it.tx == structureDescriptor.x && it.ty == structureDescriptor.y);

                Connector conFrom = structureTile.conChildren[structureDescriptor.connectionIndex];
                Vec2
                    from = conFrom.localToAscendantCoordinates(this, Tmp.v1.set(conFrom.getWidth() / 2f, conFrom.getHeight() / 2f)),
                    to = conTo.localToAscendantCoordinates(this, Tmp.v2.set(conTo.getWidth() / 2f, conTo.getHeight() / 2f));
                drawCurve(Color.white, from.x, from.y, to.x, to.y, true);
            }
        }
        if(!connecting.any()) return;

        if(connecting.isMock){
            MockConnector connecting = this.connecting.mockConnector;
            Vec2
                mouse = connecting.localToAscendantCoordinates(this, Tmp.v1.set(connecting.pointX, connecting.pointY)).add(x, y),
                anchor = connecting.localToAscendantCoordinates(this, Tmp.v2.set(connecting.getWidth() / 2f, connecting.getHeight() / 2f)).add(x, y);
//            canvas.parentToLocalCoordinates(Tmp.v3.set(mouse));
//            canvas.tilemap.parentToLocalCoordinates(Tmp.v3);
            Tmp.v3.set(mouse);
            if(hit(Tmp.v3.x, Tmp.v3.y, true) instanceof Connector connector && connector.findParent != connecting.isInput){
                mouse.set(connector.getWidth() / 2f, connector.getHeight() / 2f);
                connector.localToAscendantCoordinates(this, mouse).add(x, y);
            }
            Vec2
                from = connecting.isInput ? mouse : anchor,
                to = connecting.isInput ? anchor : mouse;

            drawCurve(Color.royal, from.x, from.y, to.x, to.y, connecting.isInput);
        }else{
            Connector connecting = this.connecting.connector;
            Vec2
                mouse = connecting.localToAscendantCoordinates(this, Tmp.v1.set(connecting.pointX, connecting.pointY)).add(x, y),
                anchor = connecting.localToAscendantCoordinates(this, Tmp.v2.set(connecting.getWidth() / 2f, connecting.getHeight() / 2f)).add(x, y);

            Tmp.v3.set(mouse);
            if(hit(Tmp.v3.x, Tmp.v3.y, true) instanceof MockConnector connector && connector.isInput != connecting.findParent){
                mouse.set(connector.getWidth() / 2f, connector.getHeight() / 2f);
                connector.localToAscendantCoordinates(this, mouse).add(x, y);
            }
            Vec2
                from = connecting.findParent ? mouse : anchor,
                to = connecting.findParent ? anchor : mouse;

            drawCurve(Color.royal, from.x, from.y, to.x, to.y, connecting.findParent);
        }
    }

    protected void drawCurve(Color color, float x1, float y1, float x2, float y2, boolean scale1){
        float scale = canvas.scaleX;

        Lines.stroke(4f * scale);
        Draw.color(color, parentAlpha);

        if(scale1){
            Fill.square(x1, y1, 8f * scale, 45f);
            Fill.square(x2, y2, 8f, 45f);
        }else{
            Fill.square(x1, y1, 8f, 45f);
            Fill.square(x2, y2, 8f * scale, 45f);
        }

        float dist = Math.abs(x1 - x2) / 2f;
        float lerpProgress = Interp.pow5Out.apply(Mathf.clamp(Math.abs(y1 - y2) * 2 / unitSize));
//            float disty = lerpProgress;
        float disty = 0;
        /*if(!selfConnecting){
            disty = 0;
        }else{
            float value = (y2 - y1) * 2;
            if(value == 0) value = unitSize / 2f;
            disty = Math.min(dist, Math.abs(value)) * Mathf.sign(value);
        }*/
        float cx1 = x1 + dist * (lerpProgress);
        float cx2 = x2 - dist * (lerpProgress);

        float cy1 = y1 + disty;
        float cy2 = y2 + disty;
        Lines.curve(x1, y1, cx1, cy1, cx2, cy2, x2, y2, Math.max(4, (int)(Mathf.dst(x1, y1, x2, y2) / 4f)));

        float progress = (Time.time % (60 * 4)) / (60 * 4);

        float t2 = progress * progress;
        float t3 = progress * t2;
        float t1 = 1 - progress;
        float t13 = t1 * t1 * t1;
        float kx1 = t13 * x1 + 3 * progress * t1 * t1 * cx1 + 3 * t2 * t1 * cx2 + t3 * x2;
        float ky1 = t13 * y1 + 3 * progress * t1 * t1 * cy1 + 3 * t2 * t1 * cy2 + t3 * y2;

        if(scale1){
            Fill.circle(kx1, ky1, 6f * Mathf.lerp(scale, 1f, progress));
        }else{
            Fill.circle(kx1, ky1, 6f * Mathf.lerp(scale, 1f, 1f - progress));
        }

        Draw.reset();
    }

    @Override
    public void show(Prov<Seq<TiledStructure>> structuresProv, Cons<Seq<TiledStructure>> out){
        throw new UnsupportedOperationException();
    }

    public static class ConnectingState{
        public Connector connector;
        public MockConnector mockConnector;
        private boolean isMock;

        public boolean isMock(){
            return isMock;
        }


        public void reset(){
            connector = null;
            mockConnector = null;
        }

        public void set(Connector connector){
            this.connector = connector;
            this.isMock = false;
        }

        public boolean any(){
            return mockConnector != null || connector != null;
        }

        public void set(MockConnector mockConnector){
            this.mockConnector = mockConnector;
            this.isMock = true;
        }

        public boolean is(MockConnector connector){
            return mockConnector == connector;
        }

        public boolean is(Connector connector){
            return connector == connector;
        }

        public boolean canConnectTo(MockConnector mockConnector){
            return !any() || isMock() ? false : connector.findParent != mockConnector.isInput;
        }
    }

    public static class ConnectorCaptureListener extends InputListener{
        public final Connector connector;
        int conPointer = -1;
        ByteLogicSchematicEditDialog self;

        public ConnectorCaptureListener(ByteLogicSchematicEditDialog self, Connector connector){
            this.connector = connector;
            this.self = self;
        }

        Connector connecting(){
            return Reflect.get(StructureTilemap.class, self.canvas.tilemap, "connecting");
        }

        void connecting(Connector connecting){
            Reflect.set(StructureTilemap.class, self.canvas.tilemap, "connecting", connecting);
        }

        @Override
        public boolean touchDown(InputEvent event, float x1, float y1, int pointer, KeyCode button){
            if(conPointer != -1 || connector.isDisabled()) return false;
            conPointer = pointer;

            if(self.connecting.any()) return false;
            self.connecting.set(connector);

            connector.pointX = x1;
            connector.pointY = y1;
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x1, float y1, int pointer){
            if(conPointer != pointer) return;
            connector.pointX = x1;
            connector.pointY = y1;
        }

        @Override
        public void touchUp(InputEvent event, float x1, float y1, int pointer, KeyCode button){
            if(conPointer != pointer || !self.connecting.is(connector)) return;
            conPointer = -1;

            Vec2 pos = connector.localToAscendantCoordinates(self.canvas.tilemap, Tmp.v1.set(x1, y1));
       /*     if(canvas.tilemap.hit(pos.x, pos.y, true) instanceof Connector con && con.canConnectTo(connector)){
                TiledStructure<?> otherObj = con.tile().obj;
                if(connector.findParent){
                    if(!connector.tile().obj.inputWires.remove(wire -> wire.parentOutput == con.id && wire.input == connector.id && wire.obj == otherObj)){
                        connector.tile().obj.addParent(otherObj, connector.id, con.id);
                    }
                }else{
                    if(!otherObj.inputWires.remove(wire -> wire.input == con.id && wire.parentOutput == connector.id && wire.obj == connector.tile().obj)){
                        otherObj.addParent(connector.tile().obj, con.id, connector.id);
                    }
                }
            }
*/
            self.connecting.reset();
        }
    }

    public class MockConnector extends Button{
        public final boolean isInput;
        public final int id;
        public float pointX, pointY;

        public MockConnector(boolean isInput, int id, ConnectorStyle style){
            super(new ButtonStyle(isInput ? style.inputStyle : style.outputStyle));
            this.isInput = isInput;
            this.id = id;

            clearChildren();

            addCaptureListener(new InputListener(){
                int conPointer = -1;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(conPointer != -1 || isDisabled()) return false;
                    conPointer = pointer;

                    if(connecting.any()) return false;
                    requestScroll();
                    connecting.set(MockConnector.this);

                    pointX = x;
                    pointY = y;
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer){
                    if(conPointer != pointer) return;
                    requestScroll();
                    pointX = x;
                    pointY = y;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(conPointer != pointer || !connecting.is(MockConnector.this)) return;
                    conPointer = -1;

                    Vec2 pos = MockConnector.this.localToAscendantCoordinates(ByteLogicSchematicEditDialog.this, Tmp.v1.set(x, y));
                    if(ByteLogicSchematicEditDialog.this.hit(pos.x, pos.y, true) instanceof StructureTile.Connector con && con.findParent != MockConnector.this.isInput){
                        if(isInput){
                            WireDescriptor wireDescriptor = schematic.connectionSettings.outputWires.get(id);
                            if(!wireDescriptor.connectedStructures.remove(it -> it.connectionIndex == con.id && it.x == con.tile().tx && it.y == con.tile().ty)){
                                wireDescriptor.connectedStructures.add(new StructureDescriptor(con.tile().tx, con.tile().ty, con.id));
                            }
                        }else{
                            WireDescriptor wireDescriptor = schematic.connectionSettings.inputWires.get(id);
                            if(!wireDescriptor.connectedStructures.remove(it -> it.connectionIndex == con.id && it.x == con.tile().tx && it.y == con.tile().ty)){
                                wireDescriptor.connectedStructures.add(new StructureDescriptor(con.tile().tx, con.tile().ty, con.id));
                            }
                        }
                    }
                    connecting.reset();
                }
            });
        }

        public boolean canConnectTo(StructureTile.Connector other){
            return
                isInput != other.findParent && !other.isDisabled()/* &&
                            tile() != other.tile()*/;
        }

        @Override
        public void draw(){
            super.draw();
            float cx = x + width / 2f;
            float cy = y + height / 2f;

            // these are all magic numbers tweaked until they looked good in-game, don't mind them.
            Lines.stroke(3f, Pal.accent);
            Draw.alpha(parentAlpha);
            if(isInput){
                Lines.line(cx, cy + 9f, cx + 9f, cy);
                Lines.line(cx + 9f, cy, cx, cy - 9f);
            }else{
                Lines.square(cx, cy, 9f, 45f);
            }
        }


        @Override
        public boolean isPressed(){
            return super.isPressed() || connecting.is(this);
        }

        @Override
        public boolean isOver(){
            return super.isOver() && (connecting == null || connecting.canConnectTo(this));
        }
    }
}
