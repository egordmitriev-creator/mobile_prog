package com.example.bugs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)

        setupSettings()
    }

    private fun setupSettings() {
        // Скорость игры
        val speedSeekBar: SeekBar = requireView().findViewById(R.id.speed_seekbar)
        val speedValue: TextView = requireView().findViewById(R.id.speed_value)

        // Максимальное количество тараканов
        val cockroachesSeekBar: SeekBar = requireView().findViewById(R.id.cockroaches_seekbar)
        val cockroachesValue: TextView = requireView().findViewById(R.id.cockroaches_value)

        // Интервал бонусов
        val bonusSeekBar: SeekBar = requireView().findViewById(R.id.bonus_seekbar)
        val bonusValue: TextView = requireView().findViewById(R.id.bonus_value)

        // Длительность раунда
        val durationSeekBar: SeekBar = requireView().findViewById(R.id.duration_seekbar)
        val durationValue: TextView = requireView().findViewById(R.id.duration_value)

        // Загружаем сохраненные настройки
        loadSettings(speedSeekBar, speedValue, cockroachesSeekBar, cockroachesValue,
            bonusSeekBar, bonusValue, durationSeekBar, durationValue)

        setupSeekBarListeners(speedSeekBar, speedValue, "speed", 50)
        setupSeekBarListeners(cockroachesSeekBar, cockroachesValue, "cockroaches", 10)
        setupSeekBarListeners(bonusSeekBar, bonusValue, "bonus", 30)
        setupSeekBarListeners(durationSeekBar, durationValue, "duration", 60)
    }

    private fun setupSeekBarListeners(
        seekBar: SeekBar,
        textView: TextView,
        key: String,
        defaultValue: Int
    ) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = progress.toString()
                sharedPreferences.edit().putInt(key, progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadSettings(
        speedSeekBar: SeekBar, speedValue: TextView,
        cockroachesSeekBar: SeekBar, cockroachesValue: TextView,
        bonusSeekBar: SeekBar, bonusValue: TextView,
        durationSeekBar: SeekBar, durationValue: TextView
    ) {
        val speed = sharedPreferences.getInt("speed", 50)
        speedSeekBar.progress = speed
        speedValue.text = speed.toString()

        val cockroaches = sharedPreferences.getInt("cockroaches", 10)
        cockroachesSeekBar.progress = cockroaches
        cockroachesValue.text = cockroaches.toString()

        val bonus = sharedPreferences.getInt("bonus", 30)
        bonusSeekBar.progress = bonus
        bonusValue.text = bonus.toString()

        val duration = sharedPreferences.getInt("duration", 60)
        durationSeekBar.progress = duration
        durationValue.text = duration.toString()
    }
}