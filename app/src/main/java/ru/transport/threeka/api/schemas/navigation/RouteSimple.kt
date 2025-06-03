package ru.transport.threeka.api.schemas.navigation

data class RouteSimple(
    val label: String,
    val number: String,
    val long: String,
    val load: Int,
    val route_id: Int,

    val time_label: String,
    val time_begin: String,
    val time_road: String,
    val info: String?,
    val stops: List<Coord>
)
