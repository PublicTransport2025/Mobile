package ru.transport.threeka.api.schemas.profile

data class Reg(
    val name: String,
    val email: String,
    val password: String,
    val code: String
)
