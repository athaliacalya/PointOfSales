package com.athalia.sellio.kategori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.R
import com.athalia.sellio.model.ModelPegawai

class PegawaiAdapter(
    private var pegawaiList: ArrayList<ModelPegawai>
) : RecyclerView.Adapter<PegawaiAdapter.PegawaiViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(pegawai: ModelPegawai)
        fun onEditClick(pegawai: ModelPegawai)
        fun onDeleteClick(pegawai: ModelPegawai)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun updateList(newList: ArrayList<ModelPegawai>) {
        pegawaiList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PegawaiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_pegawai, parent, false)
        return PegawaiViewHolder(view)
    }

    override fun onBindViewHolder(holder: PegawaiViewHolder, position: Int) {
        val pegawai = pegawaiList[position]
        holder.bind(pegawai)
    }

    override fun getItemCount(): Int = pegawaiList.size

    inner class PegawaiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imgAvatar: ImageView = itemView.findViewById(R.id.img_avatar)
        private val tvNamaPegawai: TextView = itemView.findViewById(R.id.tv_nama_pegawai)
        private val tvJabatan: TextView = itemView.findViewById(R.id.tv_jabatan)
        private val tvNoTelp: TextView = itemView.findViewById(R.id.tv_no_telp)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btn_delete)

        fun bind(pegawai: ModelPegawai) {
            // Set data ke view
            tvNamaPegawai.text = pegawai.namaPegawai.ifEmpty { "Pegawai" }
            tvJabatan.text = pegawai.jabatan.ifEmpty { "-" }
            tvNoTelp.text = pegawai.noTelp.ifEmpty { "-" }

            // Klik item
            itemView.setOnClickListener {
                listener?.onItemClick(pegawai)
            }

            // Tombol edit
            btnEdit.setOnClickListener {
                listener?.onEditClick(pegawai)
            }

            // Tombol delete
            btnDelete.setOnClickListener {
                listener?.onDeleteClick(pegawai)
            }
        }
    }
}