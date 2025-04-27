package ru.transport.threeka.api.schemas.navigation

import ru.transport.threeka.api.schemas.Coord

data class RouteSimple(
    val label: String,
    val number: String,
    val long: String,
    val load: Int,

    val time_label: String,
    val time_begin: String,
    val time_road: String,

    val stops: List<Coord>
)
