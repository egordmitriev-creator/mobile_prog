package com.example.bugs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bugs.data.AppDatabase
import com.example.bugs.data.entities.Record
import com.example.bugs.data.repository.GameRepository
import com.example.bugs.fragments.GameView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var startButton: Button
    private lateinit var restartButton: Button
    private lateinit var startMessage: TextView

    private var score = 0
    private var gameTime = 0
    private var isGameRunning = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gameHandler: Handler
    private lateinit var repository: GameRepository
    private var currentUserId: Long = 0

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (isGameRunning) {
                gameTime++
                updateGameInfo()

                // Проверяем окончание времени раунда
                val roundDuration = sharedPreferences.getInt("duration", 60)
                if (gameTime >= roundDuration) {
                    endGame()
                } else {
                    gameHandler.postDelayed(this, 1000)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация UI элементов
        gameView = view.findViewById(R.id.gameView)
        scoreTextView = view.findViewById(R.id.scoreTextView)
        timerTextView = view.findViewById(R.id.timerTextView)
        startButton = view.findViewById(R.id.startButton)
        restartButton = view.findViewById(R.id.restartButton)
        startMessage = view.findViewById(R.id.startMessage)

        // Инициализация базы данных и репозитория
        val database = AppDatabase.getInstance(requireContext())
        repository = GameRepository(database)

        // Инициализация SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        gameHandler = Handler(Looper.getMainLooper())

        setupGame()
        setupButtons()
        loadCurrentUser()
    }

    private fun setupGame() {
        // Загружаем настройки
        val gameSpeed = sharedPreferences.getInt("speed", 50)
        val maxBugs = sharedPreferences.getInt("cockroaches", 10)

        gameView.setGameSettings(gameSpeed, maxBugs)
        gameView.setOnBugTappedListener { points ->
            score += points
            updateGameInfo()
        }

        gameView.setOnMissListener {
            score = maxOf(0, score - 5) // Штраф 5 очков, но не меньше 0
            updateGameInfo()
        }

        // Изначально игра не запущена
        resetGameState()
    }

    private fun setupButtons() {
        // Кнопка начала игры
        startButton.setOnClickListener {
            setupGame()
            if (currentUserId == 0L) {
                Toast.makeText(requireContext(), "Сначала зарегистрируйтесь!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startGame()
        }

        // Кнопка перезапуска
        restartButton.setOnClickListener {
            restartGame()
        }
    }

    private fun loadCurrentUser() {
        currentUserId = sharedPreferences.getLong("current_user_id", 0)
        if (currentUserId == 0L) {
            startButton.isEnabled = false
            startButton.text = "Сначала зарегистрируйтесь"
            startButton.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        }
    }

    private fun startGame() {
        // Скрываем кнопку старта и сообщение
        startButton.visibility = View.GONE
        startMessage.visibility = View.GONE
        restartButton.visibility = View.GONE

        // Сбрасываем игру
        score = 0
        gameTime = 0
        isGameRunning = true

        // Обновляем информацию
        updateGameInfo()

        // Запускаем игровое поле
        gameView.startGame()

        // Запускаем таймер
        gameHandler.post(gameRunnable)
    }

    private fun restartGame() {
        // Скрываем кнопку перезапуска
        restartButton.visibility = View.GONE

        // Сбрасываем игру
        score = 0
        gameTime = 0
        isGameRunning = true

        // Обновляем информацию
        updateGameInfo()

        // Перезапускаем игровое поле
        gameView.restartGame()

        // Запускаем таймер
        gameHandler.post(gameRunnable)
    }

    private fun endGame() {
        isGameRunning = false
        gameView.stopGame()
        gameHandler.removeCallbacks(gameRunnable)

        // Сохраняем рекорд
        saveRecord()

        // Показываем кнопку перезапуска
        restartButton.visibility = View.VISIBLE

        // Обновляем информацию о финальном счете
        scoreTextView.text = "Игра окончена! Счет: $score"
        timerTextView.text = "Время вышло!"
    }

    private fun saveRecord() {
        if (currentUserId == 0L) {
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val difficultyLevel = sharedPreferences.getInt("speed", 50)
                val record = Record(
                    userId = currentUserId,
                    score = score,
                    difficultyLevel = difficultyLevel,
                    gameDuration = gameTime
                )
                repository.insertRecord(record)
                Toast.makeText(requireContext(), "Рекорд сохранен: $score очков!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка сохранения рекорда", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetGameState() {
        score = 0
        gameTime = 0
        isGameRunning = false
        updateGameInfo()

        // Показываем начальное состояние
        startButton.visibility = View.VISIBLE
        startMessage.visibility = View.VISIBLE
        restartButton.visibility = View.GONE
    }

    private fun updateGameInfo() {
        val roundDuration = sharedPreferences.getInt("duration", 60)
        val timeLeft = roundDuration - gameTime

        scoreTextView.text = "Очки: $score"
        timerTextView.text = "Осталось: ${timeLeft}с"
    }

    override fun onPause() {
        super.onPause()
        isGameRunning = false
        gameHandler.removeCallbacks(gameRunnable)
        gameView.stopGame()
    }

    override fun onResume() {
        super.onResume()
        // При возобновлении обновляем состояние текущего пользователя
        loadCurrentUser()
        // Не возобновляем игру автоматически - ждем нажатия кнопки
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем handler при уничтожении view
        gameHandler.removeCallbacks(gameRunnable)
    }
}