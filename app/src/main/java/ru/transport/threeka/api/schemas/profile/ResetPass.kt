package ru.transport.threeka.api.schemas.profile

data class ResetPass(
    val email: String,
    val password: String,
    val code: String
)
