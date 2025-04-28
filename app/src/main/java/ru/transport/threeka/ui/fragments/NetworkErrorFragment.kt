package ru.transport.threeka.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.transport.threeka.R
import ru.transport.threeka.data.MainViewModel

class NetworkErrorFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_network_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val buttonFrom = view.findViewById<Button>(R.id.button_okey)
        buttonFrom.setOnClickListener {
            viewModel.resetStops()
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }


    }
}