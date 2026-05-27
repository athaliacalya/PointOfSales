package com.athalia.sellio.kategori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.R
import com.athalia.sellio.model.ModelKategori
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class KategoriAdapter(
    private val listKategori: List<ModelKategori>,
    private val onItemClick: (ModelKategori) -> Unit
) : RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder>() {

    class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardKategori: MaterialCardView = itemView.findViewById(R.id.cardKategori)
        val txtNamaKategori: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_kategori, parent, false)
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val kategori = listKategori[position]

        // Set data ke view
        holder.txtNamaKategori.text = kategori.namaKategori
        holder.chipStatus.text = kategori.statusKategori

        // Klik pada card
        holder.cardKategori.setOnClickListener {
            onItemClick(kategori)
        }
    }

    override fun getItemCount(): Int = listKategori.size
}