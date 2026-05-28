package com.athalia.sellio

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
import android.content.Intent

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
        if (produk.stokProduk <= 0) {
            Toast.makeText(this, "Stok ${produk.namaProduk} habis!", Toast.LENGTH_SHORT).show()
            return
        }

        val existingItem = keranjangItems.find { it.idProduk == produk.idProduk }

        if (existingItem != null) {
            if (existingItem.jumlah >= produk.stokProduk) {
                Toast.makeText(this, "Stok tidak mencukupi!", Toast.LENGTH_SHORT).show()
                return
            }
            existingItem.jumlah++
            existingItem.subtotal = existingItem.harga.toLong() * existingItem.jumlah
            keranjangAdapter.notifyDataSetChanged()
        } else {
            val item = ItemTransaksi(
                idProduk = produk.idProduk,
                namaProduk = produk.namaProduk,
                harga = produk.hargaProduk,
                jumlah = 1,
                subtotal = produk.hargaProduk.toLong()
            )
            keranjangItems.add(item)
            keranjangAdapter.notifyItemInserted(keranjangItems.size - 1)
        }
        updateTotal()

        bottomSheet.post {
            bottomSheet.scrollBy(0, bottomSheet.height)
        }
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
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_pembayaran, null)
        val etJumlahBayar = view.findViewById<EditText>(R.id.etJumlahBayar)
        val tvTotalDialog = view.findViewById<TextView>(R.id.tvTotalDialog)
        val tvKembali = view.findViewById<TextView>(R.id.tvKembali)

        tvTotalDialog.text = formatRupiah(total)

        etJumlahBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val bayar = s.toString().toLongOrNull() ?: 0
                val kembali = bayar - total
                tvKembali.text = if (kembali >= 0) formatRupiah(kembali) else "Kurang!"
            }
        })

        AlertDialog.Builder(this)
            .setTitle("Pembayaran")
            .setView(view)
            .setPositiveButton("Selesai") { _, _ ->
                val bayar = etJumlahBayar.text.toString().toLongOrNull() ?: 0
                if (bayar >= total) {
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
        val id = database.child("transaksi").push().key ?: ""
        currentTransaksiId = id

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

    private fun showReceiptDialog(transaksi: ModelTransaksi) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_struk, null)
        val tvNoTransaksi = view.findViewById<TextView>(R.id.tvNoTransaksi)
        val tvTanggal = view.findViewById<TextView>(R.id.tvTanggal)
        val rvItemTransaksi = view.findViewById<RecyclerView>(R.id.rvItemTransaksi)
        val tvJumlahItemStruk = view.findViewById<TextView>(R.id.tvJumlahItemStruk)
        val tvBayar = view.findViewById<TextView>(R.id.tvBayar)
        val tvKembaliStruk = view.findViewById<TextView>(R.id.tvKembaliStruk)
        val btnDownload = view.findViewById<MaterialButton>(R.id.btnDownload)
        val btnPrint = view.findViewById<MaterialButton>(R.id.btnPrint)

        val noTransaksi = transaksi.idTransaksi.takeLast(12)
        tvNoTransaksi.text = noTransaksi

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("id", "ID"))
        tvTanggal.text = "${dateFormat.format(Date())} WIB"

        val itemAdapter = ItemStrukAdapter(ArrayList(transaksi.items))
        rvItemTransaksi.layoutManager = LinearLayoutManager(this)
        rvItemTransaksi.adapter = itemAdapter

        val totalItem = transaksi.items.sumOf { it.jumlah }
        tvJumlahItemStruk.text = totalItem.toString()

        tvBayar.text = formatRupiahTanpaRp(transaksi.bayar)
        tvKembaliStruk.text = formatRupiahTanpaRp(transaksi.kembali)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(true)
            .create()

        btnDownload.setOnClickListener {
            captureReceiptAndShare(view)
        }

        btnPrint.setOnClickListener {
            Toast.makeText(this, "Fitur print akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun captureReceiptAndShare(view: View) {
        try {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            val file = File(cacheDir, "receipt_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
                for (dataSnapshot in snapshot.children) {
                    val produk = dataSnapshot.getValue(ModelProduk::class.java)
                    if (produk != null && produk.statusProduk == "1") {
                        produk.idProduk = dataSnapshot.key ?: ""
                        if (produk.stokProduk > 0) {
                            listProduk.add(produk)
                        }
                        listProdukOriginal.add(produk)
                    }
                }
                produkAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransaksiActivity, "Gagal memuat produk", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        handler.removeCallbacksAndMessages(null)
    }
}