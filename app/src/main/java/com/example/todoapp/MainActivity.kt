package com.example.todoapp

import android.R
import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity() {
    private val gson = GsonBuilder().create()
    private val dataManager = DatastoreManager(this)
    private lateinit var todoAdapter : TodoListAdapter
    private lateinit var data: MutableList<ToDoItem>
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultIntent: Intent? = result.data
            val jsonResult = resultIntent?.getStringExtra("result")
            val taskStatus = resultIntent?.getBooleanExtra("newTask", false)
            val itemPos = resultIntent?.getIntExtra("position", 0)
            val deleteFlag = resultIntent?.getBooleanExtra("delete", false)
            if (taskStatus != null) {
                updateData(jsonResult, taskStatus, itemPos, data, todoAdapter, deleteFlag)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        runBlocking {  data = dataManager.getElements() }
        val clicker : (ToDoItem, Int) -> Unit = ::recyclerItemClick // function as variable
        todoAdapter = TodoListAdapter(data, clicker)
        binding.todoRecycler.adapter = todoAdapter
        binding.todoRecycler.layoutManager = LinearLayoutManager(this)
        binding.addTaskBtn.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            intent.putExtra("item", "None")
            resultLauncher.launch(intent)
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                todoAdapter.deleteItem(viewHolder.adapterPosition)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addBackgroundColor(
                        ResourcesCompat.getColor(resources, R.color.holo_red_light, null)
                    )
                    .addActionIcon(R.drawable.ic_menu_delete)
                    .create()
                    .decorate()

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

        })
        itemTouchHelper.attachToRecyclerView(binding.todoRecycler)


    }

    private fun updateData(st: String?, status: Boolean, pos: Int?, data: MutableList<ToDoItem>, adapter: TodoListAdapter, flag: Boolean?) {
        if (flag != null && flag && pos != null) {
            data.removeAt(pos)
            adapter.notifyItemRemoved(pos)
            adapter.notifyItemChanged(data.size-1)
        } else if (st != "None") {
            val item = gson.fromJson(st, ToDoItem::class.java)
            if (!status) {
                if (pos != null) {
                    data[pos] = item
                    adapter.notifyItemChanged(pos)
                }
            } else {
                data.add(item)
                adapter.notifyItemChanged(data.size-2)
                adapter.notifyItemChanged(data.size-1)
            }
        }
    }

    private fun recyclerItemClick(task : ToDoItem, pos : Int) {
        val intent = Intent(this, CreateTaskActivity::class.java)
        println(gson.toJson(task))
        intent.putExtra("item", gson.toJson(task))
        intent.putExtra("position", pos)
        resultLauncher.launch(intent)
    }

    override fun onPause() {
        runBlocking {   dataManager.saveElements(data) }
        super.onPause()
    }


}