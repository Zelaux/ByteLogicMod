package bytelogic.world.blocks.logic;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.ui.*;
import bytelogic.ui.MultiBar.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

public class SignalTimer extends AcceptorLogicBlock{
    public final int delayTimer = timers++;
    public int maxDelay = (int)Time.toSeconds;

    public SignalTimer(String name){
        super(name);
        rotate = true;
        configurable = true;
        config(Integer.class, (SignalTimerBuild build, Integer value) -> {
            build.setDelay(value);
        });
    }

    @Override
    public void setBars(){
        super.setBars();
        this.<SignalTimerBuild>addBar("signal-timer.delay", build -> {
            return new Bar(() -> {
                return Core.bundle.format("signal-timer.delay", build.currentDelay);
            }, () -> Pal.items, () -> 1f);
        });
        this.<SignalTimerBuild>addBar("signal-timer.signals", build -> {
            Seq<BarPart> with = Seq.with();
            int[] currentDelay = {build.currentDelay};
            Runnable rebuild = () -> {
                with.clear();
                int amount = currentDelay[0];
                for(int i = 0; i < amount; i++){
                    int finalI = i;
                    with.add(new BarPart(() -> {
                        int signal = build.signalsQueue[Mathf.mod(amount - finalI - 1 + build.tickCounter, amount)];
                        return signal > 0f ? Pal.accent : (signal < 0 ? Pal.remove : Color.darkGray);
                    }, () -> 1f));
                }
            };
            rebuild.run();
            MultiBar multiBar = new MultiBar("", with);
            multiBar.update(() -> {
                if(currentDelay[0] != build.currentDelay){
                    currentDelay[0] = build.currentDelay;
                    rebuild.run();
                }
                multiBar.updateParts();
            });
            return multiBar;
            /*return new Bar(() -> {
                return "";
//                return Core.bundle.format("signal-timer.signals", build.currentDelay);
            }, () -> Pal.items, () -> 1f);*/
        });
    }

    public class SignalTimerBuild extends AcceptorLogicBuild{
        public int tickCounter = 0;
        private int currentDelay = 1;
        public int[] signalsQueue = new int[currentDelay];

        @Override
        public void buildConfiguration(Table table){
//            super.buildConfiguration(table);
            table.table(Tex.pane, it -> {
                Boolf<TextButton> zeroChecker = any -> currentDelay ==1;
                Boolf<TextButton> maxChecker = any -> currentDelay == maxDelay;
                it.defaults().width(32f);
//                it.button("-15", () -> configure(Math.max(0, currentDelay - 15))).disabled(zeroChecker);
//                it.button("-10", () -> configure(Math.max(0, currentDelay - 10))).disabled(zeroChecker);
                TextButtonStyle textButtonStyle = Styles.flatBordert;
                it.button("-5", textButtonStyle, () -> configure(Math.max(1, currentDelay - 5))).disabled(zeroChecker);
                it.button("-1", textButtonStyle, () -> configure(Math.max(1, currentDelay - 1))).disabled(zeroChecker);
                it.label(() -> currentDelay + "").labelAlign(Align.center);
                it.center();
                it.button("+1", textButtonStyle, () -> configure(Math.min(maxDelay, currentDelay + 1))).disabled(maxChecker);
                it.button("+5", textButtonStyle, () -> configure(Math.min(maxDelay, currentDelay + 5))).disabled(maxChecker);
//                it.button("+10", () -> configure(Math.min(maxDelay, currentDelay + 10))).disabled(maxChecker);
//                it.button("+15", () -> configure(Math.min(maxDelay, currentDelay + 15))).disabled(maxChecker);
            }).growX();
        }

        @Override
        public Object config(){
            return currentDelay;
        }

        @Override
        public void updateTableAlign(Table table){

            Vec2 pos = Core.input.mouseScreen(x, y /*- (block.size * tilesize) / 2f - 1*/);
            table.setPosition(pos.x, pos.y, Align.top);
        }

        @Override
        public boolean output(int dir){
            return dir == rotation && front() instanceof LogicBuild;
        }

        public void setDelay(int delay){
            currentDelay = delay;
            signalsQueue = new int[delay];
            tickCounter = 0;
        }

        @Override
        public void updateSignalState(){
            if(signalsQueue.length == 0){
                lastSignal = nextSignal;
            }else{
                signalsQueue[tickCounter] = nextSignal;
                tickCounter = Mathf.mod(tickCounter + 1, signalsQueue.length);
                lastSignal = signalsQueue[tickCounter];
            }
            nextSignal = 0;
        }

        /* @Override
         public int signal(){
             int signal = super.signal();
             if(signalsQueue.length == 0) return signal;
             signalsQueue[tickCounter] = signal;
             tickCounter = Mathf.mod(tickCounter + 1, signalsQueue.length);
             return signalsQueue[tickCounter];
         }
 */
        @Override
        public void write(Writes write){
            super.write(write);
            write.i(tickCounter);
            write.i(currentDelay);
            for(int i : signalsQueue){
                write.i(i);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            int tickCounter1 = read.i();
            setDelay(read.i());
            tickCounter = tickCounter1;
            for(int i = 0; i < signalsQueue.length; i++){
                signalsQueue[i] = read.i();
            }


        }
    }
}
