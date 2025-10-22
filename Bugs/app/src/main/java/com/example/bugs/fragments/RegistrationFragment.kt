package com.example.bugs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.bugs.data.AppDatabase
import com.example.bugs.data.entities.User
import com.example.bugs.data.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RegistrationFragment : Fragment() {

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
    private lateinit var spExistingUsers: Spinner

    private var selectedBirthDate: Long = System.currentTimeMillis()
    private lateinit var repository: GameRepository
    private lateinit var sharedPreferences: SharedPreferences
    private var usersList = emptyList<User>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация базы данных
        val database = AppDatabase.getInstance(requireContext())
        repository = GameRepository(database)
        sharedPreferences = requireActivity().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

        initViews(view)
        setupCourseSpinner()
        setupExistingUsersSpinner()
        setupListeners()
        loadExistingUsers()
    }

    private fun initViews(rootView: View) {
        etFullName = rootView.findViewById(R.id.etFullName)
        rgGender = rootView.findViewById(R.id.rgGender)
        spCourse = rootView.findViewById(R.id.spCourse)
        sbDifficulty = rootView.findViewById(R.id.sbDifficulty)
        tvDifficultyLevel = rootView.findViewById(R.id.tvDifficultyLevel)
        cvBirthDate = rootView.findViewById(R.id.cvBirthDate)
        ivZodiac = rootView.findViewById(R.id.ivZodiac)
        tvZodiacName = rootView.findViewById(R.id.tvZodiacName)
        btnRegister = rootView.findViewById(R.id.btnRegister)
        tvResult = rootView.findViewById(R.id.tvResult)
        spExistingUsers = rootView.findViewById(R.id.spExistingUsers)
    }

    private fun setupExistingUsersSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf("Новый пользователь")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spExistingUsers.adapter = adapter
    }

    private fun loadExistingUsers() {
        CoroutineScope(Dispatchers.Main).launch {
            repository.getAllUsers().collect { users ->
                usersList = users
                val userNames = mutableListOf("Новый пользователь")
                userNames.addAll(users.map { it.fullName })

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    userNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spExistingUsers.adapter = adapter
            }
        }
    }

    private fun setupCourseSpinner() {
        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
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

        // Обработчик выбора существующего пользователя
        spExistingUsers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedUser = usersList[position - 1]
                    fillFormWithUserData(selectedUser)
                } else {
                    clearForm()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fillFormWithUserData(user: User) {
        etFullName.setText(user.fullName)

        // Установка пола
        when (user.gender) {
            "Мужской" -> rgGender.check(R.id.rbMale)
            "Женский" -> rgGender.check(R.id.rbFemale)
        }

        // Установка курса
        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        val coursePosition = courses.indexOf(user.course)
        if (coursePosition != -1) {
            spCourse.setSelection(coursePosition)
        }

        // Установка уровня сложности
        sbDifficulty.progress = user.difficultyLevel
        tvDifficultyLevel.text = "Уровень: ${user.difficultyLevel}"

        // Установка даты рождения
        cvBirthDate.date = user.birthDate
        selectedBirthDate = user.birthDate

        // Обновление знака зодиака
        val calendar = Calendar.getInstance().apply { timeInMillis = user.birthDate }
        updateZodiacSign(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun clearForm() {
        etFullName.text.clear()
        rgGender.clearCheck()
        spCourse.setSelection(0)
        sbDifficulty.progress = 5
        tvDifficultyLevel.text = "Уровень: 5"
        cvBirthDate.date = System.currentTimeMillis()
        selectedBirthDate = System.currentTimeMillis()
        tvZodiacName.text = "Не выбран"
        ivZodiac.setImageResource(R.drawable.ic_default)
        tvResult.visibility = View.GONE
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
            Toast.makeText(requireContext(), "Введите ФИО", Toast.LENGTH_SHORT).show()
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

        val user = User(
            fullName = fullName,
            gender = gender,
            course = course,
            difficultyLevel = difficultyLevel,
            birthDate = selectedBirthDate,
            zodiacSign = zodiacSign
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val userId = repository.insertUser(user)
                Toast.makeText(requireContext(), "Пользователь сохранен!", Toast.LENGTH_SHORT).show()

                // Сохраняем ID текущего пользователя в SharedPreferences
                sharedPreferences.edit().putLong("current_user_id", userId).apply()

                displayUserInfo(user)
                loadExistingUsers() // Обновляем список пользователей

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserInfo(user: User) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val birthDateStr = dateFormat.format(Date(user.birthDate))

        val resultText = """
            Регистрация завершена!
            
            ФИО: ${user.fullName}
            Пол: ${user.gender}
            Курс: ${user.course}
            Уровень сложности: ${user.difficultyLevel}/10
            Дата рождения: $birthDateStr
            Знак зодиака: ${user.zodiacSign}
        """.trimIndent()

        tvResult.text = resultText
        tvResult.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // При возвращении на фрагмент обновляем список пользователей
        loadExistingUsers()
    }
}