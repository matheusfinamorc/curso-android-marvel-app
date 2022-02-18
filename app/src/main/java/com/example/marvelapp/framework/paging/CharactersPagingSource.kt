package com.example.marvelapp.framework.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.core.data.repository.CharactersRemoteDataSource
import com.example.core.domain.model.Character
import com.example.marvelapp.framework.network.response.DataWrapperResponse
import com.example.marvelapp.framework.network.response.toCharacterModel
import java.lang.Exception

class CharactersPagingSource(
    private val remoteDataSource: CharactersRemoteDataSource<DataWrapperResponse>,
    private val query: String
): PagingSource<Int, Character>() {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> {
        return try {
            val offset = params.key ?: 0

            // define os parametros que vao estar na url
            val queries = hashMapOf(
                "offset" to offset.toString()
            )

            if(query.isNotEmpty()){
                queries["nameStartsWith"] = query
            }

            val response = remoteDataSource.fetchCharacters(queries)

            // para calcular qual a prox key a ser passada para o params
            val responseOffset = response.data.offset
            val totalCharacters = response.data.total

            LoadResult.Page(
                data = response.data.results.map { it.toCharacterModel() },
                prevKey = null,
                // garante fazer requisicoes se o offset(character por pag (de 20 em 20))
                // for menor que o total de personagens
                nextKey = if (responseOffset < totalCharacters){
                    // se for trabalhar com numero de pagina, acresceta +1 ao inves de LIMIT
                    responseOffset + LIMIT
                }else null
            )

        }catch (exception: Exception){
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? {
        // tenta recuperar a posicao do adapter com base na anchorPosition(pagina ancora)
        // pagina que guardou por ultimo

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(LIMIT) ?: anchorPage?.nextKey?.minus(LIMIT)
        }
    }

    companion object{
        private const val LIMIT = 20
    }

}