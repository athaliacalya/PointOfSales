package com.athalia.sellio

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.model.ModelPelanggan
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*

class DataPelangganActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var tvJumlahPelanggan: TextView
    private lateinit var tilSearch: TextInputLayout
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvPelanggan: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var listPelanggan: ArrayList<ModelPelanggan>
    private lateinit var listPelangganOriginal: ArrayList<ModelPelanggan>
    private lateinit var adapter: PelangganAdapter
    private lateinit var database: DatabaseReference

    companion object {
        const val EXTRA_PELANGGAN_ID = "extra_pelanggan_id"
        const val EXTRA_PELANGGAN_NAMA = "extra_pelanggan_nama"
        const val EXTRA_PELANGGAN_NO_TELP = "extra_pelanggan_no_telp"
        const val EXTRA_PELANGGAN_EMAIL = "extra_pelanggan_email"
        const val EXTRA_PELANGGAN_ALAMAT = "extra_pelanggan_alamat"
        const val EXTRA_PELANGGAN_MEMBER_LEVEL = "extra_pelanggan_member_level"
        const val EXTRA_PELANGGAN_POIN = "extra_pelanggan_poin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pelanggan)

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
        tvJumlahPelanggan = findViewById(R.id.tvJumlahPelanggan)
        tilSearch = findViewById(R.id.tilSearch)
        etSearch = findViewById(R.id.etSearch)
        rvPelanggan = findViewById(R.id.rvDataPelanggan)
        fabAdd = findViewById(R.id.fabAdd)

        listPelanggan = ArrayList()
        listPelangganOriginal = ArrayList()

        database = FirebaseDatabase.getInstance().getReference("pelanggan")
    }

    private fun setupRecyclerView() {
        rvPelanggan.layoutManager = LinearLayoutManager(this)
        rvPelanggan.setHasFixedSize(true)

        adapter = PelangganAdapter(listPelanggan)

        adapter.setOnItemClickListener(object : PelangganAdapter.OnItemClickListener {
            override fun onItemClick(pelanggan: ModelPelanggan) {
                Toast.makeText(this@DataPelangganActivity, "${pelanggan.namaPelanggan}", Toast.LENGTH_SHORT).show()
            }

            override fun onEditClick(pelanggan: ModelPelanggan) {
                val intent = Intent(this@DataPelangganActivity, ModPelangganActivity::class.java)
                intent.putExtra(EXTRA_PELANGGAN_ID, pelanggan.idPelanggan)
                intent.putExtra(EXTRA_PELANGGAN_NAMA, pelanggan.namaPelanggan)
                intent.putExtra(EXTRA_PELANGGAN_NO_TELP, pelanggan.noTelp)
                intent.putExtra(EXTRA_PELANGGAN_EMAIL, pelanggan.email)
                intent.putExtra(EXTRA_PELANGGAN_ALAMAT, pelanggan.alamat)
                intent.putExtra(EXTRA_PELANGGAN_MEMBER_LEVEL, pelanggan.memberLevel)
                intent.putExtra(EXTRA_PELANGGAN_POIN, pelanggan.poin)
                startActivity(intent)
            }

            override fun onDeleteClick(pelanggan: ModelPelanggan) {
                AlertDialog.Builder(this@DataPelangganActivity)
                    .setTitle("Hapus Pelanggan")
                    .setMessage("Apakah Anda yakin ingin menghapus pelanggan ${pelanggan.namaPelanggan}?")
                    .setPositiveButton("Hapus") { _, _ ->
                        if (pelanggan.idPelanggan.isNotEmpty()) {
                            database.child(pelanggan.idPelanggan).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this@DataPelangganActivity, "Pelanggan ${pelanggan.namaPelanggan} berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    loadDataFromFirebase()
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(this@DataPelangganActivity, "Gagal menghapus: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        })

        rvPelanggan.adapter = adapter
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPelanggan(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterPelanggan(query: String) {
        val filteredList = ArrayList<ModelPelanggan>()

        if (query.isEmpty()) {
            filteredList.addAll(listPelangganOriginal)
        } else {
            for (pelanggan in listPelangganOriginal) {
                if (pelanggan.namaPelanggan.contains(query, ignoreCase = true)) {
                    filteredList.add(pelanggan)
                }
            }
        }

        listPelanggan.clear()
        listPelanggan.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        fabAdd.setOnClickListener {
            val intent = Intent(this, ModPelangganActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadDataFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPelanggan.clear()
                listPelangganOriginal.clear()

                for (dataSnapshot in snapshot.children) {
                    val pelanggan = dataSnapshot.getValue(ModelPelanggan::class.java)
                    if (pelanggan != null) {
                        pelanggan.idPelanggan = dataSnapshot.key ?: ""
                        listPelanggan.add(pelanggan)
                        listPelangganOriginal.add(pelanggan)
                    }
                }

                adapter.notifyDataSetChanged()
                updateJumlahPelanggan(listPelanggan.size)

                if (listPelanggan.isEmpty()) {
                    Toast.makeText(this@DataPelangganActivity, "Tidak ada data pelanggan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataPelangganActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateJumlahPelanggan(count: Int) {
        tvJumlahPelanggan.text = "$count orang"
    }

    override fun onResume() {
        super.onResume()
        loadDataFromFirebase()
    }
}