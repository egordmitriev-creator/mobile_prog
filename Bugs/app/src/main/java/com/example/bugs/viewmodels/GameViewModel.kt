package com.example.bugs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bugs.data.entities.Record
import com.example.bugs.data.repository.GameRepository
import com.example.bugs.data.repository.GoldRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameViewModel(
    private val gameRepository: GameRepository,
    private val goldRateRepository: GoldRateRepository
) : ViewModel() {

    // LiveData для состояния игры
    private val _score = MutableLiveData<Int>(0)
    val score: LiveData<Int> = _score

    private val _gameTime = MutableLiveData<Int>(0)
    val gameTime: LiveData<Int> = _gameTime

    private val _isGameRunning = MutableLiveData<Boolean>(false)
    val isGameRunning: LiveData<Boolean> = _isGameRunning

    private val _goldRate = MutableLiveData<Float>(0f)
    val goldRate: LiveData<Float> = _goldRate

    private val _tiltModeActive = MutableLiveData<Boolean>(false)
    val tiltModeActive: LiveData<Boolean> = _tiltModeActive

    private val _gameMessage = MutableLiveData<String>("")
    val gameMessage: LiveData<String> = _gameMessage

    // Методы для изменения состояния
    fun incrementScore(points: Int) {
        _score.value = (_score.value ?: 0) + points
    }

    fun decrementScore(points: Int) {
        _score.value = maxOf(0, (_score.value ?: 0) - points)
    }

    fun updateGameTime(time: Int) {
        _gameTime.value = time
    }

    fun incrementGameTime() {
        _gameTime.value = (_gameTime.value ?: 0) + 1
    }

    fun setGameRunning(running: Boolean) {
        _isGameRunning.value = running
    }

    fun setTiltModeActive(active: Boolean) {
        _tiltModeActive.value = active
    }

    fun setGameMessage(message: String) {
        _gameMessage.value = message
    }

    // Загрузка курса золота
    fun loadGoldRate() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rate = goldRateRepository.getCurrentGoldRate()
                _goldRate.postValue(rate)
            } catch (e: Exception) {
                val cachedRate = goldRateRepository.getCachedGoldRate()
                _goldRate.postValue(cachedRate)
            }
        }
    }

    fun getCachedGoldRate(): Float {
        return goldRateRepository.getCachedGoldRate()
    }

    // Сохранение рекорда
    fun saveRecord(userId: Long, difficultyLevel: Int, gameDuration: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val record = Record(
                    userId = userId,
                    score = _score.value ?: 0,
                    difficultyLevel = difficultyLevel,
                    gameDuration = gameDuration
                )
                gameRepository.insertRecord(record)
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    // Сброс состояния игры
    fun resetGame() {
        _score.value = 0
        _gameTime.value = 0
        _isGameRunning.value = false
        _tiltModeActive.value = false
        _gameMessage.value = ""
    }

    // Получение текущих значений
    fun getCurrentScore(): Int = _score.value ?: 0
    fun getCurrentGameTime(): Int = _gameTime.value ?: 0
    fun isGameCurrentlyRunning(): Boolean = _isGameRunning.value ?: false
}