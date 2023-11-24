package com.nurani.audio

import android.media.audiofx.PresetReverb
import kotlin.properties.Delegates

class EqualizerData private constructor() {

    companion object {
        val EMPTY: EqualizerData = Builder().build()
    }

    private var _presetReverbValue by Delegates.notNull<Short>()
    val presetReverbValue: Short
        get() = _presetReverbValue

    private lateinit var _bandLevelShort: Array<Short>
    val bandLevelShort: Array<Short>
        get() = _bandLevelShort

    private var _equalizerPreset by Delegates.notNull<Short>()
    val equalizerPreset: Short
        get() = _equalizerPreset

    private var _bassStrength by Delegates.notNull<Short>()
    val bassStrength: Short
        get() = _bassStrength

    private var _virtualizerStrength by Delegates.notNull<Short>()
    val virtualizerStrength: Short
        get() = _virtualizerStrength

    class Builder {
        private var _presetReverbValue: Short = PresetReverb.PRESET_NONE
        private var _bandLevelShort: Array<Short> = emptyArray()
        private var _bassStrength: Short = 0
        private var _virtualizerStrength: Short = 0
        private var _equalizerPreset: Short = 0

        fun setReverbPreset(preset: Short): Builder {
            _presetReverbValue = preset
            return this
        }

        fun setBandLevelRange(bandLevelShort: Array<Short>): Builder {
            _bandLevelShort = bandLevelShort
            return this
        }

        fun setEqualizerPreset(equalizerPreset: Short): Builder {
            _equalizerPreset = equalizerPreset
            return this
        }

        fun setBassStrength(strength: Short): Builder {
            _bassStrength = strength
            return this
        }

        fun setVirtualizerStrength(strength: Short): Builder {
            _virtualizerStrength = strength
            return this
        }

        fun build(): EqualizerData {
            val equalizerData = EqualizerData()
            equalizerData._presetReverbValue = _presetReverbValue
            equalizerData._bandLevelShort = _bandLevelShort
            equalizerData._bassStrength = _bassStrength
            equalizerData._virtualizerStrength = _virtualizerStrength
            equalizerData._equalizerPreset = _equalizerPreset
            return equalizerData
        }
    }
}