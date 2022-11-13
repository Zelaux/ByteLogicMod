package bytelogic.annotations.assets;

import arc.Core;
import arc.files.Fi;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.Seq;
import arc.util.Strings;
import bytelogic.annotations.BLAnnotations;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import mma.annotations.ModBaseProcessor;
import mma.annotations.SupportedAnnotationTypes;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;

@SupportedAnnotationTypes(BLAnnotations.GenerateIconsClass.class)
public class IconsProcessor extends ModBaseProcessor {
    private static final int[] sizes = {32, 64};

    class IconField {
        String name;
        int size;

        public IconField(String name, int size) {
            this.name = name;
            this.size = size;
        }

        public String spriteName() {
            return name + "-" + size;
        }
    }

    @Override
    public void process(RoundEnvironment env) throws Exception {
        Fi child = rootDirectory.child("core/assets-raw/icons");

        Seq<IconField> fields = new Seq<>();
        for (Fi fi : child.list(".png")) {
            for (int size : sizes) {
                fields.add(new IconField(fi.nameWithoutExtension(), size));
            }
        }
        TypeSpec.Builder drawables = TypeSpec.classBuilder("Drawables")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        TypeSpec.Builder regions = TypeSpec.classBuilder("Regions")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        MethodSpec.Builder load = MethodSpec.methodBuilder("load")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for (IconField field : fields) {
            String camel = Strings.kebabToCamel(field.name);
            if (camel.matches(".*\\d"))camel=camel+"_";
            String fieldName = camel + field.size;
            drawables.addField(TextureRegionDrawable.class, fieldName, Modifier.PUBLIC, Modifier.STATIC);
            regions.addField(TextureRegion.class, fieldName, Modifier.PUBLIC, Modifier.STATIC);

            load.addStatement("Drawables.$L = ($T)$T.atlas.drawable(mma.ModVars.fullName($S)) ", fieldName, TextureRegionDrawable.class, Core.class, field.spriteName());
            load.addStatement("Regions.$L = $T.atlas.find(mma.ModVars.fullName($S))", fieldName, Core.class, field.spriteName());
        }

        write(TypeSpec.classBuilder(classPrefix() + "Icons")
                .addModifiers(Modifier.PUBLIC)
                .addType(drawables.build())
                .addType(regions.build())
                .addMethod(load.build())
        );
    }
}
