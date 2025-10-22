package com.example.bugs.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bugs.GameFragment
import com.example.bugs.RegistrationFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RegistrationFragment()    // Регистрация
            1 -> RulesFragment()           // Правила игры
            2 -> AuthorsFragment()         // Авторы
            3 -> SettingsFragment()        // Настройки
            4 -> GameFragment()            // вкладка с игрой
            5 -> RecordsFragment()
            else -> throw IllegalArgumentException("Неверная позиция: $position")
        }
    }
}