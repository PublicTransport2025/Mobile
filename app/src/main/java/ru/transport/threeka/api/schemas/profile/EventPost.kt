package ru.transport.threeka.api.schemas.profile

data class EventPost(
    val type: Int,
    val line: Int,
    val lat: Float,
    val lon: Float
)
