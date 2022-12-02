package bytelogic.annotations.customSavingProc;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import arc.struct.IntMap.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.serialization.*;
import bytelogic.annotations.BLAnnotations.*;
import com.github.javaparser.ParserConfiguration.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Modifier.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
import mindustry.annotations.util.*;
import mma.annotations.SupportedAnnotationTypes;
import mma.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.*;
import javax.tools.*;
import java.util.*;

@SupportedAnnotationTypes(CustomSavingSerializers.class)
public class CustomSavingProc extends ModBaseProcessor{
    private static Seq<Stype> asTypes(Iterable<? extends Element> elements){
        return Seq.with(elements).select(e -> e instanceof TypeElement).map(e -> new Stype((TypeElement)e));
    }

    @Override
    public void process(RoundEnvironment env) throws Exception{
        super.process(env);


        Seq<Stype> types = asTypes(env.getRootElements());
        Stype customSaveBuilding = types.find(it -> it.name().equals("CustomSaveBuilding"));

        Leaf root = new Leaf(customSaveBuilding);
        Seq<Leaf> leaves = Seq.with(root);
        for(Leaf leaf : leaves){

            for(Stype element : types){
                for(Stype inner : asTypes(element.e.getEnclosedElements())){
                    if(inner.interfaces().contains(leaf.element) && leaf == root || inner.allSuperclasses().contains(leaf.element) && leaf != root){
                        leaves.add(leaf.addChild(inner));
//                        System.out.println(inner.fullName());
                    }
                }
//            CustomSaveBuilding
            }
        }
        for(Element enclosedElement : root.element.e.getEnclosedElements()){
            if(enclosedElement.toString().startsWith("customWrite")){
                root.writeMethodLink = enclosedElement;
            }
            if(enclosedElement.toString().startsWith("customRead")){
                root.readMethodLink = enclosedElement;
            }
            if(enclosedElement.toString().startsWith("customVersion")){
                root.versionMethodLink = enclosedElement;
            }
        }
        printTree(root, 0);
//        System.out.println("=========");
//        System.out.println(Fi.get("tmp").file().getAbsoluteFile().getAbsolutePath());
//        System.out.println("==================");
        root.visitTree(leaf -> {
//            System.out.println("leaf: " + leaf.element.fullName());
            for(Element element : leaf.element.e.getEnclosedElements()){
                if(element.getModifiers().contains(Modifier.ABSTRACT)) continue;
                if(element.getSimpleName() == root.writeMethodLink.getSimpleName()){
                    leaf.writeMethodLink = element;
                }else if(element.getSimpleName() == root.readMethodLink.getSimpleName()){
                    leaf.readMethodLink = element;
                }else if(element.getSimpleName() == root.versionMethodLink.getSimpleName()){
                    leaf.versionMethodLink = element;
                }
            }
            boolean hasWrite = leaf.writeMethodLink != null;
            boolean hasRead = leaf.readMethodLink != null;
            boolean hasVersion = leaf.versionMethodLink != null;
            if(hasWrite && !hasRead){
                err("Has no customRead method", leaf.element);
            }
            if(!hasWrite && hasRead){
                err("Has no customWrite method", leaf.element);
            }
            if(!hasWrite && !hasRead && hasVersion){
                err("Has no customWrite & customRead method", leaf.element);
            }
        });

        String readMethodName = root.readMethodLink.getSimpleName().toString();
        String writeMethodName = root.writeMethodLink.getSimpleName().toString();
        String versionMethodName = root.versionMethodLink.getSimpleName().toString();

        StaticJavaParser.getConfiguration().setLanguageLevel(LanguageLevel.JAVA_16);
        leaves.removeAll(leaf -> {
            if(leaf == root) return true;

            TreePath path = trees.getPath(leaf.element.e.getEnclosingElement());
//            String code = path.getCompilationUnit().toString();
//            System.out.println(code);
            leaf.compilationUnit = StaticJavaParser.parse(Fi.get(path.getCompilationUnit().getSourceFile().getName()).readString());
//            System.out.println(leaf.compilationUnit);
//            System.out.println(leaf.element.name());
            leaf.classDeclaration = leaf.compilationUnit.findFirst(ClassOrInterfaceDeclaration.class, it -> it.getNameAsString().equals(leaf.element.name())).get();
            if(leaf.readMethodLink == null){
                leaf.classDeclaration.addMethod(readMethodName, Keyword.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(Reads.class, "read")
                .setBody(new BlockStmt())
                ;
            }
            if(leaf.writeMethodLink == null){
                leaf.classDeclaration.addMethod(writeMethodName, Keyword.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(Writes.class, "write")
                .setBody(new BlockStmt())
                ;
            }
            if(leaf.versionMethodLink == null){
                leaf.classDeclaration.addMethod(versionMethodName, Keyword.PUBLIC)
                .addAnnotation(Override.class)
                .setType(short.class)
                .setBody(new BlockStmt().addStatement("return " + Short.MIN_VALUE + ";"))
                ;
            }
            return false;
        });


        Fi revisionsPath = rootDirectory.child("annotations/src/main/resources/build-revisions");
        System.out.println("revisionsPath.absolutePath(): "+revisionsPath.absolutePath());
        ObjectMap<String, IntMap<ClassRevision>> revisions = new ObjectMap<>();
        collectRevisions(revisionsPath, revisions);


        root.visitTree(leaf -> {
            MethodDeclaration readMethod = leaf.classDeclaration.getMethodsByName(readMethodName).get(0);
            MethodDeclaration writeMethod = leaf.classDeclaration.getMethodsByName(writeMethodName).get(0);
            MethodDeclaration versionMethod = leaf.classDeclaration.getMethodsByName(versionMethodName).get(0);
            BlockStmt versionBody = versionMethod.getBody().get().clone();

            Seq<ReturnStmt> returnStmts = new Seq<>();
            versionBody.accept(new VoidVisitorAdapter<Seq<ReturnStmt>>(){
                @Override
                public void visit(ReturnStmt n, Seq<ReturnStmt> arg){
                    arg.add(n);
                }
            }, returnStmts);
            String labelname = "WRITE_VERSION";

            String writesObjectName = writeMethod.getParameter(0).getNameAsString();
            for(ReturnStmt stmt : returnStmts){
                Expression expression = stmt.getExpression().get();
                String setVersionStatement = Strings.format("@.i(@);", writesObjectName, expression.toString());
                BlockStmt blockStmt = new BlockStmt();
                blockStmt.addStatement(StaticJavaParser.parseStatement(setVersionStatement));
                blockStmt.addStatement("break " + labelname + ";");
                stmt.replace(blockStmt);
            }

            BlockStmt writeMethodBody = writeMethod.getBody().get();
            removeFirstSuper(writeMethodBody, writeMethodName);
            writeMethodBody.addStatement(0, new LabeledStmt(labelname, versionBody));
            if (leaf.parent.parent!=null){

                writeMethodBody.addStatement(0,
                StaticJavaParser.parseStatement(Strings.format("super.@(@);",writeMethodName, writesObjectName)));
            }
            writeMethodBody.addStatement(0,
            StaticJavaParser.parseStatement(Strings.format("@.bool(@);", writesObjectName, leaf.parent.parent != null))
            );


            short version = Short.parseShort(returnStmts.get(0).getExpression().get().toString());
            BlockStmt readBlock = readMethod.getBody().get();
            removeFirstSuper(readBlock, readMethodName);
            String readsName = readMethod.getParameter(0).getNameAsString();
            ClassRevision classRevision = new ClassRevision(ClassRevision.formatClassName(leaf.element.fullName()), readBlock.clone(), version);

            addClassRevision(revisions, classRevision);


            readBlock.getStatements().clear();
            readBlock.addStatement(Strings.format("if (@.bool()) {\n@super.@(read);\n}", readsName, leaf.parent.parent != null ? "" : "//", readMethodName));


            readBlock.addStatement("int REV=" + readsName + ".i();");

            SwitchStmt switchStmt = new SwitchStmt();
            switchStmt.setSelector(StaticJavaParser.parseExpression("REV"));
            for(Entry<ClassRevision> entry : revisions.get(ClassRevision.formatClassName(leaf.element.fullName()))){
                int versions = entry.key;
                ClassRevision value = entry.value;
                SwitchEntry switchEntry = new SwitchEntry();

                switchEntry.getLabels().add(StaticJavaParser.parseExpression("" + versions));
                switchEntry.getStatements().add(value.readBlock.clone()
                .addStatement(new BreakStmt())
                );

                switchStmt.getEntries().add(switchEntry);
            }
            SwitchEntry defEntry = new SwitchEntry();

            defEntry.getStatements().add(StaticJavaParser.parseStatement("throw new " + RuntimeException.class.getName() + "(\"ILLEGAL REVISION: \"+REV);"));
            switchStmt.getEntries().add(defEntry);

            readBlock.addStatement(switchStmt);
        });


        root.visitTree(leaf -> {
//            longExit();
            CompilationUnitTree compilationUnit = trees.getPath(leaf.element.e).getCompilationUnit();
            JavaFileObject sourceFile = compilationUnit.getSourceFile();
            try{
//                Fi filesFi = getFilesFi(StandardLocation.SOURCE_OUTPUT, compilationUnit.getPackageName().toString(), Fi.get(compilationUnit.getSourceFile().getName()).name());
                Fi filesFi = Fi.get(sourceFile.getName());
                filesFi.writeString(leaf.compilationUnit.toString());
//                Fi.get(sourceFile.getName())
//                .writeString(leaf.compilationUnit.toString());
            }catch(Exception e){
                throw new RuntimeException(e);
            }
//            filer.getResource()
//            System.out.println(trees.getPath(leaf.element.e).toString());
        });


//        printTree(root, 0);


        saveRevisions(revisionsPath, revisions);
//        UnitDataProc.generateSerializer(revisions, extraImports);
//        longExit();
    }

    private void removeFirstSuper(BlockStmt body, String methodName){
        if(body.getStatements().size() == 0) return;
        Statement stmt = body.getStatement(0);
        if(!stmt.isExpressionStmt()) return;
        Expression expr = stmt.asExpressionStmt().getExpression();
        if(!expr.isMethodCallExpr()) return;
        MethodCallExpr methodCallExpr = expr.asMethodCallExpr();
        if(!methodCallExpr.getScope().orElse(methodCallExpr).isSuperExpr()) return;
        stmt.remove();

    }

    private void saveRevisions(Fi revisionsPath, ObjectMap<String, IntMap<ClassRevision>> revisions){
        for(ObjectMap.Entry<String, IntMap<ClassRevision>> revision : revisions){
            Fi folder = revisionsPath.child(revision.key);
            IntMap<ClassRevision> entries = revision.value;
            IntSeq keys = entries.keys().toArray();
            keys.sort();
            keys.each(key -> {
                Jval jval = Jval.newObject();
                ClassRevision saveField = entries.get(key);
                jval.put("read", saveField.readBlock.toString());
                Fi path = folder.child(key + ".json");
//                System.out.println(path.absolutePath());
                path.writeString(jval.toString());
            });
        }
    }

    public void addClassRevision(ObjectMap<String, IntMap<ClassRevision>> revisions, ClassRevision datum){
        revisions.get(datum.clazz, IntMap::new).put(datum.version, datum);
    }

    public void collectRevisions(Fi revisionsPath, ObjectMap<String, IntMap<ClassRevision>> revisions){
        for(Fi folder : revisionsPath.list()){
            if(!folder.isDirectory()) continue;
            String saveName = folder.name();
            for(Fi revisionFile : folder.list()){
                if(!revisionFile.extension().equals("json")) continue;
                int version = Integer.parseInt(revisionFile.nameWithoutExtension());

                Jval jval = Jval.read(revisionFile.readString());
                BlockStmt readBlock = StaticJavaParser.parseStatement(jval.get("read").asString()).asBlockStmt();
                ClassRevision classRevision = new ClassRevision(saveName, readBlock, (short)version);
                addClassRevision(revisions, classRevision);
            }
        }
    }

    private void longExit(){
        new Scanner(System.in).next();
        throw null;
    }

    private void printTree(Leaf leaf, int indent){
        if(true) return;
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

    static class ClassRevision{
        String clazz;
        BlockStmt readBlock;
        short version;

        public ClassRevision(Class<?> clazz, BlockStmt readBlock, short version){
            this.clazz = classAsString(clazz);
            this.readBlock = readBlock;
            this.version = version;
        }

        public ClassRevision(String clazz, BlockStmt readBlock, short version){
            this.clazz = clazz;
            this.readBlock = readBlock;
            this.version = version;
        }

        public static String classAsString(Class<?> clazz){
            return clazz.getName().replace(".", "__").replace("$", "_");
        }

        public static String formatClassName(String clazz){
            return clazz.replace(".", "__").replace("$", "_");
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
