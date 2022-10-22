package bytelogic.ui.fragments;

public class ModHudFragment {
    public static void init() {
        /*BLVars.modLog("ModHudFragment.startInit()");

        try {
            Table overlaymarker = ui.hudGroup.find("overlaymarker");
            Table mobile_buttons = overlaymarker.find("mobile buttons");
            Table status = overlaymarker.<Stack>find("waves/editor").<Table>find("waves").<Table>find("status");
            Stack stack = status.<Stack>find(el -> el.getClass().getSimpleName().equals("Stack") && el.toString().contains("HudFragment$1"));
            SnapshotSeq<Element> children = stack.getChildren();
            int fragIndex = children.indexOf(el -> el.getClass().getSimpleName().equals("HudFragment$1") || el.toString().equals("HudFragment$1"));
            if (fragIndex == -1) {
                Log.info("status_ERROR: @", status);
                Log.info("stack_ERROR: @", stack);
                return;
            }
            Element oldFrag = children.get(fragIndex);
            oldFrag.parent = null;
            oldFrag.remove();
        } catch (Exception e) {
            Log.err("cannot load stealtBar reason: @", e);
        }*/
    }/*

    static class SideBar extends arc.scene.Element {
        public final Floatp amount;
        public final boolean flip;
        public final Boolp flash;

        float last, blink, value;

        public SideBar(Floatp amount, Boolp flash, boolean flip) {
            this.amount = amount;
            this.flip = flip;
            this.flash = flash;

            setColor(Pal.health);
        }

        @Override
        public void draw() {
            float next = amount.get();

            if (Float.isNaN(next) || Float.isInfinite(next)) next = 1f;

            if (next < last && flash.get()) {
                blink = 1f;
            }

            blink = Mathf.lerpDelta(blink, 0f, 0.2f);
            value = Mathf.lerpDelta(value, next, 0.15f);
            last = next;

            if (Float.isNaN(value) || Float.isInfinite(value)) value = 1f;

//            width=Scl.scl(20f);
            x+=width/2f * 0.35f;

            drawInner(Pal.darkishGray, 1f);
            drawInner(Tmp.c1.set(color).lerp(Color.white, blink), value);
        }

        void drawInner(Color color, float fract) {
            if (fract < 0) return;

            fract = Mathf.clamp(fract);
            if (flip) {
                x += width;
                width = -width;
            }

            float stroke = width * 0.35f/2f;
            float bh = height / 2f;
            Draw.color(color);

            float f1 = Math.min(fract * 2f, 1f), f2 = (fract - 0.5f) * 2f;

            float realWidth = width - stroke-stroke;
            float bo = -(1f - f1) * realWidth;

            Fill.quad(
                    x, y,
                    x + stroke, y,
                    x + realWidth+stroke + bo, y + bh * f1,
                    x + realWidth + bo, y + bh * f1
            );

            if (f2 > 0) {
                float bx = x + realWidth * (1f - f2);
                Fill.quad(
                        x + realWidth+stroke, y + bh,
                        x + realWidth, y + bh,
                        bx, y + height * fract,
                        bx + stroke, y + height * fract
                );
            }

            Draw.reset();

            if (flip) {
                width = -width;
                x -= width;
            }
        }
    }*/
}
