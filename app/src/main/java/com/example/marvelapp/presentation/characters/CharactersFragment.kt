package com.example.marvelapp.presentation.characters

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import com.example.core.domain.model.Character
import com.example.marvelapp.R
import com.example.marvelapp.databinding.FragmentCharactersBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CharactersFragment : Fragment() {

    private var _binding: FragmentCharactersBinding? = null
    private val binding: FragmentCharactersBinding get() = _binding!!
    private val viewModel: CharactersViewModel by viewModels()

    private lateinit var charactersAdapter: CharactersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentCharactersBinding.inflate(
        inflater,
        container,
        false
    ).apply {
        _binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCharactersAdapter()
        observeInitialLoadState()

        lifecycleScope.launch {
            // quando o app for pra background, ele faz o stop do flow (para de escutar as att)
            // quando abre novamente, ele comeca a coletar novamente
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.charactersPagingData("").collect { pagingData ->
                    charactersAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun initCharactersAdapter() {
        charactersAdapter = CharactersAdapter()

        with(binding.recyclerCharacters) {
            // garante que, quando navegar para uma outra tela e voltar, ele volte na posicao 0
            scrollToPosition(0)
            setHasFixedSize(true)
            adapter = charactersAdapter.withLoadStateFooter(
                footer = CharactersLoadStateAdapter(
                    charactersAdapter::retry
                )
            )
        }
    }

    private fun observeInitialLoadState() {
        lifecycleScope.launch {
            // coleta os estados
            charactersAdapter.loadStateFlow.collectLatest { loadState ->
                // verifica o estado inicial do loading
                binding.flipperCharacters.displayedChild = when (loadState.refresh) {
                    // verifica que esta carregando e mostra o shimmer
                    is LoadState.Loading -> {
                        setShimmerVisibility(true)
                        FLIPPER_CHILD_LOADING
                    }
                    // verifica que nao esta carregando e mostra os personagens
                    is LoadState.NotLoading -> {
                        setShimmerVisibility(false)
                        FLIPPER_CHILD_CHARACTERS
                    }
                    // verifica que houve erro e mostra o include de error
                    is LoadState.Error -> {
                        setShimmerVisibility(false)
                        binding.includeViewCharactersErrorState.buttonRetry.setOnClickListener {
                            charactersAdapter.refresh()
                        }
                        FLIPPER_CHILD_ERROR
                    }
                }
            }
        }
    }

    private fun setShimmerVisibility(visibility: Boolean) {
        binding.includeViewCharactersLoadingState.shimmerCharacters.run {
            isVisible = visibility
            if (visibility) {
                startShimmer()
            } else stopShimmer()
        }
    }

    companion object {
        // posicao 0 é o include de loading
        private const val FLIPPER_CHILD_LOADING = 0

        // posicao 1 é o recyclerview (com os personagens)
        private const val FLIPPER_CHILD_CHARACTERS = 1

        // posicao 2 é o include de error
        private const val FLIPPER_CHILD_ERROR = 2
    }
}