package bytelogic.tools;

import bytelogic.BLVars;
import bytelogic.gen.*;
import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;
import mma.tools.ModImagePacker;
//import testmod.gen.TMContentRegions;

public class TMImagePacker extends ModImagePacker {

    public TMImagePacker() {
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
        new TMGenerators();
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
        new TMImagePacker();
    }

}
