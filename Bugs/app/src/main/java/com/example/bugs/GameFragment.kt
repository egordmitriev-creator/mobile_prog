package com.example.bugs

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.random.Random

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private var score = 0
    private var gameTime = 0
    private var isGameRunning = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gameHandler: Handler
    private val gameRunnable = object : Runnable {
        override fun run() {
            if (isGameRunning) {
                gameTime++
                updateGameInfo()
                gameHandler.postDelayed(this, 1000)
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

        sharedPreferences = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        gameHandler = Handler(Looper.getMainLooper())

        setupGame()
    }

    private fun setupGame() {
        // Загружаем настройки
        val gameSpeed = sharedPreferences.getInt("speed", 50)
        val maxBugs = sharedPreferences.getInt("cockroaches", 10)
        val roundDuration = sharedPreferences.getInt("duration", 60)

        gameView.setGameSettings(gameSpeed, maxBugs)
        gameView.setOnBugTappedListener { points ->
            score += points
            updateGameInfo()
        }

        gameView.setOnMissListener {
            score = maxOf(0, score - 5) // Штраф 5 очков, но не меньше 0
            updateGameInfo()
        }

        startGame(roundDuration)
    }

    private fun startGame(duration: Int) {
        score = 0
        gameTime = 0
        isGameRunning = true
        gameView.startGame()
        gameHandler.post(gameRunnable)

        // Автоматическое завершение игры через duration секунд
        gameHandler.postDelayed({
            endGame()
        }, duration * 1000L)
    }

    private fun endGame() {
        isGameRunning = false
        gameView.stopGame()
        gameHandler.removeCallbacks(gameRunnable)

        // Показываем итоговый счет
        scoreTextView.text = "Игра окончена! Счет: $score"
    }

    private fun updateGameInfo() {
        scoreTextView.text = "Очки: $score"
        timerTextView.text = "Время: ${gameTime}с"
    }

    override fun onPause() {
        super.onPause()
        isGameRunning = false
        gameHandler.removeCallbacks(gameRunnable)
        gameView.stopGame()
    }

    override fun onResume() {
        super.onResume()
        if (isGameRunning) {
            gameView.startGame()
            gameHandler.post(gameRunnable)
        }
    }
}