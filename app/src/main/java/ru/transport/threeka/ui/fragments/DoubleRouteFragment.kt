package ru.transport.threeka.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.transport.threeka.R
import ru.transport.threeka.data.MainViewModel
import ru.transport.threeka.services.BusAlertManager

class DoubleRouteFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_route_double, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val routeTitle = view.findViewById<TextView>(R.id.route_title)
        routeTitle.text = viewModel.getRouteTitle().replace("ул. ", "")

        val routeTitleDouble = view.findViewById<TextView>(R.id.route_title_second)
        routeTitleDouble.text = viewModel.getRouteTitleDouble().replace("ул. ", "")

        val routeNumber = view.findViewById<TextView>(R.id.route_number)
        routeNumber.text = viewModel.getRouteNumber()

        val routeNumberDouble = view.findViewById<TextView>(R.id.route_number_second)
        routeNumberDouble.text = viewModel.getRouteNumberDouble()

        val load = viewModel.getRouteLoad()
        val load_str = when (load) {
            1 -> "Свободно"
            2 -> "Низкая"
            3 -> "Средняя"
            4 -> "Высокая"
            5 -> "Заполнен"
            else -> "Неизвестно"
        }
        val routeLoad = view.findViewById<TextView>(R.id.route_load)
        routeLoad.text = load_str

        val load2 = viewModel.getRouteLoadDouble()
        val load_str2 = when (load2) {
            1 -> "Свободно"
            2 -> "Низкая"
            3 -> "Средняя"
            4 -> "Высокая"
            5 -> "Заполнен"
            else -> "Неизвестно"
        }
        val routeLoad2 = view.findViewById<TextView>(R.id.route_load_second)
        routeLoad2.text = load_str2

        val stopLabel1 = view.findViewById<TextView>(R.id.stop_first)
        stopLabel1.text = viewModel.getRouteStop1()

        val stopLabel2 = view.findViewById<TextView>(R.id.stop_second)
        stopLabel2.text = viewModel.getRouteStop2()

        if (stopLabel2.text == stopLabel1.text) {
            stopLabel2.visibility = View.GONE
        }

        val timeLabel = view.findViewById<TextView>(R.id.time_label)
        timeLabel.text = viewModel.getRouteTimeLabel()

        val timeLabelDouble = view.findViewById<TextView>(R.id.time_label_second)
        timeLabelDouble.text = viewModel.getRouteTimeLabelDouble()

        val timeBegin = view.findViewById<TextView>(R.id.time_begin)
        val timeBeginStr = viewModel.getRouteTimeBegin()
        if (timeBeginStr == "Nothing" || timeBeginStr == "None" || timeBeginStr == "-" || timeBeginStr == "") {
            timeBegin.visibility = View.GONE
        } else {
            timeBegin.text = timeBeginStr
        }

        val timeBeginDouble = view.findViewById<TextView>(R.id.time_begin_second)
        val timeBeginStrDouble = viewModel.getRouteTimeBeginDouble()
        if (timeBeginStrDouble == "Nothing" || timeBeginStrDouble == "None" || timeBeginStrDouble == "-" || timeBeginStrDouble == "") {
            timeBeginDouble.visibility = View.GONE
        } else {
            timeBeginDouble.text = timeBeginStrDouble
        }

        val timeLabel2 = view.findViewById<TextView>(R.id.time_label2)
        val timeRoad = view.findViewById<TextView>(R.id.time_road)
        val timeRoadStr = viewModel.getRouteTimeRoad()
        if (timeRoadStr == "Nothing" || timeRoadStr == "None" || timeRoadStr == "-" || timeRoadStr == "") {
            timeLabel2.visibility = View.GONE
            timeRoad.visibility = View.GONE
        } else {
            timeRoad.text = timeRoadStr
        }


        val timeLabel2Double = view.findViewById<TextView>(R.id.time_label2_second)
        val timeRoadDouble = view.findViewById<TextView>(R.id.time_road_second)
        val timeRoadStrDouble = viewModel.getRouteTimeRoadDouble()
        if (timeRoadStrDouble == "Nothing" || timeRoadStrDouble == "None" || timeRoadStrDouble == "-" || timeRoadStrDouble == "") {
            timeLabel2Double.visibility = View.GONE
            timeRoadDouble.visibility = View.GONE
        } else {
            timeRoadDouble.text = timeRoadStrDouble
        }


        val buttonLeft = view.findViewById<ImageButton>(R.id.button_left)
        buttonLeft.setOnClickListener {
            viewModel.left()
        }
        if (!viewModel.hasLeft()) {
            buttonLeft.visibility = View.INVISIBLE
        }

        val buttonRight = view.findViewById<ImageButton>(R.id.button_right)
        buttonRight.setOnClickListener {
            viewModel.right()
        }
        if (!viewModel.hasRight()) {
            buttonRight.visibility = View.INVISIBLE
        }

        val buttonNo = view.findViewById<Button>(R.id.button_no)
        buttonNo.setOnClickListener {
            viewModel.killRoute()
        }

        val busAlertManager = BusAlertManager(requireContext())

        val buttonOk = view.findViewById<Button>(R.id.button_ok)
        buttonOk.setOnClickListener {

            if (viewModel.authorized.value == true &&
                viewModel.notif.value == true
            ) {

                if (timeLabel.text == "По графику") {
                    val input = timeBegin.text.toString()
                    val timeString = input.substringAfter("в ").trim()
                    val parts = timeString.split(":")
                    val hours = parts[0].toInt()
                    val minutes = parts[1].toInt()

                    busAlertManager.sendNotif(hours, minutes, routeNumber.text.toString())
                    Log.w("Bus notuf", routeNumber.text.toString())
                }

                if (timeLabelDouble.text == "По графику") {
                    val input2 = timeBeginDouble.text.toString()
                    val timeString2 = input2.substringAfter("в ").trim()
                    val parts2 = timeString2.split(":")
                    val hours2 = parts2[0].toInt()
                    val minutes2 = parts2[1].toInt()

                    busAlertManager.sendNotif(hours2, minutes2, routeNumberDouble.text.toString())

                    Toast.makeText(
                        requireContext(),
                        "Вы получите напоминание о прибытии транспорта",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val newFragment = BusAbsenceFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, newFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}