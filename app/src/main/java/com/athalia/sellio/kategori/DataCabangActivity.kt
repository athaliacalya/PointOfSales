package com.athalia.sellio.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.R
import com.athalia.sellio.model.ModelCabang
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

class DataCabangActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var etNamaCabang: TextInputEditText
    private lateinit var etKeterangan: TextInputEditText
    private lateinit var btnTambahCabang: MaterialButton
    private lateinit var rvDataCabang: RecyclerView

    private lateinit var listCabang: ArrayList<ModelCabang>
    private lateinit var adapter: CabangAdapter
    private lateinit var database: DatabaseReference

    companion object {
        const val EXTRA_CABANG_ID = "extra_cabang_id"
        const val EXTRA_CABANG_NAMA = "extra_cabang_nama"
        const val EXTRA_CABANG_KETERANGAN = "extra_cabang_keterangan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_cabang)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupRecyclerView()
        setupClickListeners()
        loadDataFromFirebase()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvJudul = findViewById(R.id.tvJudul)
        etNamaCabang = findViewById(R.id.etNamaCabang)
        etKeterangan = findViewById(R.id.etKeterangan)
        btnTambahCabang = findViewById(R.id.btnTambahCabang)
        rvDataCabang = findViewById(R.id.rvDataCabang)

        listCabang = ArrayList()
        database = FirebaseDatabase.getInstance().getReference("cabang")
    }

    private fun setupRecyclerView() {
        rvDataCabang.layoutManager = LinearLayoutManager(this)
        rvDataCabang.setHasFixedSize(true)

        adapter = CabangAdapter(listCabang)

        adapter.setOnItemClickListener(object : CabangAdapter.OnItemClickListener {
            override fun onItemClick(cabang: ModelCabang) {
                Toast.makeText(this@DataCabangActivity, "${cabang.namaCabang}", Toast.LENGTH_SHORT).show()
            }

            override fun onEditClick(cabang: ModelCabang) {
                val intent = Intent(this@DataCabangActivity, ModCabangActivity::class.java)
                intent.putExtra(EXTRA_CABANG_ID, cabang.idCabang)
                intent.putExtra(EXTRA_CABANG_NAMA, cabang.namaCabang)
                intent.putExtra(EXTRA_CABANG_KETERANGAN, cabang.keterangan)
                startActivity(intent)
            }

            override fun onDeleteClick(cabang: ModelCabang) {
                AlertDialog.Builder(this@DataCabangActivity)
                    .setTitle("Hapus Cabang")
                    .setMessage("Apakah Anda yakin ingin menghapus cabang ${cabang.namaCabang}?")
                    .setPositiveButton("Hapus") { _, _ ->
                        if (cabang.idCabang.isNotEmpty()) {
                            database.child(cabang.idCabang).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this@DataCabangActivity, "Cabang ${cabang.namaCabang} berhasil dihapus", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(this@DataCabangActivity, "Gagal menghapus: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        })

        rvDataCabang.adapter = adapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnTambahCabang.setOnClickListener {
            tambahCabang()
        }
    }

    private fun tambahCabang() {
        // Perbaiki: Gunakan .toString() untuk mengatasi nullable
        val namaCabang = etNamaCabang.text?.toString()?.trim() ?: ""
        val keterangan = etKeterangan.text?.toString()?.trim() ?: ""

        if (namaCabang.isEmpty()) {
            etNamaCabang.error = "Nama cabang tidak boleh kosong"
            etNamaCabang.requestFocus()
            return
        }

        val id = database.push().key

        if (id != null) {
            val cabang = ModelCabang(
                idCabang = id,
                namaCabang = namaCabang,
                keterangan = keterangan,
                status = "1"
            )

            database.child(id).setValue(cabang)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cabang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    // Clear form
                    etNamaCabang.text?.clear()
                    etKeterangan.text?.clear()
                    etNamaCabang.requestFocus()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Gagal menambahkan cabang: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadDataFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()

                for (dataSnapshot in snapshot.children) {
                    val cabang = dataSnapshot.getValue(ModelCabang::class.java)
                    if (cabang != null) {
                        cabang.idCabang = dataSnapshot.key ?: ""
                        listCabang.add(cabang)
                    }
                }

                adapter.notifyDataSetChanged()

                if (listCabang.isEmpty()) {
                    Toast.makeText(this@DataCabangActivity, "Tidak ada data cabang", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataCabangActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadDataFromFirebase()
    }
}