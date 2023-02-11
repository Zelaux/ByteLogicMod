@file:Suppress("PackageDirectoryMismatch")

package bytelogic.type

import arc.graphics.*
import mindustry.gen.*

internal object NilSignalType : DefaultSignalTypeImpl("nil-type", { Tex.clear }) {
    override fun barColor(signal: Signal): Color {
//        return BLPal.zeroSignalBarColor
        return when (signal.compareWithZero()) {
            -1 -> Color.yellow;
            1 -> Color.royal;
            else -> Color.black;
        }
    }

    override fun color(signal: Signal): Color {
//        return BLPal.zeroSignalColor
        return when (signal.compareWithZero()) {
            -1 -> Color.yellow;
            1 -> Color.royal;
            else -> Color.black;
        }
    }


    override fun setZero(abstractSignal: Signal) {
        abstractSignal.setNumber(0)
        abstractSignal.type = IntegerSignalType;
    }

    override fun or(`this&signal`: Signal, signal: Signal) {
        super.or(`this&signal`, signal)
        `this&signal`.type = signal.type;
    }

    override fun xor(`this&signal`: Signal, signal: Signal) {
        super.xor(`this&signal`, signal)
        `this&signal`.type = signal.type;
    }

    override fun and(`this&signal`: Signal, signal: Signal) {
        super.and(`this&signal`, signal)
        `this&signal`.type = signal.type;
    }

    override fun plus(`this&signal`: Signal, signal: Signal) {
        super.plus(`this&signal`, signal)
        `this&signal`.type = signal.type;
    }

    override fun minus(`this&signal`: Signal, signal: Signal) {
        super.minus(`this&signal`, signal)
        `this&signal`.type = signal.type;
    }

    override fun div(`this&signal`: Signal, signal: Signal) {
        super.div(`this&signal`, signal)
        `this&signal`.type = signal.type;
    }

    override fun mod(`this&signal`: Signal, signal: Signal) {
        super.mod(`this&signal`, signal)
        `this&signal`.type = signal.type;
    }

    override fun times(`this&signal`: Signal, signal: Signal) {
        super.times(`this&signal`, signal)
        `this&signal`.type = signal.type;
    }
}