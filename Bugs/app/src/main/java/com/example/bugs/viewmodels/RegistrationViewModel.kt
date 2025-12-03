package com.example.bugs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bugs.data.entities.User
import com.example.bugs.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _users = MutableLiveData<List<User>>(emptyList())
    val users: LiveData<List<User>> = _users

    private val _registrationStatus = MutableLiveData<String>("")
    val registrationStatus: LiveData<String> = _registrationStatus

    // Загрузка пользователей
    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                gameRepository.getAllUsers().collect { userList ->
                    _users.postValue(userList)
                }
            } catch (e: Exception) {
                _registrationStatus.postValue("Ошибка загрузки пользователей")
            }
        }
    }

    // Регистрация нового пользователя
    fun registerUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = gameRepository.insertUser(user)
                _registrationStatus.postValue("Пользователь сохранен!")
                loadUsers() // Обновляем список
            } catch (e: Exception) {
                _registrationStatus.postValue("Ошибка сохранения: ${e.message}")
            }
        }
    }
}