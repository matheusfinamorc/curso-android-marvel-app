package com.example.core.data.repository

 // <T> ->  quem implementar essa interface, fala qual o tipo que quer retornar
interface CharactersRemoteDataSource<T> {

    suspend fun fetchCharacters(queries: Map<String, String>): T
}