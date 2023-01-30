package com.kunle.randomidgeneratorkotlin

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.textfield.TextInputEditText
import com.kunle.randomidgeneratorkotlin.databinding.ActivityMainBinding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

//val number_character_list:Array<String> = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
//val lowercase_character_list = arrayOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m")
//val uppercase_character_list = arrayOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M")


class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private lateinit var filename: String
    private lateinit var toEmailAddress: String
    private lateinit var emailSubject: String
    private lateinit var emailNote: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bind = DataBindingUtil.setContentView(this,R.layout.activity_main)

        setLayoutText()
        setOnClickListeners()
    }

    private fun setLayoutText() {
        val firstPart = SpannableString("ID Characteristics")
        val secondPart = SpannableString("(must select at least 1)")
        secondPart.setSpan(
            AbsoluteSizeSpan(18, true),
            0,
            firstPart.length,
            SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
        )
        secondPart.setSpan(
            AbsoluteSizeSpan(15, true),
            0,
            secondPart.length,
            SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
        )

        val finalText = TextUtils.concat(firstPart, " ", secondPart)
        bind.checkBoxLabel.text = finalText

        setFileName()
    }

    private fun setFileName() {
        //auto-creates email file name to avoid user error (name based on unique data and time)
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd--HHmmss", Locale.US)
            val now = Date()
            filename = "GeneratedIDs- ${formatter.format(now)}.txt"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bind.fileName.setText(filename) //using setText because it takes strings, unlike ".text ="
    }

    private fun setOnClickListeners() {
        val textWatcherID = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable?) {
                bind.idsNum.removeTextChangedListener(this)
                if (!text.toString().equals("")) {
                    try {
                        var originalString = text.toString()

                        if (originalString.contains(",")) {
                            originalString = originalString.replace(",", "")
                        }
                        val tempNumStorage = Integer.parseInt(originalString)

                        val df = NumberFormat.getInstance(Locale.US) as DecimalFormat
                        df.applyPattern("#,###,###")
                        bind.idsNum.setText(df.format(tempNumStorage))
                        bind.idsNum.text?.let { it1 -> bind.idsNum.setSelection(it1.length) }
                    } catch (e: java.lang.NumberFormatException) {
                        Toast.makeText(
                            applicationContext, "Number is too large!",
                            Toast.LENGTH_SHORT
                        ).show();
                        e.printStackTrace();
                    }
                }
                bind.idsNum.addTextChangedListener(this)
            }
        }

        val textWatcherChar = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable?) {
                bind.characterNum.removeTextChangedListener(this)
                if (!text.toString().equals("")) {
                    try {
                        var originalString = text.toString()

                        if (originalString.contains(",")) {
                            originalString = originalString.replace(",", "")
                        }
                        val tempNumStorage = Integer.parseInt(originalString)

                        val df = NumberFormat.getInstance(Locale.US) as DecimalFormat
                        df.applyPattern("#,###,###")
                        bind.characterNum.setText(df.format(tempNumStorage))
                        bind.characterNum.text?.let { it1 -> bind.idsNum.setSelection(it1.length) }
                    } catch (e: java.lang.NumberFormatException) {
                        Toast.makeText(
                            applicationContext, "Number is too large!",
                            Toast.LENGTH_SHORT
                        ).show();
                        e.printStackTrace();
                    }
                }
                bind.characterNum.addTextChangedListener(this)
            }
        }

        bind.idsNum.addTextChangedListener(textWatcherID)
        bind.characterNum.addTextChangedListener(textWatcherChar)

        bind.createButton.setOnClickListener { _ ->
            var idsNumText = bind.idsNum.text?.trim().toString()
            var characterNumText = bind.characterNum.text?.trim().toString()

            if (idsNumText.contains(",")) {
                idsNumText = idsNumText.replace(",", "")
            }

            if (characterNumText.contains(",")) {
                characterNumText = characterNumText.replace(",", "")
            }

            val numCharactersPerID: Int
            val numOfIDs: Int

            try {
                numCharactersPerID = Integer.parseInt(characterNumText)
                numOfIDs = Integer.parseInt(idsNumText)

                if (numOfIDs > 0 && numCharactersPerID > 0) {
                    createIDs()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Both # of IDs and # of characters per ID need to be greater than 0",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace();
                Toast.makeText(
                    applicationContext,
                    "Please enter all required numeric values before creating IDs",
                    Toast.LENGTH_SHORT
                ).show();
            }
        }

        //next 3 on-click listeners meant to enable "Create IDs" button ONLY if
        //one of 3 checkboxes is selected. User must choose 1 to actually create IDs
        var anyCheckBoxChecked: Boolean

        bind.numbersCheckBox.setOnClickListener {
            anyCheckBoxChecked = bind.numbersCheckBox.isChecked
                    || bind.lowercaseCheckBox.isChecked
                    || bind.uppercaseCheckBox.isChecked
            bind.createButton.isEnabled = anyCheckBoxChecked
        }

        bind.lowercaseCheckBox.setOnClickListener {
            anyCheckBoxChecked = bind.numbersCheckBox.isChecked
                    || bind.lowercaseCheckBox.isChecked
                    || bind.uppercaseCheckBox.isChecked
            bind.createButton.isEnabled = anyCheckBoxChecked
        }

        bind.uppercaseCheckBox.setOnClickListener {
            anyCheckBoxChecked = bind.numbersCheckBox.isChecked
                    || bind.lowercaseCheckBox.isChecked
                    || bind.uppercaseCheckBox.isChecked
            bind.createButton.isEnabled = anyCheckBoxChecked
        }
    }

    private fun showEmailOptions() {
        //creates and inflates email options.
        //These values will populate email "To:","Subject", and email body when email is created
        val inflater = layoutInflater
        val emailView = inflater.inflate(R.layout.email_info, null)
        val builder = AlertDialog.Builder(this)
        val dialogButtonColor = ContextCompat.getColor(this, R.color.dialog_button_color)

        val toEmail: TextInputEditText = emailView.findViewById(R.id.toEmailAddress)
        val subject: TextInputEditText = emailView.findViewById(R.id.subject)
        val note: TextInputEditText = emailView.findViewById(R.id.note)

        //repopulates email information if already entered and saved previously
        toEmail.setText(toEmailAddress)
        subject.setText(emailSubject)
        note.setText(emailNote)

        builder.setView(emailView)
        builder.setTitle("Enter Email Information")
            .setPositiveButton("Confirm") { _, _ ->
                toEmailAddress = toEmail.text?.trim().toString()
                emailSubject = subject.text?.trim().toString()
                emailNote = note.text?.trim().toString()
                Toast.makeText(
                    this, "Email information received and saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }

        val alert = builder.create()
        alert.show()
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(dialogButtonColor)
        alert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(dialogButtonColor)
    }


}

