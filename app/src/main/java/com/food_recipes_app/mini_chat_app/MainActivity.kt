package com.food_recipes_app.mini_chat_app

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import messageadapter
import org.json.JSONObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var messageedt: TextView
    private lateinit var messagerv: RecyclerView
    private lateinit var messageadapter: messageadapter
    private val messageList = ArrayList<messagemodal>()
    private var messageCounter = 0 // Contador de mensajes



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        messageList.addAll(loadMessages(this))
        messageedt = findViewById(R.id.Edtmessage)
        messagerv = findViewById(R.id.zonemessages)

        messageadapter = messageadapter(messageList)
        messagerv.layoutManager = LinearLayoutManager(this)
        messagerv.adapter = messageadapter

        messageedt.setOnEditorActionListener(TextView.OnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEND) {
                val message = messageedt.text.toString()
                if (message.isNotBlank()) {
                    sendMessage(message)
                    messageedt.text = null
                } else {
                    Toast.makeText(this, "Por favor ingrese su mensaje...", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun sendMessage(message: String) {
        // Agregar el mensaje del usuario a la lista
        val userMessage = messagemodal(message, "user", "")
        messageList.add(userMessage)

        // Notificar al adaptador sobre el nuevo mensaje del usuario
        messageadapter.notifyItemInserted(messageList.size - 1)

        // Enviar solicitud a la API
        val queue = Volley.newRequestQueue(this)
        val url = "https://chat.a46-evangelista.workers.dev/chat"

        val request = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                handleResponse(response)
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error al enviar mensaje: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }

            override fun getBody(): ByteArray {
                val params = JSONObject()
                params.put("message", message)
                return params.toString().toByteArray(Charsets.UTF_8)
            }
        }

        // Agregar la solicitud a la cola de Volley
        queue.add(request)
    }
    //manejo de la respuesta de la API
    private fun handleResponse(response: String) {
        try {
            val jsonResponse = JSONObject(response)
            val text = jsonResponse.optString("text", "")
            val createdAt = jsonResponse.optString("createdAt", "")

            // Obtener la URL de la imagen, si está presente
            val attachs = jsonResponse.optJSONArray("attachs")
            var imageUrl: String? = null
            if (attachs != null && attachs.length() > 0) {
                val firstAttach = attachs.getJSONObject(0)
                imageUrl = firstAttach.optString("image", null)
            }

            // Agregar el mensaje a la lista
            val message = messagemodal(text, "bot", createdAt, imageUrl)
            messageList.add(message)

            // Notificar al adaptador sobre el nuevo mensaje
            messageadapter.notifyItemInserted(messageList.size - 1)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
        }
    }
    //guardo de los mensajes
    override fun onPause() {
        super.onPause()
        saveMessages(this, messageList)
    }
    //guardar los mensajes cuando la app se pause
    fun saveMessages(context: Context, messageList: ArrayList<messagemodal>) {
        val sharedPreferences = context.getSharedPreferences("MyMessages", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(messageList)
        editor.putString("messageList", json)
        editor.apply()
    }
    //cargar los mensajes cuando la sea abierta
    fun loadMessages(context: Context): ArrayList<messagemodal> {
        val sharedPreferences = context.getSharedPreferences("MyMessages", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("messageList", "")
        val type = object : TypeToken<ArrayList<messagemodal>>() {}.type
        return gson.fromJson(json, type) ?: ArrayList()
    }

}