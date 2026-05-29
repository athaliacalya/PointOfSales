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

        adapter = RiwayatTransaksiAdapter(listTransaksi) { transaksi ->
            Toast.makeText(this, "Transaksi: ${transaksi.idTransaksi}", Toast.LENGTH_SHORT).show()
        }
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
}