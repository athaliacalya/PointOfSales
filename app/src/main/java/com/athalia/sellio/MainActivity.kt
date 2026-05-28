package com.athalia.sellio

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvJam: TextView
    private lateinit var tvEstimasi: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var running = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupClickListeners()
        startRealTimeClock()

        // Estimasi sementara (nanti bisa diisi dari Firebase)
        tvEstimasi.text = formatRupiah(0)
    }

    private fun initViews() {
        tvGreeting = findViewById(R.id.tvGreeting)
        tvDate = findViewById(R.id.tvDate)
        tvJam = findViewById(R.id.tvJam)
        tvEstimasi = findViewById(R.id.tvEstimasi)
    }

    private fun startRealTimeClock() {
        val clockRunnable = object : Runnable {
            override fun run() {
                if (running) {
                    updateDateTime()
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(clockRunnable)
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))
        tvJam.text = timeFormat.format(calendar.time)

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        tvDate.text = dateFormat.format(calendar.time)

        updateGreeting(calendar.get(Calendar.HOUR_OF_DAY))
    }

    private fun updateGreeting(hour: Int) {
        val greeting = when (hour) {
            in 0..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
        tvGreeting.text = "$greeting, Athalia"
    }

    private fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        return "Rp ${formatter.format(amount)}"
    }

    private fun setupClickListeners() {
        // Card Akun
        findViewById<CardView>(R.id.cardAkun)?.setOnClickListener {
            startActivity(Intent(this, activity_akun::class.java))
        }

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

        // Card Printer
        findViewById<CardView>(R.id.cardPrinter)?.setOnClickListener {
            Toast.makeText(this, "Fitur Printer sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }

        // Card Transaksi
        findViewById<android.view.View>(R.id.cardTransaksi)?.setOnClickListener {
            Toast.makeText(this, "Fitur Transaksi sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }

        // Card Pelanggan
        findViewById<android.view.View>(R.id.cardPelanggan)?.setOnClickListener {
            Toast.makeText(this, "Fitur Pelanggan sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }

        // Card Laporan
        findViewById<android.view.View>(R.id.cardLaporan)?.setOnClickListener {
            Toast.makeText(this, "Fitur Laporan sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        handler.removeCallbacksAndMessages(null)
    }
}