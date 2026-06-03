package com.athalia_calya.pos.kategori

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.athalia_calya.pos.R
import com.athalia_calya.pos.model.ModelPegawai
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference  // Tambahkan import ini
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ModPegawaiActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var etNamaLengkap: TextInputEditText
    private lateinit var etTempatLahir: TextInputEditText
    private lateinit var etTanggalLahir: TextInputEditText
    private lateinit var etNoTelp: TextInputEditText
    private lateinit var etJabatan: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var btnSimpan: MaterialButton

    private lateinit var database: DatabaseReference  // Sekarang tidak error
    private var pegawaiId: String? = null
    private var isEditMode = false

    companion object {
        const val EXTRA_PEGAWAI_ID = "extra_pegawai_id"
        const val EXTRA_PEGAWAI_NAMA = "extra_pegawai_nama"
        const val EXTRA_PEGAWAI_TEMPAT_LAHIR = "extra_pegawai_tempat_lahir"
        const val EXTRA_PEGAWAI_TANGGAL_LAHIR = "extra_pegawai_tanggal_lahir"
        const val EXTRA_PEGAWAI_NO_TELP = "extra_pegawai_no_telp"
        const val EXTRA_PEGAWAI_JABATAN = "extra_pegawai_jabatan"
        const val EXTRA_PEGAWAI_ALAMAT = "extra_pegawai_alamat"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_pegawai)

        // Perbaiki: Gunakan findViewById dengan ID yang benar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        getIntentData()
        setupDatePicker()
        setupListeners()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        etNamaLengkap = findViewById(R.id.etNamaLengkap)
        etTempatLahir = findViewById(R.id.etTempatLahir)
        etTanggalLahir = findViewById(R.id.etTanggalLahir)
        etNoTelp = findViewById(R.id.etNoTelp)
        etJabatan = findViewById(R.id.etJabatan)
        etAlamat = findViewById(R.id.etAlamat)
        btnSimpan = findViewById(R.id.btnSimpan)

        // Perbaiki: Inisialisasi database dengan benar
        database = FirebaseDatabase.getInstance().getReference("pegawai")
    }

    private fun getIntentData() {
        pegawaiId = intent.getStringExtra(EXTRA_PEGAWAI_ID)

        if (pegawaiId != null && pegawaiId!!.isNotEmpty()) {
            isEditMode = true
            tvTitle.text = "Edit Pegawai"

            // Isi data dari Intent
            etNamaLengkap.setText(intent.getStringExtra(EXTRA_PEGAWAI_NAMA) ?: "")
            etTempatLahir.setText(intent.getStringExtra(EXTRA_PEGAWAI_TEMPAT_LAHIR) ?: "")
            etTanggalLahir.setText(intent.getStringExtra(EXTRA_PEGAWAI_TANGGAL_LAHIR) ?: "")
            etNoTelp.setText(intent.getStringExtra(EXTRA_PEGAWAI_NO_TELP) ?: "")
            etJabatan.setText(intent.getStringExtra(EXTRA_PEGAWAI_JABATAN) ?: "")
            etAlamat.setText(intent.getStringExtra(EXTRA_PEGAWAI_ALAMAT) ?: "")
        } else {
            isEditMode = false
            tvTitle.text = "Tambah Pegawai"
        }
    }

    private fun setupDatePicker() {
        etTanggalLahir.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val tanggal = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                etTanggalLahir.setText(tanggal)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setupListeners() {
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
        val namaLengkap = etNamaLengkap.text.toString().trim()
        val tempatLahir = etTempatLahir.text.toString().trim()
        val tanggalLahir = etTanggalLahir.text.toString().trim()
        val noTelp = etNoTelp.text.toString().trim()
        val jabatan = etJabatan.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()

        // Validasi Nama Lengkap
        if (namaLengkap.isEmpty()) {
            etNamaLengkap.error = "Nama lengkap tidak boleh kosong"
            etNamaLengkap.requestFocus()
            return
        }

        // Validasi Tempat Lahir
        if (tempatLahir.isEmpty()) {
            etTempatLahir.error = "Tempat lahir tidak boleh kosong"
            etTempatLahir.requestFocus()
            return
        }

        // Validasi Tanggal Lahir
        if (tanggalLahir.isEmpty()) {
            etTanggalLahir.error = "Tanggal lahir tidak boleh kosong"
            etTanggalLahir.requestFocus()
            return
        }

        // Validasi No Telepon
        if (noTelp.isEmpty()) {
            etNoTelp.error = "No telepon tidak boleh kosong"
            etNoTelp.requestFocus()
            return
        }

        // Validasi Jabatan
        if (jabatan.isEmpty()) {
            etJabatan.error = "Jabatan tidak boleh kosong"
            etJabatan.requestFocus()
            return
        }

        if (isEditMode && pegawaiId != null) {
            // Mode EDIT - Update data yang sudah ada
            val updates = mapOf(
                "namaPegawai" to namaLengkap,
                "tempatLahir" to tempatLahir,
                "tanggalLahir" to tanggalLahir,
                "noTelp" to noTelp,
                "jabatan" to jabatan,
                "alamat" to alamat
            )

            database.child(pegawaiId!!).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pegawai berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Gagal mengupdate pegawai: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Mode TAMBAH - Buat data baru
            val id = database.push().key

            if (id != null) {
                val pegawai = ModelPegawai(
                    idPegawai = id,
                    namaPegawai = namaLengkap,
                    tempatLahir = tempatLahir,
                    tanggalLahir = tanggalLahir,
                    noTelp = noTelp,
                    jabatan = jabatan,
                    alamat = alamat,
                    status = "1"
                )

                database.child(id).setValue(pegawai)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pegawai berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "Gagal menambahkan pegawai: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Gagal membuat ID pegawai", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
