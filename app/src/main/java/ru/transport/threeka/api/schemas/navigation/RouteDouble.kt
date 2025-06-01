package ru.transport.threeka.api.schemas.navigation

data class RouteDouble(
    val label1: String,
    val number1: String,
    val load1: Int,
    val time_label1: String,
    val time_begin1: String,
    val time_road1: String,
    val stops1: List<Coord>,
    val stop1: String,

    val label2: String,
    val number2: String,
    val load2: Int,
    val time_label2: String,
    val time_begin2: String,
    val time_road2: String,
    val stops2: List<Coord>,
    val stop2: String,
)
