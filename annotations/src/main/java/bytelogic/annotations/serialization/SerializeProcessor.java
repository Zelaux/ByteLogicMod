package bytelogic.annotations.serialization;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.annotations.*;
import bytelogic.annotations.BLAnnotations.*;
import bytelogic.annotations.serialization.ObjectIO.*;
import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.*;
import mindustry.annotations.util.*;
import mindustry.annotations.util.TypeIOResolver.*;
import mma.annotations.SupportedAnnotationTypes;
import mma.annotations.*;
import mma.annotations.remote.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.lang.annotation.*;
import java.util.*;

@SupportedAnnotationTypes(BLAnnotations.Serializable.class)
public class SerializeProcessor extends ModBaseProcessor{
    ClassSerializer resolve;

    static String writeMethod(Stype type){
        return "write" + Strings.capitalize(type.name());
    }

    static String readMethod(Stype type){
        return "read" + Strings.capitalize(type.name());
    }

    @Override
    public void process(RoundEnvironment env) throws Exception{
        Seq<Stype> types = types(BLAnnotations.Serializable.class);
        resolve = ModTypeIOResolver.resolve(this);
        ObjectMap<String, Seq<TypePair>> map = new ObjectMap<>();
        for(Stype type : types){
            Serializable annotation = type.annotation(Serializable.class);
            Stype found = types(annotation, Serializable::type).get(0);
            if(Objects.equals(found.fullName(), Void.class.getCanonicalName())){
                map.get(annotation.prefix(), Seq::new).add(new TypePair(type,annotation));
            }else{
                map.get(annotation.prefix(), Seq::new).add(new TypePair(found,annotation));
            }
        }
        for(Entry<String, Seq<TypePair>> entry : map){
            generateSerializer(entry.key,  true,entry.value);
        }
//        write(compilationUnit);
    }

    <T extends Annotation> Seq<Stype> types(T t, Cons<T> consumer){
        try{
            consumer.get(t);
        }catch(MirroredTypesException e){
            return Seq.with(e.getTypeMirrors()).map(Stype::of);
        }
        throw new IllegalArgumentException("Missing types.");
    }

    public String generateSerializer(String serializerPrefix, Seq<Stype> types, boolean canCreateNewInstances) throws Exception{
        return generateSerializer(serializerPrefix, canCreateNewInstances, types.map(it -> new TypePair(it, null)));
    }

    public String generateSerializer(String serializerPrefix, boolean canCreateNewInstances, Seq<TypePair> types) throws Exception{
        ObjectMap<String, TypePair> typeMap = types.asMap(TypePair::fullName);


        Builder builder = TypeSpec.classBuilder(serializerPrefix + "Serializer").addModifiers(Modifier.PUBLIC, Modifier.FINAL);
//        CompilationUnit compilationUnit = new CompilationUnit();

//        ClassOrInterfaceDeclaration declaration = compilationUnit.addClass("Serializer", Keyword.PUBLIC, Keyword.FINAL);

        for(Entry<String, TypePair> entry : typeMap){
            String name = entry.key;
            Stype type = entry.value.type;
            resolve.writers.get(type.fullName(), () -> "Serializer." + writeMethod(type));
            resolve.readers.get(type.fullName(), () -> "Serializer." + readMethod(type));
        }

        for(Entry<String, TypePair> entry : typeMap){
            String name = entry.key;
            Stype type = entry.value.type;
            @Nullable
            Serializable annotation = entry.value.annotation;

            Fi directory = rootDirectory.child(annotationsSettings(AnnotationSettingsEnum.revisionsPath, "annotations/src/main/resources/serializer-revisions")).child(type.name());
            Seq<FieldObject> fields = type.fields().map(var -> {
                return new FieldObject(var.name(), type.name(), var.tname().toString(), var.has(FinalField.class), var.e.getModifiers().toArray(new Modifier[0]));
            });
            ObjectIO io = new ObjectIO(type.fullName(), type.cname(), fields, resolve, directory);

//            ClassName className = type.cname();

            MethodSpec.Builder writeMethod = MethodSpec.methodBuilder(writeMethod(type))
                                                 .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                                 .addParameter(Writes.class, "write");
            io.write(writeMethod, true);

            MethodSpec.Builder readMethod = MethodSpec.methodBuilder(readMethod(type))
                                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                                .addParameter(Reads.class, "read");
            io.write(readMethod, false);

            builder.addMethod(writeMethod.build());
            builder.addMethod(readMethod.build());

            //extra readMethod
            if(canCreateNewInstances && (annotation == null || annotation.canCreateInstance()))
                builder.addMethod(MethodSpec.methodBuilder(readMethod(type))
                                      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                      .addParameter(Reads.class, "read")
                                      .addStatement("$L object = new $L()", type.fullName(), type.fullName())
                                      .addStatement("$L(read,object)", readMethod(type))
                                      .addStatement("return object")
                                      .returns(type.cname())
                                      .build());

        }

        write(builder);
        return packageName + "." + serializerPrefix + "Serializer";
    }

    static class TypePair{
        Stype type;
        Serializable annotation;

        public TypePair(Stype type, Serializable annotation){
            this.type = type;
            this.annotation = annotation;
        }

        public String fullName(){
            return type.fullName();
        }
    }
}
