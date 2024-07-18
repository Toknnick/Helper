package com.example.helper1

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.helper1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var fragment: HomeFragment
    private lateinit var fm: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        setContentView(binding.root)
        fm = supportFragmentManager

        val navHostFragment = fm.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigationView = binding.bottomNavigationView
        setupWithNavController(bottomNavigationView,navController)
    }

    fun getContext():Context{
        return this
    }

    fun createError(text: String){
        Toast.makeText(applicationContext,text,Toast.LENGTH_SHORT).show()
    }

    //TODO: добавить возможность менять цвет в настройки
    fun createText(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 22F
        return textView
    }

    fun createRelativeLayout(): RelativeLayout{
        val relativeLayout = RelativeLayout(this)
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                 RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(5,5,5,10)
        relativeLayout.setLayoutParams(params)
        relativeLayout.setPadding(0,0,0,10)
        return relativeLayout
    }

    fun createEditText(): EditText {
        val editText = EditText(this)
        return editText
    }

    fun createCheckBox(): CheckBox{
        val checkBox = CheckBox(this)
        return checkBox
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