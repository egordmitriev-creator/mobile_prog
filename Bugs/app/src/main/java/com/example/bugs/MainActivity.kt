package com.example.bugs

import Player
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*
import android.widget.*
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var spCourse: Spinner
    private lateinit var sbDifficulty: SeekBar
    private lateinit var tvDifficultyLevel: TextView
    private lateinit var cvBirthDate: CalendarView
    private lateinit var ivZodiac: ImageView
    private lateinit var tvZodiacName: TextView
    private lateinit var btnRegister: Button
    private lateinit var tvResult: TextView

    private var selectedBirthDate: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupCourseSpinner()
        setupListeners()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        rgGender = findViewById(R.id.rgGender)
        spCourse = findViewById(R.id.spCourse)
        sbDifficulty = findViewById(R.id.sbDifficulty)
        tvDifficultyLevel = findViewById(R.id.tvDifficultyLevel)
        cvBirthDate = findViewById(R.id.cvBirthDate)
        ivZodiac = findViewById(R.id.ivZodiac)
        tvZodiacName = findViewById(R.id.tvZodiacName)
        btnRegister = findViewById(R.id.btnRegister)
        tvResult = findViewById(R.id.tvResult)
    }

    private fun setupCourseSpinner() {
        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс", "5 курс", "6 курс")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCourse.adapter = adapter
    }

    private fun setupListeners() {
        sbDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDifficultyLevel.text = "Уровень: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        cvBirthDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedBirthDate = calendar.timeInMillis
            updateZodiacSign(year, month + 1, dayOfMonth)
        }

        btnRegister.setOnClickListener {
            registerPlayer()
        }
    }

    private fun updateZodiacSign(year: Int, month: Int, day: Int) {
        val zodiacInfo = calculateZodiacSign(month, day)
        tvZodiacName.text = zodiacInfo.first

        val resourceId = when (zodiacInfo.second) {
            "aries" -> R.drawable.ic_aries
            "taurus" -> R.drawable.ic_taurus
            "gemini" -> R.drawable.ic_gemini
            "cancer" -> R.drawable.ic_cancer
            "leo" -> R.drawable.ic_leo
            "virgo" -> R.drawable.ic_virgo
            "libra" -> R.drawable.ic_libra
            "scorpio" -> R.drawable.ic_scorpio
            "sagittarius" -> R.drawable.ic_sagittarius
            "capricorn" -> R.drawable.ic_capricorn
            "aquarius" -> R.drawable.ic_aquarius
            "pisces" -> R.drawable.ic_pisces
            else -> R.drawable.ic_default
        }

        ivZodiac.setImageResource(resourceId)
    }

    private fun calculateZodiacSign(month: Int, day: Int): Pair<String, String> {
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) ->
                Pair("Овен", "aries")
            (month == 4 && day >= 20) || (month == 5 && day <= 20) ->
                Pair("Телец", "taurus")
            (month == 5 && day >= 21) || (month == 6 && day <= 20) ->
                Pair("Близнецы", "gemini")
            (month == 6 && day >= 21) || (month == 7 && day <= 22) ->
                Pair("Рак", "cancer")
            (month == 7 && day >= 23) || (month == 8 && day <= 22) ->
                Pair("Лев", "leo")
            (month == 8 && day >= 23) || (month == 9 && day <= 22) ->
                Pair("Дева", "virgo")
            (month == 9 && day >= 23) || (month == 10 && day <= 22) ->
                Pair("Весы", "libra")
            (month == 10 && day >= 23) || (month == 11 && day <= 21) ->
                Pair("Скорпион", "scorpio")
            (month == 11 && day >= 22) || (month == 12 && day <= 21) ->
                Pair("Стрелец", "sagittarius")
            (month == 12 && day >= 22) || (month == 1 && day <= 19) ->
                Pair("Козерог", "capricorn")
            (month == 1 && day >= 20) || (month == 2 && day <= 18) ->
                Pair("Водолей", "aquarius")
            (month == 2 && day >= 19) || (month == 3 && day <= 20) ->
                Pair("Рыбы", "pisces")
            else -> Pair("Неизвестно", "default")
        }
    }

    private fun registerPlayer() {
        val fullName = etFullName.text.toString().trim()

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Введите ФИО", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = when (rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Мужской"
            R.id.rbFemale -> "Женский"
            else -> "Не указан"
        }

        val course = spCourse.selectedItem.toString()
        val difficultyLevel = sbDifficulty.progress
        val zodiacSign = tvZodiacName.text.toString()

        val player = Player(
            fullName = fullName,
            gender = gender,
            course = course,
            difficultyLevel = difficultyLevel,
            birthDate = selectedBirthDate,
            zodiacSign = zodiacSign
        )

        displayPlayerInfo(player)
    }

    private fun displayPlayerInfo(player: Player) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val birthDateStr = dateFormat.format(Date(player.birthDate))

        val resultText = """
            Регистрация завершена!
            
            ФИО: ${player.fullName}
            Пол: ${player.gender}
            Курс: ${player.course}
            Уровень сложности: ${player.difficultyLevel}/10
            Дата рождения: $birthDateStr
            Знак зодиака: ${player.zodiacSign}
        """.trimIndent()

        tvResult.text = resultText
        tvResult.visibility = android.view.View.VISIBLE
    }
}