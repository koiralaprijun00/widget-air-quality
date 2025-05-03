package com.example.nepalweatherwidget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment

class SearchFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editText = view.findViewById<EditText>(R.id.editTextLocation)
        val button = view.findViewById<Button>(R.id.buttonSearch)
        button.setOnClickListener {
            val location = editText.text.toString().trim()
            if (location.isNotEmpty()) {
                (activity as? SearchListener)?.onLocationSearched(location)
            }
        }
    }

    interface SearchListener {
        fun onLocationSearched(location: String)
    }
} 