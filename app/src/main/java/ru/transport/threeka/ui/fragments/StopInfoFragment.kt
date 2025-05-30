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
import ru.transport.threeka.api.schemas.Stop
import ru.transport.threeka.data.MainViewModel
import ru.transport.threeka.services.TokenManager

class StopInfoFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private var stop_id = 0;
    private lateinit var buttonLike: Button
    private lateinit var buttonDislike: Button

    companion object {
        private const val STOP_ID = "0"

        fun newInstance(stop_id: Int): StopInfoFragment {
            val fragment = StopInfoFragment()
            val args = Bundle()
            args.putInt(STOP_ID, stop_id)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stop_info, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.authorized.value == true) {
            if (viewModel.getStopLike(stop_id)) {
                buttonLike.visibility = View.GONE
                buttonDislike.visibility = View.VISIBLE
            } else {
                buttonLike.visibility = View.VISIBLE
                buttonDislike.visibility = View.GONE
            }
        } else {
            buttonLike.visibility = View.GONE
            buttonDislike.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stop_id = arguments?.getInt(STOP_ID) ?: 0

        val stopName = view.findViewById<TextView>(R.id.stop_name)
        stopName.text = viewModel.getStopName(stop_id)

        val stopAbout = view.findViewById<TextView>(R.id.stop_about)
        val about = viewModel.getStopAbout(stop_id)
        if (about == "Nothing" || about == "" || about == "None") {
            stopAbout.visibility = View.GONE
        } else {
            stopAbout.text = viewModel.getStopAbout(stop_id)
        }

        val buttonFrom = view.findViewById<Button>(R.id.button_from)
        buttonFrom.setOnClickListener {
            viewModel.setStopFrom(stop_id)
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }

        val buttonTo = view.findViewById<Button>(R.id.button_to)
        buttonTo.setOnClickListener {
            viewModel.setStopTo(stop_id)
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }

        val buttonClose = view.findViewById<ImageButton>(R.id.button_close)
        buttonClose.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
            viewModel.awakeRoute()
        }

        buttonLike = view.findViewById(R.id.button_like)
        buttonDislike = view.findViewById(R.id.button_dislike)

        if (viewModel.authorized.value == true) {
            if (viewModel.getStopLike(stop_id)) {
                buttonLike.visibility = View.GONE
            } else {
                buttonDislike.visibility = View.GONE
            }
        } else {
            buttonLike.visibility = View.GONE
            buttonDislike.visibility = View.GONE
        }

        val tokenManager = TokenManager(requireContext())

        buttonLike.setOnClickListener {
            val call = apiService.likeStop(
                tokenManager.getAccessToken()!!,
                viewModel.getStopId(stop_id)
            )
            call.enqueue(object : Callback<Stop> {
                override fun onResponse(call: Call<Stop>, response: Response<Stop>) {
                    if (response.isSuccessful) {
                        val stop = response.body()
                        viewModel.setLikedStop(stop_id)
                        viewModel.replaceStop(stop_id, stop)
                        buttonLike.visibility = View.GONE
                        buttonDislike.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Сервер не ответил",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Profile", "Liking stop error" + response.code())
                    }
                }

                override fun onFailure(call: Call<Stop>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Сервер не ответил",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Profile", "Liking stop error" + t.message)
                }
            })
        }


        buttonDislike.setOnClickListener {
            val call = apiService.dislikeStop(
                tokenManager.getAccessToken()!!,
                viewModel.getStopId(stop_id)
            )
            call.enqueue(object : Callback<Stop> {
                override fun onResponse(call: Call<Stop>, response: Response<Stop>) {
                    if (response.isSuccessful) {
                        val stop = response.body()
                        viewModel.replaceStop(stop_id, stop)
                        viewModel.setDislikedStop(stop_id)
                        buttonDislike.visibility = View.GONE
                        buttonLike.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Сервер не ответил",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Profile", "Liking stop error" + response.code())
                    }
                }

                override fun onFailure(call: Call<Stop>, t: Throwable) {
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