package com.athalia.sellio

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.athalia.sellio.kategori.DataCabangActivity
import com.athalia.sellio.kategori.DataKategoriActivity
import com.athalia.sellio.kategori.DataPegawaiActivity
import com.athalia.sellio.kategori.DataProdukActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Card Kategori
        findViewById<CardView>(R.id.cardKategori)?.setOnClickListener {
            startActivity(Intent(this, DataKategoriActivity::class.java))
        }

        // Card Produk / Menu
        findViewById<CardView>(R.id.cardProduk)?.setOnClickListener {
            startActivity(Intent(this, DataProdukActivity::class.java))
        }

        // Card Pegawai
        findViewById<CardView>(R.id.cardPegawai)?.setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }

        // Card Cabang
        findViewById<CardView>(R.id.cardCabang)?.setOnClickListener {
            startActivity(Intent(this, DataCabangActivity::class.java))
        }

        // Card Akun - Tambahkan Intent ke activity_akun
        findViewById<CardView>(R.id.cardAkun)?.setOnClickListener {
            startActivity(Intent(this, activity_akun::class.java))
        }

        // Card Printer (opsional)
        findViewById<CardView>(R.id.cardPrinter)?.setOnClickListener {
            Toast.makeText(this, "Fitur Printer sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }
    }
}