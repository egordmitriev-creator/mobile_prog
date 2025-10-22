package com.example.bugs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bugs.R
import com.example.bugs.adapter.RecordsAdapter
import com.example.bugs.data.AppDatabase
import com.example.bugs.data.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecordsAdapter
    private lateinit var repository: GameRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getInstance(requireContext())
        repository = GameRepository(database)

        recyclerView = view.findViewById(R.id.recordsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecordsAdapter(emptyList())
        recyclerView.adapter = adapter

        loadRecords()
    }

    private fun loadRecords() {
        CoroutineScope(Dispatchers.Main).launch {
            repository.getTopRecords().collect { records ->
                adapter.updateData(records)
            }
        }
    }
}