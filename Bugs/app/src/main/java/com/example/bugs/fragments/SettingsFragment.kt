package com.example.bugs.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bugs.R

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

    // В методе setupSettings добавьте/измените настройки:

    private fun setupSettings() {
        // Скорость игры
        val speedSeekBar: SeekBar = requireView().findViewById(R.id.speed_seekbar)
        val speedValue: TextView = requireView().findViewById(R.id.speed_value)

        // Максимальное количество тараканов - УВЕЛИЧИВАЕМ ДИАПАЗОН
        val cockroachesSeekBar: SeekBar = requireView().findViewById(R.id.cockroaches_seekbar)
        val cockroachesValue: TextView = requireView().findViewById(R.id.cockroaches_value)

        // Интервал бонусов
        val bonusSeekBar: SeekBar = requireView().findViewById(R.id.bonus_seekbar)
        val bonusValue: TextView = requireView().findViewById(R.id.bonus_value)

        // Длительность раунда
        val durationSeekBar: SeekBar = requireView().findViewById(R.id.duration_seekbar)
        val durationValue: TextView = requireView().findViewById(R.id.duration_value)

        // Устанавливаем максимальные значения
        cockroachesSeekBar.max = 50  // Максимум 50 жуков
        speedSeekBar.max = 100

        // Загружаем сохраненные настройки с увеличенными значениями по умолчанию
        val speed = sharedPreferences.getInt("speed", 60)
        val cockroaches = sharedPreferences.getInt("cockroaches", 20)  // По умолчанию 20
        val bonus = sharedPreferences.getInt("bonus", 30)
        val duration = sharedPreferences.getInt("duration", 90)  // Увеличили время раунда

        speedSeekBar.progress = speed
        speedValue.text = speed.toString()

        cockroachesSeekBar.progress = cockroaches
        cockroachesValue.text = cockroaches.toString()

        bonusSeekBar.progress = bonus
        bonusValue.text = bonus.toString()

        durationSeekBar.progress = duration
        durationValue.text = duration.toString()

        // Устанавливаем слушатели
        setupSeekBarListeners(speedSeekBar, speedValue, "speed", 60)
        setupSeekBarListeners(cockroachesSeekBar, cockroachesValue, "cockroaches", 20)
        setupSeekBarListeners(bonusSeekBar, bonusValue, "bonus", 30)
        setupSeekBarListeners(durationSeekBar, durationValue, "duration", 90)
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