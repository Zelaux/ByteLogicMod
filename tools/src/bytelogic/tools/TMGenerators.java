package bytelogic.tools;

import arc.files.*;
import arc.graphics.*;
import arc.struct.*;
import mma.tools.*;
import org.w3c.dom.*;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;

import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.util.*;

import static bytelogic.BLVars.fullName;
import static mma.tools.gen.MindustryImagePacker.generate;

public class TMGenerators extends ModGenerators{
    @Override
    protected void run(){
        super.run();
        generate("animated-logo", this::animatedLogo);
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
            Fi.get("../"+fullName("logo-info.properties")).writeString("size = "+frames.size);
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
