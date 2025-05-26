package ru.transport.threeka.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.appmetrica.analytics.AppMetrica
import ru.transport.threeka.R
import ru.transport.threeka.data.MainViewModel
import ru.transport.threeka.ui.activities.AtpActivity

class BusAbsenceFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_no_bus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonClose = view.findViewById<ImageButton>(R.id.button_close)
        buttonClose.setOnClickListener {
            viewModel.killRoute()
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }


        val buttonFrom = view.findViewById<Button>(R.id.button_no_bus)
        buttonFrom.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
            val eventParameters = mapOf("number" to viewModel.getRouteNumber())
            AppMetrica.reportEvent("BusAbsence", eventParameters)
            val intent = Intent(requireContext(), AtpActivity::class.java)
            intent.putExtra("number", viewModel.getRouteNumber())
            startActivity(intent)
        }


    }
}