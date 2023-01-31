package com.kunle.randomidgeneratorkotlin

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.google.android.material.textfield.TextInputEditText
import com.kunle.randomidgeneratorkotlin.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.pow


val numberCharacterList = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
val lowercaseCharacterList = arrayOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m")
val uppercaseCharacterList = arrayOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M")


class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private lateinit var filename: String
    private var toEmailAddress = ""
    private var emailSubject = ""
    private var emailNote = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_main)

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
                        bind.characterNum.text?.let { it1 -> bind.characterNum.setSelection(it1.length) }
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

        bind.emailInfoButton.setOnClickListener { showEmailOptions() }

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

    private fun createIDs() {
        val externalFilesDirectory = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val textFile = File(externalFilesDirectory, filename)
        textFile.deleteOnExit()

        val numbers = bind.numbersCheckBox.isChecked
        val lowercase = bind.lowercaseCheckBox.isChecked
        val uppercase = bind.uppercaseCheckBox.isChecked

        val selectedIDArray =
            when {
                numbers && !lowercase && !uppercase -> numberCharacterList
                !numbers && lowercase && !uppercase -> lowercaseCharacterList
                !numbers && !lowercase && uppercase -> uppercaseCharacterList
                numbers && lowercase && !uppercase -> numberCharacterList + lowercaseCharacterList
                !numbers && lowercase && uppercase -> lowercaseCharacterList + uppercaseCharacterList
                numbers && !lowercase && uppercase -> numberCharacterList + uppercaseCharacterList
                else -> numberCharacterList + uppercaseCharacterList + lowercaseCharacterList
            }

        val charListCount = selectedIDArray.size

        var idsNumText = bind.idsNum.text?.trim().toString()
        var characterNumText = bind.characterNum.text?.trim().toString()

        if (idsNumText.contains(",")) {
            idsNumText = idsNumText.replace(",", "")
        }

        if (characterNumText.contains(",")) {
            characterNumText = characterNumText.replace(",", "")
        }

        val numCharactersPerID = Integer.parseInt(characterNumText)
        val numOfIDs = Integer.parseInt(idsNumText)

        //check to see if even possible for the selected character array
        //to create as many unique IDs as user need
        //Math.pow() calculation is calculating maximum # of possible unique permutations
        if (numOfIDs <= charListCount.toDouble().pow(numCharactersPerID.toDouble())) {
            try {
                val myObj = File(filename)
                myObj.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.progress_bar, null)

//            var progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
//            var percentProgressValue: TextView = view.findViewById(R.id.percent_progress)
//            progressBar.max = numOfIDs

            builder.setView(view)
            val alert = builder.create()
            alert.setCancelable(false)
            alert.show()

            //runs labor-intensive ID creation on background thread,
            //with updates to UI progress bar on main thread
            var count = 0
            val uniqueIDChecker: HashSet<String> = HashSet()

            runBlocking {
                launch {
                    try {
                        val myWriter = FileWriter(textFile)
                        while (count < numOfIDs) {
                            val respid = StringBuilder(numCharactersPerID)
                            for (x in 0 until numCharactersPerID) {
                                respid.append(selectedIDArray[Random().nextInt(charListCount)])
                            }
                            //checks if ID is already in hashset (i.e. is it unique)
                            if (uniqueIDChecker.add(respid.toString())) {
                                respid.append(System.lineSeparator())
                                myWriter.write(respid.toString())
                                Log.d("Respids", "respid: $respid")
                                count++
                            }
                        }
                        myWriter.flush()
                        myWriter.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            alert.dismiss()
            sendEmail(textFile)

        } else {
            val builder = AlertDialog.Builder(this)
            val df = DecimalFormat("#,###")
            builder.setMessage("Can't create ${df.format(numOfIDs)} unique IDs with " +
                    "parameters chosen. Please either change ID characteristics or " +
                    "increase # of characters per ID")
                .setPositiveButton("OK") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
            builder.create().show()
        }
    }

    private fun sendEmail(file: File) {
        val fileUri = FileProvider.getUriForFile(this, "$packageName.provider",file)
        file.deleteOnExit()

        try {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            emailIntent.setType("message/rfc822")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmailAddress))
            emailIntent.putExtra(Intent.EXTRA_STREAM,fileUri)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,emailSubject)
            emailIntent.putExtra(Intent.EXTRA_TEXT,emailNote)
            this.startActivity(Intent.createChooser(emailIntent,"Sending email..."))
        } catch (e: Throwable) {
            Toast.makeText(this, "Request failed try again: $e", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        //updates file name when user comes back to app
        //update needed because filename based on current data and time
        setFileName()
    }
}



