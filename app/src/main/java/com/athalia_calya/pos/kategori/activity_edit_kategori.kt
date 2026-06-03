package com.athalia_calya.pos.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.athalia_calya.pos.R

class activity_edit_kategori : AppCompatActivity() {

    private lateinit var etNamaKategori: EditText
    private lateinit var switchStatus: SwitchCompat
    private lateinit var btnUpdateKategori: Button
    private lateinit var icBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_kategori)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi view
        etNamaKategori = findViewById(R.id.etNamaKategori)
        switchStatus = findViewById(R.id.switchStatus)
        btnUpdateKategori = findViewById(R.id.btnUpdateKategori)
        icBack = findViewById(R.id.ic_back)

        // Simulasi data kategori yang akan diedit (bisa dari Intent)
        // Ambil data dari Intent jika dikirim dari activity sebelumnya
        val kategoriLama = intent.getStringExtra("kategori_nama") ?: "Makanan"
        val statusLama = intent.getBooleanExtra("kategori_status", true)

        // Set data ke view
        etNamaKategori.setText(kategoriLama)
        switchStatus.isChecked = statusLama
        updateStatusText(statusLama)

        // Listener untuk Switch: ubah teks status saat toggle
        switchStatus.setOnCheckedChangeListener { _, isChecked ->
            updateStatusText(isChecked)
        }

        // Tombol Back - Kembali ke ActivityDataKategori
        icBack.setOnClickListener {
            val intent = Intent(this, DataKategoriActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Tombol Update Kategori
        btnUpdateKategori.setOnClickListener {
            val namaKategoriBaru = etNamaKategori.text.toString().trim()
            val statusBaru = switchStatus.isChecked

            if (namaKategoriBaru.isEmpty()) {
                Toast.makeText(this, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan perubahan kategori
            val updateBerhasil = updateKategori(namaKategoriBaru, statusBaru)

            if (updateBerhasil) {
                Toast.makeText(
                    this,
                    "Kategori berhasil diupdate: $namaKategoriBaru (${if (statusBaru) "Aktif" else "Tidak Aktif"})",
                    Toast.LENGTH_LONG
                ).show()

                // Kembali ke ActivityDataKategori setelah update berhasil
                val intent = Intent(this, DataKategoriActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Update gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatusText(isActive: Boolean) {
        val tvStatusValue = findViewById<TextView>(R.id.tvStatusValue)
        tvStatusValue.text = if (isActive) "Aktif" else "Tidak Aktif"
    }

    private fun updateKategori(nama: String, status: Boolean): Boolean {
        // TODO: Ganti dengan kode penyimpanan ke database / SharedPreferences / API
        // Contoh dengan SharedPreferences:
        // val sharedPref = getSharedPreferences("kategori_pref", MODE_PRIVATE)
        // with(sharedPref.edit()) {
        //     putString("kategori_nama", nama)
        //     putBoolean("kategori_status", status)
        //     apply()
        // }

        // Sementara return true untuk simulasi berhasil
        return true
    }
}
