package com.schuster.einkaufsliste

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.schuster.einkaufsliste.databinding.ActivityMainBinding
import java.io.ObjectInputStream
import android.os.CountDownTimer
import java.io.ObjectOutputStream
import android.view.View
import com.google.android.material.appbar.MaterialToolbar
import android.util.Log
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.Switch
import com.google.android.material.snackbar.Snackbar

var isLongClick = false

var shoppingItems: ArrayList<String> = ArrayList()
//TODO: Counter soll sich bei klick erhöhen
//TODO: Alles machen
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
    var shoppingItems: ArrayList<String> = ArrayList()
    private lateinit var itemAdapter: ArrayAdapter<String>
    private lateinit var Tobbar: MaterialToolbar
    lateinit var shoppingCounterList: ArrayList<Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lvTodoList = findViewById(R.id.lvTodoList)
        fab = findViewById(R.id.floatingActionButton)
        shoppingItems = ArrayList()
        Tobbar = findViewById(R.id.materialToolbar)

        try {
            shoppingItems = loadArrayList(this, "$ShoppingName.txt") ?: ArrayList()
        } catch (e: Exception) {
            Log.e("Second", "Error loading shopping items", e)
        }

        itemAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, shoppingItems)
        lvTodoList.adapter = itemAdapter
        //Bei Klick Counter Erhöhen
        lvTodoList.setOnItemClickListener { parent, view, position, _ ->
            val currentItem = shoppingItems[position]
            val itemName = currentItem.substringAfter("x ").trim()
            val currentCount = currentItem.substringBefore("x").toIntOrNull() ?: 1

            val newCount = currentCount + 1
            // Remove the old item from the list
            if (newCount > -1 && isLongClick == false){
                shoppingItems.removeAt(position)
                val newItem = "${newCount}x $itemName"
                shoppingItems.add(position, newItem)
            }else if(isLongClick == false) {
                shoppingItems.removeAt(position)
                Toast.makeText(
                    this,
                    "Element gelöscht",
                    Toast.LENGTH_SHORT).show()
            }
            itemAdapter.notifyDataSetChanged()
        }
            lvTodoList.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { arg0, arg1, pos, id ->
                    //Gemini: Decrease Counter, wenn Counter = 0 Element löschen
                    val currentItem = shoppingItems[pos]
                    val itemName = currentItem.substringAfter("x ").trim()
                    var currentCount = currentItem.substringBefore("x").toIntOrNull() ?: 1

                    currentCount--
                    if (currentCount > 0) {
                        shoppingItems.removeAt(pos)
                        var pressed = false
                        Snackbar.make(arg1, "Verringert", Snackbar.LENGTH_LONG)
                            .setAction("Alles Löschen") {
                                shoppingItems.removeAt(pos)
                                itemAdapter.notifyDataSetChanged()
                                pressed = true
                            }
                            .show()

                        if (pressed == false) {
                            val newItem = "${currentCount}x $itemName"
                            shoppingItems.add(pos, newItem)
                            itemAdapter.notifyDataSetChanged()

                        }
                    }else {
                        shoppingItems.removeAt(pos)
                        itemAdapter.notifyDataSetChanged()
                    }

                    var isLongClick = true
                    object : CountDownTimer(200, 100) {
                        override fun onTick(millisUntilFinished: Long) {}

                        override fun onFinish() {
                            isLongClick = false
                        }
                    }.start()


                    isLongClick
                }
            fab.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Neues Element hinzufügen")

                val input = EditText(this)
                input.hint = "Name des Elements"
                input.inputType = InputType.TYPE_CLASS_TEXT
                builder.setView(input)

                builder.setPositiveButton("Hinzufügen") { _, _ ->
                    val newItemText = input.text.toString().trim()
                    if (newItemText.isNotBlank()) {
                        val formattedItem = if (shoppingItems.any { it.endsWith(newItemText) }) {
                            // Check if the item already exists, increment the counter
                            val existingItemIndex =
                                shoppingItems.indexOfFirst { it.endsWith(newItemText) }
                            val currentCount =
                                shoppingItems[existingItemIndex].substringBefore("x").toInt()
                            shoppingItems.removeAt(existingItemIndex)
                            (currentCount + 1).toString() + "x " + newItemText
                        } else {
                            // If the item doesn't exist, add it with count 1
                            "1x " + newItemText
                        }
                        // Add the (possibly updated) item to the list
                        shoppingItems.add(formattedItem)

                        itemAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Bitte Text eingeben",
                            Toast.LENGTH_SHORT
                        ).show() // Show a toast message
                    }
                }

                builder.setNegativeButton("Schließen") { _, _ ->
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
        saveArrayList(this, shoppingItems, "$ShoppingName.txt")
    }

    }

