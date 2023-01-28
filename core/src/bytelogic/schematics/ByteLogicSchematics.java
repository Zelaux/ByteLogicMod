package bytelogic.schematics;

import arc.*;
import arc.assets.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.io.Streams.*;
import arc.util.serialization.*;
import bytelogic.*;
import bytelogic.io.*;
import bytelogic.type.*;
import bytelogic.utils.*;
import mindustry.game.EventType.*;
import mindustry.io.*;
import mma.ui.tiledStructures.TiledStructures.*;

import java.io.*;
import java.util.zip.*;

import static bytelogic.BLVars.byteLogicSchematicDirectory;
import static mindustry.Vars.*;

public class ByteLogicSchematics implements Loadable{

    private static final ByteLogicSchematic tmpSchem = new ByteLogicSchematic(new Seq<>(), new StringMap(), 1, 1, ByteLogicGateProvider.defaultProvider);
    private static final ByteLogicSchematic tmpSchem2 = new ByteLogicSchematic(new Seq<>(), new StringMap(), 1, 1, ByteLogicGateProvider.defaultProvider);

    public static final byte[] header = {'m', 'b', 's', 'c', 'h'};
    private static final byte version = 1;

    private static final int padding = 2;
    private static final int maxPreviewsMobile = 32;
    private static final int resolution = 32;

    private OptimizedByteArrayOutputStream out = new OptimizedByteArrayOutputStream(1024);
    private Seq<ByteLogicSchematic> all = new Seq<>();
    private OrderedMap<ByteLogicSchematic, FrameBuffer> previews = new OrderedMap<>();
    private ObjectSet<ByteLogicSchematic> errored = new ObjectSet<>();
    private Texture errorTexture;
    private long lastClearTime;


    public ByteLogicSchematics(){

        Events.on(ClientLoadEvent.class, event -> {
            errorTexture = new Texture("sprites/error.png");
        });
    }


    /** Loads a schematic from base64. May throw an exception. */
    public static ByteLogicSchematic readBase64(String schematic){
        try{
            return read(new ByteArrayInputStream(Base64Coder.decode(schematic.trim())));
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static ByteLogicSchematic read(Fi file) throws IOException{
        ByteLogicSchematic s = read(new DataInputStream(file.read(1024)));
        if(!s.tags.containsKey("name")){
            s.tags.put("name", file.nameWithoutExtension());
        }
        s.file = file;
        return s;
    }

    public static ByteLogicSchematic read(InputStream input) throws IOException{
        for(byte b : header){
            if(input.read() != b){
                throw new IOException("Not a schematic file (missing header).");
            }
        }

        int ver = input.read();

        try(DataInputStream stream = new DataInputStream(new InflaterInputStream(input))){
            short width = stream.readShort(), height = stream.readShort();
            StringMap map = new StringMap();
            int tags = stream.readUnsignedByte();
            for(int i = 0; i < tags; i++){
                map.put(stream.readUTF(), stream.readUTF());
            }

            String[] labels = null;

            //try to read the categories, but skip if it fails
            try{
                labels = JsonIO.read(String[].class, map.get("labels", "[]"));
            }catch(Exception ignored){
            }
            Reads read = new Reads(stream);
            ByteLogicTiledStructures tmpStructures = new ByteLogicTiledStructures(new Seq<>());
            if (ver==0){
                JsonIO.read(ByteLogicTiledStructures.class, tmpStructures, read.str());
            } else{

                BLTypeIO.readByteLogicTiledStructures(read,tmpStructures);
            }
            Seq<TiledStructure> tiledStructures = tmpStructures.all.copy();
            ByteLogicGateProvider provider = ByteLogicGateProvider.providerMap.get(read.str());

            ByteLogicSchematic out = new ByteLogicSchematic(tiledStructures, map, width, height, provider);
            out.connectionSettings.read(read);
            if(labels != null) out.labels.addAll(labels);
            return out;
        }
    }

    public static void write(ByteLogicSchematic schematic, Fi file) throws IOException{
        write(schematic, file.write(false, 1024));
    }

    public static void write(ByteLogicSchematic schematic, OutputStream output) throws IOException{
        output.write(header);
        output.write(version);

        try(DataOutputStream stream = new DataOutputStream(new DeflaterOutputStream(output))){

            stream.writeShort(schematic.width);
            stream.writeShort(schematic.height);

            JsonUtils.saveWriterIO(() -> {
                schematic.tags.put("labels", JsonIO.write(schematic.labels.toArray(String.class)));
            });


            stream.writeByte(schematic.tags.size);
            for(var e : schematic.tags.entries()){
                stream.writeUTF(e.key);
                stream.writeUTF(e.value);
            }
            ByteLogicTiledStructures tmpStructures = new ByteLogicTiledStructures(new Seq<>());
            tmpStructures.set(schematic.provider.providers.as());

            tmpStructures.all.set(schematic.structures);
            Writes write = new Writes(stream);
            BLTypeIO.writeByteLogicTiledStructures(write, tmpStructures);
            write.str(schematic.provider.name);
            schematic.connectionSettings.write(write);
        }
    }

    @Override
    public void loadSync(){
        load();
    }

    /** Load all schematics in the folder immediately. */
    public void load(){
        all.clear();


        for(Fi file : byteLogicSchematicDirectory.list()){
            loadFile(file);
        }

//        platform.getWorkshopContent(ByteLogicSchematic.class).each(this::loadFile);

        //mod-specific schematics, cannot be removed
        mods.listFiles(byteLogicSchematicDirectory.name(), (mod, file) -> {
            ByteLogicSchematic s = loadFile(file);
            if(s != null){
                s.mod = mod;
            }
        });

        all.sort();
    }
/*
    public void overwrite(ByteLogicSchematic target, ByteLogicSchematic newByteLogicSchematic){
        if(previews.containsKey(target)){
            previews.get(target).dispose();
            previews.remove(target);
        }

        target.tiles.clear();
        target.tiles.addAll(newByteLogicSchematic.tiles);
        target.width = newByteLogicSchematic.width;
        target.height = newByteLogicSchematic.height;
        newByteLogicSchematic.labels = target.labels;
        newByteLogicSchematic.tags.putAll(target.tags);
        newByteLogicSchematic.file = target.file;

        checkLoadout(target, true);

        try{
            write(newByteLogicSchematic, target.file);
        }catch(Exception e){
            Log.err("Failed to overwrite schematic '@' (@)", newByteLogicSchematic.name(), target.file);
            Log.err(e);
            ui.showException(e);
        }
    }*/

    private @Nullable ByteLogicSchematic loadFile(Fi file){
        if(!file.extension().equals(BLVars.byteLogicSchematicExtension)) return null;

        try{
            ByteLogicSchematic s = read(file);
            all.add(s);

            //external file from workshop
            if(!s.file.parent().equals(byteLogicSchematicDirectory)){
                s.tags.put("steamid", s.file.parent().name());
            }

            return s;
        }catch(Throwable e){
            Log.err("Failed to read schematic from file '@'", file);
            Log.err(e);
        }
        return null;
    }

    public Seq<ByteLogicSchematic> all(){
        return all;
    }

    public void saveChanges(ByteLogicSchematic s){
        if(s.file != null){
            try{
                write(s, s.file);
            }catch(Exception e){
                ui.showException(e);
            }
        }
        all.sort();
    }


    //region IO methods

    public boolean hasPreview(ByteLogicSchematic schematic){
        return previews.containsKey(schematic);
    }
    /*
     *//** Creates an array of build plans from a schematic's data, centered on the provided x+y coordinates. *//*
    public Seq<BuildPlan> toPlans(ByteLogicSchematic schem, int x, int y){
        return schem.tiles.map(t -> new BuildPlan(t.x + x - schem.width / 2, t.y + y - schem.height / 2, t.rotation, t.block, t.config).original(t.x, t.y, schem.width, schem.height))
                   .removeAll(s -> (!s.block.isVisible() && !(s.block instanceof CoreBlock)) || !s.block.unlockedNow()).sort(Structs.comparingInt(s -> -s.block.schematicPriority));
    }*/

    /** Adds a schematic to the list, also copying it into the files. */
    public void add(ByteLogicSchematic schematic){
        all.add(schematic);
        try{
            Fi file = byteLogicSchematicDirectory.child(Time.millis() + "." + BLVars.byteLogicSchematicExtension);
            write(schematic, file);
            schematic.file = file;
        }catch(Exception e){
            ui.showException(e);
            Log.err(e);
        }

        all.sort();
    }

    public void remove(ByteLogicSchematic s){
        all.remove(s);
        if(s.file != null){
            s.file.delete();
        }

        if(previews.containsKey(s)){
            previews.get(s).dispose();
            previews.remove(s);
        }
        all.sort();
    }

    /** Converts a schematic to base64. Note that the result of this will always start with 'bXNjaAB'. */
    public String writeBase64(ByteLogicSchematic schematic){
        try{
            out.reset();
            write(schematic, out);
            return new String(Base64Coder.encode(out.getBuffer(), out.size()));
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }


    //endregion
}
