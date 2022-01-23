package com.example.readitloud

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputLayout
import com.itextpdf.text.pdf.PdfReader
import java.io.InputStream
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.io.IOException
import java.util.*


class OpenFileManager : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var textToSpeech : TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_file_manager)
        val button = findViewById<Button>(R.id.button)
        val speakButton = findViewById<Button>(R.id.speakButton)
        textToSpeech = TextToSpeech(this,this)
        val editText = findViewById<EditText>(R.id.content)
        if(editText.text.isEmpty()){
            speakButton.isEnabled = false
        }
        speakButton.setOnClickListener {
            if(editText.text.isEmpty()) {
                Toast.makeText(this, "Please Select a Pdf file first", Toast.LENGTH_LONG).show()
            } else {
              speakOut(editText.text.toString())
            }
        }
        button.setOnClickListener{
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.type = "*/*"
            startActivityForResult(intent,10)
        }

    }

    private fun speakOut(text: String) {
        textToSpeech!!.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val speakButton = findViewById<Button>(R.id.speakButton)
        speakButton.isEnabled = true
        if(resultCode == RESULT_OK){
            data?.data?.let { extractText(it) }
        }
    }
    private var inputStream:InputStream? = null
    private fun extractText(uri: Uri) {
        val editText = findViewById<EditText>(R.id.content)
        try {
            inputStream = this@OpenFileManager.contentResolver.openInputStream(uri)
        }catch (e:IOException){

        }
        var textExtracted = ""

        try {
            val reader = PdfReader(inputStream)
            val totalNumberOfPages = reader.numberOfPages
            for (i in 0 until totalNumberOfPages) {
                textExtracted =
                    textExtracted + PdfTextExtractor.getTextFromPage(reader, i + 1)
                        .trim { it <= ' ' } + "\n"
                // to extract the PDF content from the different pages
            }
            reader.close()
            editText.setText(textExtracted)
        }catch (e:IOException){
            Log.d("ReadingProblem","run"+e.message.toString())
        }

    }

    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            val result = textToSpeech!!.setLanguage(Locale.ENGLISH)
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TextToSpeech","The Language either not supported or not available!")
            }

        }
        else{
            Log.e("TextToSpeech","Failed")
        }
    }

    public override fun onDestroy() {
        if(textToSpeech != null){
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }
        super.onDestroy()
    }
}