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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.athalia.sellio.model.ItemTransaksi
import com.athalia.sellio.model.ModelProduk
import com.athalia.sellio.model.ModelTransaksi
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
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

    private lateinit var produkRef: DatabaseReference
    private lateinit var transaksiRef: DatabaseReference
    private lateinit var listProduk: ArrayList<ModelProduk>
    private lateinit var listProdukOriginal: ArrayList<ModelProduk>
    private lateinit var produkAdapter: ProdukTransaksiAdapter
    private lateinit var keranjangAdapter: KeranjangTransaksiAdapter

    private val keranjangItems = ArrayList<ItemTransaksi>()
    private val handler = Handler(Looper.getMainLooper())
    private var running = true

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
        produkRef = FirebaseDatabase.getInstance().getReference("produk")
        transaksiRef = FirebaseDatabase.getInstance().getReference("transaksi")
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

    private fun formatRupiahTanpaRp(amount: Long): String {
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        return formatter.format(amount)
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
        val pajak = (total * 0.1).toLong()
        val totalDenganPajak = total + pajak

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_pembayaran, null)
        val tvSubtotalDialog = view.findViewById<TextView>(R.id.tvSubtotalDialog)
        val tvPajakDialog = view.findViewById<TextView>(R.id.tvPajakDialog)
        val tvTotalDialog = view.findViewById<TextView>(R.id.tvTotalDialog)
        val etJumlahBayar = view.findViewById<EditText>(R.id.etJumlahBayar)
        val tvKembali = view.findViewById<TextView>(R.id.tvKembali)

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

    private fun generateTransactionId(): String {
        val dateFormat = SimpleDateFormat("ddMMyyyyHHmmss", Locale("id", "ID"))
        val timestamp = dateFormat.format(Date())
        val random = (100..999).random()
        return "TRX${timestamp}${random}"
    }

    private fun simpanTransaksi(total: Long, bayar: Long) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))

        val id = generateTransactionId()
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

        transaksiRef.child(id).setValue(transaksi)
            .addOnSuccessListener {
                for (item in keranjangItems) {
                    produkRef.child(item.idProduk).child("stokProduk")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val stokLama = snapshot.getValue(Int::class.java) ?: 0
                                val stokBaru = stokLama - item.jumlah
                                produkRef.child(item.idProduk).child("stokProduk")
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
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menyimpan transaksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showReceiptDialog(transaksi: ModelTransaksi) {
        try {
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
            tvTanggal.text = dateFormat.format(Date())

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

            val btnPrint = view.findViewById<MaterialButton>(R.id.btnPrint)

            val dialog = AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Tutup") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            btnDownload.setOnClickListener {
                captureReceiptAndShare(view)
            }

            btnPrint.setOnClickListener {
                printReceipt(transaksi)
            }

            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menampilkan struk: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun captureReceiptAndShare(view: View) {
        try {
            val receiptView = view.findViewById<ScrollView>(R.id.layoutStruk) ?: view
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

    private fun loadProdukFromFirebase() {
        produkRef.addValueEventListener(object : ValueEventListener {
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

                if (listProduk.isEmpty()) {
                    Toast.makeText(this@TransaksiActivity, "Tidak ada produk. Silakan tambah produk terlebih dahulu", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransaksiActivity, "Gagal memuat produk: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private val PERMISSION_REQUEST_CODE = 1012

    private fun checkBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                PERMISSION_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin Bluetooth diberikan. Silakan coba cetak lagi.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin Bluetooth ditolak. Gagal mencetak struk.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun printReceipt(transaksi: ModelTransaksi) {
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions()
            return
        }

        val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Perangkat tidak mendukung Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Silakan aktifkan Bluetooth terlebih dahulu", Toast.LENGTH_LONG).show()
            val enableBtIntent = Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
            try {
                startActivity(enableBtIntent)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            return
        }

        val sharedPreferences = getSharedPreferences("SellioPrinterPrefs", MODE_PRIVATE)
        val savedMacAddress = sharedPreferences.getString("printer_mac", null)

        val pairedDevices = BluetoothPrintersConnections().list
        if (pairedDevices.isNullOrEmpty()) {
            showNoPrinterDialog()
            return
        }

        if (savedMacAddress != null) {
            val targetDevice = pairedDevices.find { it.device.address == savedMacAddress }
            if (targetDevice != null) {
                doPrint(targetDevice, transaksi)
                return
            }
        }

        showPrinterSelectionDialog(pairedDevices, transaksi)
    }

    private fun showPrinterSelectionDialog(pairedDevices: Array<BluetoothConnection>, transaksi: ModelTransaksi) {
        val printerNames = pairedDevices.map { 
            val name = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    "Printer"
                } else {
                    it.device.name ?: "Unknown Printer"
                }
            } catch (e: SecurityException) {
                "Printer"
            }
            "$name\n(${it.device.address})" 
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Printer Bluetooth")
            .setItems(printerNames) { _, which ->
                val selectedDevice = pairedDevices[which]
                
                val sharedPreferences = getSharedPreferences("SellioPrinterPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("printer_mac", selectedDevice.device.address).apply()
                
                doPrint(selectedDevice, transaksi)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showNoPrinterDialog() {
        AlertDialog.Builder(this)
            .setTitle("Printer Tidak Ditemukan")
            .setMessage("Tidak ada printer Bluetooth yang terpasang (paired).\n\n" +
                    "Langkah-langkah menghubungkan printer:\n" +
                    "1. Nyalakan printer Bluetooth Anda.\n" +
                    "2. Buka Pengaturan Bluetooth HP Anda dan pasangkan (pair) dengan printer tersebut (biasanya PIN: 0000 atau 1234).\n" +
                    "3. Setelah berhasil terpasang, kembali ke aplikasi Sellio dan coba cetak struk lagi.")
            .setPositiveButton("Buka Pengaturan Bluetooth") { _, _ ->
                try {
                    startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal membuka Pengaturan Bluetooth", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun doPrint(connection: BluetoothConnection, transaksi: ModelTransaksi) {
        Toast.makeText(this, "Menghubungkan ke printer...", Toast.LENGTH_SHORT).show()
        
        Thread {
            try {
                connection.connect()
                val printer = EscPosPrinter(connection, 203, 48f, 32)
                
                val stringBuilder = StringBuilder()
                stringBuilder.append("[C]<b>SELLIO</b>\n")
                stringBuilder.append("[C]Smart Daily Store\n")
                stringBuilder.append("[C]================================\n")
                stringBuilder.append("[L]NO TRX : ${transaksi.idTransaksi}\n")
                stringBuilder.append("[L]TGL    : ${transaksi.tanggal} ${transaksi.waktu}\n")
                stringBuilder.append("[L]KASIR  : ${transaksi.kasir.ifEmpty { "Admin" }}\n")
                stringBuilder.append("[L]PELANGGAN: ${transaksi.pelangganNama.ifEmpty { "Umum" }}\n")
                stringBuilder.append("[C]--------------------------------\n")
                
                for (item in transaksi.items) {
                    stringBuilder.append("[L]<b>${item.namaProduk}</b>\n")
                    val itemDetail = "  ${item.jumlah} x ${formatRupiahTanpaRp(item.harga.toLong())}"
                    val itemSubtotal = formatRupiahTanpaRp(item.subtotal)
                    stringBuilder.append("[L]${itemDetail}[R]${itemSubtotal}\n")
                }
                
                stringBuilder.append("[C]--------------------------------\n")
                stringBuilder.append("[L]SUBTOTAL[R]${formatRupiahTanpaRp(transaksi.subtotal)}\n")
                stringBuilder.append("[L]PAJAK (10%)[R]${formatRupiahTanpaRp(transaksi.pajak)}\n")
                stringBuilder.append("[C]--------------------------------\n")
                stringBuilder.append("[L]<b>TOTAL</b>[R]<b>${formatRupiahTanpaRp(transaksi.total)}</b>\n")
                stringBuilder.append("[L]BAYAR[R]${formatRupiahTanpaRp(transaksi.bayar)}\n")
                stringBuilder.append("[L]KEMBALI[R]${formatRupiahTanpaRp(transaksi.kembali)}\n")
                stringBuilder.append("[C]================================\n")
                stringBuilder.append("[C]Terima kasih sudah berbelanja\n")
                stringBuilder.append("[C]di Sellio\n\n\n\n")
                
                printer.printFormattedTextAndCut(stringBuilder.toString())
                
                runOnUiThread {
                    Toast.makeText(this@TransaksiActivity, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: EscPosConnectionException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@TransaksiActivity, "Gagal terhubung ke printer. Silakan hubungkan ulang.", Toast.LENGTH_LONG).show()
                    val sharedPreferences = getSharedPreferences("SellioPrinterPrefs", MODE_PRIVATE)
                    sharedPreferences.edit().remove("printer_mac").apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@TransaksiActivity, "Error mencetak: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                try {
                    connection.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        handler.removeCallbacksAndMessages(null)
    }
}