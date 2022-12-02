package bytelogic.type

import arc.graphics.*

abstract class SignalType(val name: String) {
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

    companion object {
        var all = emptyArray<SignalType>()
    }
}


object SignalTypes {
    @JvmField
    val colorType: SignalType = ColorSignalType

    @JvmField
    val nilType: SignalType = NilSignalType

    @JvmField
    val numberType: SignalType = NumberSignalType

    //may be item and liquid type? hmmm what about color type?
}