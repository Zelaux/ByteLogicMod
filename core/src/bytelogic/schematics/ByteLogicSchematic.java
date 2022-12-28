package bytelogic.schematics;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import bytelogic.*;
import bytelogic.type.*;
import mindustry.mod.Mods.*;
import mma.ui.tiledStructures.TiledStructures.*;

public class ByteLogicSchematic implements Comparable<ByteLogicSchematic>{
    public static ByteLogicTiledStructures tmpStructure = new ByteLogicTiledStructures(new Seq<>());
    public final Seq<TiledStructure> structures;
    /** These are used for the schematic tag UI. */
    public Seq<String> labels = new Seq<>();
    /** Internal meta tags. */
    public StringMap tags;
    public int width, height;
    public final ConnectionSettings connectionSettings = new ConnectionSettings();
    public @Nullable Fi file;
    /** Associated mod. If null, no mod is associated with this schematic. */
    public @Nullable LoadedMod mod;
    public ByteLogicGateProvider provider;


    public ByteLogicSchematic(Seq<TiledStructure> structures, StringMap tags, int width, int height, ByteLogicGateProvider provider){
        this.structures = structures;
        this.tags = tags;
        this.width = width;
        this.height = height;
        this.provider = provider;
    }


    public String name(){
        return tags.get("name", "unknown");
    }

    public String description(){
        return tags.get("description", "");
    }

    public void save(){
        BLVars.schematics.saveChanges(this);
    }

    @Override
    public int compareTo(ByteLogicSchematic schematic){
        return name().compareTo(schematic.name());
    }

}