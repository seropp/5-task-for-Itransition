package com.example.registrationapplication.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import com.example.registrationapplication.Model
import com.example.registrationapplication.R


class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val view = WeakReference(itemView)
    private lateinit var textView: TextView

    var textViewDelete: TextView = itemView.findViewById(R.id.textViewDelete)
    var textViewBlock: TextView = itemView.findViewById(R.id.textViewBlock)
    var textViewUnblock: TextView = itemView.findViewById(R.id.textViewUnblock)

    var name: TextView = itemView.findViewById(R.id.nameXML)
    var email: TextView = itemView.findViewById(R.id.emailXML)
    var id: TextView = itemView.findViewById(R.id.idXML)
    var lastLogin: TextView = itemView.findViewById(R.id.lastLoginXML)
    var registrationDate: TextView = itemView.findViewById(R.id.registrationXML)
    var status: TextView = itemView.findViewById(R.id.statusXML)

    init {
        view.get()?.let {
            it.setOnClickListener {
                if (view.get()?.scrollX != 0) {
                    view.get()?.scrollTo(0, 0)
                }
            }
        }
    }

    fun bindViewModel(model: Model) {
        name.text = model.name
        email.text = model.email
        id.text = model.id
        lastLogin.text = model.lastLogin
        registrationDate.text = model.registrationDate
        status.text = model.status
        if (model.status == "not blocked") {
            textViewBlock.visibility = View.VISIBLE
            textViewUnblock.visibility = View.GONE
        } else {

            textViewBlock.visibility = View.GONE
            textViewUnblock.visibility = View.VISIBLE
        }

    }

    fun updateView() {
        view.get()?.scrollTo(0, 0)
    }
}