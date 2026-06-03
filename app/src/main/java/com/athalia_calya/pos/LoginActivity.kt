package com.athalia_calya.pos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnMasuk: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Cek status login di SharedPreferences (Auto-Login)
        val sharedPreferences = getSharedPreferences("SellioPreferences", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            navigateToDashboard()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        btnMasuk = findViewById(R.id.btnMasuk)

        // Set email dummy default untuk memudahkan pengetesan
        etEmail.setText("admin@sellio.com")
        etPassword.setText("admin123")
    }

    private fun setupListeners() {
        btnMasuk.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        findViewById<android.view.View>(R.id.tvForgotPassword)?.setOnClickListener {
            Toast.makeText(this, "Fitur Reset Password sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.view.View>(R.id.tvHubungiAdmin)?.setOnClickListener {
            Toast.makeText(this, "Hubungi admin di support@sellio.com", Toast.LENGTH_LONG).show()
        }
    }

    private fun validateInputs(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        var isValid = true

        // Validasi Email
        if (email.isEmpty()) {
            tilEmail.error = "Email tidak boleh kosong"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Format email tidak valid"
            isValid = false
        } else {
            tilEmail.error = null
        }

        // Validasi Password
        if (password.isEmpty()) {
            tilPassword.error = "Kata sandi tidak boleh kosong"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Kata sandi minimal terdiri dari 6 karakter"
            isValid = false
        } else {
            tilPassword.error = null
        }

        return isValid
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validasi Kredensial (admin@sellio.com / admin123)
        // Kita juga mengizinkan email valid lainnya dengan password >= 6 karakter untuk fleksibilitas testing
        if (email == "admin@sellio.com" && password == "admin123") {
            saveLoginStatus(email)
            Toast.makeText(this, "Selamat datang kembali, Athalia!", Toast.LENGTH_SHORT).show()
            navigateToDashboard()
        } else {
            // Memberikan opsi masuk untuk demo dengan akun custom
            saveLoginStatus(email)
            Toast.makeText(this, "Masuk berhasil (Akun Demo: $email)", Toast.LENGTH_SHORT).show()
            navigateToDashboard()
        }
    }

    private fun saveLoginStatus(email: String) {
        val sharedPreferences = getSharedPreferences("SellioPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", true)
        editor.putString("logged_in_email", email)
        editor.apply()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

