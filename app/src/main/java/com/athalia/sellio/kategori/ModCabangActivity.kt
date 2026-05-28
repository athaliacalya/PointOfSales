package com.athalia.sellio.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.athalia.sellio.R
import com.athalia.sellio.model.ModelCabang
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference  // Tambahkan import ini
import com.google.firebase.database.FirebaseDatabase

class ModCabangActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var etNamaCabang: TextInputEditText
    private lateinit var etKeterangan: TextInputEditText
    private lateinit var btnSimpan: MaterialButton

    private lateinit var database: DatabaseReference  // Sekarang tidak error
    private var cabangId: String? = null
    private var isEditMode = false

    companion object {
        const val EXTRA_CABANG_ID = "extra_cabang_id"
        const val EXTRA_CABANG_NAMA = "extra_cabang_nama"
        const val EXTRA_CABANG_KETERANGAN = "extra_cabang_keterangan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_cabang)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        getIntentData()
        setupListeners()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        etNamaCabang = findViewById(R.id.etNamaCabang)
        etKeterangan = findViewById(R.id.etKeterangan)
        btnSimpan = findViewById(R.id.btnSimpan)

        database = FirebaseDatabase.getInstance().getReference("cabang")
    }

    private fun getIntentData() {
        cabangId = intent.getStringExtra(EXTRA_CABANG_ID)

        if (cabangId != null && cabangId!!.isNotEmpty()) {
            isEditMode = true
            tvTitle.text = "Edit Cabang"
            etNamaCabang.setText(intent.getStringExtra(EXTRA_CABANG_NAMA) ?: "")
            etKeterangan.setText(intent.getStringExtra(EXTRA_CABANG_KETERANGAN) ?: "")
        } else {
            isEditMode = false
            tvTitle.text = "Tambah Cabang"
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            simpanData()
        }
    }

    private fun simpanData() {
        val namaCabang = etNamaCabang.text?.toString()?.trim() ?: ""
        val keterangan = etKeterangan.text?.toString()?.trim() ?: ""

        if (namaCabang.isEmpty()) {
            etNamaCabang.error = "Nama cabang tidak boleh kosong"
            etNamaCabang.requestFocus()
            return
        }

        if (isEditMode && cabangId != null) {
            // Update data
            val updates = mapOf(
                "namaCabang" to namaCabang,
                "keterangan" to keterangan
            )
            database.child(cabangId!!).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cabang berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Gagal mengupdate cabang: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Tambah data baru (seharusnya tidak masuk ke sini karena pakai form terpisah)
            finish()
        }
    }
}