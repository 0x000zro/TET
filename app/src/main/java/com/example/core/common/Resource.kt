package com.example.core.common

/**
 * A generic class that holds a value with its loading status.
 * Used across the Domain and UI layers for reactive state communication.
 */
sealed class Resource<out T> {
    data object Idle : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}
