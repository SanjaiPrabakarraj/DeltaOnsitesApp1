package com.example.countdowntimer

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment

class ExampleDialog: AppCompatDialogFragment() {
    lateinit var listener: ExampleDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(activity)
        val view = layoutInflater.inflate(R.layout.dialog_layout, null)

        val hourText = view.findViewById(R.id.hour_input) as TextView
        val minuteText = view.findViewById(R.id.minute_input) as TextView
        val secondText = view.findViewById(R.id.second_input) as TextView
        val hourUp = view.findViewById(R.id.hourTimeUp) as ImageButton
        val hourDown = view.findViewById(R.id.hourTimeDown) as ImageButton
        val minuteUp = view.findViewById(R.id.minuteTimeUp) as ImageButton
        val minuteDown = view.findViewById(R.id.minuteTimeDown) as ImageButton
        val secondUp = view.findViewById(R.id.secondTimeUp) as ImageButton
        val secondDown = view.findViewById(R.id.secondTimeDown) as ImageButton

        val build = alertDialog.setView(view)
            .setTitle("Select Duration")
            .setNegativeButton("Cancel"){dialog, which -> }
            .setPositiveButton("Set"){dialog, which->
                val hourInput = hourText.text.toString()
                val minuteInput = minuteText.text.toString()
                val secondInput = secondText.text.toString()
                listener.applyTexts(hourInput, minuteInput, secondInput)
            }
        hourUp.setOnClickListener{
            var text = hourText.text.toString().toInt()
            text += 1
            val string = text.toString()
            hourText.text = if (string.length == 2) string
            else "0" + string
        }
        hourDown.setOnClickListener{
            var text = hourText.text.toString().toInt()
            text -= 1
            val string = text.toString()
            hourText.text = if (string.length == 2) string
            else "0" + string
        }
        minuteUp.setOnClickListener{
            var text = minuteText.text.toString().toInt()
            text += 1
            val string = text.toString()
            minuteText.text = if (string.length == 2) string
            else "0" + string
        }
        minuteDown.setOnClickListener{
            var text = minuteText.text.toString().toInt()
            text -= 1
            val string = text.toString()
            minuteText.text = if (string.length == 2) string
            else "0" + string
        }
        secondUp.setOnClickListener{
            var text = secondText.text.toString().toInt()
            text += 1
            val string = text.toString()
            secondText.text = if (string.length == 2) string
            else "0" + string
        }
        secondDown.setOnClickListener{
            var text = secondText.text.toString().toInt()
            text -= 1
            val string = text.toString()
            secondText.text = if (string.length == 2) string
            else "0" + string
        }

        return alertDialog.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ExampleDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString())
        }

    }

    interface ExampleDialogListener{
        fun applyTexts(hourInput:String, minuteInput:String, secondInput:String){}
    }
}