package com.athalia_calya.pos.kategori

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
import com.athalia_calya.pos.R
import com.athalia_calya.pos.model.ModelPegawai
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*


class DataPegawaiActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var tvJumlahPegawai: TextView
    private lateinit var tilSearch: TextInputLayout
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvDataPegawai: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var listPegawai: ArrayList<ModelPegawai>
    private lateinit var listPegawaiOriginal: ArrayList<ModelPegawai>
    private lateinit var adapter: PegawaiAdapter  // Langsung pakai, satu package
    private lateinit var database: DatabaseReference

    companion object {
        const val EXTRA_PEGAWAI_ID = "extra_pegawai_id"
        const val EXTRA_PEGAWAI_NAMA = "extra_pegawai_nama"
        const val EXTRA_PEGAWAI_TEMPAT_LAHIR = "extra_pegawai_tempat_lahir"
        const val EXTRA_PEGAWAI_TANGGAL_LAHIR = "extra_pegawai_tanggal_lahir"
        const val EXTRA_PEGAWAI_NO_TELP = "extra_pegawai_no_telp"
        const val EXTRA_PEGAWAI_JABATAN = "extra_pegawai_jabatan"
        const val EXTRA_PEGAWAI_ALAMAT = "extra_pegawai_alamat"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pegawai)

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
        tvJumlahPegawai = findViewById(R.id.tvJumlahPegawai)
        tilSearch = findViewById(R.id.tilSearch)
        etSearch = findViewById(R.id.etSearch)
        rvDataPegawai = findViewById(R.id.rvDataPegawai)
        fabAdd = findViewById(R.id.fabAdd)

        listPegawai = ArrayList()
        listPegawaiOriginal = ArrayList()

        database = FirebaseDatabase.getInstance().getReference("pegawai")
    }

    private fun setupRecyclerView() {
        rvDataPegawai.layoutManager = LinearLayoutManager(this)
        rvDataPegawai.setHasFixedSize(true)

        adapter = PegawaiAdapter(listPegawai)

        adapter.setOnItemClickListener(object : PegawaiAdapter.OnItemClickListener {
            override fun onItemClick(pegawai: ModelPegawai) {
                Toast.makeText(this@DataPegawaiActivity, "${pegawai.namaPegawai}", Toast.LENGTH_SHORT).show()
            }

            override fun onEditClick(pegawai: ModelPegawai) {
                val intent = Intent(this@DataPegawaiActivity, ModPegawaiActivity::class.java)
                intent.putExtra(EXTRA_PEGAWAI_ID, pegawai.idPegawai)
                intent.putExtra(EXTRA_PEGAWAI_NAMA, pegawai.namaPegawai)
                intent.putExtra(EXTRA_PEGAWAI_TEMPAT_LAHIR, pegawai.tempatLahir)
                intent.putExtra(EXTRA_PEGAWAI_TANGGAL_LAHIR, pegawai.tanggalLahir)
                intent.putExtra(EXTRA_PEGAWAI_NO_TELP, pegawai.noTelp)
                intent.putExtra(EXTRA_PEGAWAI_JABATAN, pegawai.jabatan)
                intent.putExtra(EXTRA_PEGAWAI_ALAMAT, pegawai.alamat)
                startActivity(intent)
            }

            override fun onDeleteClick(pegawai: ModelPegawai) {
                AlertDialog.Builder(this@DataPegawaiActivity)
                    .setTitle("Hapus Pegawai")
                    .setMessage("Apakah Anda yakin ingin menghapus pegawai ${pegawai.namaPegawai}?")
                    .setPositiveButton("Hapus") { _, _ ->
                        if (pegawai.idPegawai.isNotEmpty()) {
                            database.child(pegawai.idPegawai).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this@DataPegawaiActivity, "Pegawai ${pegawai.namaPegawai} berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    loadDataFromFirebase()
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(this@DataPegawaiActivity, "Gagal menghapus: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        })

        rvDataPegawai.adapter = adapter
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPegawai(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterPegawai(query: String) {
        val filteredList = ArrayList<ModelPegawai>()

        if (query.isEmpty()) {
            filteredList.addAll(listPegawaiOriginal)
        } else {
            for (pegawai in listPegawaiOriginal) {
                if (pegawai.namaPegawai.contains(query, ignoreCase = true)) {
                    filteredList.add(pegawai)
                }
            }
        }

        listPegawai.clear()
        listPegawai.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        fabAdd.setOnClickListener {
            val intent = Intent(this, ModPegawaiActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadDataFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPegawai.clear()
                listPegawaiOriginal.clear()

                for (dataSnapshot in snapshot.children) {
                    val pegawai = dataSnapshot.getValue(ModelPegawai::class.java)
                    if (pegawai != null) {
                        pegawai.idPegawai = dataSnapshot.key ?: ""
                        listPegawai.add(pegawai)
                        listPegawaiOriginal.add(pegawai)
                    }
                }

                adapter.notifyDataSetChanged()
                updateJumlahPegawai(listPegawai.size)

                if (listPegawai.isEmpty()) {
                    Toast.makeText(this@DataPegawaiActivity, "Tidak ada data pegawai", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataPegawaiActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateJumlahPegawai(count: Int) {
        tvJumlahPegawai.text = "$count orang"
    }

    override fun onResume() {
        super.onResume()
        loadDataFromFirebase()
    }
}
