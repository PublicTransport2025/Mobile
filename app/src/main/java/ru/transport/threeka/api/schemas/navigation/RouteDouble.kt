package ru.transport.threeka.api.schemas.navigation

data class RouteDouble(
    val route_id1: Int,
    val label1: String,
    val number1: String,
    val load1: Int,
    val time_label1: String,
    val time_begin1: String,
    val time_road1: String,
    val stops1: List<Coord>,
    val stop1: String,
    val info1: String?,

    val route_id2: Int,
    val label2: String,
    val number2: String,
    val load2: Int,
    val time_label2: String,
    val time_begin2: String,
    val time_road2: String,
    val stops2: List<Coord>,
    val stop2: String,
    val info2: String?,
)
