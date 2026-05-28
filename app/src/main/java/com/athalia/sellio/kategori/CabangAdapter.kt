package com.athalia.sellio.kategori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.R
import com.athalia.sellio.model.ModelCabang

class CabangAdapter(
    private var cabangList: ArrayList<ModelCabang>
) : RecyclerView.Adapter<CabangAdapter.CabangViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(cabang: ModelCabang)
        fun onEditClick(cabang: ModelCabang)
        fun onDeleteClick(cabang: ModelCabang)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun updateList(newList: ArrayList<ModelCabang>) {
        cabangList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CabangViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_cabang, parent, false)
        return CabangViewHolder(view)
    }

    override fun onBindViewHolder(holder: CabangViewHolder, position: Int) {
        val cabang = cabangList[position]
        holder.bind(cabang)
    }

    override fun getItemCount(): Int = cabangList.size

    inner class CabangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvNamaCabang: TextView = itemView.findViewById(R.id.tvNamaCabang)
        private val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btn_delete)

        fun bind(cabang: ModelCabang) {
            tvNamaCabang.text = cabang.namaCabang.ifEmpty { "Cabang" }
            tvKeterangan.text = cabang.keterangan.ifEmpty { "Tidak ada keterangan" }

            itemView.setOnClickListener {
                listener?.onItemClick(cabang)
            }

            btnEdit.setOnClickListener {
                listener?.onEditClick(cabang)
            }

            btnDelete.setOnClickListener {
                listener?.onDeleteClick(cabang)
            }
        }
    }
}