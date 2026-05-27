package com.athalia.sellio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.R
import com.athalia.sellio.model.ModelKategori
import com.google.android.material.chip.Chip

class DetailAdapterKategori(private val kategoriList: List<ModelKategori>) :
    RecyclerView.Adapter<DetailAdapterKategori.KategoriViewHolder>() {

    lateinit var appContext: Context

    interface OnItemClickListener {
        fun onItemClick(kategori: ModelKategori)
        fun onEditClick(kategori: ModelKategori)
        fun onDeleteClick(kategori: ModelKategori)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_kategori, parent, false)

        appContext = parent.context
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val kategori = kategoriList[position]
        holder.bind(kategori)
    }

    override fun getItemCount(): Int = kategoriList.size

    inner class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvNamaKategori: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val btnEditKategori: ImageView = itemView.findViewById(R.id.btnEditKategori)
        val btnDeleteKategori: ImageView = itemView.findViewById(R.id.btnDeleteKategori)
        val imgKategori: ImageView = itemView.findViewById(R.id.imgKategori)

        fun bind(kategori: ModelKategori) {
            tvNamaKategori.text = kategori.namaKategori ?: ""

            // Set status chip berdasarkan statusKategori ("1" = aktif, "0" = tidak aktif)
            when (kategori.statusKategori) {
                "1" -> {
                    chipStatus.text = "Aktif"
                    chipStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                }
                "0" -> {
                    chipStatus.text = "Tidak Aktif"
                    chipStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))
                }
                else -> {
                    chipStatus.text = "Aktif"
                    chipStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                }
            }

            // Klik pada card
            itemView.setOnClickListener {
                listener?.onItemClick(kategori)
            }

            // Klik tombol edit
            btnEditKategori.setOnClickListener {
                listener?.onEditClick(kategori)
            }

            // Klik tombol delete
            btnDeleteKategori.setOnClickListener {
                listener?.onDeleteClick(kategori)
            }
        }
    }
}