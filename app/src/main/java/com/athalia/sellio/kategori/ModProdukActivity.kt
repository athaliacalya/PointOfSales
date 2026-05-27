package com.athalia.sellio.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.athalia.sellio.R
import com.athalia.sellio.model.ModelCabang
import com.athalia.sellio.model.ModelKategori
import com.athalia.sellio.model.ModelProduk
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*

class ModProdukActivity : AppCompatActivity() {

    // Deklarasi view
    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var etNamaProduk: TextInputEditText
    private lateinit var etFotoUrl: TextInputEditText
    private lateinit var imgPreview: ImageView
    private lateinit var etHargaJual: TextInputEditText
    private lateinit var etHargaModal: TextInputEditText
    private lateinit var etStok: TextInputEditText
    private lateinit var actCabang: AutoCompleteTextView
    private lateinit var actKategori: AutoCompleteTextView
    private lateinit var rgStatus: RadioGroup
    private lateinit var rbTersedia: RadioButton
    private lateinit var rbHabis: RadioButton
    private lateinit var btnUpdateMenu: MaterialButton

    // Card untuk status
    private lateinit var cardTersedia: MaterialCardView
    private lateinit var cardHabis: MaterialCardView

    // Firebase
    private lateinit var database: DatabaseReference

    // Data
    private var produkId: String? = null
    private var isEditMode = false

    // List untuk dropdown
    private lateinit var listCabang: ArrayList<ModelCabang>
    private lateinit var listKategori: ArrayList<ModelKategori>
    private lateinit var cabangNames: ArrayList<String>
    private lateinit var kategoriNames: ArrayList<String>

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
        etHargaJual = findViewById(R.id.etHargaJual)
        etHargaModal = findViewById(R.id.etHargaModal)
        etStok = findViewById(R.id.etStok)
        actCabang = findViewById(R.id.actCabang)
        actKategori = findViewById(R.id.actKategori)
        rgStatus = findViewById(R.id.rgStatus)
        rbTersedia = findViewById(R.id.rbTersedia)
        rbHabis = findViewById(R.id.rbHabis)
        btnUpdateMenu = findViewById(R.id.btnUpdateMenu)

        cardTersedia = rbTersedia.parent.parent as MaterialCardView
        cardHabis = rbHabis.parent.parent as MaterialCardView

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
                updateStatusColor(true)
            } else {
                rbHabis.isChecked = true
                updateStatusColor(false)
            }
        } else {
            isEditMode = false
            tvTitle.text = "Tambah Menu"
            rbTersedia.isChecked = true
            updateStatusColor(true)
        }
    }

    private fun setupDropdowns() {
        val cabangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cabangNames)
        actCabang.setAdapter(cabangAdapter)
        actCabang.threshold = 1

        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategoriNames)
        actKategori.setAdapter(kategoriAdapter)
        actKategori.threshold = 1
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        etFotoUrl.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                s?.toString()?.let { url ->
                    if (url.isNotEmpty()) {
                        loadImagePreview(url)
                    } else {
                        imgPreview.visibility = android.view.View.GONE
                    }
                }
            }
        })

        rgStatus.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTersedia -> updateStatusColor(true)
                R.id.rbHabis -> updateStatusColor(false)
            }
        }

        btnUpdateMenu.setOnClickListener {
            saveProduk()
        }
    }

    private fun updateStatusColor(isTersedia: Boolean) {
        if (isTersedia) {
            cardTersedia.strokeColor = ContextCompat.getColor(this, R.color.green_500)
            cardTersedia.strokeWidth = 2
            cardHabis.strokeColor = ContextCompat.getColor(this, R.color.gray_300)
            cardHabis.strokeWidth = 1

            rbTersedia.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.ic_check_circle_green),
                null, null, null
            )
            rbHabis.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.ic_check_circle_gray),
                null, null, null
            )
        } else {
            cardHabis.strokeColor = ContextCompat.getColor(this, R.color.red_500)
            cardHabis.strokeWidth = 2
            cardTersedia.strokeColor = ContextCompat.getColor(this, R.color.gray_300)
            cardTersedia.strokeWidth = 1

            rbHabis.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.ic_cancel_red),
                null, null, null
            )
            rbTersedia.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.ic_check_circle_gray),
                null, null, null
            )
        }
    }

    private fun updateUIForMode() {
        btnUpdateMenu.text = if (isEditMode) "Update Menu" else "Simpan Menu"
    }

    private fun loadImagePreview(url: String) {
        imgPreview.setImageResource(R.drawable.ic_image_placeholder)
        imgPreview.visibility = android.view.View.VISIBLE
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

                val cabangAdapter = ArrayAdapter(this@ModProdukActivity, android.R.layout.simple_dropdown_item_1line, cabangNames)
                actCabang.setAdapter(cabangAdapter)
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

                val kategoriAdapter = ArrayAdapter(this@ModProdukActivity, android.R.layout.simple_dropdown_item_1line, kategoriNames)
                actKategori.setAdapter(kategoriAdapter)
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
        val cabangTerpilih = actCabang.text.toString().trim()
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

        var idCabang = ""
        for (cabang in listCabang) {
            if (cabang.namaCabang == cabangTerpilih) {
                idCabang = cabang.idCabang
                break
            }
        }

        var idKategori = ""
        for (kategori in listKategori) {
            if (kategori.namaKategori == kategoriTerpilih) {
                idKategori = kategori.idKategori
                break
            }
        }

        val status = if (rbTersedia.isChecked) "1" else "0"

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
            idCabang = idCabang,
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
                val message = if (isEditMode) "Menu berhasil diupdate" else "Menu berhasil ditambahkan"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                val resultIntent = Intent()
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menyimpan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}