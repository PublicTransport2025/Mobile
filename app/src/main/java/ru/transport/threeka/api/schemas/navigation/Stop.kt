package ru.transport.threeka.api.schemas.navigation

data class Stop(
    val id: Int,
    val name: String,
    val about: String?,
    val like: Boolean,
    val coord: Coord
)
