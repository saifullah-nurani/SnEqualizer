package com.nurani.audio.dialog

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.audiofx.PresetReverb
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.datastore.core.DataStore
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.nurani.audio.R
import com.nurani.audio.SnEqualizer
import com.nurani.audio.databinding.BottomsheetEqualizerBinding
import com.nurani.audio.datastore.EqualizerPreferences
import kotlinx.coroutines.launch

internal class EqualizerDialog(
    private val snEqualizer: SnEqualizer,
    private val useDefaultEqualizerData: Boolean,
    private val equalizerDataStore: DataStore<EqualizerPreferences>? = null
) : AppCompatDialogFragment(), SeekBar.OnSeekBarChangeListener {

    private lateinit var binding: BottomsheetEqualizerBinding

    private val backBtn: TextView by lazy { binding.beBackBtn }
    private val equalizerOnOffSwitch: SwitchCompat by lazy { binding.beEqualizerOnOffSwitch }
    private val equalizerControllerLayout: LinearLayout by lazy { binding.beEqualizerControllerLayout }

    private val customChipBtn: Chip by lazy { binding.beCustomChipBtn }
    private val normalChipBtn: Chip by lazy { binding.beNormalChipBtn }
    private val classicalChipBtn: Chip by lazy { binding.beClassicalChipBtn }
    private val danceChipBtn: Chip by lazy { binding.beDanceChipBtn }
    private val flatChipBtn: Chip by lazy { binding.beFlatChipBtn }
    private val folkChipBtn: Chip by lazy { binding.beFolkChipBtn }
    private val heavyMetalChipBtn: Chip by lazy { binding.beHeavyMetalChipBtn }
    private val hipHopChipBtn: Chip by lazy { binding.beHipHopChipBtn }
    private val jazzChipBtn: Chip by lazy { binding.beJazzChipBtn }
    private val popChipBtn: Chip by lazy { binding.bePopChipBtn }
    private val rockChipBtn: Chip by lazy { binding.beRockChipBtn }
    private val seekbar60hz: SeekBar by lazy { binding.beSeekbar60hz }
    private val seekbar230hz: SeekBar by lazy { binding.beSeekbar230hz }
    private val seekbar910hz: SeekBar by lazy { binding.beSeekbar910hz }
    private val seekbar3600hz: SeekBar by lazy { binding.beSeekbar3600hz }
    private val seekbar14000hz: SeekBar by lazy { binding.beSeekbar14000hz }
    private val spinner: Spinner by lazy { binding.beSpinner }
    private val bassBoosterSeekbar: SeekBar by lazy { binding.beBassBoosterSeekbar }
    private val virtualizerSeekbar: SeekBar by lazy { binding.beVirtualizerSeekbar }

    private val spinnerAdapter: ArrayAdapter<String> by lazy {
        ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            R.id.is_textView,
            arrayOf("None", "Small Room", "Medium Room", "Medium Hall", "Large Hall", "Plate")
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = BottomsheetEqualizerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(spinner) {
            adapter = spinnerAdapter
            setPopupBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(requireContext(), android.R.color.black)
                )
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val selectedReverb = when (position) {
                        0 -> PresetReverb.PRESET_NONE
                        1 -> PresetReverb.PRESET_SMALLROOM
                        2 -> PresetReverb.PRESET_MEDIUMROOM
                        3 -> PresetReverb.PRESET_MEDIUMHALL
                        4 -> PresetReverb.PRESET_LARGEHALL
                        else -> PresetReverb.PRESET_PLATE
                    }
                    snEqualizer.useReverbPreset(selectedReverb)
                    if (useDefaultEqualizerData) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            equalizerDataStore?.updateData {
                                it.copy(presetReverb = selectedReverb)
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            setSelection(snEqualizer.currentEqualizerPreset.toInt())
        }

        equalizerOnOffSwitch.setOnCheckedChangeListener { _, isChecked ->
            snEqualizer.enabled = isChecked
            enableDisableUi(snEqualizer.enabled)
            if (useDefaultEqualizerData) {
                viewLifecycleOwner.lifecycleScope.launch {
                    equalizerDataStore?.updateData {
                        it.copy(isEnabled = isChecked)
                    }
                }
            }
        }

        /////////////////////////////////
        backBtn.setOnClickListener { dialog?.dismiss() }
        ////////////////////////////////
        customChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((-1).toShort())
            chipSelected(it as Chip)
        }
        normalChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((0).toShort())
            chipSelected(it as Chip)
        }
        classicalChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((1).toShort())
            chipSelected(it as Chip)
        }
        danceChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((2).toShort())
            chipSelected(it as Chip)
        }
        flatChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((3).toShort())
            chipSelected(it as Chip)
        }
        folkChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((4).toShort())
            chipSelected(it as Chip)
        }
        heavyMetalChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((5).toShort())
            chipSelected(it as Chip)
        }
        hipHopChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((6).toShort())
            chipSelected(it as Chip)
        }
        jazzChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((7).toShort())
            chipSelected(it as Chip)
        }
        popChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((8).toShort())
            chipSelected(it as Chip)
        }
        rockChipBtn.setOnClickListener {
            snEqualizer.useEqualizerPreset((9).toShort())
            chipSelected(it as Chip)
        }

        seekbar60hz.setOnSeekBarChangeListener(this)
        seekbar230hz.setOnSeekBarChangeListener(this)
        seekbar910hz.setOnSeekBarChangeListener(this)
        seekbar3600hz.setOnSeekBarChangeListener(this)
        seekbar14000hz.setOnSeekBarChangeListener(this)
        bassBoosterSeekbar.setOnSeekBarChangeListener(this)
        virtualizerSeekbar.setOnSeekBarChangeListener(this)

        if (snEqualizer.isBassStrengthSupported) {
            bassBoosterSeekbar.progress = snEqualizer.currentBoostStrength.toInt()
        } else {
            bassBoosterSeekbar.visibility = View.GONE
        }

        if (snEqualizer.isVirtualizerStrengthSupported) {
            virtualizerSeekbar.progress = snEqualizer.currentVirtualizerStrength.toInt()
        } else {
            virtualizerSeekbar.visibility = View.GONE
        }


        enableDisableUi(snEqualizer.enabled)
    }

    private fun chipSelected(selectedView: Chip) {
        listOf(
            customChipBtn,
            normalChipBtn,
            classicalChipBtn,
            danceChipBtn,
            flatChipBtn,
            folkChipBtn,
            heavyMetalChipBtn,
            hipHopChipBtn,
            jazzChipBtn,
            popChipBtn,
            rockChipBtn
        ).forEach {
            if (selectedView == it) {
                selectedView.chipBackgroundColor =
                    ColorStateList.valueOf(Color.parseColor("#03DAC6"))
                selectedView.chipStrokeColor = null
                selectedView.chipStrokeWidth = 0f
            } else {
                it.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), android.R.color.transparent
                    )
                )
                it.chipStrokeColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), android.R.color.white
                    )
                )
                it.chipStrokeWidth = 1f

            }
        }
        setUpBands()
    }

    private fun enableDisableUi(enable: Boolean) {
        listOf(
            customChipBtn,
            normalChipBtn,
            classicalChipBtn,
            danceChipBtn,
            flatChipBtn,
            folkChipBtn,
            heavyMetalChipBtn,
            hipHopChipBtn,
            jazzChipBtn,
            popChipBtn,
            rockChipBtn,
            seekbar60hz,
            seekbar230hz,
            seekbar910hz,
            seekbar3600hz,
            seekbar14000hz,
            spinner,
            bassBoosterSeekbar,
            virtualizerSeekbar
        ).forEach {
            it.isEnabled = enable
        }
        if (enable) {
            equalizerOnOffSwitch.text = getString(R.string.on)
            equalizerControllerLayout.alpha = 1f
        } else {
            equalizerControllerLayout.alpha = 0.3f
            equalizerOnOffSwitch.text = getString(R.string.off)
        }
        equalizerOnOffSwitch.isChecked = enable
        when (snEqualizer.currentEqualizerPreset.toInt()) {
            0 -> chipSelected(normalChipBtn)
            1 -> chipSelected(classicalChipBtn)
            2 -> chipSelected(danceChipBtn)
            3 -> chipSelected(flatChipBtn)
            4 -> chipSelected(folkChipBtn)
            5 -> chipSelected(heavyMetalChipBtn)
            6 -> chipSelected(hipHopChipBtn)
            7 -> chipSelected(jazzChipBtn)
            8 -> chipSelected(popChipBtn)
            9 -> chipSelected(rockChipBtn)
            else -> chipSelected(customChipBtn)
        }
    }

    private fun setUpBands() {
        for (bandIndex in 0 until snEqualizer.numberOfEqualizerBands) {
            val seekBar: SeekBar = when (bandIndex) {
                0 -> seekbar60hz
                1 -> seekbar230hz
                2 -> seekbar910hz
                3 -> seekbar3600hz
                4 -> seekbar14000hz
                else -> throw IllegalArgumentException("Invalid band index: $bandIndex")
            }
            val bandLevel =
                snEqualizer.equalizerBandLevelRange[bandIndex] - snEqualizer.lowerEqualizerBandLevel
            val maxSeekBarProgress =
                snEqualizer.upperEqualizerBandLevel - snEqualizer.lowerEqualizerBandLevel
            seekBar.max = maxSeekBarProgress.toInt()
            seekBar.progress = bandLevel.toInt()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bottom_sheet
                )
            )
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                decorView.updateLayoutParams {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = resources.displayMetrics.widthPixels
                }
                dialog?.window?.attributes?.windowAnimations = R.style.PortraitDialogAnimation
                setGravity(Gravity.BOTTOM)
            } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                decorView.updateLayoutParams {
                    width = resources.displayMetrics.heightPixels
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                attributes?.windowAnimations = R.style.LandscapeDialogAnimation
                setGravity(Gravity.END)
            }
        }
    }

    private fun updateBand(seekBar: SeekBar, bandIndex: Int) {
        val levelProgress = seekBar.progress + snEqualizer.lowerEqualizerBandLevel
        snEqualizer.setBandLevel(bandIndex.toShort(), levelProgress.toShort())
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            when (seekBar) {
                seekbar60hz -> updateBand(seekBar, 0)
                seekbar230hz -> updateBand(seekBar, 1)
                seekbar910hz -> updateBand(seekBar, 2)
                seekbar3600hz -> updateBand(seekBar, 3)
                seekbar14000hz -> updateBand(seekBar, 4)

                bassBoosterSeekbar -> snEqualizer.setBassStrength(seekBar.progress.toShort())
                virtualizerSeekbar -> snEqualizer.setVirtualizerStrength(seekBar.progress.toShort())
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (useDefaultEqualizerData) {
            when (seekBar) {
                seekbar60hz, seekbar230hz, seekbar910hz, seekbar3600hz, seekbar14000hz -> {
                    val bandArray = arrayOf(
                        (seekbar60hz.progress + snEqualizer.lowerEqualizerBandLevel).toShort(),
                        (seekbar230hz.progress + snEqualizer.lowerEqualizerBandLevel).toShort(),
                        (seekbar910hz.progress + snEqualizer.lowerEqualizerBandLevel).toShort(),
                        (seekbar3600hz.progress + snEqualizer.lowerEqualizerBandLevel).toShort(),
                        (seekbar14000hz.progress + snEqualizer.lowerEqualizerBandLevel).toShort()
                    )
                    viewLifecycleOwner.lifecycleScope.launch {
                        equalizerDataStore?.updateData {
                            it.copy(equalizerPreset = -1, bandLevelShort = bandArray)
                        }
                    }
                }

                bassBoosterSeekbar -> viewLifecycleOwner.lifecycleScope.launch {
                    equalizerDataStore?.updateData {
                        it.copy(bassStrength = seekBar.progress.toShort())
                    }
                }

                virtualizerSeekbar -> viewLifecycleOwner.lifecycleScope.launch {
                    equalizerDataStore?.updateData {
                        it.copy(virtualizerStrength = seekBar.progress.toShort())
                    }
                }
            }
        }

    }

}