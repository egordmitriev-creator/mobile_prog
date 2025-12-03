package com.example.bugs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.bugs.databinding.ActivityMainBinding
import com.example.bugs.fragments.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация Koin
        startKoin {
            androidContext(this@MainActivity)
            modules(com.example.bugs.di.appModule)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)

        viewPager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Регистрация"
                1 -> "Правила"
                2 -> "Авторы"
                3 -> "Настройки"
                4 -> "Игра"
                5 -> "Рекорды"
                else -> null
            }
        }.attach()
    }
}