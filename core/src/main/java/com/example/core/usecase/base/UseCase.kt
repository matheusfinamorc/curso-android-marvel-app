package com.example.core.usecase.base

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

// recebe um "P"arameto e "R"etorna algo
abstract class UseCase<in P, R> {

    // recebe o parametro na fun
    // retorna um fluxo de ResultStatus<R>
    operator fun invoke(params: P): Flow<ResultStatus<R>> = flow {
        emit(ResultStatus.Loading)
        emit(doWork(params))
    }.catch { throwable ->
        // dentro do catch se recupera qualquer erro
        emit(ResultStatus.Error(throwable))
    }

    // passo o "P"arametro e "R"ecebo o resultado
    protected abstract suspend fun doWork(params: P): ResultStatus<R>
}

abstract class PagingUseCase<in P, R : Any> {

    operator fun invoke(params: P): Flow<PagingData<R>> = createFlowObservable(params)

    protected abstract fun createFlowObservable(params: P): Flow<PagingData<R>>
}