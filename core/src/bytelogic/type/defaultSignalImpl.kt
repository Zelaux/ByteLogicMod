package bytelogic.type

import arc.graphics.*
import arc.scene.style.*
import bytelogic.graphics.*
import mindustry.gen.*
import mindustry.logic.*
import kotlin.math.*

open class DefaultSignalTypeImpl(name: String, icon: () -> Drawable) : SignalType(name, icon) {
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

    override fun applyControl(`this&signal`: Signal, building: Building) {
        building.control(LAccess.enabled, `this&signal`.compareWithZero().toDouble(), 0.0, 0.0, 0.0);
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
        abstractSignal.type = IntegerSignalType
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
        `this&signal`.setNumber(`this&signal`.number / signal.number)
    }

    override fun mod(`this&signal`: Signal, signal: Signal) {
        `this&signal`.setNumber(`this&signal`.number % signal.number)
    }

    override fun times(`this&signal`: Signal, signal: Signal) {
        `this&signal`.setNumber(`this&signal`.number * signal.number)
    }

    override fun absolute(`this&signal`: Signal) {
        `this&signal`.setNumber(`this&signal`.number.absoluteValue)
    }

    override fun log(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.log(signal.number.toDouble()).toRawBits())
    }

    override fun sin(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.sin(signal.number.toDouble()).toRawBits())
    }

    override fun cos(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.cos(signal.number.toDouble()).toRawBits())
    }

    override fun tan(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.tan(signal.number.toDouble()).toRawBits())
    }

    override fun asin(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.asin(signal.number.toDouble()).toRawBits())
    }

    override fun acos(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.acos(signal.number.toDouble()).toRawBits())
    }

    override fun atan(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.atan(signal.number.toDouble()).toRawBits())
    }
}