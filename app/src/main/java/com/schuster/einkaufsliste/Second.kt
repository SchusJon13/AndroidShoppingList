package com.schuster.einkaufsliste

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.schuster.einkaufsliste.databinding.ActivityMainBinding
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import android.util.Log
import com.google.android.material.appbar.MaterialToolbar

class Second : AppCompatActivity() {
    fun saveArrayList(context: Context, arrayList: ArrayList<String>, filename: String) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            ObjectOutputStream(it).writeObject(arrayList)
        }
    }

    // Loading the ArrayList
    fun loadArrayList(context: Context, filename: String): ArrayList<String>? {
        return try {
            context.openFileInput(filename).use {
                ObjectInputStream(it).readObject() as ArrayList<String>
            }
        } catch (e: Exception) {
            null // Handle exceptions appropriately
        }
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var lvTodoList: ListView
    private lateinit var fab: FloatingActionButton
    private lateinit var shoppingItems: ArrayList<String>
    private lateinit var itemAdapter: ArrayAdapter<String>
    private lateinit var Tobbar: MaterialToolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lvTodoList = findViewById(R.id.lvTodoList)
        fab = findViewById(R.id.floatingActionButton)
        shoppingItems = ArrayList()
        Tobbar = findViewById(R.id.materialToolbar)

        try {
            shoppingItems = loadArrayList(this, "$ShoppingItem.txt") ?: ArrayList()
        } catch (e: Exception) {
            Log.e("Second", "Error loading shopping items", e)
        }

        itemAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, shoppingItems)
        lvTodoList.adapter = itemAdapter

        lvTodoList.onItemLongClickListener = OnItemLongClickListener { arg0, arg1, pos, id ->
            shoppingItems.removeAt(pos)
            itemAdapter.notifyDataSetChanged()
            Toast.makeText(applicationContext, "Element gelöscht", Toast.LENGTH_SHORT).show()
            true
        }
        fab.setOnClickListener {
            var builder = AlertDialog.Builder(this)
            builder.setTitle("Hinzufügen")

            var input = EditText(this)
            input.hint = "Text eingeben"
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                shoppingItems.add(input.text.toString())
            }

            builder.setNegativeButton("Abbrechen") { _, _ ->
                Toast.makeText(applicationContext, "Abgebrochen", Toast.LENGTH_SHORT).show()
            }

            builder.show()
        }
        Tobbar.setNavigationIcon(getDrawable(R.drawable.back))
        Tobbar.setNavigationOnClickListener {
            finish()
        }
        //Titel der Toolbar auf die Gruppe ändern -> ShoppingItem
        Tobbar.title = "$ShoppingName"



    }
    override fun onPause() {
        super.onPause()
        saveArrayList(this, shoppingItems, "$ShoppingItem.txt")
    }
}