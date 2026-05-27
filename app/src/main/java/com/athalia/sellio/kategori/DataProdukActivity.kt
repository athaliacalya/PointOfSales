package com.athalia.sellio.kategori

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.R
import com.athalia.sellio.model.ModelProduk
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*

class DataProdukActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var tilSearch: TextInputLayout
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvDataProduk: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var listProduk: ArrayList<ModelProduk>
    private lateinit var listProdukOriginal: ArrayList<ModelProduk>
    private lateinit var adapter: ProdukAdapter
    private lateinit var database: DatabaseReference

    companion object {
        const val EXTRA_PRODUK_ID = "extra_produk_id"
        const val EXTRA_PRODUK_NAMA = "extra_produk_nama"
        const val EXTRA_PRODUK_HARGA = "extra_produk_harga"
        const val EXTRA_PRODUK_STOK = "extra_produk_stok"
        const val EXTRA_PRODUK_STATUS = "extra_produk_status"
        const val EXTRA_PRODUK_FOTO = "extra_produk_foto"
        const val EXTRA_PRODUK_KATEGORI_ID = "extra_produk_kategori_id"
        const val EXTRA_PRODUK_CABANG_ID = "extra_produk_cabang_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_produk)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupRecyclerView()
        setupSearchListener()
        setupClickListeners()
        loadDataFromFirebase()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvJudul = findViewById(R.id.tvJudul)
        tilSearch = findViewById(R.id.tilSearch)
        etSearch = findViewById(R.id.etSearch)
        rvDataProduk = findViewById(R.id.rvDataProduk)
        fabAdd = findViewById(R.id.fabAdd)

        listProduk = ArrayList()
        listProdukOriginal = ArrayList()

        database = FirebaseDatabase.getInstance().getReference("produk")
    }

    private fun setupRecyclerView() {
        rvDataProduk.layoutManager = LinearLayoutManager(this)
        rvDataProduk.setHasFixedSize(true)

        adapter = ProdukAdapter(listProduk)

        adapter.setOnItemClickListener(object : ProdukAdapter.OnItemClickListener {
            override fun onItemClick(produk: ModelProduk) {
                Toast.makeText(this@DataProdukActivity, "Klik: ${produk.namaProduk}", Toast.LENGTH_SHORT).show()
            }

            override fun onEditClick(produk: ModelProduk) {
                val intent = Intent(this@DataProdukActivity, ModProdukActivity::class.java)
                intent.putExtra(EXTRA_PRODUK_ID, produk.idProduk)
                intent.putExtra(EXTRA_PRODUK_NAMA, produk.namaProduk)
                intent.putExtra(EXTRA_PRODUK_HARGA, produk.hargaProduk)
                intent.putExtra(EXTRA_PRODUK_STOK, produk.stokProduk)
                intent.putExtra(EXTRA_PRODUK_STATUS, produk.statusProduk)
                intent.putExtra(EXTRA_PRODUK_FOTO, produk.fotoProduk)
                intent.putExtra(EXTRA_PRODUK_KATEGORI_ID, produk.idKategori)
                intent.putExtra(EXTRA_PRODUK_CABANG_ID, produk.idCabang)
                startActivity(intent)
            }

            override fun onDeleteClick(produk: ModelProduk) {
                if (produk.idProduk.isNotEmpty()) {
                    database.child(produk.idProduk).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this@DataProdukActivity, "Produk ${produk.namaProduk} berhasil dihapus", Toast.LENGTH_SHORT).show()
                            loadDataFromFirebase()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this@DataProdukActivity, "Gagal menghapus produk: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        })

        rvDataProduk.adapter = adapter
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
        adapter.notifyDataSetChanged()
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        fabAdd.setOnClickListener {
            try {
                // Cara 1: Intent biasa
                val intent = Intent(this, ModProdukActivity::class.java)
                startActivity(intent)

                // Cara 2: Intent dengan package lengkap (alternatif jika cara 1 tidak berhasil)
                // val intent = Intent(this, Class.forName("com.athalia.sellio.kategori.ModProdukActivity"))
                // startActivity(intent)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadDataFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduk.clear()
                listProdukOriginal.clear()

                for (dataSnapshot in snapshot.children) {
                    val produk = dataSnapshot.getValue(ModelProduk::class.java)
                    if (produk != null) {
                        produk.idProduk = dataSnapshot.key ?: ""
                        listProduk.add(produk)
                        listProdukOriginal.add(produk)
                    }
                }

                adapter.notifyDataSetChanged()

                if (listProduk.isEmpty()) {
                    Toast.makeText(this@DataProdukActivity, "Tidak ada data produk", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataProdukActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadDataFromFirebase()
    }
}