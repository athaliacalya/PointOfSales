package com.athalia.sellio

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.model.ItemTransaksi
import com.athalia.sellio.model.ModelProduk
import com.athalia.sellio.model.ModelTransaksi
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransaksiActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvProduk: RecyclerView
    private lateinit var rvKeranjang: RecyclerView
    private lateinit var tvJumlahItem: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnBayar: MaterialButton
    private lateinit var bottomSheet: View

    private lateinit var database: DatabaseReference
    private lateinit var listProduk: ArrayList<ModelProduk>
    private lateinit var listProdukOriginal: ArrayList<ModelProduk>
    private lateinit var produkAdapter: ProdukTransaksiAdapter
    private lateinit var keranjangAdapter: KeranjangTransaksiAdapter

    private val keranjangItems = ArrayList<ItemTransaksi>()
    private val handler = Handler(Looper.getMainLooper())
    private var running = true
    private var currentTransaksiId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupSearchListener()
        setupClickListeners()
        loadProdukFromFirebase()
        startRealTimeClock()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvJudul = findViewById(R.id.tvJudul)
        tvDateTime = findViewById(R.id.tvDateTime)
        etSearch = findViewById(R.id.etSearch)
        rvProduk = findViewById(R.id.rvProduk)
        rvKeranjang = findViewById(R.id.rvKeranjang)
        tvJumlahItem = findViewById(R.id.tvJumlahItem)
        tvTotal = findViewById(R.id.tvTotal)
        btnBayar = findViewById(R.id.btnBayar)
        bottomSheet = findViewById(R.id.bottomSheet)

        listProduk = ArrayList()
        listProdukOriginal = ArrayList()
        database = FirebaseDatabase.getInstance().getReference("produk")
    }

    private fun setupRecyclerView() {
        rvProduk.layoutManager = GridLayoutManager(this, 2)
        rvProduk.setHasFixedSize(true)
        produkAdapter = ProdukTransaksiAdapter(listProduk) { produk ->
            tambahKeKeranjang(produk)
        }
        rvProduk.adapter = produkAdapter

        rvKeranjang.layoutManager = LinearLayoutManager(this)
        keranjangAdapter = KeranjangTransaksiAdapter(keranjangItems,
            onItemChanged = { updateTotal() },
            onItemRemoved = { updateTotal() }
        )
        rvKeranjang.adapter = keranjangAdapter
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProduk(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterProduk(query: String) {
        val filteredList = ArrayList<ModelProduk>()
        if (query.isEmpty()) {
            filteredList.addAll(listProdukOriginal)
        } else {
            for (produk in listProdukOriginal) {
                if (produk.namaProduk.contains(query, ignoreCase = true)) {
                    filteredList.add(produk)
                }
            }
        }
        listProduk.clear()
        listProduk.addAll(filteredList)
        produkAdapter.notifyDataSetChanged()
    }

    private fun tambahKeKeranjang(produk: ModelProduk) {

        // Cek stok produk
        if (produk.stokProduk <= 0) {
            Toast.makeText(
                this,
                "Stok ${produk.namaProduk} habis!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Cari item yang sudah ada di keranjang
        val existingItem = keranjangItems.find {
            it.idProduk == produk.idProduk
        }

        // Jika item sudah ada
        if (existingItem != null) {

            // Cek stok mencukupi
            if (existingItem.jumlah >= produk.stokProduk) {
                Toast.makeText(
                    this,
                    "Stok tidak mencukupi!",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            // Tambah jumlah
            existingItem.jumlah++

            // Update subtotal
            existingItem.subtotal =
                existingItem.harga.toLong() * existingItem.jumlah

            // Refresh RecyclerView
            keranjangAdapter.notifyDataSetChanged()

        } else {

            // Tambahkan item baru
            val item = ItemTransaksi(
                idProduk = produk.idProduk,
                namaProduk = produk.namaProduk,
                harga = produk.hargaProduk,
                jumlah = 1,
                subtotal = produk.hargaProduk.toLong()
            )

            keranjangItems.add(item)

            // Notify item baru
            keranjangAdapter.notifyItemInserted(
                keranjangItems.size - 1
            )
        }

        // Update total harga
        updateTotal()
    }

    private fun updateTotal() {
        val total = keranjangItems.sumOf { it.subtotal }
        val jumlahItem = keranjangItems.sumOf { it.jumlah }

        tvTotal.text = formatRupiah(total)
        tvJumlahItem.text = "$jumlahItem item"
    }

    private fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        return "Rp ${formatter.format(amount)}"
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
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID"))
        tvDateTime.text = dateFormat.format(calendar.time)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnBayar.setOnClickListener {
            if (keranjangItems.isEmpty()) {
                Toast.makeText(this, "Keranjang masih kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPaymentDialog()
        }
    }

    private fun showPaymentDialog() {
        val total = keranjangItems.sumOf { it.subtotal }

        // Hitung pajak 10% dan total dengan pajak
        val pajak = (total * 0.1).toLong()
        val totalDenganPajak = total + pajak

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_pembayaran, null)
        val tvSubtotalDialog = view.findViewById<TextView>(R.id.tvSubtotalDialog)
        val tvPajakDialog = view.findViewById<TextView>(R.id.tvPajakDialog)
        val tvTotalDialog = view.findViewById<TextView>(R.id.tvTotalDialog)
        val etJumlahBayar = view.findViewById<EditText>(R.id.etJumlahBayar)
        val tvKembali = view.findViewById<TextView>(R.id.tvKembali)

        // Tampilkan rincian
        tvSubtotalDialog.text = formatRupiah(total)
        tvPajakDialog.text = formatRupiah(pajak)
        tvTotalDialog.text = formatRupiah(totalDenganPajak)

        etJumlahBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val bayar = s.toString().toLongOrNull() ?: 0
                val kembali = bayar - totalDenganPajak
                tvKembali.text = if (kembali >= 0) formatRupiah(kembali) else "Kurang!"
            }
        })

        AlertDialog.Builder(this)
            .setTitle("Pembayaran")
            .setView(view)
            .setPositiveButton("Selesai") { _, _ ->
                val bayar = etJumlahBayar.text.toString().toLongOrNull() ?: 0
                if (bayar >= totalDenganPajak) {
                    simpanTransaksi(total, bayar)
                } else {
                    Toast.makeText(this, "Jumlah bayar kurang!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun simpanTransaksi(total: Long, bayar: Long) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))

        val id = generateTransactionId()
        currentTransaksiId = id

        // Hitung pajak 10%
        val pajak = (total * 0.1).toLong()
        val totalDenganPajak = total + pajak

        val transaksi = ModelTransaksi(
            idTransaksi = id,
            tanggal = dateFormat.format(calendar.time),
            waktu = timeFormat.format(calendar.time),
            items = ArrayList(keranjangItems),
            subtotal = total,
            diskon = 0,
            pajak = pajak,
            total = totalDenganPajak,
            bayar = bayar,
            kembali = bayar - totalDenganPajak,
            pelangganId = "",
            pelangganNama = "Umum",
            kasir = "Admin",
            status = "selesai"
        )

        database.child("transaksi").child(id).setValue(transaksi)
            .addOnSuccessListener {
                // Kurangi stok produk
                for (item in keranjangItems) {
                    database.child("produk").child(item.idProduk).child("stokProduk")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val stokLama = snapshot.getValue(Int::class.java) ?: 0
                                val stokBaru = stokLama - item.jumlah
                                database.child("produk").child(item.idProduk).child("stokProduk")
                                    .setValue(stokBaru)
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                showReceiptDialog(transaksi)
                keranjangItems.clear()
                keranjangAdapter.notifyDataSetChanged()
                updateTotal()
                loadProdukFromFirebase()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk generate ID Transaksi unik
    private fun generateTransactionId(): String {
        val dateFormat = SimpleDateFormat("ddMMyyyyHHmmss", Locale("id", "ID"))
        val timestamp = dateFormat.format(Date())
        val random = (100..999).random()
        return "TRX${timestamp}${random}"
    }

    private fun showReceiptDialog(transaksi: ModelTransaksi) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_struk, null)

        val tvNoTransaksi = view.findViewById<TextView>(R.id.tvNoTransaksi)
        val tvTanggal = view.findViewById<TextView>(R.id.tvTanggal)
        val rvItemTransaksi = view.findViewById<RecyclerView>(R.id.rvItemTransaksi)
        val tvJumlahItemStruk = view.findViewById<TextView>(R.id.tvJumlahItemStruk)
        val tvSubtotal = view.findViewById<TextView>(R.id.tvSubtotal)
        val tvPajak = view.findViewById<TextView>(R.id.tvPajak)
        val tvTotalStruk = view.findViewById<TextView>(R.id.tvTotalStruk)
        val tvBayar = view.findViewById<TextView>(R.id.tvBayar)
        val tvKembaliStruk = view.findViewById<TextView>(R.id.tvKembaliStruk)
        val btnDownload = view.findViewById<MaterialButton>(R.id.btnDownload)

        tvNoTransaksi.text = transaksi.idTransaksi

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("id", "ID"))
        tvTanggal.text = "${dateFormat.format(Date())} WIB"

        val itemAdapter = ItemStrukAdapter(ArrayList(transaksi.items))
        rvItemTransaksi.layoutManager = LinearLayoutManager(this)
        rvItemTransaksi.adapter = itemAdapter

        val totalItem = transaksi.items.sumOf { it.jumlah }
        tvJumlahItemStruk.text = totalItem.toString()

        tvSubtotal.text = formatRupiahTanpaRp(transaksi.subtotal)
        tvPajak.text = formatRupiahTanpaRp(transaksi.pajak)
        tvTotalStruk.text = formatRupiahTanpaRp(transaksi.total)
        tvBayar.text = formatRupiahTanpaRp(transaksi.bayar)
        tvKembaliStruk.text = formatRupiahTanpaRp(transaksi.kembali)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(true)
            .create()

        btnDownload.setOnClickListener {
            captureReceiptAndShare(view)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun captureReceiptAndShare(view: View) {
        try {
            // Ambil layoutStruk yang berisi konten struk (tanpa tombol)
            val receiptView = view.findViewById<ScrollView>(R.id.layoutStruk)

            if (receiptView == null) {
                Toast.makeText(this, "Gagal menemukan layout struk", Toast.LENGTH_SHORT).show()
                return
            }

            val bitmap = Bitmap.createBitmap(receiptView.width, receiptView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            receiptView.draw(canvas)

            val file = File(cacheDir, "receipt_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Bagikan Struk"))
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal menyimpan struk: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiahTanpaRp(amount: Long): String {
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }

    private fun loadProdukFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduk.clear()
                listProdukOriginal.clear()

                Log.d("Transaksi", "Jumlah data dari Firebase: ${snapshot.childrenCount}")

                for (dataSnapshot in snapshot.children) {
                    val produk = dataSnapshot.getValue(ModelProduk::class.java)
                    Log.d("Transaksi", "Produk: ${produk?.namaProduk}, Status: ${produk?.statusProduk}")

                    if (produk != null && produk.statusProduk == "1") {
                        produk.idProduk = dataSnapshot.key ?: ""
                        if (produk.stokProduk > 0) {
                            listProduk.add(produk)
                        }
                        listProdukOriginal.add(produk)
                    }
                }

                Log.d("Transaksi", "List produk size: ${listProduk.size}")
                produkAdapter.notifyDataSetChanged()

                if (listProduk.isEmpty()) {
                    Toast.makeText(this@TransaksiActivity, "Tidak ada produk. Silakan tambah produk terlebih dahulu", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Transaksi", "Error: ${error.message}")
                Toast.makeText(this@TransaksiActivity, "Gagal memuat produk: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        handler.removeCallbacksAndMessages(null)
    }
}