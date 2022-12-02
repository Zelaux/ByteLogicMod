package bytelogic.world.blocks.logic;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.graphics.Pal;

public class DisplayBlock extends AcceptorLogicBlock {

    public DisplayBlock(String name) {
        super(name);
        rotate = false;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{Core.atlas.find(name)};
    }

    public class DisplayBuild extends AcceptorLogicBuild {

        @Override
        public void draw() {
            super.draw();
            //TODO buffer
            Draw.rect(region, tile.drawx(), tile.drawy());

            float dw = 2, dh = 2, xs = 2f, ys = 2f;

            int w = 5, h = 5;
            long signal = currentSignal().number();
            if (signal == 0) return;
            if (signal < 0) Draw.color(Pal.remove);
            signal &= ~0b100_0000_0000_0000_0000_0000_0000_0000;
            if (signal < 0) signal = -signal;
//            int jjj = 2 * ;


            for (int i = 0; i < w * h; i++) {
                int x = i % w;
                int y = i / w;

                if ((signal & (1 << i)) != 0) {
                    Fill.rect(tile.drawx() + x * xs - (w - 1) * xs / 2f, tile.drawy() + y * ys - (h - 1) * ys / 2f, dw, dh);
                }
            }
        }
    }
}
