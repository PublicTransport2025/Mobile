package ru.transport.threeka.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.appmetrica.analytics.AppMetrica
import ru.transport.threeka.R
import ru.transport.threeka.data.MainViewModel
import ru.transport.threeka.services.BusAlertManager

class SimpleRouteFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_route_simple, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val routeTitle = view.findViewById<TextView>(R.id.route_title)
        routeTitle.text = viewModel.getRouteTitle().replace("ул. ", "")

        val routeNumber = view.findViewById<TextView>(R.id.route_number)
        routeNumber.text = viewModel.getRouteNumber()

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

        val timeLabel = view.findViewById<TextView>(R.id.time_label)
        timeLabel.text = viewModel.getRouteTimeLabel()

        val timeBegin = view.findViewById<TextView>(R.id.time_begin)
        val timeBeginStr = viewModel.getRouteTimeBegin()
        if (timeBeginStr == "Nothing" || timeBeginStr == "None" || timeBeginStr == "-" || timeBeginStr == "") {
            timeBegin.visibility = View.GONE
        } else {
            timeBegin.text = timeBeginStr
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

        val buttonLeft = view.findViewById<ImageButton>(R.id.button_left)
        buttonLeft.setOnClickListener {
            viewModel.left()
        }
        if (!viewModel.hasLeft()) {
            buttonLeft.visibility = View.INVISIBLE
        }

        val buttonRight = view.findViewById<ImageButton>(R.id.button_right)
        if (viewModel.hasRight()) {
            buttonRight.setOnClickListener {
                viewModel.right()
            }
        } else {
            buttonRight.setOnClickListener {
                val newFragment = BusAbsenceFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, newFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        val buttonNo = view.findViewById<Button>(R.id.button_no)
        buttonNo.setOnClickListener {
            viewModel.killRoute()
        }

        val busAlertManager = BusAlertManager(requireContext())

        val buttonOk = view.findViewById<Button>(R.id.button_ok)
        buttonOk.setOnClickListener {
            if (timeLabel.text == "По графику" &&
                viewModel.authorized.value == true &&
                viewModel.notif.value == true
            ) {

                val input = timeBegin.text.toString()
                val timeString = input.substringAfter("в ").trim()
                val parts = timeString.split(":")
                val hours = parts[0].toInt()
                val minutes = parts[1].toInt()

                busAlertManager.sendNotif(hours, minutes, routeNumber.text.toString())
                Toast.makeText(
                    requireContext(),
                    "Вы получите напоминание о прибытии транспорта",
                    Toast.LENGTH_SHORT
                ).show()
            }

            val eventParameters =
                mapOf("count" to "simple", "number" to routeNumber.text.toString())
            AppMetrica.reportEvent("RouteStarted", eventParameters)

            buttonOk.visibility = View.GONE
            buttonNo.visibility = View.GONE
            buttonLeft.visibility = View.GONE
            buttonRight.visibility = View.GONE
        }

        val root = view.findViewById<View>(R.id.root_container)
        root.setOnClickListener {
            buttonOk.visibility = View.VISIBLE
            buttonNo.visibility = View.VISIBLE
            if (viewModel.hasLeft()) {
                buttonLeft.visibility = View.VISIBLE
            }
            buttonRight.visibility = View.VISIBLE
        }
    }
}