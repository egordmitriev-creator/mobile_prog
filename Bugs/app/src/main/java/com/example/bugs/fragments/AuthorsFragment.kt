package com.example.bugs.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bugs.R

class AuthorsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_authors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView: ListView = view.findViewById(R.id.authors_list)

        val authors = listOf(
            Author(R.drawable.avatar1, "Дмитриев Егор"),
            Author(R.drawable.avatar2, "Дмитриев Антон"),
        )

        val adapter = AuthorAdapter(requireContext(), authors)
        listView.adapter = adapter
    }
}

data class Author(val photoResId: Int, val name: String)

class AuthorAdapter(context: Context, private val authors: List<Author>) :
    ArrayAdapter<Author>(context, R.layout.list_item_author, authors) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_author, parent, false)

        val author = authors[position]
        val photo: ImageView = view.findViewById(R.id.author_photo)
        val name: TextView = view.findViewById(R.id.author_name)

        photo.setImageResource(author.photoResId)
        name.text = author.name

        return view
    }
}