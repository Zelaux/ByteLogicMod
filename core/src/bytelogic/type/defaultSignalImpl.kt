package bytelogic.type

import arc.graphics.*
import bytelogic.graphics.*

open class DefaultSignalTypeImpl(name:String):SignalType(name){
    override fun barColor(signal: Signal): Color {
        val compareTo = signal.compareWithZero()
        if (compareTo > 0) {
            return BLPal.positiveSignalBarColor
        }
        return if (compareTo < 0) {
            BLPal.negativeSignalBarColor
        } else BLPal.zeroSignalBarColor
        //        Color[] colors = {BLPal.negativeSignalBarColor, BLPal.zeroSignalBarColor, BLPal.positiveSignalBarColor};
    }

    override fun color(signal: Signal): Color {
        return when (signal.compareWithZero()) {
            -1 -> BLPal.negativeSignalColor
            0 -> BLPal.zeroSignalColor
            1 -> BLPal.positiveSignalColor
            else -> throw RuntimeException("Illegal value")
        }
    }

    override fun setZero(abstractSignal: Signal) {
        abstractSignal.setNumber(0)
        abstractSignal.type = NilSignalType
    }

    override fun or(`this&signal`: Signal, signal: Signal) {
        `this&signal`.setNumber(`this&signal`.number or signal.number)
    }

    override fun xor(`this&signal`: Signal, signal: Signal) {
        `this&signal`.setNumber(`this&signal`.number xor signal.number)
    }

    override fun and(`this&signal`: Signal, signal: Signal) {
        `this&signal`.setNumber(`this&signal`.number and signal.number)
    }

    override fun plus(`this&signal`: Signal, signal: Signal) {
        `this&signal`.setNumber(`this&signal`.number + signal.number)
    }

    override fun minus(`this&signal`: Signal, signal: Signal) {
        `this&signal`.setNumber(`this&signal`.number - signal.number)
    }

    override fun div(`this&signal`: Signal, signal: Signal) {
        `this&signal`.setNumber(`this&signal`.number - signal.number)
    }

    override fun mod(`this&signal`: Signal, signal: Signal) {
        `this&signal`.setNumber(`this&signal`.number % signal.number)
    }

    override fun times(`this&signal`: Signal, signal: Signal) {
        `this&signal`.setNumber(`this&signal`.number * signal.number)
    }
}