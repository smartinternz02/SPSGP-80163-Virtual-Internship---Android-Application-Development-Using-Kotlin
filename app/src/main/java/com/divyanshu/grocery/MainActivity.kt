package com.divyanshu.grocery

import android.app.Dialog
import android.graphics.Color
import android.media.CamcorderProfile.get
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.lang.reflect.Array.get


class MainActivity : AppCompatActivity() {
    lateinit var itemRV: RecyclerView
    lateinit var addFAB: ExtendedFloatingActionButton
    lateinit var list: List<GroceryItems>
    lateinit var itemTouchHelper: ItemTouchHelper
    lateinit var groceryRVAdapter: GroceryRVAdapter
    lateinit var groceryViewModel: GroceryViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        itemRV = findViewById(R.id.rvitems)
        addFAB = findViewById(R.id.fabAdd)
        list = ArrayList<GroceryItems>()
        groceryRVAdapter = GroceryRVAdapter(list as ArrayList<GroceryItems>, this)
        itemRV.layoutManager = LinearLayoutManager(this)
        itemRV.adapter = groceryRVAdapter
        val groceryRepository = GroceryRepository(GroceryDatabase(this))
        val factory = GroceryViewModelFactory(groceryRepository)
        groceryViewModel = ViewModelProvider(this, factory).get(GroceryViewModel::class.java)
        groceryViewModel.getAllGroceryItems().observe(this, Observer {
            groceryRVAdapter.list = it as ArrayList<GroceryItems>
            groceryRVAdapter.notifyDataSetChanged()

        })

        addFAB.setOnClickListener {
            openDialog()
        }
        enableSwipeToDeleteAndUndo()
        groceryRVAdapter.notifyDataSetChanged()

    }
    fun openDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.grocery_add_dialog)
        val cancelbtn = dialog.findViewById<AppCompatButton>(R.id.idbtncancel)
        val addbtn = dialog.findViewById<AppCompatButton>(R.id.idbtnadd)
        val itemEdt = dialog.findViewById<EditText>(R.id.idEdtitemname)
        val itemQuantityEdt = dialog.findViewById<EditText>(R.id.idEdtitemquantity)
        cancelbtn.setOnClickListener {
            dialog.dismiss()
        }
        addbtn.setOnClickListener {
            val itemname: String = itemEdt.text.toString()
            val itemquantity: String = itemQuantityEdt.text.toString()
            val qty: Double = itemquantity.toDouble()
            if (itemname.isNotEmpty() && itemquantity.isNotEmpty()) {
                val items = GroceryItems(itemname, qty)
                groceryViewModel.insert(items)
                Toast.makeText(applicationContext, "Item Added", Toast.LENGTH_SHORT).show()
                groceryRVAdapter.notifyDataSetChanged()
                dialog.dismiss()
            } else {
                Toast.makeText(applicationContext, "Please fill all details", Toast.LENGTH_SHORT)
                    .show()
            }

        }
        dialog.show()
    }
    private fun enableSwipeToDeleteAndUndo() {
        val swipeToDeleteCallback: SwipeToDeleteCallback = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                val item: GroceryItems = groceryRVAdapter.getData().get(position)
                groceryRVAdapter.removeItem(position)
                groceryViewModel.delete(item)
                val snackbar = Snackbar
                    .make(itemRV,

                        "Item removed from the list.",
                        Snackbar.LENGTH_LONG
                    )
                snackbar.setAction("UNDO") {
                    groceryRVAdapter.restoreItem(item, position)
                    itemRV.scrollToPosition(position)
                }
                    snackbar.setBackgroundTint(resources.getColor(R.color.Green_light2))
                    snackbar.setActionTextColor(resources.getColor(R.color.green_dark))
                snackbar.show()

            }
        }
        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(itemRV)
        groceryRVAdapter.notifyDataSetChanged()
    }
}