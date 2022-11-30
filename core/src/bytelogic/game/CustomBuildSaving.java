package bytelogic.game;

import arc.struct.*;
import arc.util.io.*;
import bytelogic.annotations.BLAnnotations.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.io.SaveFileReader.*;
import mindustry.io.*;

import java.io.*;

@CustomSavingSerializers
public class CustomBuildSaving implements CustomChunk{
    private final IntSet tmpSeq = new IntSet();

    public static void register(){
        SaveVersion.addCustomChunk("custom-building-save-v1", new CustomBuildSaving());
    }

    @Override
    public void write(DataOutput stream) throws IOException{
        tmpSeq.clear();
        Vars.world.tiles.eachTile(tile -> {
            if(tile.build instanceof CustomSaveBuilding){
                tmpSeq.add(tile.build.pos());
            }
        });
        IntSeq array = tmpSeq.iterator().toArray();
        stream.writeInt(array.size);
        for(int i = 0; i < array.size; i++){
            int pos = array.get(i);
            stream.writeInt(pos);
            Building build = Vars.world.build(pos);
            CustomSaveBuilding customSaveBuilding = (CustomSaveBuilding)build;
            customSaveBuilding.customWrite(Writes.get(stream));
        }
//        Writes.get(stream).var
    }

    @Override
    public void read(DataInput stream) throws IOException{
        int buildAmount = stream.readInt();
        for(int i = 0; i < buildAmount; i++){
            int buildPos = stream.readInt();
            Building build = Vars.world.build(buildPos);
            ((CustomSaveBuilding)build).customRead(Reads.get(stream));
        }
    }

}
