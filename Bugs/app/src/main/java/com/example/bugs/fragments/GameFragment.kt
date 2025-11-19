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
import com.example.bugs.data.repository.GoldRateRepository
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
    private lateinit var tiltStatusTextView: TextView
    private lateinit var goldRateTextView: TextView

    private var score = 0
    private var gameTime = 0
    private var isGameRunning = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gameHandler: Handler
    private lateinit var repository: GameRepository
    private lateinit var goldRateRepository: GoldRateRepository
    private var currentUserId: Long = 0

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (isGameRunning) {
                gameTime++
                updateGameInfo()

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

        gameView = view.findViewById(R.id.gameView)
        scoreTextView = view.findViewById(R.id.scoreTextView)
        timerTextView = view.findViewById(R.id.timerTextView)
        startButton = view.findViewById(R.id.startButton)
        restartButton = view.findViewById(R.id.restartButton)
        startMessage = view.findViewById(R.id.startMessage)
        tiltStatusTextView = view.findViewById(R.id.tiltStatusTextView)
        goldRateTextView = view.findViewById(R.id.goldRateTextView)

        val database = AppDatabase.getInstance(requireContext())
        repository = GameRepository(database)
        goldRateRepository = GoldRateRepository(requireContext())

        sharedPreferences = requireActivity().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        gameHandler = Handler(Looper.getMainLooper())

        setupGame()
        setupButtons()
        loadCurrentUser()
        loadGoldRate()
    }

    private fun setupGame() {
        val gameSpeed = sharedPreferences.getInt("speed", 50)
        val maxBugs = sharedPreferences.getInt("cockroaches", 25) // Увеличили по умолчанию

        gameView.setGameSettings(gameSpeed, maxBugs)
        gameView.setOnBugTappedListener { points ->
            score += points
            updateGameInfo()
        }

        gameView.setOnMissListener {
            score = maxOf(0, score - 5)
            updateGameInfo()
        }

        gameView.setOnTiltBonusActivated { isActive ->
            if (isActive) {
                tiltStatusTextView.text = "🌀 РЕЖИМ НАКЛОНА АКТИВЕН!"
                tiltStatusTextView.visibility = View.VISIBLE
            } else {
                tiltStatusTextView.visibility = View.GONE
            }
        }

        gameView.setOnGoldenBugTapped { points ->
            score += points
            updateGameInfo()
            Toast.makeText(requireContext(), "Золотой таракан! +${points}₽", Toast.LENGTH_SHORT).show()
        }

        resetGameState()
    }

    private fun setupButtons() {
        startButton.setOnClickListener {
            if (currentUserId == 0L) {
                Toast.makeText(requireContext(), "Сначала зарегистрируйтесь!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startGame()
        }

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

    private fun loadGoldRate() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val goldRate = goldRateRepository.getCurrentGoldRate()
                gameView.setGoldRate(goldRate)
                goldRateTextView.text = "Курс золота: ${String.format("%.2f", goldRate)}₽/унция"
                goldRateTextView.visibility = View.VISIBLE
            } catch (e: Exception) {
                val cachedRate = goldRateRepository.getCachedGoldRate()
                gameView.setGoldRate(cachedRate)
                goldRateTextView.text = "Курс золота: ${String.format("%.2f", cachedRate)}₽/унция (кэш)"
                goldRateTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun startGame() {
        startButton.visibility = View.GONE
        startMessage.visibility = View.GONE
        restartButton.visibility = View.GONE
        tiltStatusTextView.visibility = View.GONE

        score = 0
        gameTime = 0
        isGameRunning = true

        updateGameInfo()
        gameView.startGame()
        gameHandler.post(gameRunnable)
    }

    private fun restartGame() {
        restartButton.visibility = View.GONE
        tiltStatusTextView.visibility = View.GONE

        score = 0
        gameTime = 0
        isGameRunning = true

        updateGameInfo()
        gameView.restartGame()
        gameHandler.post(gameRunnable)
    }

    private fun endGame() {
        isGameRunning = false
        gameView.stopGame()
        gameHandler.removeCallbacks(gameRunnable)

        saveRecord()
        restartButton.visibility = View.VISIBLE
        tiltStatusTextView.visibility = View.GONE

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

        startButton.visibility = View.VISIBLE
        startMessage.visibility = View.VISIBLE
        restartButton.visibility = View.GONE
        tiltStatusTextView.visibility = View.GONE
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
        loadCurrentUser()
        loadGoldRate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gameHandler.removeCallbacks(gameRunnable)
    }
}