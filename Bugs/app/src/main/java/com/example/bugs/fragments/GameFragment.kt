package com.example.bugs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.bugs.databinding.FragmentGameBinding
import com.example.bugs.fragments.GameView
import com.example.bugs.viewmodels.GameViewModel
import org.koin.android.ext.android.inject

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    // Внедрение ViewModel через Koin
    private val viewModel: GameViewModel by viewModels()

    // Внедрение зависимостей через Koin
    private val sharedPreferences: SharedPreferences by inject()

    private lateinit var gameView: GameView
    private lateinit var gameHandler: Handler
    private var currentUserId: Long = 0

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (viewModel.isGameCurrentlyRunning()) {
                viewModel.incrementGameTime()
                updateGameInfo()

                val roundDuration = sharedPreferences.getInt("duration", 60)
                if (viewModel.getCurrentGameTime() >= roundDuration) {
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
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация UI элементов через ViewBinding
        gameView = binding.gameView
        gameHandler = Handler(Looper.getMainLooper())

        setupGame()
        setupButtons()
        loadCurrentUser()
        loadGoldRate()

        // Наблюдатели LiveData
        setupObservers()
    }

    private fun setupObservers() {
        // Наблюдаем за изменениями счета
        viewModel.score.observe(viewLifecycleOwner, Observer { score ->
            binding.scoreTextView.text = "Очки: $score"
        })

        // Наблюдаем за временем игры
        viewModel.gameTime.observe(viewLifecycleOwner, Observer { time ->
            val roundDuration = sharedPreferences.getInt("duration", 60)
            val timeLeft = roundDuration - time
            binding.timerTextView.text = "Осталось: ${timeLeft}с"
        })

        // Наблюдаем за статусом режима наклона
        viewModel.tiltModeActive.observe(viewLifecycleOwner, Observer { isActive ->
            if (isActive) {
                binding.tiltStatusTextView.text = "🌀 РЕЖИМ НАКЛОНА АКТИВЕН!"
                binding.tiltStatusTextView.visibility = View.VISIBLE
            } else {
                binding.tiltStatusTextView.visibility = View.GONE
            }
        })

        // Наблюдаем за курсом золота
        viewModel.goldRate.observe(viewLifecycleOwner, Observer { rate ->
            if (rate > 0) {
                gameView.setGoldRate(rate)
                binding.goldRateTextView.text = "Курс золота: ${String.format("%.2f", rate)}₽/унция"
                binding.goldRateTextView.visibility = View.VISIBLE
            }
        })

        // Наблюдаем за сообщениями игры
        viewModel.gameMessage.observe(viewLifecycleOwner, Observer { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupGame() {
        try {
            val gameSpeed = sharedPreferences.getInt("speed", 50)
            val maxBugs = sharedPreferences.getInt("cockroaches", 25)

            gameView.setGameSettings(gameSpeed, maxBugs)

            // Коллбэки теперь обновляют ViewModel
            gameView.setOnBugTappedListener { points ->
                viewModel.incrementScore(points)
            }

            gameView.setOnMissListener {
                viewModel.decrementScore(5)
            }

            gameView.setOnTiltBonusActivated { isActive ->
                viewModel.setTiltModeActive(isActive)
            }

            gameView.setOnGoldenBugTapped { points ->
                viewModel.incrementScore(points)
                viewModel.setGameMessage("Золотой таракан! +${points}₽")
            }

            resetGameState()
        } catch (e: Exception) {
            Log.e("GameFragment", "Error in setupGame: ${e.message}", e)
            Toast.makeText(requireContext(), "Ошибка инициализации игры", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupButtons() {
        binding.startButton.setOnClickListener {
            if (currentUserId == 0L) {
                Toast.makeText(requireContext(), "Сначала зарегистрируйтесь!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startGame()
        }

        binding.restartButton.setOnClickListener {
            restartGame()
        }
    }

    private fun loadCurrentUser() {
        try {
            currentUserId = sharedPreferences.getLong("current_user_id", 0)
            if (currentUserId == 0L) {
                binding.startButton.isEnabled = false
                binding.startButton.text = "Сначала зарегистрируйтесь"
                binding.startButton.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            }
        } catch (e: Exception) {
            Log.e("GameFragment", "Error loading current user: ${e.message}", e)
        }
    }

    private fun loadGoldRate() {
        viewModel.loadGoldRate()
    }

    private fun startGame() {
        try {
            binding.startButton.visibility = View.GONE
            binding.startMessage.visibility = View.GONE
            binding.restartButton.visibility = View.GONE
            binding.tiltStatusTextView.visibility = View.GONE

            viewModel.resetGame()
            viewModel.setGameRunning(true)

            updateGameInfo()
            gameView.startGame()
            gameHandler.post(gameRunnable)
        } catch (e: Exception) {
            Log.e("GameFragment", "Error starting game: ${e.message}", e)
            Toast.makeText(requireContext(), "Ошибка запуска игры", Toast.LENGTH_SHORT).show()
            resetGameState()
        }
    }

    private fun restartGame() {
        try {
            binding.restartButton.visibility = View.GONE
            binding.tiltStatusTextView.visibility = View.GONE

            viewModel.resetGame()
            viewModel.setGameRunning(true)

            updateGameInfo()
            gameView.restartGame()
            gameHandler.post(gameRunnable)
        } catch (e: Exception) {
            Log.e("GameFragment", "Error restarting game: ${e.message}", e)
            Toast.makeText(requireContext(), "Ошибка перезапуска игры", Toast.LENGTH_SHORT).show()
        }
    }

    private fun endGame() {
        viewModel.setGameRunning(false)
        try {
            gameView.stopGame()
        } catch (e: Exception) {
            Log.e("GameFragment", "Error stopping game view: ${e.message}", e)
        }
        gameHandler.removeCallbacks(gameRunnable)

        // Сохраняем рекорд через ViewModel
        saveRecord()

        binding.restartButton.visibility = View.VISIBLE
        binding.tiltStatusTextView.visibility = View.GONE

        binding.scoreTextView.text = "Игра окончена! Счет: ${viewModel.getCurrentScore()}"
        binding.timerTextView.text = "Время вышло!"
    }

    private fun saveRecord() {
        if (currentUserId == 0L) {
            return
        }

        val difficultyLevel = sharedPreferences.getInt("speed", 50)
        viewModel.saveRecord(
            userId = currentUserId,
            difficultyLevel = difficultyLevel,
            gameDuration = viewModel.getCurrentGameTime()
        )

        viewModel.setGameMessage("Рекорд сохранен: ${viewModel.getCurrentScore()} очков!")
    }

    private fun resetGameState() {
        binding.startButton.visibility = View.VISIBLE
        binding.startMessage.visibility = View.VISIBLE
        binding.restartButton.visibility = View.GONE
        binding.tiltStatusTextView.visibility = View.GONE
    }

    private fun updateGameInfo() {
        // Обновление через LiveData, поэтому здесь ничего не нужно
    }

    override fun onPause() {
        super.onPause()
        gameHandler.removeCallbacks(gameRunnable)
        try {
            if (viewModel.isGameCurrentlyRunning()) {
                gameView.stopGame()
            }
        } catch (e: Exception) {
            Log.e("GameFragment", "Error stopping game on pause: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        loadCurrentUser()
        loadGoldRate()

        // Если игра была запущена, продолжаем
        if (viewModel.isGameCurrentlyRunning()) {
            gameView.startGame()
            gameHandler.post(gameRunnable)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gameHandler.removeCallbacks(gameRunnable)
        _binding = null
    }
}