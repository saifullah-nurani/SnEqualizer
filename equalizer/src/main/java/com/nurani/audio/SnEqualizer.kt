package com.nurani.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.sn.equalizer.Constant.EQUALIZER_PREFERENCES_FILE
import com.nurani.audio.datastore.EqualizerPreferences
import com.nurani.audio.datastore.EqualizerPreferencesSerializer
import com.nurani.audio.dialog.EqualizerDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface SnEqualizer : LifecycleEventObserver {
    val lowerEqualizerBandLevel: Short
    val upperEqualizerBandLevel: Short
    val currentEqualizerPreset: Short
    val currentPresetReverb: Short
    val currentBoostStrength: Short
    val currentVirtualizerStrength: Short
    val numberOfEqualizerBands: Short
    val equalizerBandLevelRange: Array<Short>
    val isVirtualizerStrengthSupported: Boolean
    val isBassStrengthSupported: Boolean
    var enabled: Boolean

    fun setBandLevel(band: Short, level: Short)
    fun setBassStrength(strength: Short)
    fun setVirtualizerStrength(strength: Short)
    fun useReverbPreset(preset: Short)
    fun useEqualizerPreset(preset: Short)

    fun showEqualizerDialog(fragmentManager: FragmentManager)
    fun release()

    class Builder(private val context: Context) {
        private var audioSessionId: Int = 0
        private var equalizerChangesListener: SnEqualizerChangesListener? = null
        private var equalizerData: EqualizerData = EqualizerData.EMPTY
        private var lifecycleOwner: LifecycleOwner? = null
        private var useDefaultEqualizerDataSaver: Boolean = false

        fun setAudioSessionId(sessionId: Int): Builder {
            this.audioSessionId = sessionId
            return this
        }

        fun setLifecycleOwner(lifecycleOwner: LifecycleOwner): Builder {
            this.lifecycleOwner = lifecycleOwner
            return this
        }

        fun useDefaultEqualizerDataSaver(enable: Boolean): Builder {
            this.useDefaultEqualizerDataSaver = enable
            return this
        }

        fun setEqualizerData(equalizerData: EqualizerData): Builder {
            this.equalizerData = equalizerData
            return this
        }

        fun addEqualizerChangesListener(equalizerChangesListener: SnEqualizerChangesListener): Builder {
            this.equalizerChangesListener = equalizerChangesListener
            return this
        }

        fun build(): SnEqualizer {
            return SnEqualizerImpl(
                context,
                audioSessionId,
                lifecycleOwner,
                equalizerChangesListener,
                equalizerData,
                useDefaultEqualizerDataSaver
            )
        }
    }

    private class SnEqualizerImpl(
        private val context: Context,
        audioSessionId: Int,
        private val lifecycleOwner: LifecycleOwner?,
        private val equalizerChangesListener: SnEqualizerChangesListener?,
        private val equalizerData: EqualizerData,
        private val useDefaultEqualizerDataSaver: Boolean
    ) : SnEqualizer, BassBoost.OnParameterChangeListener, Virtualizer.OnParameterChangeListener,
        PresetReverb.OnParameterChangeListener, Equalizer.OnParameterChangeListener {

        private var equalizerDataStore: DataStore<EqualizerPreferences>? = null
            get() {
                if (field == null) {
                    DataStoreFactory.create(
                        serializer = EqualizerPreferencesSerializer,
                        produceFile = {
                            context.dataStoreFile(EQUALIZER_PREFERENCES_FILE)
                        },
                        scope = CoroutineScope(Dispatchers.IO)
                    )
                }
                return field
            }
        private var equalizerPreferences = EqualizerPreferences()

        init {
            if (useDefaultEqualizerDataSaver) {
                if (lifecycleOwner == null) {
                    throw IllegalArgumentException("LifecycleOwner can't be null when useDefaultEqualizerDataComponent set to true")
                }
                lifecycleOwner.lifecycle.addObserver(this)
            }
        }

        private var mEqualizer: Equalizer? = null
        private var mBassBoost: BassBoost? = null
        private var mPresetReverb: PresetReverb? = null
        private var mVirtualizer: Virtualizer? = null

        private var bandLevelShort: MutableList<Short> = mutableListOf()

        init {
            mEqualizer = Equalizer(0, audioSessionId)
            mBassBoost = BassBoost(0, audioSessionId)
            mPresetReverb = PresetReverb(0, audioSessionId)
            mVirtualizer = Virtualizer(0, audioSessionId)
            if (useDefaultEqualizerDataSaver) {
                loadEqualizerPreferences()
            }
            setupEqualizer()
            setupPresetReverb()
            setupBass()
            setupVirtualizer()
        }

        override val lowerEqualizerBandLevel: Short
            get() = mEqualizer?.bandLevelRange?.first() ?: 0
        override val upperEqualizerBandLevel: Short
            get() = mEqualizer?.bandLevelRange?.last() ?: 0
        override val currentEqualizerPreset: Short
            get() = mEqualizer?.currentPreset ?: 0
        override val currentPresetReverb: Short
            get() = mPresetReverb?.preset ?: PresetReverb.PRESET_NONE
        override val currentBoostStrength: Short
            get() = mBassBoost?.roundedStrength ?: 0
        override val currentVirtualizerStrength: Short
            get() = mVirtualizer?.roundedStrength ?: 0
        override val equalizerBandLevelRange: Array<Short>
            get() {
                val bandLevelRange = mutableListOf<Short>()
                for (i in 0 until mEqualizer?.numberOfBands!!) {
                    bandLevelRange.add(mEqualizer?.getBandLevel(i.toShort()) ?: 0)
                }
                return bandLevelRange.toTypedArray()
            }
        override val numberOfEqualizerBands: Short
            get() = mEqualizer?.numberOfBands ?: 0
        override val isBassStrengthSupported: Boolean
            get() = mBassBoost?.strengthSupported ?: false
        override val isVirtualizerStrengthSupported: Boolean
            get() = mVirtualizer?.strengthSupported ?: false

        override var enabled: Boolean
            get() = mEqualizer?.enabled == true
            set(value) {
                mEqualizer?.enabled = value
                mPresetReverb?.enabled = value
                mBassBoost?.enabled = value
                mVirtualizer?.enabled = value
                equalizerChangesListener?.onEqualizerEnableChange(value)
            }

        override fun release() {
            mEqualizer?.release()
            mBassBoost?.release()
            mPresetReverb?.release()
            mVirtualizer?.release()
            lifecycleOwner?.lifecycle?.removeObserver(this)
        }

        override fun setBandLevel(band: Short, level: Short) {
            mEqualizer?.setBandLevel(band, level)
            bandLevelShort.clear()
            for (i in 0 until numberOfEqualizerBands) {
                bandLevelShort.add(mEqualizer?.getBandLevel(i.toShort()) ?: 0)
            }
        }

        override fun setBassStrength(strength: Short) {
            mBassBoost?.setStrength(strength)
        }

        override fun setVirtualizerStrength(strength: Short) {
            mVirtualizer?.setStrength(strength)
        }

        override fun useEqualizerPreset(preset: Short) {
            if (preset.toInt() == -1) {
                if (bandLevelShort.isNotEmpty()) {
                    for (i in 0 until bandLevelShort.size) {
                        mEqualizer?.setBandLevel(i.toShort(), bandLevelShort[i])
                    }
                }
            } else {
                mEqualizer?.usePreset(preset)
            }
        }

        override fun useReverbPreset(preset: Short) {
            mPresetReverb?.preset = preset
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_DESTROY -> {
                    release()
                }

                else -> {}
            }
        }

        override fun showEqualizerDialog(fragmentManager: FragmentManager) {
            EqualizerDialog(this, useDefaultEqualizerDataSaver, equalizerDataStore).show(
                fragmentManager,
                "Show Equalizer Dialog"
            )
        }

        private fun setupEqualizer() {
            mEqualizer ?: return
            val equalizerPreset =
                if (useDefaultEqualizerDataSaver) equalizerPreferences.equalizerPreset else equalizerData.equalizerPreset
            val bandLevelShort =
                if (useDefaultEqualizerDataSaver) equalizerPreferences.bandLevelShort else equalizerData.bandLevelShort

            if (equalizerPreset.toInt() == -1) {
                if (bandLevelShort.isNotEmpty()) {
                    for (i in 0 until mEqualizer!!.numberOfBands) {
                        mEqualizer?.setBandLevel(bandLevelShort[i], i.toShort())
                    }
                }
            } else {
                mEqualizer?.usePreset(equalizerPreset)
            }
        }

        private fun setupPresetReverb() {
            mPresetReverb ?: return
            val reverbPresetValue =
                if (useDefaultEqualizerDataSaver) equalizerPreferences.presetReverb else equalizerData.presetReverbValue
            val presetVerbSetting = PresetReverb.Settings(mPresetReverb!!.properties.toString())
            presetVerbSetting.preset = reverbPresetValue
            mPresetReverb!!.properties = presetVerbSetting
            mPresetReverb!!.preset = reverbPresetValue
        }

        private fun setupBass() {
            mBassBoost ?: return
            if (mBassBoost!!.strengthSupported) {
                val bassStrength =
                    if (useDefaultEqualizerDataSaver) equalizerPreferences.bassStrength else equalizerData.bassStrength
                val bassBoostSetting = BassBoost.Settings(mBassBoost!!.properties.toString())
                bassBoostSetting.strength = bassStrength
                mBassBoost!!.properties = bassBoostSetting
                mBassBoost!!.setStrength(bassStrength)
            }
        }

        private fun setupVirtualizer() {
            mVirtualizer ?: return
            if (!mVirtualizer!!.strengthSupported) {
                val virtualizerStrength =
                    if (useDefaultEqualizerDataSaver) equalizerPreferences.virtualizerStrength else equalizerData.virtualizerStrength
                val virtualizerSetting = Virtualizer.Settings(mVirtualizer!!.properties.toString())
                virtualizerSetting.strength = virtualizerStrength
                mVirtualizer!!.properties = virtualizerSetting
                mVirtualizer!!.setStrength(virtualizerStrength)
            }
        }


        private fun loadEqualizerPreferences() {
            lifecycleOwner?.lifecycleScope?.launch {
                equalizerDataStore?.data?.collect {
                    equalizerPreferences = it
                }
            }
        }

        override fun onParameterChange(effect: BassBoost?, status: Int, param: Int, value: Short) {

        }

        override fun onParameterChange(
            effect: PresetReverb?, status: Int, param: Int, value: Short
        ) {

        }

        override fun onParameterChange(
            effect: Virtualizer?, status: Int, param: Int, value: Short
        ) {

        }

        override fun onParameterChange(
            effect: Equalizer?, status: Int, param1: Int, param2: Int, value: Int
        ) {

        }

    }
}
