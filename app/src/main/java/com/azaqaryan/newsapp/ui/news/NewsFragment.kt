package com.azaqaryan.newsapp.ui.news

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.azaqaryan.newsapp.appComponent
import com.azaqaryan.newsapp.CommonStates
import com.azaqaryan.newsapp.databinding.FragmentNewsBinding
import com.azaqaryan.newsapp.ui.NewsViewModel
import com.azaqaryan.newsapp.ui.adapter.NewsAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsFragment : Fragment() {
	private var binding: FragmentNewsBinding? = null

	@Inject
	lateinit var viewModelFactory: NewsViewModel.Factory
	private val viewModel: NewsViewModel by viewModels {
		viewModelFactory
	}

	private val newsAdapter by lazy {
		NewsAdapter { newsSource ->
			val action = NewsFragmentDirections.actionNewsFragmentToArticlesFragment(newsSource.id ?: "")
			findNavController().navigate(action)
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val binding = FragmentNewsBinding.inflate(inflater, container, false)
		this.binding = binding
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding?.recyclerNews?.apply {
			layoutManager = LinearLayoutManager(requireContext())
			adapter = this@NewsFragment.newsAdapter
		}
		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.state.collectLatest { state ->
					binding?.progressBar?.isVisible = state == CommonStates.PROGRESS
				}
			}
		}
		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.sources.collectLatest { sources ->
					newsAdapter.submitList(sources)
				}
			}
		}
		viewModel.fetchSources()
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		context.appComponent.inject(this)
	}

	override fun onDestroy() {
		super.onDestroy()
		binding = null
	}
}
