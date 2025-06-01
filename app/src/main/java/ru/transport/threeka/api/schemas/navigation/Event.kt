package ru.transport.threeka.api.schemas.navigation

data class Event(
    val id: String,
    val type: Int,
    val line: Int,
    val lat: Float,
    val lon: Float,
    val moderated: Int,
    val my: Boolean
)
