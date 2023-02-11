package bytelogic.annotations.serialization;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.annotations.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.squareup.javapoet.*;
import mindustry.annotations.*;
import mindustry.annotations.util.*;
import mma.annotations.SupportedAnnotationTypes;
import mma.annotations.*;
import mma.annotations.remote.*;

import javax.annotation.processing.*;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.*;

@SupportedAnnotationTypes(BLAnnotations.GenerateByteLogicGatesSerializer.class)
public class ByteLogicGatesSerializer extends ModBaseProcessor{
    public static final String pathByteLogicGate = "bytelogic.type.byteGates.ByteLogicOperators.ByteLogicGate";

    private static Seq<Stype> asTypes(Iterable<? extends Element> elements){
        return Seq.with(elements).select(e -> e instanceof TypeElement).map(e -> new Stype((TypeElement)e));
    }

    @Override
    public void process(RoundEnvironment env) throws Exception{

        Seq<Stype> allTypes = asTypes(env.getRootElements());

        while(true){
            int beginSize = allTypes.size;
            for(Stype type : allTypes){
                for(Stype newType : asTypes(type.e.getEnclosedElements())){
                    allTypes.addUnique(newType);
                }
            }
            if(beginSize == allTypes.size) break;
        }

        System.out.println(allTypes.toString("\n"));
        Stype rootClass = allTypes.find(it -> it.fullName().equals(pathByteLogicGate));

        Leaf root = new Leaf(rootClass);
        Seq<Leaf> leaves = Seq.with(root);
        ObjectMap<String, Leaf> leafMap = ObjectMap.of(root.element.fullName(), root);
        for(Leaf leaf : leaves){

            for(Stype element : allTypes){
                if(element.superclass().fullName().equals(leaf.element.fullName())){
                    leaves.add(leaf.addChild(element));
                    leafMap.put(element.fullName(), leaves.peek());
//                        System.out.println(element.fullName());

                }
//            CustomSaveBuilding
            }
        }
        printTree(root, 0);
        Seq<Stype> requiredTypes = Seq.with(root.element);

        root.visitTree(value -> requiredTypes.addUnique(value.element));
        leaves.sort(it -> requiredTypes.indexOf(it.element));
        SerializeProcessor serializeProcessor = new SerializeProcessor();
        Reflect.set(BaseProcessor.class,serializeProcessor,"rootDirectory",rootDirectory);
        serializeProcessor.resolve= ModTypeIOResolver.resolve(this);
        String generateSerializer = serializeProcessor.generateSerializer(classPrefix() + "Gates", requiredTypes, false);


        TypeSpec.Builder serializer = TypeSpec.classBuilder("ByteLogicGateSerializer")
                                          .addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder write = MethodSpec.methodBuilder("write")
                                       .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                                       .addParameter(Writes.class, "write")
                                       .addParameter(Class.class, "clazz")
                                       .addParameter(ClassName.bestGuess(pathByteLogicGate), "gate");
        MethodSpec.Builder read = MethodSpec.methodBuilder("read")
                                       .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                                       .addParameter(Reads.class, "read")
                                       .addParameter(Class.class, "clazz")
                                       .addParameter(ClassName.bestGuess(pathByteLogicGate), "gate");
        Integer.class.isAssignableFrom(Number.class);
        for(int i = 0; i < leaves.size; i++){
            Leaf leaf = leaves.get(i);
            ClassName typeName = ClassName.bestGuess(leaf.element.fullName());
            write.beginControlFlow("if ($T.class.isAssignableFrom(clazz))", typeName);
            write.addStatement(generateSerializer + "." + SerializeProcessor.writeMethod(leaf.element) + "(write,($T)gate)", typeName);
            write.endControlFlow();

            read.beginControlFlow("if ($T.class.isAssignableFrom(clazz))", typeName);
            read.addStatement(generateSerializer + "." + SerializeProcessor.readMethod(leaf.element) + "(read,($T)gate)", typeName);
            read.endControlFlow();
        }

        serializer.addMethod(write.build());
        serializer.addMethod(read.build());

        write(serializer);
    }

    private void printTree(Leaf leaf, int indent){
//        if(true) return;
        for(int i = 0; i < indent; i++){
            if(i + 1 < indent){
                System.out.print("| ");
            }else{
                System.out.print("|-");
            }
        }
        System.out.println(leaf.element.fullName());
        for(int i = 0; i < leaf.children.size; i++){
            Leaf child = leaf.children.get(i);
            printTree(child, indent + 1);
        }
    }

    static class Leaf{
        final Seq<Leaf> children = new Seq<>();
        public Element writeMethodLink;
        public Element readMethodLink;
        public Element versionMethodLink;
        public CompilationUnit compilationUnit;
        public ClassOrInterfaceDeclaration classDeclaration;
        Stype element;
        Leaf parent;

        public Leaf(Stype element){
            this.element = element;
        }

        Leaf addChild(Stype element){
            Leaf leaf = new Leaf(element);
            children.add(leaf);
            leaf.parent = this;
            return leaf;
        }

        public void visitTree(Cons<Leaf> leafCons){
            for(Leaf leaf : children){
                leafCons.get(leaf);
                leaf.visitTree(leafCons);
            }
        }

        public void remove(Leaf leaf){
            children.remove(leaf);
            leaf.parent = null;
        }

        public void remove(){
            parent.remove(this);
        }
    }
}
