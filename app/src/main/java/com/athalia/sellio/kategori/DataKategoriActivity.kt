package com.athalia.sellio.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.R
import com.athalia.sellio.adapter.DetailAdapterKategori
import com.athalia.sellio.model.ModelKategori
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var listKategori: ArrayList<ModelKategori>
    private lateinit var adapter: DetailAdapterKategori
    private lateinit var database: DatabaseReference
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_kategori)

        init()
        loadData()

        fabAdd.setOnClickListener {
            val intent = Intent(this, ModKategoriActivity::class.java)
            startActivity(intent)
        }
    }

    private fun init() {
        recyclerView = findViewById(R.id.rvDataKategori)
        fabAdd = findViewById(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        listKategori = ArrayList()

        adapter = DetailAdapterKategori(listKategori)
        adapter.setOnItemClickListener(object : DetailAdapterKategori.OnItemClickListener {
            override fun onItemClick(kategori: ModelKategori) {
                Toast.makeText(this@DataKategoriActivity, "Klik: ${kategori.namaKategori}", Toast.LENGTH_SHORT).show()
            }

            override fun onEditClick(kategori: ModelKategori) {
                val intent = Intent(this@DataKategoriActivity, ModKategoriActivity::class.java)
                intent.putExtra(ModKategoriActivity.EXTRA_ID, kategori.idKategori)
                intent.putExtra(ModKategoriActivity.EXTRA_NAMA, kategori.namaKategori)
                intent.putExtra(ModKategoriActivity.EXTRA_STATUS, kategori.statusKategori)
                startActivity(intent)
            }

            override fun onDeleteClick(kategori: ModelKategori) {
                if (kategori.idKategori.isNotEmpty()) {
                    database.child(kategori.idKategori).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this@DataKategoriActivity, "Kategori ${kategori.namaKategori} berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this@DataKategoriActivity, "Gagal menghapus kategori: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@DataKategoriActivity, "ID kategori tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
        })

        recyclerView.adapter = adapter
        database = FirebaseDatabase.getInstance().getReference("kategori")
    }

    private fun loadData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKategori.clear()

                for (dataSnapshot in snapshot.children) {
                    val kategori = dataSnapshot.getValue(ModelKategori::class.java)
                    if (kategori != null) {
                        kategori.idKategori = dataSnapshot.key ?: ""
                        listKategori.add(kategori)
                    }
                }

                adapter.notifyDataSetChanged()

                if (listKategori.isEmpty()) {
                    Toast.makeText(this@DataKategoriActivity, "Tidak ada data kategori", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataKategoriActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}