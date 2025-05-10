package com.example.nepalweatherwidget.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nepalweatherwidget.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    
    private var searchListener: SearchListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.searchButton.setOnClickListener {
            val location = binding.searchInput.text.toString()
            if (location.isNotEmpty()) {
                searchListener?.onLocationSearched(location)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface SearchListener {
        fun onLocationSearched(location: String)
    }

    companion object {
        fun newInstance(listener: SearchListener): SearchFragment {
            return SearchFragment().apply {
                searchListener = listener
            }
        }
    }
} 