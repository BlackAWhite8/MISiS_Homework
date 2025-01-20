package com.example.todoapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView

class ItemViewHolder(itemView : View, click : (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.setOnClickListener {

            click(super.getAdapterPosition())
        }
    }

    private val item: TextView = itemView.findViewById(R.id.todo_item)
    private val checkbox: ImageView =  itemView.findViewById(R.id.importance_flag)
    private val isDone : CheckBox = itemView.findViewById(R.id.is_done_checkbox)
    private val deadlineText : TextView = itemView.findViewById(R.id.deadline_text)
    fun bind(task : ToDoItem) {
        item.text = task.text

        deadlineText.text = if(task.deadline != "") {
            "date: ${task.deadline}"
        } else {
            ""
        }

        val checkboxResId = when(task.importance) {
           "mid" -> R.drawable.mid_importance_checkbox_background
           "high" -> R.drawable.high_importance_checkbox_background
           else -> R.drawable.low_importance_checkbox_background
        }
        checkbox.background = AppCompatResources.getDrawable(checkbox.context, checkboxResId)
        isDone.setOnClickListener {
            task.isDone = isDone.isChecked
        }
        isDone.isChecked = task.isDone
    }
}
class TodoListAdapter(private var data : MutableList<ToDoItem>, private val onItemClick : (ToDoItem, Int) -> Unit) : RecyclerView.Adapter<ItemViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todolist_item, parent, false)
        return ItemViewHolder(view) {
            onItemClick(data[it], it)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val task = data[position]
        holder.bind(task)
    }

    fun deleteItem(pos : Int) {
        data.removeAt(pos)
        notifyItemRemoved(pos)
    }

}