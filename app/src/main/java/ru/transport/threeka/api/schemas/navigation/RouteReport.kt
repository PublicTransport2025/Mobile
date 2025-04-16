package ru.transport.threeka.api.schemas.navigation

import ru.transport.threeka.api.schemas.Coord

data class RouteReport(
    val result: Int,
    val count: Int,
    val simple_routes: List<RouteSimple>
)
