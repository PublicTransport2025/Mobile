package ru.transport.threeka.ui.fragments

import android.content.Intent
import android.net.Uri
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
import io.appmetrica.analytics.AppMetrica
import ru.transport.threeka.R
import ru.transport.threeka.data.MainViewModel
import ru.transport.threeka.ui.activities.AtpActivity

class AdvFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_adv, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonClose: ImageButton = view.findViewById(R.id.button_close)
        buttonClose.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
            viewModel.awakeRoute()
        }

        val buttonOpen: Button = view.findViewById(R.id.button_open)
        buttonOpen.setOnClickListener {
            val eventParameters = mapOf("type" to "adv1")
            AppMetrica.reportEvent("AdvOpen", eventParameters)
            val url = getString(R.string.adv1_url)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        val buttonDiscard: Button = view.findViewById(R.id.button_discard)
        buttonDiscard.setOnClickListener {
            viewModel.setAdv(true)
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
            viewModel.awakeRoute()
        }

    }
}