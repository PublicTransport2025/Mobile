package ru.transport.threeka.api.schemas.profile

data class Token(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expires_in: Long,
    val login: String?,
    val name: String,
    val email: Boolean,
    val vk: Boolean
)
