package com.athalia.sellio.kategori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.athalia.sellio.R
import com.athalia.sellio.model.ModelCabang
import com.athalia.sellio.model.ModelKategori
import com.athalia.sellio.model.ModelProduk
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

class ModProdukActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var etNamaProduk: TextInputEditText
    private lateinit var etFotoUrl: TextInputEditText
    private lateinit var imgPreview: ImageView
    private lateinit var tvFotoEmpty: TextView
    private lateinit var etHargaJual: TextInputEditText
    private lateinit var etHargaModal: TextInputEditText
    private lateinit var etStok: TextInputEditText
    private lateinit var actCabang: AutoCompleteTextView
    private lateinit var actKategori: AutoCompleteTextView
    private lateinit var rgStatus: RadioGroup
    private lateinit var rbTersedia: RadioButton
    private lateinit var rbHabis: RadioButton
    private lateinit var btnUpdateMenu: MaterialButton
    private lateinit var llSelectedCabang: LinearLayout

    private lateinit var database: DatabaseReference
    private var produkId: String? = null
    private var isEditMode = false

    private lateinit var listCabang: ArrayList<ModelCabang>
    private lateinit var listKategori: ArrayList<ModelKategori>
    private lateinit var cabangNames: ArrayList<String>
    private lateinit var kategoriNames: ArrayList<String>

    private val selectedCabangIds = ArrayList<String>()
    private val selectedCabangNames = ArrayList<String>()

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
        setContentView(R.layout.activity_mod_produk)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        getIntentData()
        setupDropdowns()
        setupListeners()
        updateUIForMode()
        loadDropdownData()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        etNamaProduk = findViewById(R.id.etNamaProduk)
        etFotoUrl = findViewById(R.id.etFotoUrl)
        imgPreview = findViewById(R.id.imgPreview)
        tvFotoEmpty = findViewById(R.id.tvFotoEmpty)
        etHargaJual = findViewById(R.id.etHargaJual)
        etHargaModal = findViewById(R.id.etHargaModal)
        etStok = findViewById(R.id.etStok)
        actCabang = findViewById(R.id.actCabang)
        actKategori = findViewById(R.id.actKategori)
        rgStatus = findViewById(R.id.rgStatus)
        rbTersedia = findViewById(R.id.rbTersedia)
        rbHabis = findViewById(R.id.rbHabis)
        btnUpdateMenu = findViewById(R.id.btnUpdateMenu)
        llSelectedCabang = findViewById(R.id.llSelectedCabang)

        database = FirebaseDatabase.getInstance().reference

        listCabang = ArrayList()
        listKategori = ArrayList()
        cabangNames = ArrayList()
        kategoriNames = ArrayList()
    }

    private fun getIntentData() {
        produkId = intent.getStringExtra(EXTRA_PRODUK_ID)

        if (produkId != null && produkId!!.isNotEmpty()) {
            isEditMode = true
            tvTitle.text = "Edit Menu"

            etNamaProduk.setText(intent.getStringExtra(EXTRA_PRODUK_NAMA) ?: "")
            etHargaJual.setText(intent.getIntExtra(EXTRA_PRODUK_HARGA, 0).toString())
            etStok.setText(intent.getIntExtra(EXTRA_PRODUK_STOK, 0).toString())
            etFotoUrl.setText(intent.getStringExtra(EXTRA_PRODUK_FOTO) ?: "")

            val fotoUrl = intent.getStringExtra(EXTRA_PRODUK_FOTO)
            if (!fotoUrl.isNullOrEmpty()) {
                loadImagePreview(fotoUrl)
            }

            val status = intent.getStringExtra(EXTRA_PRODUK_STATUS) ?: "1"
            if (status == "1") {
                rbTersedia.isChecked = true
            } else {
                rbHabis.isChecked = true
            }
        } else {
            isEditMode = false
            tvTitle.text = "Tambah Menu"
            rbTersedia.isChecked = true
        }
    }

    private fun setupDropdowns() {
        actCabang.setOnItemClickListener { _, _, position, _ ->
            val selected = cabangNames[position]
            if (!selectedCabangNames.contains(selected)) {
                val cabang = listCabang.find { it.namaCabang == selected }
                cabang?.let {
                    selectedCabangIds.add(it.idCabang)
                    selectedCabangNames.add(it.namaCabang ?: "")
                    addChipForCabang(it.namaCabang ?: "")
                }
            }
            actCabang.setText("")
        }
    }

    private fun addChipForCabang(cabangName: String) {
        val chip = Chip(this).apply {
            text = cabangName
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                val index = selectedCabangNames.indexOf(cabangName)
                if (index >= 0) {
                    selectedCabangNames.removeAt(index)
                    selectedCabangIds.removeAt(index)
                    llSelectedCabang.removeView(this)
                }
                if (selectedCabangNames.isEmpty()) {
                    llSelectedCabang.visibility = View.GONE
                }
            }
        }
        llSelectedCabang.addView(chip)
        llSelectedCabang.visibility = View.VISIBLE
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        etFotoUrl.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                s?.toString()?.let { url ->
                    if (url.isNotEmpty()) {
                        loadImagePreview(url)
                    } else {
                        imgPreview.setImageResource(R.drawable.ic_image_placeholder)
                        tvFotoEmpty.visibility = View.VISIBLE
                    }
                }
            }
        })

        btnUpdateMenu.setOnClickListener { saveProduk() }
    }

    private fun loadImagePreview(url: String) {
        try {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_error)
                .into(imgPreview)
            imgPreview.visibility = View.VISIBLE
            tvFotoEmpty.visibility = View.GONE
        } catch (e: Exception) {
            imgPreview.setImageResource(R.drawable.ic_image_error)
            tvFotoEmpty.visibility = View.GONE
        }
    }

    private fun updateUIForMode() {
        btnUpdateMenu.text = if (isEditMode) "Update Menu" else "Simpan Menu"
    }

    private fun loadDropdownData() {
        database.child("cabang").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                cabangNames.clear()
                for (dataSnapshot in snapshot.children) {
                    val cabang = dataSnapshot.getValue(ModelCabang::class.java)
                    if (cabang != null) {
                        cabang.idCabang = dataSnapshot.key ?: ""
                        listCabang.add(cabang)
                        cabang.namaCabang?.let { cabangNames.add(it) }
                    }
                }
                actCabang.setAdapter(ArrayAdapter(this@ModProdukActivity, android.R.layout.simple_dropdown_item_1line, cabangNames))
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ModProdukActivity, "Gagal load cabang: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        database.child("kategori").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKategori.clear()
                kategoriNames.clear()
                for (dataSnapshot in snapshot.children) {
                    val kategori = dataSnapshot.getValue(ModelKategori::class.java)
                    if (kategori != null) {
                        kategori.idKategori = dataSnapshot.key ?: ""
                        listKategori.add(kategori)
                        kategori.namaKategori?.let { kategoriNames.add(it) }
                    }
                }
                actKategori.setAdapter(ArrayAdapter(this@ModProdukActivity, android.R.layout.simple_dropdown_item_1line, kategoriNames))

                if (isEditMode) {
                    val kategoriId = intent.getStringExtra(EXTRA_PRODUK_KATEGORI_ID) ?: ""
                    if (kategoriId.isNotEmpty()) {
                        val selectedKategori = listKategori.find { it.idKategori == kategoriId }
                        selectedKategori?.let { actKategori.setText(it.namaKategori, false) }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ModProdukActivity, "Gagal load kategori: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProduk() {
        val namaProduk = etNamaProduk.text.toString().trim()
        val fotoUrl = etFotoUrl.text.toString().trim()
        val hargaJual = etHargaJual.text.toString().trim()
        val stok = etStok.text.toString().trim()
        val kategoriTerpilih = actKategori.text.toString().trim()

        if (namaProduk.isEmpty()) {
            etNamaProduk.error = "Nama produk tidak boleh kosong"
            etNamaProduk.requestFocus()
            return
        }

        if (hargaJual.isEmpty()) {
            etHargaJual.error = "Harga jual tidak boleh kosong"
            etHargaJual.requestFocus()
            return
        }

        if (stok.isEmpty()) {
            etStok.error = "Stok tidak boleh kosong"
            etStok.requestFocus()
            return
        }

        var idKategori = ""
        for (kategori in listKategori) {
            if (kategori.namaKategori == kategoriTerpilih) {
                idKategori = kategori.idKategori
                break
            }
        }

        val status = if (rbTersedia.isChecked) "1" else "0"
        val cabangIds = selectedCabangIds.joinToString(",")

        val produkRef = if (isEditMode && produkId != null) {
            database.child("produk").child(produkId!!)
        } else {
            database.child("produk").push()
        }

        val newProdukId = if (isEditMode) produkId else produkRef.key

        val produk = ModelProduk(
            idProduk = newProdukId ?: "",
            namaProduk = namaProduk,
            hargaProduk = hargaJual.toIntOrNull() ?: 0,
            idKategori = idKategori,
            idCabang = cabangIds,
            fotoProduk = fotoUrl,
            stokProduk = stok.toIntOrNull() ?: 0,
            statusProduk = status,
            updatedAt = System.currentTimeMillis().toString()
        )

        if (!isEditMode) {
            produk.createdAt = System.currentTimeMillis().toString()
        }

        produkRef.setValue(produk)
            .addOnSuccessListener {
                Toast.makeText(this, if (isEditMode) "Menu berhasil diupdate" else "Menu berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK, Intent())
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menyimpan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}