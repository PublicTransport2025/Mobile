package ru.transport.threeka.api.schemas

data class ResetPass(
    val email: String,
    val password: String,
    val code: String
)
