package ru.transport.threeka.api.schemas

data class Token(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expires_in: Long
)
