package br.com.jrsoft.chatgptandroidbyjrsoft

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.completion.TextCompletion
import com.aallam.openai.api.exception.OpenAIException
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class ConversaActivity: AppCompatActivity() {

    private lateinit var openAI: OpenAI
    private lateinit var textoPrompt: EditText
    private lateinit var textoResposta: TextView
    private lateinit var avatarUsuario: ImageView
    private lateinit var mAuth: FirebaseAuth
    //private lateinit var usuarioLogado: String
    lateinit var mAdView : AdView
    private val scope = CoroutineScope(Dispatchers.Main)
    private val PREFS_NAME = "ConversaPrefs"
    private val HISTORICO_KEY = "historico"

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("textoResposta", textoResposta.text.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversa)

        //Banner AdMob
        iniciaBanner()

        //inicia autenticação do Google
        mAuth = Firebase.auth

        //Token OpenAT API
        openAI = OpenAI("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")

        //inicia campos
        iniciaCampos()

        if (savedInstanceState != null) {
            textoResposta.text = savedInstanceState.getString("textoResposta")
        }

        //configura botões
        configuraBotoes()
    }

    override fun onPause() {
        super.onPause()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putStringSet(HISTORICO_KEY, textoResposta.text.lines().toSet())
        editor.apply()
    }

    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historico = prefs.getStringSet(HISTORICO_KEY, emptySet()) ?: emptySet()
        textoResposta.text = historico.joinToString(separator = "\n")
    }

    private fun iniciaBanner() {
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.conversa_banner)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun iniciaCampos() {
        textoPrompt = findViewById(R.id.conversa_texto_prompt)
        textoResposta = findViewById(R.id.conversa_texto_resposta)
        textoResposta.movementMethod = ScrollingMovementMethod()
        val usuarioLogado = mAuth.currentUser
        try {
            val avatar = usuarioLogado?.photoUrl
            avatarUsuario = findViewById<ImageView>(R.id.conversa_avatar)
            Glide.with(this)
                .load(avatar)
                .into(avatarUsuario)
            //avatarUsuario.setImageURI(avatar)
        } catch (e: Exception) {

        }
    }

    private fun conclusao(consulta: String) {
        scope.launch {
            try {
                val completionRequest = CompletionRequest(
                    model = ModelId("text-babbage-001"),
                    prompt = consulta,
                    1900,
                    temperature = 0.9,
                    echo = false
                )
                val textCompletion: TextCompletion = withContext(Dispatchers.Default) {
                    openAI.completion(completionRequest)
                }
                val resposta = textCompletion.choices[0].text
                val usuarioLogado = mAuth.currentUser
                val usuario = usuarioLogado!!.displayName
                val chat = "ChatGPT"
                textoResposta.append("$usuario -> $consulta\n")
                textoResposta.append("$chat -> $resposta\n")
                textoResposta.post {
                    textoResposta.scrollTo(0, textoResposta.baseline)
                }
                textoPrompt.setText("")
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(textoPrompt.windowToken, 0)
            } catch (e: Exception) {
                Toast.makeText(this@ConversaActivity, "Erro ao obter a resposta: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("CHAT", "Erro ao obter a resposta: ${e.message}")
                e.printStackTrace()
            }

        }
    }

    private fun configuraBotoes() {
        val limpar = findViewById<Button>(R.id.conversa_botao_limpar)
        limpar.setOnClickListener {
            limpaConversa()
        }
        val desconectar = findViewById<Button>(R.id.conversa_botao_desconectar)
        desconectar.setOnClickListener { signOut() }
        val enviar = findViewById<Button>(R.id.conversa_botao_enviar)
        enviar.setOnClickListener {
            val consulta = textoPrompt.text.toString()
            conclusao(consulta)
        }
    }

    private fun limpaConversa() {
        textoResposta.text = ""
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(HISTORICO_KEY)
        editor.apply()
    }

    fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                Toast.makeText(applicationContext, "Logout concluído", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, BoasVindasActivity::class.java)
                startActivity(intent)
            }
    }
}
