package ru.transport.threeka.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.transport.threeka.R
import ru.transport.threeka.api.RetrofitClient.apiService
import ru.transport.threeka.api.schemas.navigation.Event
import ru.transport.threeka.api.schemas.profile.EventPost
import ru.transport.threeka.data.MainViewModel
import ru.transport.threeka.services.TokenManager

class PointInfoFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private var stop_id = 0;

    private var point_lat = 0.0;
    private var point_lon = 0.0;

    private lateinit var buttonEvent: Button
    private lateinit var buttonTypeEvent: Button
    private lateinit var buttonLineEvent: Button
    private lateinit var buttonSaveEvent: Button
    private lateinit var groupEvent: LinearLayout

    private var eventType = -1
    private var eventLine = -1

    companion object {
        private const val POINT_LAT = "0"
        private const val POINT_LON = "1"

        fun newInstance(point_lat: Double, point_lon: Double): PointInfoFragment {
            val fragment = PointInfoFragment()
            val args = Bundle()
            args.putDouble(POINT_LAT, point_lat)
            args.putDouble(POINT_LON, point_lon)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_point_info, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.authorized.value == false) {
            buttonEvent.visibility = View.GONE
            groupEvent.visibility = View.GONE
        }
    }

    private val eventTypes = arrayOf("ДТП", "Дорожные работы", "Перекрытие движения", "Затор", "Неблагоприятные погодные условия", "Опасность на дороге")
    private val eventLines = arrayOf("1 (в т.ч. выделенка)", "2", "3", "4", "все")

    private fun showOptionsDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Что произошло?")
            .setItems(eventTypes) { dialog, which ->
                eventType = which
                buttonTypeEvent.text = eventTypes[which]
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLinesDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("На какой полосе?")
            .setItems(eventLines) { dialog, which ->
                eventLine = which
                buttonLineEvent.text = eventLines[which]
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearMapPoint()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        point_lat = arguments?.getDouble(POINT_LAT) ?: 0.0
        point_lon = arguments?.getDouble(POINT_LON) ?: 0.0

        val headerLabel = view.findViewById<TextView>(R.id.header_view)
        val pointAbout = view.findViewById<TextView>(R.id.label_about)
        buttonEvent = view.findViewById(R.id.button_event)
        buttonTypeEvent = view.findViewById(R.id.button_type_event)
        buttonLineEvent = view.findViewById(R.id.button_line_event)
        buttonSaveEvent = view.findViewById(R.id.button_save_event)
        groupEvent = view.findViewById(R.id.input_event_group)



        pointAbout.text = String.format("%.4f СШ %.4f ВД", point_lat, point_lon)


        val buttonClose = view.findViewById<ImageButton>(R.id.button_close)
        buttonClose.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
            viewModel.awakeRoute()
        }


        if (viewModel.authorized.value == true) {
            buttonEvent.visibility = View.VISIBLE

        } else {
            buttonEvent.visibility = View.GONE
        }

        buttonEvent.setOnClickListener {
            groupEvent.visibility = View.VISIBLE
            buttonEvent.visibility = View.GONE
            headerLabel.text = "Дорожное событие"
        }


        buttonTypeEvent.setOnClickListener {
            showOptionsDialog(requireContext())
        }

        buttonLineEvent.setOnClickListener {
            showLinesDialog(requireContext())
        }

        val tokenManager = TokenManager(requireContext())

        buttonSaveEvent.setOnClickListener {

            if (eventType < 0) {
                Toast.makeText(requireContext(), "Укажите тип события", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (eventLine < 0) {
                Toast.makeText(requireContext(), "Укажите полосу дороги", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val event = EventPost(
                type = eventType,
                line = eventLine,
                lat = point_lat.toFloat(),
                lon = point_lon.toFloat()
            )

            val call = apiService.writeEvent(tokenManager.getAccessToken()!!, event)
            call.enqueue(object : Callback<Event> {
                override fun onResponse(
                    call: Call<Event>,
                    response: Response<Event>
                ) {
                    if (response.isSuccessful) {
                        val event_rez = response.body()
                        if (event_rez != null) {
                            viewModel.addEvent(event_rez)
                            viewModel.awakeRoute()
                        } else{
                            Toast.makeText(
                                requireContext(),
                                "Не удалось добавить событие",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val message = when (response.code()) {
                            400 -> "Вы уже предложили 3 события, дождитесь их оценки редакцией"
                            422 -> "Указали ли Вы все значения?"
                            else -> "Произошла неизвестная ошибка"
                        }
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Event>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Проблема с интернет соединением",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

    }
}