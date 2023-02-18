package br.com.jrsoft.chatgptandroidbyjrsoft

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth

class ChatActivity: AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var avatarUsuario: ImageView
    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        iniciaAvatar()
        iniciaBanner()
        configuraBotoes()
    }

    private fun iniciaBanner() {
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.chat_banner)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun iniciaAvatar() {
        val usuarioLogado = mAuth.currentUser
        try {
            val avatar = usuarioLogado?.photoUrl
            avatarUsuario = findViewById<ImageView>(R.id.chat_avatar)
            Glide.with(this)
                .load(avatar)
                .into(avatarUsuario)
            //avatarUsuario.setImageURI(avatar)
        } catch (e: Exception) {

        }
    }

    private fun configuraBotoes() {
        val desconectar = findViewById<Button>(R.id.chat_botao_desconectar)
        desconectar.setOnClickListener { signOut() }
        val conversa = findViewById<ImageButton>(R.id.chat_img_btn_nova_conversa)
        conversa.setOnClickListener {
            val intent = Intent(this@ChatActivity, ConversaActivity::class.java)
            startActivity(intent)
        }
        val imagem = findViewById<ImageButton>(R.id.chat_img_btn_nova_imagem)
        imagem.setOnClickListener {
            Toast.makeText(this, "Em breve!", Toast.LENGTH_LONG).show()
        }
        val compara = findViewById<ImageButton>(R.id.chat_img_btn_nova_comparaao)
        compara.setOnClickListener {
            Toast.makeText(this, "Em breve!", Toast.LENGTH_LONG).show()
        }
        val codigo = findViewById<ImageButton>(R.id.chat_img_btn_novo_codigo)
        codigo.setOnClickListener {
            Toast.makeText(this, "Em breve!", Toast.LENGTH_LONG).show()
        }
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                Toast.makeText(applicationContext, "Logout concluído", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, BoasVindasActivity::class.java)
                startActivity(intent)
        }
    }
}




