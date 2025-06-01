package ru.transport.threeka.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import ru.transport.threeka.R

class SelectAdapter(
    private val activity: Activity,
    private val buttons: MutableList<String>,
    private val indexes: MutableList<Int>,
    private val icons: MutableList<Int>,
    private var _time: Int
) :
    RecyclerView.Adapter<SelectAdapter.ButtonViewHolder>() {

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: MaterialButton = itemView.findViewById(R.id.buttonItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stop_item, parent, false)
        return ButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.button.text = buttons[position]
        if (icons[position] == 1) {
            holder.button.setIconResource(R.drawable.map)
        } else if (icons[position] == 2) {
            holder.button.setIconResource(R.drawable.north)
        }
        holder.button.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("stop_id", indexes[position])
            resultIntent.putExtra("time", _time)
            activity.setResult(Activity.RESULT_OK, resultIntent)
            activity.finish()
        }
    }

    override fun getItemCount(): Int = buttons.size

    @SuppressLint("NotifyDataSetChanged")
    fun addButton(text: List<String>, numbers: List<Int>, likes: List<Boolean>, time: Int) {
        _time = time
        buttons.clear()
        buttons.addAll(text)
        indexes.clear()
        indexes.addAll(numbers)
        icons.clear()
        icons.addAll(likes.map { if (it) 2 else 0 })
        notifyDataSetChanged()
    }

    fun insertButton(text: String, number: Int) {
        buttons.add(0, text)
        indexes.add(0, number)
        icons.add(0, 1)
        notifyItemInserted(0)
    }
}