package bytelogic.type

import arc.graphics.*
import arc.scene.style.*
import arc.util.*
import mindustry.gen.*

abstract class SignalType(val name: String, iconInitializer: () -> Drawable) {
    val icon by lazy(iconInitializer)
    val id: Int = all.size

    init {
        val newSize = all.size + 1
        val prev = all
        //reallocate the array and copy everything over; performance matters very little here anyway
        all = Array(newSize) { i ->
            if (i < prev.size)
                prev[i]
            else
                this
        }
//        System.arraycopy(prev, 0, all, 0, prev.size)
//        all[prev.size] = this
    }

    abstract fun barColor(signal: Signal): Color

    abstract fun color(signal: Signal): Color

    fun set(`this$signal`: Signal, signal: Signal) {
        `this$signal`.type = signal.type;
        `this$signal`.setNumber(signal.number);
    }

    abstract fun setZero(abstractSignal: Signal)

    abstract fun or(`this&signal`: Signal, signal: Signal)
    abstract fun xor(`this&signal`: Signal, signal: Signal)

    abstract fun and(`this&signal`: Signal, signal: Signal)
    abstract fun plus(`this&signal`: Signal, signal: Signal)
    abstract fun minus(`this&signal`: Signal, signal: Signal)
    abstract fun div(`this&signal`: Signal, signal: Signal)
    abstract fun mod(`this&signal`: Signal, signal: Signal)
    abstract fun times(`this&signal`: Signal, signal: Signal)
    abstract fun applyControl(`this&signal`: Signal, building: Building)
    open fun toString(signal: Signal): String {
        return signal.number.toString()
    }

    abstract fun absolute(`this&signal`: Signal)
    abstract fun log(signal: Signal)
    abstract fun sin(signal: Signal)
    abstract fun cos(signal: Signal)
    abstract fun tan(signal: Signal)
    abstract fun asin(signal: Signal)
    abstract fun acos(signal: Signal)
    abstract fun atan(signal: Signal)

    companion object {
        @JvmStatic
        fun findByName(name: String): SignalType {
            return all.find { it.name == name } ?: run {
                Log.err("Cannot find signal type with name $name")
                SignalTypes.nilType
            }
        }

        @kotlin.jvm.JvmField
        var all = emptyArray<SignalType>()
    }
}


object SignalTypes {
    @JvmField
    val nilType: SignalType = NilSignalType

    @JvmField
    val colorType: SignalType = ColorSignalType

    @JvmField
    val numberType: SignalType = IntegerSignalType
    @JvmField
    val floatType: SignalType = FloatSignalType

    @JvmField
    val contentType: ContentSignalType = ContentSignalType

    //may be item and liquid type? hmmm what about color type?
}