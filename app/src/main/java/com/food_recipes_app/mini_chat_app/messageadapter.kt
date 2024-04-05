import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.food_recipes_app.mini_chat_app.R
import com.food_recipes_app.mini_chat_app.messagemodal
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso


class messageadapter(private val messageList: ArrayList<messagemodal>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // ViewHolder para mensajes de usuario
    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampTextView: TextView = itemView.findViewById(R.id.TimestampTextView)
        val usermsgTV: TextView = itemView.findViewById(R.id.TVuser)
    }

    // ViewHolder para mensajes del bot
    class BotMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampTextView: TextView = itemView.findViewById(R.id.TimestampTextView)
        val botmsgTV: TextView = itemView.findViewById(R.id.bot_message)
        val botmsgImage: ImageView = itemView.findViewById(R.id.bot_image)
    }

    // Crea un nuevo ViewHolder según el tipo de vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            val userView = inflater.inflate(R.layout.mensajes_user, parent, false)
            UserMessageViewHolder(userView)
        } else {
            val botView = inflater.inflate(R.layout.respuestas_chat, parent, false)
            BotMessageViewHolder(botView)
        }
    }

    // Vincula los datos del mensaje a la vista correspondiente en el ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        when (holder.itemViewType) {
            0 -> {
                val userHolder = holder as UserMessageViewHolder
                userHolder.usermsgTV.text = message.message
                userHolder.timestampTextView.text = message.createdAt
            }
            1 -> {
                val botHolder = holder as BotMessageViewHolder
                botHolder.botmsgTV.text = message.message
                botHolder.timestampTextView.text = message.createdAt
                if (message.imageUrl != null && message.imageUrl!!.isNotEmpty()) {
                    // Carga la imagen del bot si está presente en el mensaje
                    Picasso.get()
                        .load(message.imageUrl)
                        .into(botHolder.botmsgImage, object : Callback {
                            override fun onSuccess() {}
                            override fun onError(e: Exception?) {}
                        })
                }
            }
        }
    }

    // Devuelve el número total de elementos en el conjunto de datos
    override fun getItemCount(): Int = messageList.size

    // Devuelve el tipo de vista para un elemento en una posición determinada
    override fun getItemViewType(position: Int): Int {
        return when (messageList[position].sender) {
            "user" -> 0 // Mensaje del usuario
            "bot" -> 1 // Mensaje del bot
            else -> 1
        }
    }
}
