package com.athalia.sellio

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.athalia.sellio.model.ModelAkun
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class activity_akun : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var tvEdit: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var tvNamaProfile: TextView
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etMobileNumber: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etDateOfBirth: TextInputEditText
    private lateinit var actGender: AutoCompleteTextView
    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnLogout: MaterialButton

    private lateinit var database: DatabaseReference
    private var isEditMode = false
    private var akunId: String = "1" // ID default untuk akun (karena single user)

    // Gender options
    private val genderList = arrayOf("Male", "Female", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_akun)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupGenderDropdown()
        setupDatePicker()
        loadDataFromFirebase()
        setupClickListeners()
        setEditMode(false)
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvJudul = findViewById(R.id.tvJudul)
        tvEdit = findViewById(R.id.tvEdit)
        imgProfile = findViewById(R.id.imgProfile)
        tvNamaProfile = findViewById(R.id.tvNamaProfile)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etEmail = findViewById(R.id.etEmail)
        etDateOfBirth = findViewById(R.id.etDateOfBirth)
        actGender = findViewById(R.id.actGender)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnLogout = findViewById(R.id.btnLogout)

        database = FirebaseDatabase.getInstance().getReference("akun")
    }

    private fun setupGenderDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderList)
        actGender.setAdapter(adapter)
        actGender.threshold = 1
    }

    private fun setupDatePicker() {
        etDateOfBirth.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val tanggal = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                etDateOfBirth.setText(tanggal)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        tvEdit.setOnClickListener {
            isEditMode = !isEditMode
            setEditMode(isEditMode)
        }

        btnSimpan.setOnClickListener {
            simpanData()
        }

        btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun setEditMode(edit: Boolean) {
        val isEnabled = edit

        etFirstName.isEnabled = isEnabled
        etLastName.isEnabled = isEnabled
        etMobileNumber.isEnabled = isEnabled
        etEmail.isEnabled = isEnabled
        etDateOfBirth.isEnabled = isEnabled
        actGender.isEnabled = isEnabled
        btnSimpan.visibility = if (edit) android.view.View.VISIBLE else android.view.View.GONE
        btnLogout.visibility = if (edit) android.view.View.GONE else android.view.View.VISIBLE
        tvEdit.text = if (edit) "Batal" else "Edit"

        if (!edit) {
            loadDataFromFirebase()
        }
    }

    private fun loadDataFromFirebase() {
        database.child(akunId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val akun = snapshot.getValue(ModelAkun::class.java)
                if (akun != null) {
                    // Isi data ke form
                    etFirstName.setText(akun.firstName)
                    etLastName.setText(akun.lastName)
                    etMobileNumber.setText(akun.mobileNumber)
                    etEmail.setText(akun.email)
                    etDateOfBirth.setText(akun.dateOfBirth)
                    actGender.setText(akun.gender, false)

                    // Update nama profile
                    val fullName = "${akun.firstName} ${akun.lastName}".trim()
                    tvNamaProfile.text = if (fullName.isNotEmpty()) fullName else "Pengguna"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@activity_akun, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun simpanData() {
        val firstName = etFirstName.text?.toString()?.trim() ?: ""
        val lastName = etLastName.text?.toString()?.trim() ?: ""
        val mobileNumber = etMobileNumber.text?.toString()?.trim() ?: ""
        val email = etEmail.text?.toString()?.trim() ?: ""
        val dateOfBirth = etDateOfBirth.text?.toString()?.trim() ?: ""
        val gender = actGender.text?.toString()?.trim() ?: ""

        // Validasi
        if (firstName.isEmpty()) {
            etFirstName.error = "Nama depan tidak boleh kosong"
            etFirstName.requestFocus()
            return
        }

        if (lastName.isEmpty()) {
            etLastName.error = "Nama belakang tidak boleh kosong"
            etLastName.requestFocus()
            return
        }

        if (mobileNumber.isEmpty()) {
            etMobileNumber.error = "Nomor telepon tidak boleh kosong"
            etMobileNumber.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email tidak boleh kosong"
            etEmail.requestFocus()
            return
        }

        if (dateOfBirth.isEmpty()) {
            etDateOfBirth.error = "Tanggal lahir tidak boleh kosong"
            etDateOfBirth.requestFocus()
            return
        }

        if (gender.isEmpty()) {
            actGender.error = "Gender harus dipilih"
            actGender.requestFocus()
            return
        }

        val akun = ModelAkun(
            idAkun = akunId,
            firstName = firstName,
            lastName = lastName,
            mobileNumber = mobileNumber,
            email = email,
            dateOfBirth = dateOfBirth,
            gender = gender,
            updatedAt = System.currentTimeMillis().toString()
        )

        database.child(akunId).setValue(akun)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                setEditMode(false)
                loadDataFromFirebase()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menyimpan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun performLogout() {
        val sharedPreferences = getSharedPreferences("SellioPreferences", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show()

        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(loginIntent)
        finish()
    }
}