package ru.transport.threeka.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.transport.threeka.R
import ru.transport.threeka.data.MainViewModel

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
        if (timeBeginStr == "Nothing" || timeBeginStr == "None" || timeBeginStr == "-" || timeBeginStr == ""){
            timeBegin.visibility = View.GONE
        } else {
            timeBegin.text = timeBeginStr
        }

        val timeLabel2 = view.findViewById<TextView>(R.id.time_label2)
        val timeRoad = view.findViewById<TextView>(R.id.time_road)
        val timeRoadStr = viewModel.getRouteTimeRoad()
        if (timeRoadStr == "Nothing" || timeRoadStr == "None" || timeRoadStr == "-" || timeRoadStr == ""){
            timeLabel2.visibility = View.GONE
            timeRoad.visibility = View.GONE
        } else {
            timeRoad.text = timeRoadStr
        }

        val buttonLeft = view.findViewById<ImageButton>(R.id.button_left)
        buttonLeft.setOnClickListener {
            viewModel.left()
        }
        if (!viewModel.hasLeft()){
            buttonLeft.visibility = View.INVISIBLE
        }

        val buttonRight = view.findViewById<ImageButton>(R.id.button_right)
        buttonRight.setOnClickListener {
            viewModel.right()
        }
        if (!viewModel.hasRight()){
            buttonRight.visibility = View.INVISIBLE
        }

        val buttonNo = view.findViewById<Button>(R.id.button_no)
        buttonNo.setOnClickListener {
            viewModel.killRoute()
        }

        val buttonOk = view.findViewById<Button>(R.id.button_ok)
        buttonOk.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }
    }
}