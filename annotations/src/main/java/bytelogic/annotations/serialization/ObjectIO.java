package bytelogic.annotations.serialization;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import com.squareup.javapoet.*;
import mindustry.annotations.*;
import mindustry.annotations.util.*;
import mindustry.annotations.util.TypeIOResolver.*;

import javax.lang.model.element.*;

import static mindustry.annotations.BaseProcessor.instanceOf;

public class ObjectIO{
    final static Json json = new Json();
    //suffixes for sync fields
    final static String targetSuf = "_TARGET_", lastSuf = "_LAST_";
    //replacements after refactoring
    final static StringMap replacements = StringMap.of("mindustry.entities.units.BuildRequest", "mindustry.entities.units.BuildPlan");

    final ClassSerializer serializer;
    final String name;
    final Fi directory;
    final Seq<Revision> revisions = new Seq<>();
    private final TypeName type;

    boolean write;
    MethodSpec.Builder method;
    ObjectSet<String> presentFields = new ObjectSet<>();

    ObjectIO(String name, TypeName type, Seq<FieldObject> typeFields, ClassSerializer serializer, Fi directory){
        this.directory = directory;
        this.serializer = serializer;
        this.type = type;
        this.name = name;

        json.setIgnoreUnknownFields(true);

        directory.mkdirs();

        //load old revisions
        for(Fi fi : directory.list()){
            revisions.add(json.fromJson(Revision.class, fi));
        }

        revisions.sort(r -> r.version);

        //next revision to be used
        int nextRevision = revisions.isEmpty() ? 0 : revisions.max(r -> r.version).version + 1;

        //resolve preferred field order based on fields that fit
        Seq<FieldObject> fields = typeFields.select(spec ->
                                                        !spec.hasModifier(Modifier.TRANSIENT) &&
                                                            !spec.hasModifier(Modifier.STATIC) &&
                                                            !spec.hasModifier(Modifier.FINAL)/* &&
            (spec.type.isPrimitive() || serializer.has(spec.type.toString()))*/);

        //sort to keep order
        fields.sortComparing(f -> f.name);

        //keep track of fields present in the entity
        presentFields.addAll(fields.map(f -> f.name));

        Revision previous = revisions.isEmpty() ? null : revisions.peek();

        //add new revision if it doesn't match or there are no revisions
        if(revisions.isEmpty() || !revisions.peek().equal(fields)){
            revisions.add(new Revision(nextRevision,
                fields.map(f -> new RevisionField(f.name, f.type, f.isFinal(), f.hasModifier(Modifier.PRIVATE)))));
            Log.warn("Adding new revision @ for @.\nPre = @\nNew = @\n", nextRevision, name, previous == null ? null : previous.fields.toString(", ", f -> f.name + ":" + f.type), fields.toString(", ", f -> f.name + ":" + f.type.toString()));
            //write revision
            directory.child(nextRevision + ".json").writeString(json.toJson(revisions.peek()));
        }
    }

    void write(MethodSpec.Builder method, boolean write) throws Exception{
        this.method = method;
        this.write = write;
        method.addParameter(type, "rootObject");

        //subclasses *have* to call this method
        method.addAnnotation(Annotations.CallSuper.class);

        if(write){
            //write short revision
            st("write.s($L)", revisions.peek().version);
            //write uses most recent revision
            for(RevisionField field : revisions.peek().fields){
                io(field.type, "rootObject." + field.name, field.isFinal, field.isPrivate);
            }
        }else{
            //read revision
            st("short REV = read.s()");

            for(int i = 0; i < revisions.size; i++){
                //check for the right revision
                Revision rev = revisions.get(i);
                if(i == 0){
                    cont("if(REV == $L)", rev.version);
                }else{
                    ncont("else if(REV == $L)", rev.version);
                }

                //add code for reading revision
                for(RevisionField field : rev.fields){
                    //if the field doesn't exist, the result will be an empty string, it won't get assigned
                    io(field.type, presentFields.contains(field.name) ? "rootObject." + field.name + " = " : "", field.isFinal, field.isPrivate);
                }
            }

            //throw exception on illegal revisions
            ncont("else");
            st("throw new IllegalArgumentException(\"Unknown revision '\" + REV + \"' for object type '" + name + "'\")");
            econt();
        }
    }

    void writeSync(MethodSpec.Builder method, boolean write, Seq<Svar> syncFields, Seq<Svar> allFields) throws Exception{
        this.method = method;
        this.write = write;

        if(write){
            //write uses most recent revision
            for(RevisionField field : revisions.peek().fields){

                io(field.type, "rootObject." + field.name, false, false);
            }
        }else{
            Revision rev = revisions.peek();

            //base read code
            st("if(lastUpdated != 0) updateSpacing = $T.timeSinceMillis(lastUpdated)", Time.class);
            st("lastUpdated = $T.millis()", Time.class);
            st("boolean islocal = isLocal()");

            //add code for reading revision
            for(RevisionField field : rev.fields){
                Svar var = allFields.find(s -> s.name().equals(field.name));
                boolean sf = var.has(Annotations.SyncField.class), sl = var.has(Annotations.SyncLocal.class);

                if(sl) cont("if(!islocal)");

                if(sf){
                    st(field.name + lastSuf + " = rootObject." + field.name);
                }

                io(field.type, "this." + (sf ? field.name + targetSuf : field.name) + " = ", false, false);

                if(sl){
                    ncont("else");

                    io(field.type, "", false, false);

                    //just assign the two values so jumping does not occur on de-possession
                    if(sf){
                        st(field.name + lastSuf + " = rootObject." + field.name);
                        st(field.name + targetSuf + " = rootObject." + field.name);
                    }

                    econt();
                }
            }

            st("afterSync()");
        }
    }

    void writeSyncManual(MethodSpec.Builder method, boolean write, Seq<Svar> syncFields) throws Exception{
        this.method = method;
        this.write = write;

        if(write){
            for(Svar field : syncFields){
                st("buffer.put(rootObject.$L)", field.name());
            }
        }else{
            //base read code
            st("if(lastUpdated != 0) updateSpacing = $T.timeSinceMillis(lastUpdated)", Time.class);
            st("lastUpdated = $T.millis()", Time.class);

            //just read the field
            for(Svar field : syncFields){
                //last
                st("rootObject.$L = rootObject.$L", field.name() + lastSuf, field.name());
                //assign target
                st("rootObject.$L = buffer.get()", field.name() + targetSuf);
            }
        }
    }

    private void io(String type, String field, boolean isFinal, boolean isPrivate) throws Exception{
        type = type.replace("mindustry.gen.", "");
        type = replacements.get(type, type);

        String reflectCanonicalName = Reflect.class.getCanonicalName();
        String selfCalling = "rootObject";
        if(BaseProcessor.isPrimitive(type)){
            String serializingMethod = type.equals("boolean") ? "bool" : type.charAt(0) + "";
            if(isPrivate){
                String fieldName = field.replace(" = ", "").replace("this.", "");
                if (fieldName.startsWith(selfCalling)){
                    fieldName=fieldName.substring(selfCalling.length()+1);
                }

                String typeName = name.replaceAll("<[^>]*>","");
                if (write){

                    sInsert(serializingMethod,
                        Strings.format("@.get(@.class,@,\"@\")", reflectCanonicalName, typeName, selfCalling,fieldName),""
                    );
                } else{
                    sInsert(serializingMethod,
                        Strings.format("@.set(@.class,@,\"@\",", reflectCanonicalName, typeName, selfCalling,fieldName),
                        ")");
                }
            }else{
                s(serializingMethod, field);
            }
        }else if(instanceOf(type, "mindustry.ctype.Content")){
            if(write){
                s("s", field + ".id");
            }else{
                String simpleName = BaseProcessor.simpleName(type);
                String contentType = simpleName.toLowerCase().replace("type", "");
                if(simpleName.contains("Unit")){
                    contentType = "unit";
                }else if(contentType.equals("gas")){
                    contentType = "typeid_UNUSED";
                }
                st(field + "mindustry.Vars.content.getByID(mindustry.ctype.ContentType.$L, read.s())", contentType);
            }
        }else if(serializer.writers.containsKey(type) && write){
            st("$L(write, $L)", serializer.writers.get(type), field);
        }else if(serializer.mutatorReaders.containsKey(type) && !write && !field.replace(" = ", "").contains(" ") && !field.isEmpty()){
            st("$L$L(read, $L)", field, serializer.mutatorReaders.get(type), field.replace(" = ", ""));
        }else if(serializer.readers.containsKey(type) && !write){
            st("$L$L(read)", field, serializer.readers.get(type));
        }else if(type.endsWith("[]")){ //it's a 1D array
            String fieldName = field.replace(" = ", "").replace("this.", "");
            String rawType = type.substring(0, type.length() - 2);

            if(write){
                s("i", field + ".length");
                cont("for(int INDEX = 0; INDEX < $L.length; INDEX ++)", field);
                io(rawType, field + "[INDEX]", false, false);
            }else{
                String lenf = fieldName.replace(".", "__") + "_LENGTH";
                s("i", "int " + lenf + " = ");
                if(!field.isEmpty() && !isFinal){
                    st("$Lnew $L[$L]", field, type.replace("[]", ""), lenf);
                }
                cont("for(int INDEX = 0; INDEX < $L; INDEX ++)", lenf);
                cont("if (INDEX<$L.length)", fieldName);
                io(rawType, field.replace(" = ", "[INDEX] = "), false, false);
                ncont("else");
                io(rawType, "", false, false);
                econt();
            }

            econt();
        }else if(type.startsWith("arc.struct") && type.contains("<")){ //it's some type of data structure
            String fieldName = field.replace(" = ", "").replace("this.", "");
            String struct = type.substring(0, type.indexOf("<"));
            String generic = type.substring(type.indexOf("<") + 1, type.indexOf(">"));

            if(struct.equals("arc.struct.Queue") || struct.equals("arc.struct.Seq")){
                if(write){
                    s("i", field + ".size");
                    cont("for(int INDEX = 0; INDEX < $L.size; INDEX ++)", field);
                    io(generic, field + ".get(INDEX)", false, false);
                }else{
//                    String fieldName = field.replace(" = ", "").replace("this.", "");
                    String lenf = fieldName + "_LENGTH";
                    s("i", "int " + lenf + " = ");
                    if(!field.isEmpty()){
                        st("$L.clear()", field.replace(" = ", ""));
                    }
                    cont("for(int INDEX = 0; INDEX < $L; INDEX ++)", lenf);
                    io(generic, field.replace(" = ", "_ITEM = ").replace("this.", generic + " "), false, false);
                    if(!field.isEmpty()){
                        String temp = field.replace(" = ", "_ITEM").replace("this.", "");
                        st("if($L != null) $L.add($L)", temp, field.replace(" = ", ""), temp);
                    }
                }

                econt();
            }else{
                Log.warn("Missing serialization code for collection '@' in '@'", type, name);
            }
        }else{
            Log.warn("Missing serialization code for type '@' in '@'", type, name);
        }
    }

    private void cont(String text, Object... fmt){
        method.beginControlFlow(text, fmt);
    }

    private void econt(){
        method.endControlFlow();
    }

    private void ncont(String text, Object... fmt){
        method.nextControlFlow(text, fmt);
    }

    private void st(String text, Object... args){
        method.addStatement(text, args);
    }

    private void s(String type, String field){
        if(write){
            method.addStatement("write.$L($L)", type, field);
        }else{
            method.addStatement("$Lread.$L()", field, type);
        }
    }

    private void sInsert(String type, String fieldBegin, String fieldEnd){
        if(write){
            method.addStatement("write.$L($L$L)", type, fieldBegin, fieldEnd);
        }else{
            method.addStatement("$Lread.$L()$L", fieldBegin, type, fieldEnd);
        }
    }

    static class FieldObject{
        public final String name;
        public final String parentName;
        public final String type;
        final Modifier[] modifiers;
        final boolean isFinal;

        public FieldObject(String name, String parentName, String type, boolean isFinal, Modifier... modifiers){
            this.name = name;
            this.parentName = parentName;
            this.type = type;
            this.isFinal = isFinal;
            this.modifiers = modifiers;
        }

        public boolean isFinal(){
            return isFinal || hasModifier(Modifier.FINAL);
        }

        public boolean hasModifier(Modifier modifier){
            return Structs.contains(modifiers, modifier);
        }
    }

    public static class Revision{
        int version;
        Seq<RevisionField> fields;

        Revision(int version, Seq<RevisionField> fields){
            this.version = version;
            this.fields = fields;
        }

        Revision(){
        }

        /** @return whether these two revisions are compatible */
        boolean equal(Seq<FieldObject> specs){
            if(fields.size != specs.size) return false;

            for(int i = 0; i < fields.size; i++){
                RevisionField field = fields.get(i);
                FieldObject spec = specs.get(i);
                if(!field.type.replace("mindustry.gen.", "").equals(spec.type.replace("mindustry.gen.", ""))){
                    return false;
                }
                if(spec.isFinal() != field.isFinal){
                    return false;
                }
            }
            return true;
        }
    }

    public static class RevisionField{
        String name, type;
        boolean isFinal, isPrivate;

        public RevisionField(String name, String type, boolean isFinal, boolean isPrivate){
            this.name = name;
            this.type = type;
            this.isFinal = isFinal;
            this.isPrivate = isPrivate;
        }

        RevisionField(){
        }
    }
}
