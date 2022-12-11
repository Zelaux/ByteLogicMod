package bytelogic.annotations.extra;

import arc.files.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import bytelogic.annotations.*;
import bytelogic.annotations.BLAnnotations.*;
import com.github.javaparser.*;
import com.github.javaparser.ParserConfiguration.*;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.nodeTypes.*;
import com.sun.source.util.*;
import mindustry.annotations.util.*;
import mma.annotations.SupportedAnnotationTypes;
import mma.annotations.*;

import javax.annotation.processing.*;
import java.util.*;
import java.util.stream.*;

@SupportedAnnotationTypes(BLAnnotations.RemoveFromCompilation.class)
public class FilePostprocessor extends ModBaseProcessor{
    @Override
    public void process(RoundEnvironment env) throws Exception{
        StaticJavaParser.getConfiguration().setLanguageLevel(LanguageLevel.JAVA_16);
        ObjectMap<String, CompilationUnit> toRemove = new ObjectMap<>();
        for(Selement element : elements(RemoveFromCompilation.class)){
            TreePath treePath = trees.getPath(element.e);
//            System.out.println(element.fullName());
            Fi path = Fi.get(treePath.getCompilationUnit().getSourceFile().getName());
            if(toRemove.containsKey(path.absolutePath())) continue;
            toRemove.put(path.absolutePath(), StaticJavaParser.parse(path.readString()));
        }
        for(Entry<String, CompilationUnit> entry : toRemove){
            Fi file = Fi.get(entry.key);
            CompilationUnit compilationUnit = entry.value;
            List<Node> nodes = compilationUnit.stream().filter(it -> it instanceof NodeWithAnnotations).collect(Collectors.toList());
            Seq<NodeWithAnnotations<Node>> annotated = Seq.with(nodes).as();
            for(NodeWithAnnotations<Node> node : annotated){
                if(node.getAnnotationByName(RemoveFromCompilation.class.getSimpleName()).isPresent()){
                    ((Node)node).remove();
                }
            }
            file.writeString(compilationUnit.toString());
//            compilationUnit.findAll(NodeWithAnnotations.class)
        }
    }
}
