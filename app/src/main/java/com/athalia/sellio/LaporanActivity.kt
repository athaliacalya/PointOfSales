package com.athalia.sellio

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.athalia.sellio.model.ModelProduk
import com.athalia.sellio.model.ModelTransaksi
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*

class LaporanActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvTotalPenjual: TextView
    private lateinit var tvTotalUntung: TextView
    private lateinit var cardRiwayatLink: CardView

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laporan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        loadFinancialData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTotalPenjual = findViewById(R.id.tvTotalPenjual)
        tvTotalUntung = findViewById(R.id.tvTotalUntung)
        cardRiwayatLink = findViewById(R.id.cardRiwayatLink)

        database = FirebaseDatabase.getInstance().reference
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        cardRiwayatLink.setOnClickListener {
            startActivity(Intent(this, RiwayatTransaksiActivity::class.java))
        }
    }

    private fun loadFinancialData() {
        // Step 1: Ambil data produk untuk mengetahui hargaModal masing-masing produk
        database.child("produk").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(productSnapshot: DataSnapshot) {
                val productModalMap = HashMap<String, Int>()
                for (snap in productSnapshot.children) {
                    val id = snap.key ?: ""
                    val produk = snap.getValue(ModelProduk::class.java)
                    if (produk != null) {
                        productModalMap[id] = produk.hargaModal
                    }
                }

                // Step 2: Ambil riwayat transaksi untuk menghitung total penjualan dan keuntungan
                database.child("transaksi").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(transaksiSnapshot: DataSnapshot) {
                        var totalSales = 0L
                        var totalProfit = 0L

                        for (snap in transaksiSnapshot.children) {
                            val transaksi = snap.getValue(ModelTransaksi::class.java)
                            if (transaksi != null && transaksi.status == "selesai") {
                                // Penjualan berdasarkan total nilai transaksi (setelah pajak)
                                totalSales += transaksi.total

                                var transProfit = 0L
                                for (item in transaksi.items) {
                                    val modalPrice = productModalMap[item.idProduk] ?: 0
                                    // Keuntungan per item = (harga jual - harga modal) * jumlah
                                    val itemProfit = (item.harga - modalPrice).toLong() * item.jumlah
                                    transProfit += itemProfit
                                }
                                
                                // Jika ada diskon pada transaksi, sesuaikan untung bersih
                                transProfit -= transaksi.diskon
                                totalProfit += transProfit
                            }
                        }

                        // Tampilkan hasil perhitungan terformat di UI
                        tvTotalPenjual.text = formatRupiah(totalSales)
                        tvTotalUntung.text = formatRupiah(totalProfit)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LaporanActivity, "Gagal memuat data transaksi: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LaporanActivity, "Gagal memuat data produk: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        return "Rp ${formatter.format(amount)}"
    }
}
