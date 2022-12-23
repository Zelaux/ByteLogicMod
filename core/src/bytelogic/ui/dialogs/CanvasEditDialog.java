package bytelogic.ui.dialogs;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import bytelogic.*;
import bytelogic.ui.*;
import bytelogic.world.blocks.logic.*;
import bytelogic.world.blocks.logic.SignalBlock.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

import static mindustry.Vars.*;

public class CanvasEditDialog extends BaseDialog{
    final Cons<Long> setter;
    final Prov<Long> getter;

    public CanvasEditDialog(SignalLogicBuild build){
        super("@block.editsignal.as-image");
        getter = () -> build.currentSignal().number();
        setter = value -> build.configureNumber(value);
        setup();
    }

    public CanvasEditDialog( Cons<Long> setter, Prov<Long> getter){
        super("@block.editsignal.as-image");
        this.setter = setter;
        this.getter = getter;
        setup();
    }

    private void setup(){
        var disabledColor = Color.grays(0.2f);
        cont.table(Tex.pane, table -> {
            table.defaults().pad(3);
            long canvasSize = DisplayBlock.canvasSize;
            table.stack(new Element(){
                long lastX, lastY;
                boolean coloring;

                {
                    addListener(new InputListener(){
                        long convertX(float ex){
                            return (long)((ex - x) / width * canvasSize);
                        }

                        long convertY(float ey){
                            return (long)((ey - y) / height * canvasSize);
                        }

                        @Override
                        public boolean touchDown(InputEvent event, float ex, float ey, int pointer, KeyCode button){
                            long cx = convertX(ex), cy = convertY(ey);

                            draw(cx, cy, true);
                            lastX = cx;
                            lastY = cy;
                            return true;
                        }

                        @Override
                        public void touchDragged(InputEvent event, float ex, float ey, int pointer){
                            long cx = convertX(ex), cy = convertY(ey);
                            Bresenham2Long.line(lastX, lastY, cx, cy, (x, y) -> draw(x, y, false));
                            lastX = cx;
                            lastY = cy;
                        }
                    });
                }

                void draw(long x, long y, boolean first){
                    long pow = y * canvasSize + x;
                    long mask = 1L << pow;
                    long number = getter.get();
                    boolean currentColor = (number & mask) != 0;
                    if(first){
                        coloring = !currentColor;
                    }
                    if(coloring){
                        setter.get(number & ~mask | mask);
                    }else{
                        setter.get(number & ~mask);
                    }
                }

                @Override
                public void draw(){
//                                Tmp.tr1.set(texture);
                    Draw.alpha(parentAlpha);
//                                Draw.rect(Tmp.tr1, x + width / 2f, y + height / 2f, width, height);
                    float rectWidth = width / canvasSize;
                    float rectHeight = height / canvasSize;

                    Long number = getter.get();
                    for(long dy = 0; dy < canvasSize; dy++){
                        for(long dx = 0; dx < canvasSize; dx++){
                            long pow = dy * canvasSize + dx;
                            long mask = 1L << pow;
                            if((number & mask) != 0){
                                Draw.color(Color.white);
                            }else{
                                Draw.color(disabledColor);
                            }
                            Fill.rect(dx * width / canvasSize + rectWidth / 2f + x, dy * height / canvasSize + rectHeight / 2f + y, rectWidth, rectHeight);
                        }
                    }
                    Draw.color(Color.black);//grid color
                }
            }, new GridImageLong(canvasSize, canvasSize){{
                touchable = Touchable.disabled;
            }}).size(mobile && !Core.graphics.isPortrait() ? Math.min(290f, Core.graphics.getHeight() / Scl.scl(1f) - 75f / Scl.scl(1f)) : 480f);
        });
//                    getCell(cont).expand(false, false);
        addCloseButton();
        this.hidden(control.input.config::hideConfig);
    }
}
