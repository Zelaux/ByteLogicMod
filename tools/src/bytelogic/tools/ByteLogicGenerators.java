package bytelogic.tools;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.struct.*;
import bytelogic.gen.*;
import bytelogic.world.blocks.logic.*;
import mindustry.*;
import mma.tools.*;
import mma.type.pixmap.*;
import org.w3c.dom.*;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.util.*;

import static bytelogic.BLVars.fullName;
import static mma.tools.gen.MindustryImagePacker.*;

public class ByteLogicGenerators extends ModGenerators{
    @Override
    protected void run(){
        generate("binary-blocks-staff", this::binaryBlocksStaff);
        super.run();
        generate("animated-logo", this::animatedLogo);
    }

    private void binaryBlocksStaff(){
        Seq<BinaryLogicBlock> blocks = Vars.content.blocks().select(it -> it instanceof BinaryLogicBlock).<BinaryLogicBlock>as();
        for(BinaryLogicBlock block : blocks){
            block.load();
            block.loadIcon();

            Pixmap regionPixmap = get(block.region);

            if(!block.outputsRegion.found()){
                Pixmap copy = regionPixmap.copy();
                copy.each((x, y) -> {
                    if(x >= 3 && y >= 3 && x < copy.width - 3 && y < copy.height - 3){
                        copy.set(x, y, Color.clearRgba);
                    }
                });
                ModImagePacker.save(copy, ((AtlasRegion)block.outputsRegion).name);
            }
            if(!block.sideOutputsRegion.found()){
                Pixmap copy;
                if(!block.outputsRegion.found()){
                    copy = regionPixmap.copy();
                    copy.each((x, y) -> {
                        if(x >= 3 && y >= 3 && x < copy.width - 3 && y < copy.height - 3){
                            copy.set(x, y, Color.clearRgba);
                        }
                    });
                }else{
                    copy = get(block.outputsRegion);
                }
                PixmapProcessor.rotatePixmap(copy, 1);
                ModImagePacker.save(copy, ((AtlasRegion)block.sideOutputsRegion).name);
            }
            if(!block.centerRegion.found()){
                Pixmap copy = regionPixmap.copy();
                copy.each((x, y) -> {
                    if(x < 3 || y < 3 || x >= copy.width - 3 || y >= copy.height - 3){
                        copy.set(x, y, Color.clearRgba);
                    }
                });
                ModImagePacker.save(copy, ((AtlasRegion)block.centerRegion).name);
            }
//            BLContentRegions.loadRegions(block);
//            block.load();
//            block.loadIcon();
        }
    }

    private void animatedLogo(){
        try{
            String[] imageatt = new String[]{
            "imageLeftPosition",
            "imageTopPosition",
            "imageWidth",
            "imageHeight"
            };

            ImageReader reader = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
            Fi rootGif = Fi.get("../logo/logo-gif.gif");
            ImageInputStream ciis = ImageIO.createImageInputStream(rootGif.file());
            reader.setInput(ciis, false);

            int noi = reader.getNumImages(true);
            BufferedImage master = null;
            byte[] previousPixels = null;
            Seq<Pixmap> frames = new Seq<>();
            for(int i = 0; i < noi; i++){
                BufferedImage image = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);

                Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");
                NodeList children = tree.getChildNodes();

                for(int j = 0; j < children.getLength(); j++){
                    Node nodeItem = children.item(j);

                    if(nodeItem.getNodeName().equals("ImageDescriptor")){
                        Map<String, Integer> imageAttr = new HashMap<String, Integer>();

                        for(int k = 0; k < imageatt.length; k++){
                            NamedNodeMap attr = nodeItem.getAttributes();
                            Node attnode = attr.getNamedItem(imageatt[k]);
                            imageAttr.put(imageatt[k], Integer.valueOf(attnode.getNodeValue()));
                        }
                        if(i == 0){
                            master = new BufferedImage(imageAttr.get("imageWidth"), imageAttr.get("imageHeight"), BufferedImage.TYPE_INT_ARGB);
                        }
                        master.getGraphics().drawImage(image, imageAttr.get("imageLeftPosition"), imageAttr.get("imageTopPosition"), null);
                    }
                }

                Fi tmp = Fi.get("../logo/logo_temp.png");
                ImageIO.write(master, "PNG", tmp.file());
                Pixmap pixmap = new Pixmap(tmp);
                ByteBuffer byteBuffer = pixmap.getPixels().duplicate();
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                boolean sameAsPrevious = false;
                if(previousPixels != null && Arrays.equals(bytes, previousPixels)){
                    sameAsPrevious = true;
                }else{
                    previousPixels = bytes;
                }
                tmp.delete();
                if(!sameAsPrevious){
                    frames.add(pixmap);
                }
            }
            for(int i = 0; i < frames.size; i++){
                Fi.get("../logo/logo-" + i + ".png").writePng(frames.get(i));
            }
            Fi.get("../" + fullName("logo-info.properties")).writeString("size = " + frames.size);
            rootGif.delete();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void blockIcons(){
        super.blockIcons();
    }

    @Override
    protected void unitIcons(){
        super.unitIcons();
    }
}
