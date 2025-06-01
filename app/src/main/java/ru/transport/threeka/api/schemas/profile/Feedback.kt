package ru.transport.threeka.api.schemas.profile

data class Feedback(
    val name: String,
    val email: String,
    val mark: Int,
    val about: String
)
