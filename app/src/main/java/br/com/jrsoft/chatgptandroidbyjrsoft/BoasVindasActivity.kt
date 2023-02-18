package br.com.jrsoft.chatgptandroidbyjrsoft

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class BoasVindasActivity: AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boas_vindas)
        FirebaseApp.initializeApp(this)
        mAuth = Firebase.auth

        iniciaAutenticacao()
        configuraBotoes()
    }

    private fun signIn() {
        val intentClient = googleSignInClient.signInIntent
        abreActivity.launch(intentClient)
    }

    var abreActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result: ActivityResult ->
        if (result.resultCode == RESULT_OK){
            val intentAbre = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(intentAbre)
            try {
                val conta = task.getResult(ApiException::class.java)
                loginGoogle(conta.idToken!!)
            }catch (exception: ApiException){

            }

        }
    }

    private fun loginGoogle(token: String) {
        val credencial = GoogleAuthProvider.getCredential(token, null)
        Log.d("AUTENT", "login")
        mAuth.signInWithCredential(credencial).addOnCompleteListener(this)
        { task: Task<AuthResult> ->
            if (task.isSuccessful) {
                Log.d("AUTENT", "login ok")
                Toast.makeText(this, "Login realizado", Toast.LENGTH_SHORT).show()
                vaiParaOChat()
            } else {
                Toast.makeText(this, "Falha no Login", Toast.LENGTH_SHORT).show()
                Log.d("AUTENT", "login não")
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        Log.d("AUTENT", "request - 80: $requestCode")
        super.onActivityResult(requestCode, resultCode, intent)
        Log.d("AUTENT", "request: $requestCode")
        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            Log.d("AUTENT", "linha 117: ${intent.toString()}")
            try {
                val conta = task.getResult(ApiException::class.java)
                conta!!.idToken?.let { loginGoogle(it) }
                Log.d("AUTENT", "result ok")
            } catch (exception: ApiException) {
                Log.d("AUTENT", "result não: $exception")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val usuarioLogado = mAuth.currentUser
        try {
            Toast.makeText(this, "Usuário " + usuarioLogado!!.email + " logado", Toast.LENGTH_LONG)
                .show()
            vaiParaOChat()
        } catch (e: Exception) {

        }
    }

    private fun iniciaAutenticacao() {
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1029433356019-qjbar5gjffn0q1uthitpu45vu4khcscs.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        Log.d("AUTENT", "autenticação " + gso.toString())
    }

    private fun configuraBotoes() {
        val login = findViewById<SignInButton>(R.id.boas_vindas_botao_login_google)
        login.setOnClickListener { signIn() }
    }

    private fun vaiParaOChat() {
        val intentChat = Intent(this@BoasVindasActivity, ChatActivity::class.java)
        startActivity(intentChat)
    }
}