package ru.transport.threeka.api.schemas.profile

data class VKLogin(
    val code_verifier: String,
    val state: String,
    val code: String,
    val device_id: String
)
