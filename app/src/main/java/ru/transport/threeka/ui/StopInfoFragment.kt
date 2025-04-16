package ru.transport.threeka.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.transport.threeka.R
import ru.transport.threeka.data.MainViewModel

class StopInfoFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stop_id = arguments?.getInt(STOP_ID) ?: 0

        val stopName = view.findViewById<TextView>(R.id.stop_name)
        stopName.text = viewModel.getStopName(stop_id)

        val stopAbout = view.findViewById<TextView>(R.id.stop_about)
        val about = viewModel.getStopAbout(stop_id)
        if (about == "Nothing" || about == ""){
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


    }
}