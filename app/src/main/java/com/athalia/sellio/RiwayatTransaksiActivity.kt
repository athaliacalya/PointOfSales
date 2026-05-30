package com.athalia.sellio

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.model.ModelTransaksi
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
import android.content.Intent
import android.app.AlertDialog
import java.text.NumberFormat
import java.util.Locale
import android.view.LayoutInflater
import android.view.View
import android.graphics.Bitmap
import android.graphics.Canvas
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider
import android.widget.ScrollView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat

class RiwayatTransaksiActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var tvJumlahTransaksi: TextView
    private lateinit var tilSearch: TextInputLayout
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvRiwayat: RecyclerView
    private lateinit var adapter: RiwayatTransaksiAdapter

    private lateinit var listTransaksi: ArrayList<ModelTransaksi>
    private lateinit var listTransaksiOriginal: ArrayList<ModelTransaksi>
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_riwayat_transaksi)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupSearchListener()
        setupClickListeners()
        loadDataFromFirebase()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvJudul = findViewById(R.id.tvJudul)
        tvJumlahTransaksi = findViewById(R.id.tvJumlahTransaksi)
        tilSearch = findViewById(R.id.tilSearch)
        etSearch = findViewById(R.id.etSearch)
        rvRiwayat = findViewById(R.id.rvRiwayatTransaksi)

        listTransaksi = ArrayList()
        listTransaksiOriginal = ArrayList()
        database = FirebaseDatabase.getInstance().getReference("transaksi")
    }

    private fun setupRecyclerView() {
        rvRiwayat.layoutManager = LinearLayoutManager(this)
        rvRiwayat.setHasFixedSize(true)

        adapter = RiwayatTransaksiAdapter(
            listTransaksi,
            onItemClick = { transaksi ->
                showReceiptDialog(transaksi)
            },
            onPrintClick = { transaksi ->
                printReceipt(transaksi)
            }
        )
        rvRiwayat.adapter = adapter
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTransaksi(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterTransaksi(query: String) {
        val filteredList = ArrayList<ModelTransaksi>()
        if (query.isEmpty()) {
            filteredList.addAll(listTransaksiOriginal)
        } else {
            for (transaksi in listTransaksiOriginal) {
                if (transaksi.idTransaksi.contains(query, ignoreCase = true)) {
                    filteredList.add(transaksi)
                }
            }
        }
        listTransaksi.clear()
        listTransaksi.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadDataFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listTransaksi.clear()
                listTransaksiOriginal.clear()

                Log.d("Riwayat", "Jumlah data dari Firebase: ${snapshot.childrenCount}")

                for (dataSnapshot in snapshot.children) {
                    val transaksi = dataSnapshot.getValue(ModelTransaksi::class.java)
                    if (transaksi != null) {
                        transaksi.idTransaksi = dataSnapshot.key ?: ""
                        listTransaksi.add(transaksi)
                        listTransaksiOriginal.add(transaksi)
                        Log.d("Riwayat", "Transaksi ditemukan: ${transaksi.idTransaksi}, Total: ${transaksi.total}")
                    } else {
                        Log.d("Riwayat", "Gagal parsing transaksi untuk key: ${dataSnapshot.key}")
                    }
                }

                // Urutkan dari yang terbaru
                listTransaksi.sortByDescending { it.tanggal + it.waktu }
                listTransaksiOriginal.sortByDescending { it.tanggal + it.waktu }

                adapter.notifyDataSetChanged()
                updateJumlahTransaksi(listTransaksi.size)

                if (listTransaksi.isEmpty()) {
                    Toast.makeText(this@RiwayatTransaksiActivity, "Belum ada transaksi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@RiwayatTransaksiActivity, "Ditemukan ${listTransaksi.size} transaksi", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Riwayat", "Error: ${error.message}")
                Toast.makeText(this@RiwayatTransaksiActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateJumlahTransaksi(count: Int) {
        tvJumlahTransaksi.text = "$count transaksi"
    }

    override fun onResume() {
        super.onResume()
        loadDataFromFirebase()
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

    private fun formatRupiahTanpaRp(amount: Long): String {
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }

    private fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        return "Rp ${formatter.format(amount)}"
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
                    val itemDetail = "  ${item.jumlah} x ${formatRupiah(item.harga.toLong())}"
                    val itemSubtotal = formatRupiah(item.subtotal)
                    stringBuilder.append("[L]${itemDetail}[R]${itemSubtotal}\n")
                }
                
                stringBuilder.append("[C]--------------------------------\n")
                stringBuilder.append("[L]SUBTOTAL[R]${formatRupiah(transaksi.subtotal)}\n")
                stringBuilder.append("[L]PAJAK (10%)[R]${formatRupiah(transaksi.pajak)}\n")
                stringBuilder.append("[C]--------------------------------\n")
                stringBuilder.append("[L]<b>TOTAL</b>[R]<b>${formatRupiah(transaksi.total)}</b>\n")
                stringBuilder.append("[L]BAYAR[R]${formatRupiah(transaksi.bayar)}\n")
                stringBuilder.append("[L]KEMBALI[R]${formatRupiah(transaksi.kembali)}\n")
                stringBuilder.append("[C]================================\n")
                stringBuilder.append("[C]Terima kasih sudah berbelanja\n")
                stringBuilder.append("[C]di Sellio\n\n\n\n")
                
                printer.printFormattedTextAndCut(stringBuilder.toString())
                
                runOnUiThread {
                    Toast.makeText(this@RiwayatTransaksiActivity, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: EscPosConnectionException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@RiwayatTransaksiActivity, "Gagal terhubung ke printer. Silakan hubungkan ulang.", Toast.LENGTH_LONG).show()
                    val sharedPreferences = getSharedPreferences("SellioPrinterPrefs", MODE_PRIVATE)
                    sharedPreferences.edit().remove("printer_mac").apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@RiwayatTransaksiActivity, "Error mencetak: ${e.message}", Toast.LENGTH_LONG).show()
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
            tvTanggal.text = "${transaksi.tanggal} ${transaksi.waktu}"

            val itemAdapter = ItemStrukAdapter(ArrayList(transaksi.items))
            rvItemTransaksi.layoutManager = LinearLayoutManager(this)
            rvItemTransaksi.adapter = itemAdapter

            val totalItem = transaksi.items.sumOf { it.jumlah }
            tvJumlahItemStruk.text = totalItem.toString()

            tvSubtotal.text = formatRupiah(transaksi.subtotal)
            tvPajak.text = formatRupiah(transaksi.pajak)
            tvTotalStruk.text = formatRupiah(transaksi.total)
            tvBayar.text = formatRupiah(transaksi.bayar)
            tvKembaliStruk.text = formatRupiah(transaksi.kembali)

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
}