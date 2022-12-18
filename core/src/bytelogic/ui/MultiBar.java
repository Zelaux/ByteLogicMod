package bytelogic.ui;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.struct.*;
import arc.util.pooling.*;
import mindustry.gen.*;
import mindustry.ui.*;

public class MultiBar extends Bar{
    private static Rect scissor = new Rect();
    Seq<BarPart> barParts = new Seq<>();
    private String name = "";

    public MultiBar(String name, Seq<BarPart> barParts){
        this.barParts = barParts;
        this.name = Core.bundle.get(name, name);
        this.update(() -> {
            updateParts();
        });
    }

    public MultiBar(Prov<String> name, Seq<BarPart> barParts){
        this.barParts = barParts;
        this.update(() -> {
            updateParts();
            try{
                this.name = (String)name.get();
            }catch(Exception var4){
                this.name = "";
            }

        });
    }

    public static float normalize(float f){
        if(Float.isNaN(f)){
            return 0.0F;
        }

        if(Float.isInfinite(f)){
            return 1.0F;
        }
        return f;
    }

    public void reset(float value){
        float v = value / barParts.size;
        barParts.each((part) -> {
            part.value = part.lastValue = part.blink = v;
        });
    }

    public void updateParts(){
        this.barParts.each(BarPart::update);
    }

    public void drawParts(){
        float[] offset = new float[]{x, x};

        this.barParts.each((part) -> {
            part.parentAlpha=parentAlpha;
            part.draw(offset[0], this.y, this.width, this.height, offset, this.barParts);
            offset[0] = part.offset;
        });
    }

    public void set(Prov<String> name, Seq<BarPart> barParts){
        this.barParts = barParts;
        this.update(() -> {
            this.name = (String)name.get();
            updateParts();
        });
    }

    public Bar blink(Color color){
        return this;
    }

    public void draw(){
        if(barParts == null || barParts.size <= 0) return;
        Drawable bar = Tex.bar;
        Draw.colorl(0.1F);
        bar.draw(this.x, this.y, this.width, this.height);

        drawParts();

        Draw.color();
        Font font = Fonts.outline;
        GlyphLayout lay = (GlyphLayout)Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, this.name);
        font.setColor(Color.white);
        font.draw(this.name, this.x + this.width / 2.0F - lay.width / 2.0F, this.y + this.height / 2.0F + lay.height / 2.0F + 1.0F);
        Pools.free(lay);
    }

    public static class BarPart{
        public float lastValue = 0;
        public float blink = 0;
        public float value = 0;
        public Color color;
        public Color blinkColor = new Color();
        public Floatp fraction;
        public float parentAlpha;
        Runnable runnable = () -> {
        };
        float x, y, width, height, offset;

        public BarPart(Color color, Floatp fraction){
            this.fraction = fraction;
            this.blinkColor.set(color);
            this.lastValue = this.value = fraction.get();
            this.color = color.cpy();
        }

        public BarPart(Prov<Color> color, Floatp fraction){
            this.fraction = fraction;

            try{
                this.lastValue = this.value = Mathf.clamp(fraction.get());
            }catch(Exception var5){
                this.lastValue = this.value = 0.0F;
            }
            this.color = new Color();
            this.update(() -> {
                try{
                    Color calculatedColor = color.get();
                    blinkColor.set(calculatedColor);
                    this.color.set(calculatedColor);
                }catch(Exception ignored){
                }

            });
        }

        public void update(Runnable runnable){
            this.runnable = runnable;
        }

        public void update(){
            runnable.run();
        }

        public void draw(float x, float y, float width, float height, float[] data, Seq<BarPart> barParts){
            if(this.fraction == null) return;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
           drawInternal(data,barParts);


        }

        private void drawInternal(float[] data, Seq<BarPart> barParts){
            float computed;
            try{
                computed = Mathf.clamp(fraction.get());
            }catch(Exception var7){
                computed = 0.0F;
            }

            if(lastValue > computed){
                blink = 1.0F;
                lastValue = computed;
            }
            lastValue = normalize(lastValue);
            value = normalize(value);
            computed = normalize(computed);

            blink = Mathf.lerpDelta(blink, 0.0F, 0.2F);
            value = Mathf.lerpDelta(value, computed, 0.15F);
            Drawable top = Tex.barTop;
            float topWidth = width * value;
            topWidth /= barParts.size;
            topWidth = Mathf.round(topWidth);

            Draw.color(color, blinkColor, blink);
            Draw.alpha(parentAlpha);

            if(ScissorStack.push(scissor.set(x + 1, y, topWidth - 2, height))){
                top.draw(data[1], y, width, height);
                offset = x + topWidth;
                ScissorStack.pop();
            }else{
                offset = x;
            }
        }
    }
}
