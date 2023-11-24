package com.nurani.audio.datastore

import android.media.audiofx.PresetReverb
import kotlinx.serialization.Serializable

@Serializable
internal data class EqualizerPreferences(
    val isEnabled: Boolean = false,
    val equalizerPreset: Short = 0,
    val bandLevelShort: Array<Short> = arrayOf(),
    val presetReverb: Short = PresetReverb.PRESET_NONE,
    val bassStrength: Short = 0,
    val virtualizerStrength: Short = 0

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EqualizerPreferences

        if (isEnabled != other.isEnabled) return false
        if (equalizerPreset != other.equalizerPreset) return false
        if (!bandLevelShort.contentEquals(other.bandLevelShort)) return false
        if (presetReverb != other.presetReverb) return false
        if (bassStrength != other.bassStrength) return false
        if (virtualizerStrength != other.virtualizerStrength) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isEnabled.hashCode()
        result = 31 * result + equalizerPreset
        result = 31 * result + bandLevelShort.contentHashCode()
        result = 31 * result + presetReverb
        result = 31 * result + bassStrength
        result = 31 * result + virtualizerStrength
        return result
    }
}