package com.umc.edison.domain

sealed class DataResource<out T> {
    class Success<out T>(val data: T) : DataResource<T>()
    class Error(val throwable: Throwable) : DataResource<Nothing>()
    class Loading<out T>(val data: T? = null) : DataResource<T>()

    companion object {
        fun <T> success(data: T): Success<T> = Success(data)
        fun error(throwable: Throwable): Error = Error(throwable)
        fun <T> loading(data: T? = null): Loading<T> = Loading(data)
    }
}