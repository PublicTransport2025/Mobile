package ru.transport.threeka.api.schemas

data class VKLogin(
    val code_verifier: String,
    val state: String,
    val code: String,
    val device_id: String
)
