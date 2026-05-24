package com.athalia.sellio.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.R
import com.athalia.sellio.model.ModelKategori
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var listKategori: ArrayList<ModelKategori>
    private lateinit var adapter: KategoriAdapter
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
        recyclerView = findViewById(R.id.rvData_Kategori)
        fabAdd = findViewById(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        listKategori = ArrayList()
        
        adapter = KategoriAdapter(
            listKategori = listKategori,
            onItemClick = { kategori ->

                Toast.makeText(this, "${kategori.namaKategori}", Toast.LENGTH_SHORT).show()

            }
        )

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
                        listKategori.add(kategori)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataKategoriActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}