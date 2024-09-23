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
import mmc.annotations.SupportedAnnotationTypes;
import mmc.annotations.*;

import javax.annotation.processing.*;
import java.util.*;
import java.util.stream.*;

@SupportedAnnotationTypes(BLAnnotations.RemoveFromCompilation.class)
public class FilePostprocessor extends ModBaseProcessor{
    @Override
    public void process(RoundEnvironment env) throws Exception{
        StaticJavaParser.getConfiguration().setLanguageLevel(LanguageLevel.JAVA_17);
        ObjectMap<String, CompilationUnit> toRemove = new ObjectMap<>();
        boolean errors = false;
        for(Selement element : elements(RemoveFromCompilation.class)){
            TreePath treePath = trees.getPath(element.e);
//            System.out.println(element.fullName());
            Fi path = Fi.get(treePath.getCompilationUnit().getSourceFile().getName());
            if(toRemove.containsKey(path.absolutePath())) continue;
            try{
                toRemove.put(path.absolutePath(), StaticJavaParser.parse(path.readString()));
            }catch(ParseProblemException e){
                List<Problem> problems = e.getProblems();
                String[] strings = new String[problems.size()];
                for(int i = 0; i < problems.size(); i++){
                    Problem problem = problems.get(i);
                    strings[i] = problem.getVerboseMessage();
                }
                err(path.absolutePath().replace("core\\build\\src","core\\src") + "\n" + String.join("\n",strings), element.e);
                errors = true;
            }
        }
        if(errors){
            RuntimeException foundErrors = new RuntimeException("Found errors");
            foundErrors.setStackTrace(new StackTraceElement[0]);
            throw foundErrors;
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
