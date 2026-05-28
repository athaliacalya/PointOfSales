package com.athalia.sellio

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.athalia.sellio.model.ModelPelanggan
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ModPelangganActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var etNamaPelanggan: TextInputEditText
    private lateinit var etNoTelp: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var actMemberLevel: AutoCompleteTextView
    private lateinit var etPoin: TextInputEditText
    private lateinit var btnSimpan: MaterialButton

    private lateinit var database: DatabaseReference
    private var pelangganId: String? = null
    private var isEditMode = false

    private val memberLevelList = arrayOf("Regular", "Gold", "Platinum")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_pelanggan)

        init()
        getIntentData()
        setupMemberLevelDropdown()
        setupListeners()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        etNamaPelanggan = findViewById(R.id.etNamaPelanggan)
        etNoTelp = findViewById(R.id.etNoTelp)
        etEmail = findViewById(R.id.etEmail)
        etAlamat = findViewById(R.id.etAlamat)
        actMemberLevel = findViewById(R.id.actMemberLevel)
        etPoin = findViewById(R.id.etPoin)
        btnSimpan = findViewById(R.id.btnSimpan)

        database = FirebaseDatabase.getInstance().getReference("pelanggan")
    }

    private fun getIntentData() {
        pelangganId = intent.getStringExtra(DataPelangganActivity.EXTRA_PELANGGAN_ID)

        if (pelangganId != null && pelangganId!!.isNotEmpty()) {
            isEditMode = true
            tvTitle.text = "Edit Pelanggan"
            etNamaPelanggan.setText(intent.getStringExtra(DataPelangganActivity.EXTRA_PELANGGAN_NAMA) ?: "")
            etNoTelp.setText(intent.getStringExtra(DataPelangganActivity.EXTRA_PELANGGAN_NO_TELP) ?: "")
            etEmail.setText(intent.getStringExtra(DataPelangganActivity.EXTRA_PELANGGAN_EMAIL) ?: "")
            etAlamat.setText(intent.getStringExtra(DataPelangganActivity.EXTRA_PELANGGAN_ALAMAT) ?: "")
            actMemberLevel.setText(intent.getStringExtra(DataPelangganActivity.EXTRA_PELANGGAN_MEMBER_LEVEL) ?: "Regular", false)
            etPoin.setText(intent.getIntExtra(DataPelangganActivity.EXTRA_PELANGGAN_POIN, 0).toString())
        } else {
            isEditMode = false
            tvTitle.text = "Tambah Pelanggan"
            actMemberLevel.setText("Regular", false)
            etPoin.setText("0")
        }
    }

    private fun setupMemberLevelDropdown() {
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, memberLevelList)
        actMemberLevel.setAdapter(adapter)
        actMemberLevel.threshold = 1
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener { simpanData() }
    }

    private fun simpanData() {
        val namaPelanggan = etNamaPelanggan.text?.toString()?.trim() ?: ""
        val noTelp = etNoTelp.text?.toString()?.trim() ?: ""
        val email = etEmail.text?.toString()?.trim() ?: ""
        val alamat = etAlamat.text?.toString()?.trim() ?: ""
        val memberLevel = actMemberLevel.text?.toString()?.trim() ?: "Regular"
        val poin = etPoin.text?.toString()?.trim()?.toIntOrNull() ?: 0

        if (namaPelanggan.isEmpty()) {
            etNamaPelanggan.error = "Nama pelanggan tidak boleh kosong"
            etNamaPelanggan.requestFocus()
            return
        }

        if (noTelp.isEmpty()) {
            etNoTelp.error = "No telepon tidak boleh kosong"
            etNoTelp.requestFocus()
            return
        }

        if (isEditMode && pelangganId != null) {
            val updates = mapOf(
                "namaPelanggan" to namaPelanggan,
                "noTelp" to noTelp,
                "email" to email,
                "alamat" to alamat,
                "memberLevel" to memberLevel,
                "poin" to poin,
                "updatedAt" to System.currentTimeMillis().toString()
            )
            database.child(pelangganId!!).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pelanggan berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Gagal mengupdate: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            val id = database.push().key
            if (id != null) {
                val pelanggan = ModelPelanggan(
                    idPelanggan = id,
                    namaPelanggan = namaPelanggan,
                    noTelp = noTelp,
                    email = email,
                    alamat = alamat,
                    memberLevel = memberLevel,
                    poin = poin,
                    createdAt = System.currentTimeMillis().toString(),
                    updatedAt = System.currentTimeMillis().toString()
                )
                database.child(id).setValue(pelanggan)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pelanggan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "Gagal menambahkan: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}