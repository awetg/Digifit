package com.bwet.digifit.utils

// Thread safe initialization of singleton with parameter in Kotlin
// source https://stackoverflow.com/a/53580852

open class SingletonHolder<out T, in A>(private val constructor: (A) -> T) {

    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        return when {
            instance != null -> instance!!
            else -> synchronized(this) {
                if (instance == null) instance = constructor(arg)
                instance!!
            }
        }
    }
}