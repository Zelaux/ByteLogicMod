package bytelogic.world.blocks.logic;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.gen.BLIcons.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.*;
import bytelogic.ui.guide.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mma.io.*;
import org.jetbrains.annotations.*;

public class SignalTimer extends UnaryLogicBlock{
    protected static final ByteReads tmpRead = new ByteReads();
    protected static final ByteWrites tmpWrite = new ByteWrites();
    public int maxDelay = (int)Time.toSeconds;

    public SignalTimer(String name){
        super(name);
        rotate = true;
        configurable = true;
        config(byte[].class, (SignalTimerBuild build, byte[] value) -> {
            tmpRead.setBytes(value);
            int version = tmpRead.i();
            build.setDelay(tmpRead.i());
            build.inputType = tmpRead.i();
        });
    }

    @Override
    public void init(){

        super.init();

        blockPreview = new BlockShowcase(this, 5, 5, blockPreview.getWorldBuilder()){
            @Override
            public boolean shouldBuildConfiguration(@NotNull Block block){
                return super.shouldBuildConfiguration(block) || block instanceof SignalTimer;
            }
        };
    }

    public static byte[] stateToBytes(int delay, int inputType){
        tmpWrite.reset();
        tmpWrite.i(0);
        tmpWrite.i(delay);
        tmpWrite.i(inputType);
        return tmpWrite.getBytes();
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
            Seq<MultiBar.BarPart> with = Seq.with();
            int[] currentDelay = {build.currentDelay};
            Runnable rebuild = () -> {
                with.clear();
                int amount = currentDelay[0];
                for(int i = 0; i < amount; i++){
                    int finalI = i;
                    with.add(new MultiBar.BarPart(() -> {
                        Signal signal = build.signalsQueue[Mathf.mod(amount - finalI - 1 + build.tickCounter, amount)];
                        return signal.barColor();
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

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){
        if(req.config instanceof Integer integer){
            req.config = stateToBytes(integer, backInput);
            drawPlanRegion(req, list);
            return;
        }
        if(!(req.config instanceof byte[] bytes)){
            super.drawPlanRegion(req, list);
            return;
        }
        try{
            Container.set(bytes);
        }catch(Exception e){
            byte[] stateToBytes = stateToBytes(1, backInput);
            req.config = stateToBytes;
            Container.set(stateToBytes);
        }

        if(Container.inputType == backInput){
            super.drawPlanRegion(req, list);
            return;
        }
        TextureRegion back = base;
        Draw.rect(back, req.drawx(), req.drawy(),
            back.width * req.animScale * Draw.scl,
            back.height * req.animScale * Draw.scl,
            0);

        Draw.rect(sideRegion, req.drawx(), req.drawy(),
            region.width * req.animScale * Draw.scl,
            region.height * req.animScale * Draw.scl * Mathf.sign(Container.inputType == leftInput),
            req.rotation * 90);
    }


    @Override
    public void flipRotation(BuildPlan req, boolean x){
        if(req.config instanceof Integer integer){
            req.config = stateToBytes(integer, backInput);
            flipRotation(req, x);
            return;
        }
        if(!(req.config instanceof byte[] bytes)){
            super.flipRotation(req, x);
            return;
        }
        Container.set(bytes);


        if((req.rotation % 2 == 0) == x){
            req.rotation = Mathf.mod(req.rotation + 2, 4);
        }
        if(Container.inputType == leftInput){
            Container.inputType = rightInput;
        }else{
            Container.inputType = leftInput;
        }
        req.config = Container.bytes();

    }

    protected Signal[] createTmpSignals(){
        Signal[] signals = new Signal[maxDelay];
        for(int i = 0; i < signals.length; i++){
            signals[i] = new Signal();
        }
        return signals;
    }

    static class Container{
        static int delay;
        static int inputType;

        static void set(byte[] bytes){
            tmpRead.setBytes(bytes);
            tmpRead.i();
            delay = tmpRead.i();
            inputType = tmpRead.i();
        }

        public static byte[] bytes(){
            return stateToBytes(delay, inputType);
        }
    }

    public class SignalTimerBuild extends UnaryLogicBuild{
        private final Signal[] tmpSignals = createTmpSignals();
        public int tickCounter = 0;
        public Signal[] signalsQueue;
        private int currentDelay = 1;

        {
            setDelay(1);
        }

        @Override
        public void buildConfiguration(Table table){
//            super.buildConfiguration(table);
            table.table(Tex.pane, t -> {
                t.table(it -> {
                    Boolf<TextButton> zeroChecker = any -> currentDelay == 1;
                    Boolf<TextButton> maxChecker = any -> currentDelay == maxDelay;
                    it.defaults().width(32f);
//                it.button("-15", () -> configure(Math.max(0, currentDelay - 15))).disabled(zeroChecker);
//                it.button("-10", () -> configure(Math.max(0, currentDelay - 10))).disabled(zeroChecker);
                    TextButtonStyle textButtonStyle = Styles.flatBordert;
                    it.button("-5", textButtonStyle, () -> configureState(Math.max(1, currentDelay - 5), inputType)).disabled(zeroChecker);
                    it.button("-1", textButtonStyle, () -> configureState(Math.max(1, currentDelay - 1), inputType)).disabled(zeroChecker);
                    it.label(() -> currentDelay + "").labelAlign(Align.center);
                    it.center();
                    it.button("+1", textButtonStyle, () -> configureState(Math.min(maxDelay, currentDelay + 1), inputType)).disabled(maxChecker);
                    it.button("+5", textButtonStyle, () -> configureState(Math.min(maxDelay, currentDelay + 5), inputType)).disabled(maxChecker);
                });
                t.row();
                t.table(inputButtons -> {
                    ButtonStyle style = new ButtonStyle(Styles.togglet);
                    ButtonGroup<Button> group = new ButtonGroup<>();
                    for(int i : new int[]{leftInput, backInput, rightInput}){

//                        int i = i;
                        float tailOffset = switch(i){
                            case backInput -> 0;
                            case leftInput -> -90;
                            case rightInput -> 90;
                            default -> throw new RuntimeException("Impossible value");
                        };

                        inputButtons.button(button -> {
                            button.setStyle(style);
                            Image arrow = new Image(Drawables.unaryInputArrow64);
                            Image tail = new Image(Drawables.unaryInputBack64);
                            button.stack(arrow, tail).update(_n -> {
                                arrow.setRotationOrigin(rotdeg(), Align.center);
                                tail.setRotationOrigin(rotdeg() + tailOffset, Align.center);
                            }).size(32f);
                        }, () -> configureState(currentDelay, i)).checked(i == inputType).size(48f).with(group::add);
                    }
                });
//                it.button("+10", () -> configure(Math.min(maxDelay, currentDelay + 10))).disabled(maxChecker);
//                it.button("+15", () -> configure(Math.min(maxDelay, currentDelay + 15))).disabled(maxChecker);
            }).growX();
        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            Building build = switch(inputType){
                case backInput -> back();
                case leftInput -> left();
                case rightInput -> right();
                default -> null;
            };
            if(build == otherBuilding) return super.acceptSignal(otherBuilding, signal);
            return false;
        }

        private void configureState(int currentDelay, int inputType){
            configure(stateToBytes(currentDelay, inputType));
        }

        @Override
        public Object config(){
            return stateToBytes(currentDelay, inputType);
        }

        @Override
        public void draw(){
            if(inputType == backInput){
                super.draw();
                return;
            }
            sideRegion.flip(false, inputType == rightInput);
            Draw.rect(base, tile.drawx(), tile.drawy());

            Draw.color(signalColor());

            Draw.rect(sideRegion, x, y, drawrot());

            this.drawTeamTop();
            Draw.color();
            sideRegion.flip(false, inputType == rightInput);


        }

        @Override
        public void updateTableAlign(Table table){

            Vec2 pos = Core.input.mouseScreen(x, y /*- (block.size * tilesize) / 2f - 1*/);
            table.setPosition(pos.x, pos.y, Align.top);
        }

        @Override
        public boolean canOutputSignal(int dir){
            return dir == rotation && front() instanceof ByteLogicBuildingc;
        }

        public void setDelay(int delay){
            currentDelay = delay;
            signalsQueue = new Signal[delay];
            for(int i = 0; i < signalsQueue.length; i++){
                signalsQueue[i] = tmpSignals[i];
                signalsQueue[i].setZero();
            }
//            System.arraycopy(tmpSignals, 0, signalsQueue, 0, delay);
            tickCounter = 0;
        }

        @Override
        public void updateSignalState(){
            if(signalsQueue.length == 0){
                lastSignal.set(nextSignal);
            }else{
                signalsQueue[tickCounter].set(nextSignal);
                tickCounter = Mathf.mod(tickCounter + 1, signalsQueue.length);
                lastSignal.set(signalsQueue[tickCounter]);
            }
            nextSignal.setZero();
        }

        @Override
        public byte version(){
            return (byte)(0b1001_0000 +/* 0b10000 +*/ 1);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            /*write.i(tickCounter);
            write.i(currentDelay);
            for (int i : signalsQueue) {
                write.i(i);
            }
            write.i(inputType);*/
        }

        @Override
        public void read(Reads read, byte revision){
            byte parentRevision = (byte)(revision & (0b0000_1111));

            if(revision == 0){
                Signal.valueOf(nextSignal, read.i());
                lastSignal.set(nextSignal);
            }else{
                int version = read.i();
                if(version == 2) return;
                Signal.valueOf(nextSignal, read.i());
                Signal.valueOf(lastSignal, read.i());
            }
            if(revision < 0) return;
            revision /= 0x10;
            int tickCounter1 = read.i();
            setDelay(read.i());
            tickCounter = tickCounter1;
            for(int i = 0; i < signalsQueue.length; i++){
                signalsQueue[i] = Signal.valueOf(read.i());
            }
            if(revision == 0) return;
            inputType = read.i();


        }

        @Override
        public void customWrite(Writes write){
            write.i(tickCounter);
            write.i(currentDelay);
            for(Signal signal : signalsQueue){
                signal.write(write);
            }
            write.i(inputType);
        }

        @Override
        public void customRead(Reads read){
            int tickCounter1 = read.i();
            setDelay(read.i());
            tickCounter = tickCounter1;
            for(int i = 0; i < signalsQueue.length; i++){
                signalsQueue[i].read(read);
            }
            inputType = read.i();
        }

        @Override
        public short customVersion(){
            return 1;
        }
    }
}
