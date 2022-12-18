@file:JvmName("SettingsManager")

package bytelogic.core

import arc.*
import arc.util.serialization.*

object SettingManager {
    @JvmField
    val enabledLogicNetSelection = BooleanSettingKey("enabled-logic-net-selection") { false }


    class IntSettingKey(key: String, defaultProvider: () -> Int) : SettingKey<Int>(key, defaultProvider, {
        val value: Int = Core.settings[it.key, it.def()].toString().toIntOrNull() ?: run {
            Core.settings.put(it.key, it.def())
            return@run it.def()
        }
        value
    }, { value, key ->
        Core.settings.put(key.key, value)
    })

    class FloatSettingKey(key: String, defaultProvider: () -> Float) : SettingKey<Float>(key, defaultProvider, {
        val value: Float = Core.settings[it.key, it.def()].toString().toFloatOrNull() ?: run {
            Core.settings.put(it.key, it.def())
            return@run it.def()
        }
        value
    }, { value, key ->
        Core.settings.put(key.key, value)
    })

    class BooleanSettingKey(key: String, defaultProvider: () -> Boolean) : SettingKey<Boolean>(key, defaultProvider, {
        val value: Boolean = Core.settings[it.key, it.def()].toString().toBooleanStrictOrNull() ?: run {
            Core.settings.put(it.key, it.def())
            return@run it.def()
        }
        value
    }, { value, key ->
        Core.settings.put(key.key, value)
    })

    class JvalSettingKey(key: String, defaultProvider: () -> Jval) : SettingKey<Jval>(key, defaultProvider, {
        val value: Jval = Core.settings[it.key, it.def()].toString().let { str -> Jval.read(str) } ?: run {
            Core.settings.put(it.key, it.def().toString(Jval.Jformat.formatted))
            return@run it.def()
        }
        value
    }, { value, key ->
        Core.settings.put(key.key, value.toString(Jval.Jformat.formatted))
    })

    open class SettingKey<T>(
        @JvmField val key: String,
        val defaultProvider: () -> T,
        val valueGetter: (SettingKey<T>) -> T,
        val valueSetter: (T, SettingKey<T>) -> Unit,
    ) {
        open fun def() = defaultProvider()
        fun get(): T = valueGetter(this)
        fun set(value: T) = valueSetter(value, this)
    }

}