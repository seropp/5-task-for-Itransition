package com.example.registrationapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.registrationapplication.Model
import com.example.registrationapplication.R


class Adapter : RecyclerView.Adapter<ItemViewHolder>() {

    private var modelList: ArrayList<Model> = ArrayList()
    private var onClickBlockItem: ((Model) -> Unit)? = null
    private var onClickDeleteItem: ((Model) -> Unit)? = null


    fun addItems(user_items: ArrayList<Model>) {
        modelList = user_items
        notifyDataSetChanged()
    }

    fun setOnClickBlockItemOrUnBlock(callback: (Model) -> Unit) {
        onClickBlockItem = callback
    }
    fun setOnClickDeleteItem(callback: (Model) -> Unit) {
        onClickDeleteItem = callback
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val user = modelList[position]
        holder.bindViewModel(user)


        holder.textViewDelete.setOnClickListener {
            onClickDeleteItem?.invoke(user)
            notifyItemRemoved(position)
        }

        holder.textViewBlock.setOnClickListener {
            onClickBlockItem?.invoke(user)
            notifyItemChanged(position)
        }

        holder.textViewUnblock.setOnClickListener {
            onClickBlockItem?.invoke(user)
            notifyItemChanged(position)
        }

        holder.updateView()
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.view_holder_item, parent, false)
        )
    }
}