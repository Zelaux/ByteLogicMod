package bytelogic.core;

import arc.assets.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.struct.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;

public class SchematicsWrapper extends Schematics{
    public final Schematics wrapped;

    public SchematicsWrapper(Schematics wrapped){
        this.wrapped = wrapped;
    }

    @Override
    public void loadSync(){
        wrapped.loadSync();
    }

    @Override
    public void load(){
        wrapped.load();
    }

    @Override
    public void overwrite(Schematic target, Schematic newSchematic){
        wrapped.overwrite(target, newSchematic);
    }

    @Override
    public Seq<Schematic> all(){
        return wrapped.all();
    }

    @Override
    public void saveChanges(Schematic s){
        wrapped.saveChanges(s);
    }

    @Override
    public void savePreview(Schematic schematic, Fi file){
        wrapped.savePreview(schematic, file);
    }

    @Override
    public Texture getPreview(Schematic schematic){
        return wrapped.getPreview(schematic);
    }

    @Override
    public boolean hasPreview(Schematic schematic){
        return wrapped.hasPreview(schematic);
    }

    @Override
    public FrameBuffer getBuffer(Schematic schematic){
        return wrapped.getBuffer(schematic);
    }

    @Override
    public Seq<BuildPlan> toPlans(Schematic schem, int x, int y){
        return wrapped.toPlans(schem, x, y);
    }

    @Override
    public Seq<Schematic> getLoadouts(CoreBlock block){
        return wrapped.getLoadouts(block);
    }

    @Override
    public ObjectMap<CoreBlock, Seq<Schematic>> getLoadouts(){
        return wrapped.getLoadouts();
    }

    @Override
    public Schematic getDefaultLoadout(CoreBlock block){
        return wrapped.getDefaultLoadout(block);
    }

    @Override
    public int getMaxLaunchSize(Block block){
        return wrapped.getMaxLaunchSize(block);
    }

    @Override
    public void add(Schematic schematic){
        wrapped.add(schematic);
    }

    @Override
    public void remove(Schematic s){
        wrapped.remove(s);
    }

    @Override
    public Schematic create(int x, int y, int x2, int y2){
        return wrapped.create(x, y, x2, y2);
    }

    @Override
    public String writeBase64(Schematic schematic){
        return wrapped.writeBase64(schematic);
    }

    @Override
    public void loadAsync(){
        wrapped.loadAsync();
    }

    @Override
    public String getName(){
        return wrapped.getName();
    }
/*
    @Override
    public Seq<AssetDescriptor> getDependencies(){
        return wrapped.getDependencies();
    }*/
}
