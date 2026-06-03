package com.athalia_calya.pos.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.athalia_calya.pos.R
import com.athalia_calya.pos.model.ModelKategori
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase

class ModKategoriActivity : AppCompatActivity() {

    // Deklarasi variabel sesuai layout
    private lateinit var btnBack: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var etNamaKategori: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: MaterialButton

    // Firebase
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    // Variabel untuk menampung data yang akan diedit
    private var kategoriId: String? = null
    private var kategoriNama: String? = null
    private var kategoriStatus: String? = null

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NAMA = "extra_nama"
        const val EXTRA_STATUS = "extra_status"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        getIntentData()
        setupSpinner()
        setupClickListeners()
        updateTitle()
    }

    private fun init() {
        // Inisialisasi semua view sesuai layout
        btnBack = findViewById(R.id.btnBack)
        tvJudul = findViewById(R.id.tvJudul)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun getIntentData() {
        // Ambil data dari intent jika ada (mode edit)
        kategoriId = intent.getStringExtra(EXTRA_ID)
        kategoriNama = intent.getStringExtra(EXTRA_NAMA)
        kategoriStatus = intent.getStringExtra(EXTRA_STATUS)

        // Jika mode edit, tampilkan data yang ada
        if (kategoriId != null && kategoriNama != null) {
            etNamaKategori.setText(kategoriNama)

            // Set status spinner berdasarkan nilai status
            val statusPosition = when (kategoriStatus) {
                "1" -> 0 // Aktif
                "0" -> 1 // Tidak Aktif
                else -> 0 // Default Aktif
            }
            spinnerStatus.setSelection(statusPosition)
        }
    }

    private fun updateTitle() {
        // Update judul halaman
        tvJudul.text = if (kategoriId == null) "Tambah Kategori" else "Edit Kategori"
    }

    private fun setupSpinner() {
        // Data untuk spinner
        val listStatus = arrayOf("Aktif", "Tidak Aktif")

        // Buat adapter untuk spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listStatus
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set adapter ke spinner
        spinnerStatus.adapter = adapter
    }

    private fun setupClickListeners() {
        // Tombol back
        btnBack.setOnClickListener {
            finish()
        }

        // Tombol simpan
        btnSimpan.setOnClickListener {
            simpanData()
        }
    }

    private fun simpanData() {
        // Ambil data dari form
        val nama = etNamaKategori.text.toString().trim()
        val statusPosition = spinnerStatus.selectedItemPosition

        // Konversi posisi spinner ke nilai status ("1" atau "0")
        val statusValue = when (statusPosition) {
            0 -> "1"  // Aktif
            1 -> "0"  // Tidak Aktif
            else -> "1" // Default aktif
        }

        // Validasi nama
        if (nama.isEmpty()) {
            etNamaKategori.error = "Nama kategori tidak boleh kosong"
            etNamaKategori.requestFocus()
            return
        }

        if (kategoriId == null) {
            // Mode TAMBAH data baru
            val id = myRef.push().key

            if (id == null) {
                Toast.makeText(this, "Gagal membuat ID", Toast.LENGTH_SHORT).show()
                return
            }

            // Buat objek kategori
            val kategori = ModelKategori(
                idKategori = id,
                namaKategori = nama,
                statusKategori = statusValue
            )

            // Simpan ke Firebase
            myRef.child(id).setValue(kategori)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data kategori berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Mode EDIT data yang sudah ada
            val kategori = ModelKategori(
                idKategori = kategoriId!!,
                namaKategori = nama,
                statusKategori = statusValue
            )

            // Update data di Firebase
            myRef.child(kategoriId!!).setValue(kategori)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data kategori berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mengupdate data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
