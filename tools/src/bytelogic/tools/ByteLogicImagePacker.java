package bytelogic.tools;

import bytelogic.BLVars;
import bytelogic.gen.*;
import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;
import mma.tools.ModImagePacker;
//import testmod.gen.TMContentRegions;

public class ByteLogicImagePacker extends ModImagePacker {

    public ByteLogicImagePacker() {
    }

    @Override
    protected void start() throws Exception {
        BLVars.create();

        super.start();
    }

    @Override
    protected void preCreatingContent() {
        super.preCreatingContent();

//        TMEntityMapping.init();
    }

    @Override
    protected void runGenerators() {
        IconRasterizer.main(new String[]{"32","64"});
        new ByteLogicGenerators();
    }

    @Override
    protected void checkContent(Content content) {
        super.checkContent(content);
        if (content instanceof MappableContent){
            BLContentRegions.loadRegions((MappableContent)content);
//            TMContentRegions.loadRegions((MappableContent) content);
        }
    }

    public static void main(String[] args) throws Exception {
        new ByteLogicImagePacker();
    }

}
