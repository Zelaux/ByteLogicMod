package bytelogic.type

import arc.graphics.*
import arc.util.io.*
import mindustry.gen.*
import mma.io.*


typealias NUMBER_TYPE = Long

open class Signal {
    companion object {
        @JvmField
        val tmpReads: ByteReads = ByteReads()

        @JvmField
        val tmpWrites: ByteWrites = ByteWrites()
        val nbits: Int = 64

        @JvmStatic
        fun valueOf(int: Int) = valueOf(Signal(), int.toLong())

        @JvmStatic
        fun valueOf(long: Long) = valueOf(Signal(), long)

        @JvmStatic
        fun valueOf(signal: Signal, number: Int): Signal = valueOf(signal, number.toLong())

        @JvmStatic
        fun valueOf(signal: Signal, number: Long): Signal = signal.apply {
            type = if (number != 0L)
                SignalTypes.numberType
            else
                SignalTypes.numberType

            this.number = number
        }
    }

    override fun toString(): String = type.toString(this)

    @JvmField
    var type: SignalType = SignalTypes.numberType

    @get:JvmName("number")
    var number: NUMBER_TYPE = 0L
        private set


    @get:JvmName("intNumber")
    val intNumber get() = number.toInt()

    fun setNumber(number: Int) {
        setNumber(number.toLong())
    }

    fun setNumber(number: NUMBER_TYPE) {

        this.number = number;
    }

    fun barColor(): Color {
        return type.barColor(this)
    }

    fun color(): Color {
        return type.color(this)
    }

    fun set(signal: Signal) {
        type.set(this, signal)
    }

    fun setZero() {
        type.setZero(this)
    }

    fun or(signal: Signal) {
        type.or(this, signal)
    }

    fun fromBytes(bytes: ByteArray) {
        tmpReads.setBytes(bytes);
        read(tmpReads)
    }

    fun read(read: Reads) {
        read.i()//version

        type = SignalType.findByName(read.str())

        number = read.l()
    }

    fun asBytes(): ByteArray {
        tmpWrites.reset()
        write(tmpWrites)
        return tmpWrites.bytes
    }

    fun write(write: Writes) {
        write.i(0)//version

        write.str(type.name)

        write.l(number)
    }

    fun compareWithZero(): Int {

        return when {
            number == 0L -> 0
            number > 0L -> 1
            else -> -1

        }
    }

    fun xor(signal: Signal) {
        type.xor(this, signal)
    }

    fun compareTo(other: Signal): Long {
        return number - other.number;
    }

    fun and(signal: Signal) {
        type.and(this, signal)
    }

    fun plus(signal: Signal) {
        type.plus(this, signal)
    }

    fun minus(signal: Signal) {
        type.minus(this, signal)
    }

    fun div(signal: Signal) {
        type.div(this, signal)
    }

    fun mod(signal: Signal) {
        type.mod(this, signal)
    }

    fun times(signal: Signal) {
        type.times(this, signal)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Signal) return false;
        return other.number == number && type.id == other.type.id;
    }

    fun applyControl(building: Building) {
        type.applyControl(this, building)
    }

    fun absolute() {
        type.absolute(this);
    }
}