package ru.transport.threeka.api.schemas.navigation

data class RouteReport(
    val result: Int,
    val count: Int,
    val count_simple: Int,
    val simple_routes: List<RouteSimple>,
    val double_routes: List<RouteDouble>
)
