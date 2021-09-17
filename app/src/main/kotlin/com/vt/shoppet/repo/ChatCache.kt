package com.vt.shoppet.repo

import com.vt.shoppet.model.Chat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatCache @Inject constructor() {
    private val _value: MutableStateFlow<Chat.Event?> = MutableStateFlow(null)
    val value = _value.asStateFlow().filterNotNull()

    fun setValue(event: Chat.Event) {
        _value.value = event
    }

    fun clear() {
        _value.value = null
    }
}