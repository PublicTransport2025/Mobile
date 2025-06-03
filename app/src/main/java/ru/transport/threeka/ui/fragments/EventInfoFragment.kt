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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.transport.threeka.R
import ru.transport.threeka.api.RetrofitClient.apiService
import ru.transport.threeka.api.schemas.navigation.Event
import ru.transport.threeka.api.schemas.navigation.Stop
import ru.transport.threeka.data.MainViewModel
import ru.transport.threeka.services.TokenManager

class EventInfoFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private var event_id = 0;
    private lateinit var buttonDelete: Button
    private lateinit var buttonFix: Button

    companion object {
        private const val EVENT_ID = "0"

        fun newInstance(stop_id: Int): EventInfoFragment {
            val fragment = EventInfoFragment()
            val args = Bundle()
            args.putInt(EVENT_ID, stop_id)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_info, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.authorized.value == false) {
            buttonDelete.visibility = View.GONE
            buttonFix.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        event_id = arguments?.getInt(EVENT_ID) ?: 0
        val event = viewModel.getEvent(event_id)

        val typeView = view.findViewById<TextView>(R.id.view_type)
        val lineView = view.findViewById<TextView>(R.id.view_line)
        val markView = view.findViewById<TextView>(R.id.view_mark)


        val eventTypes = arrayOf("ДТП", "Дорожные работы", "Перекрытие движения", "Затор", "Неблагоприятные погодные условия", "Опасность на дороге")
        val eventLines = arrayOf("Правая полоса", "2 полоса", "3 полоса", "4 полоса", "Вся дорога")
        val eventMarks = arrayOf(
            "Модерация",
            "Пользовательское",
            "Отклонено",
            "Отозвано",
            "Официальное",
            "Решено"
        )

        typeView.text = eventTypes[event?.type ?: 0]
        lineView.text = eventLines[event?.line ?: 0]
        markView.text = eventMarks[event?.moderated ?: 0]

        val buttonClose = view.findViewById<ImageButton>(R.id.button_close)
        buttonClose.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
            viewModel.awakeRoute()
        }

        buttonDelete = view.findViewById(R.id.button_delete)
        buttonFix = view.findViewById(R.id.button_fix)

        if (viewModel.authorized.value == false || (event?.my
                ?: false) == false || (event?.moderated ?: 99) >= 2
        ) {
            buttonDelete.visibility = View.GONE
            buttonFix.visibility = View.GONE

        }
        if ((event?.moderated ?: 0) == 0) {
            buttonFix.visibility = View.GONE
        }

        val tokenManager = TokenManager(requireContext())

        buttonDelete.setOnClickListener {
            val call = apiService.clearEvent(
                tokenManager.getAccessToken()!!,
                event!!.id
            )
            call.enqueue(object : Callback<Event> {
                override fun onResponse(call: Call<Event>, response: Response<Event>) {
                    if (response.isSuccessful) {
                        viewModel.setDeletedEvent(event_id)
                        viewModel.awakeRoute()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Сервер не ответил",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Profile", "Liking stop error" + response.code())
                    }
                }

                override fun onFailure(call: Call<Event>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Сервер не ответил",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Profile", "Liking stop error" + t.message)
                }
            })
        }


        buttonFix.setOnClickListener {
            val call = apiService.fixEvent(
                tokenManager.getAccessToken()!!,
                event!!.id
            )
            call.enqueue(object : Callback<Event> {
                override fun onResponse(call: Call<Event>, response: Response<Event>) {
                    if (response.isSuccessful) {
                        viewModel.replaceEvent(event_id, response.body())
                        viewModel.awakeRoute()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Сервер не ответил",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Profile", "Liking stop error" + response.code())
                    }
                }

                override fun onFailure(call: Call<Event>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Сервер не ответил",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Profile", "Liking stop error" + t.message)
                }
            })
        }
    }
}