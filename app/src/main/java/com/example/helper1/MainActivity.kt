package com.example.helper1

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.helper1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fm: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        setContentView(binding.root)

        val navController = findNavController(R.id.mainContainer)
        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)
        fm = supportFragmentManager

        navController.addOnDestinationChangedListener { controller, destination, bundle ->
            when(destination.id) {
                R.id.roomsFragment -> {}
                R.id.homeFragment -> {}
                R.id.settingsFragment -> {}
            }
        }
    }

    fun createError(text: String){
        Toast.makeText(applicationContext,text,Toast.LENGTH_SHORT).show()
    }

    fun showDialog(fragment: HomeFragment){
        val langArray: Array<String> = arrayOf("Задача", "Событие")
        val selectedEvent = BooleanArray(langArray.size)
        val langList: ArrayList<Int> = ArrayList()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Выбрерите тип")
        builder.setCancelable(false)

        builder.setMultiChoiceItems(langArray, selectedEvent
        ) { dialogInterface, i, b ->
            if (b) {
                langList.add(i)
                langList.sort()
            } else {
                langList.remove(i)
            }
        }

        builder.setPositiveButton("OK"
        ) { dialogInterface, i -> // Initialize string builder
            if (langList.size != 1) {
                val stringBuilder = StringBuilder()
                for (j in langList.indices) {
                    stringBuilder.append(langArray[langList[j]])
                    if (j != langList.size - 1) {
                        stringBuilder.append(", ")
                    }
                }
                createError("Ошибка! Необходимо выбрать что-то одно")
            } else {
                fragment.createNewText(langList[0])
            }
        }

        builder.setNegativeButton("Cancel"
        ) { dialogInterface, i -> // dismiss dialog
            dialogInterface.dismiss()
        }

        builder.show()
    }
}