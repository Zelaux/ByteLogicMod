package bytelogic.type.byteGates;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import bytelogic.schematics.*;
import bytelogic.type.ConnectionSettings.*;
import bytelogic.type.ConnectionSettings.WireDescriptor.*;
import bytelogic.type.*;
import bytelogic.type.byteGates.ByteLogicOperators.*;
import bytelogic.type.byteGates.ByteLogicOperators.UnaryGate.*;
import bytelogic.ui.dialogs.*;
import bytelogic.world.blocks.ByteLogicProcessor.*;
import mindustry.gen.*;
import mindustry.io.*;
import mma.ui.tiledStructures.*;
import org.jetbrains.annotations.Nullable;
import zelaux.arclib.ui.tooltips.*;

import static bytelogic.BLVars.byteLogicSchematics;

public class SchematicGate extends ByteLogicGate{
    private transient static ByteLogicSchematic tmpSchematic;

    static{
        Seq<Json> jsons = Seq.with(JsonIO.json, Reflect.get(JsonIO.class, "jsonBase"));
        Serializer<ByteLogicSchematic> schematicSerializer = new Serializer<>(){

            @Override
            public void write(Json json, ByteLogicSchematic object, Class knownType){
                json.writeValue(byteLogicSchematics.writeBase64(object));
            }

            @Override
            public ByteLogicSchematic read(Json json, JsonValue jsonData, Class type){
                return ByteLogicSchematics.readBase64(jsonData.asString());
            }
        };
        for(Json json : jsons){
            json.setSerializer(ByteLogicSchematic.class, schematicSerializer);
        }
    }

    public ByteLogicSchematic schematic;
    private transient boolean loadedFromJson;
    private transient Seq<RelayGate> inputGates = new Seq<>();
    private transient Seq<RelayGate> outputGates = new Seq<>();

    /*for json*/
    private SchematicGate(){
        setupEditor();
        loadedFromJson = true;
        Core.app.post(this::tryLoad);
    }

    public SchematicGate(ByteLogicSchematic schematic){
        setupEditor();
        this.schematic = schematic;
        initInputAndOutputGates();
    }

    @Nullable
    @Override
    public Cons2<TiledStructuresDialog, Table> editor(){
        return (tiledStructuresDialog, table) -> {
            table.defaults().size(40f);
            /*table.button(Icon.copySmall, () -> {
                ByteLogicSchematic it = ByteLogicSchematics.readBase64(byteLogicSchematics.writeBase64(schematic));
                SchematicGate gate = new SchematicGate(it);
                it.parentFile(schematic.parentFile);
                tiledStructuresDialog.canvas.beginQuery(gate);
            });*/
            table.button(Icon.uploadSmall, () -> {
                ByteLogicSchematic it = ByteLogicSchematics.readBase64(byteLogicSchematics.writeBase64(schematic));
                it.parentFile(schematic.parentFile);
                new ByteLogicSchematicEditDialog(it,null).show();
            }).disabled(byteLogicSchematics.all().contains(it -> it.file != null && it.file == schematic.parentFile));
        };
    }

    private void setupEditor(){
        editor = (tiledStructuresDialog, table) -> {
            table.button(Icon.copySmall, () -> {
                ByteLogicSchematic it = ByteLogicSchematics.readBase64(byteLogicSchematics.writeBase64(schematic));
                SchematicGate gate = new SchematicGate(it);
                it.parentFile(schematic.parentFile);
                tiledStructuresDialog.canvas.beginQuery(gate);
            });
            table.button(Icon.uploadSmall, () -> {
                new ByteLogicSchematicEditDialog(schematic,null).show();
            }).disabled(byteLogicSchematics.all().contains(it -> it.file != null && it.file == schematic.parentFile));
        };
    }

    @Override
    public void afterRead(){
        tryLoad();
    }

    @Override
    protected void initSignals(){
        if(schematic == null){
            signals = inputSignals = new Signal[0];
            return;
        }
        super.initSignals();
    }

    @Override
    public String typeName(){
        tryLoad();
        return schematic.name();
    }

    @Override
    public int objWidth(){
        tryLoad();
        return objHeight() + Mathf.num(schematic.connectionSettings.inputWires.any()) + Mathf.num(schematic.connectionSettings.outputWires.any());
    }

    @Override
    public int objHeight(){
        tryLoad();
        int max = Math.max(schematic.connectionSettings.inputWires.size, schematic.connectionSettings.outputWires.size);
        return (int)Math.max(max * 2f / 3f, 2f);
    }

    @Override
    public boolean hasFields(){
        tryLoad();
        return true;
    }

    private void initInputAndOutputGates(){
        initSignals();
        for(WireDescriptor inputWire : schematic.connectionSettings.inputWires){
            RelayGate relayGate = new RelayGate();
            for(StructureDescriptor descriptor : inputWire.connectedStructures){
                ByteLogicGate found = (ByteLogicGate)schematic.structures.find(it -> it.editorX == descriptor.x && it.editorY == descriptor.y);
                found.addParent(relayGate, descriptor.connectionIndex, 0);
            }
            inputGates.add(relayGate);
        }
        for(WireDescriptor outputWire : schematic.connectionSettings.outputWires){
            RelayGate relayGate = new RelayGate();
            for(StructureDescriptor descriptor : outputWire.connectedStructures){
                ByteLogicGate found = (ByteLogicGate)schematic.structures.find(it -> it.editorX == descriptor.x && it.editorY == descriptor.y);
                relayGate.addParent(found, 0, descriptor.connectionIndex);
            }
            outputGates.add(relayGate);
        }
        for(int i = 0; i < outputGates.size; i++){
            outputGates.get(i).inputSignals[0] = signals[i];
        }
        for(int i = 0; i < inputGates.size; i++){
            inputGates.get(i).signals[0] = inputSignals[i];
        }
    }

    @Override
    public ByteLogicGateGroup group(){
        return ByteLogicGateGroup.nil;
    }

    @Override
    public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
        tryLoad();
        String inputName = schematic.connectionSettings.inputWires.get(inputIndex).name;
        return inputName == null ? null : SideTooltips.INSTANCE.create(inputName);
    }

    @Override
    public @Nullable Tooltip outputConnectorTooltip(int outputIndex){
        tryLoad();
        String outputName = schematic.connectionSettings.outputWires.get(outputIndex).name;
        return outputName == null ? null : SideTooltips.INSTANCE.create(outputName);
    }

    @Override
    public int outputConnections(){
        return schematic.connectionSettings.outputWires.size;
    }

    @Override
    public int inputConnections(){
        return schematic.connectionSettings.inputWires.size;
    }

    @Override
    public boolean update(){
        tryLoad();
        return super.update();
    }

    private void tryLoad(){
        if(loadedFromJson && schematic != null){
            loadedFromJson = false;
            initInputAndOutputGates();
        }
    }

    @Override
    public void setLink(ByteLogicProcessorBuild build){
        for(ByteLogicGate structure : schematic.structures.<ByteLogicGate>as()){
            structure.setLink(build);
        }
        super.setLink(build);
    }

    @Override
    public void updateSignals(){
        ByteLogicSchematic.tmpStructures.all.set(schematic.structures);
        ByteLogicSchematic.tmpStructures.update();
        outputGates.each(ByteLogicGate::updateInputs);
    }

}
