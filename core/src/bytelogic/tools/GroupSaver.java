package bytelogic.tools;

import arc.struct.*;
import arc.util.*;
import bytelogic.gen.*;
import mindustry.entities.*;
import mindustry.gen.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;

public class GroupSaver{
    private static Seq<EntityGroup[]> history = new Seq<>();
    private static Seq<Field> fields = new Seq<>();

    static{
        Seq<Field> allFields = Seq.with(Groups.class.getDeclaredFields())
                                   .addAll(BLGroups.class.getDeclaredFields());
        for(Field field : allFields.filter(it -> it.getType() == EntityGroup.class)){
            field.setAccessible(true);
        }
        fields.addAll(allFields);
    }

    public static void store(){
        store(null);
    }

    public static void store(@Nullable EntityGroup[] groups){
        history.add(fields.map(Reflect::get).<EntityGroup>toArray(EntityGroup.class));
        if(groups == null){
            Groups.init();
            BLGroups.init();
            return;
        }
        for(int i = 0; i < groups.length; i++){
            Reflect.set(null, fields.get(i), groups[i]);
        }
    }

    public static EntityGroup[] restore(){
        EntityGroup[] groups = fields.map(Reflect::get).<EntityGroup>toArray(EntityGroup.class);
        EntityGroup[] entityGroups = history.pop();
        for(int i = 0; i < entityGroups.length; i++){
            Reflect.set(null, fields.get(i), entityGroups[i]);
        }
        return groups;
    }
}
