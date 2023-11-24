package com.nurani.audio

interface SnEqualizerChangesListener {
    fun onBandLevelChange(bandLevel: Short, bandIndex: Short) {}
    fun onVirtualizerChange(strength: Short) {}
    fun onBassBoostChange(strength: Short) {}
    fun onRevertPresetChange(presetReverb: Short) {}
    fun onEqualizerEnableChange(enable: Boolean)
}