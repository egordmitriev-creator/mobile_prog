package com.example.bugs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.example.bugs.R

class RulesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView: WebView = view.findViewById(R.id.webView)

        // Загрузка HTML из ресурсов
        val htmlText = resources.openRawResource(R.raw.game_rules)
            .bufferedReader().use { it.readText() }

        webView.loadData(htmlText, "text/html", "UTF-8")
    }
}