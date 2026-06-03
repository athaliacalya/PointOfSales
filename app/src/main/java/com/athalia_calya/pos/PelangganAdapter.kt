package com.athalia_calya.pos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.athalia_calya.pos.model.ModelPelanggan

class PelangganAdapter(
    private var pelangganList: ArrayList<ModelPelanggan>
) : RecyclerView.Adapter<PelangganAdapter.PelangganViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(pelanggan: ModelPelanggan)
        fun onEditClick(pelanggan: ModelPelanggan)
        fun onDeleteClick(pelanggan: ModelPelanggan)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun updateList(newList: ArrayList<ModelPelanggan>) {
        pelangganList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelangganViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_pelanggan, parent, false)
        return PelangganViewHolder(view)
    }

    override fun onBindViewHolder(holder: PelangganViewHolder, position: Int) {
        val pelanggan = pelangganList[position]
        holder.bind(pelanggan)
    }

    override fun getItemCount(): Int = pelangganList.size

    inner class PelangganViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imgAvatar: ImageView = itemView.findViewById(R.id.img_avatar)
        private val tvNamaPelanggan: TextView = itemView.findViewById(R.id.tv_nama_pelanggan)
        private val tvNoTelp: TextView = itemView.findViewById(R.id.tv_no_telp)
        private val tvMemberLevel: TextView = itemView.findViewById(R.id.tv_member_level)
        private val tvPoin: TextView = itemView.findViewById(R.id.tv_poin)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btn_delete)

        fun bind(pelanggan: ModelPelanggan) {
            tvNamaPelanggan.text = pelanggan.namaPelanggan.ifEmpty { "Pelanggan" }
            tvNoTelp.text = pelanggan.noTelp.ifEmpty { "-" }
            tvMemberLevel.text = pelanggan.memberLevel.ifEmpty { "Regular" }
            tvPoin.text = "${pelanggan.poin} Poin"

            // Set warna member level
            when (pelanggan.memberLevel) {
                "Platinum" -> tvMemberLevel.setTextColor(Color.parseColor("#9C27B0"))
                "Gold" -> tvMemberLevel.setTextColor(Color.parseColor("#FF9800"))
                else -> tvMemberLevel.setTextColor(Color.parseColor("#4CAF50"))
            }

            itemView.setOnClickListener {
                listener?.onItemClick(pelanggan)
            }

            btnEdit.setOnClickListener {
                listener?.onEditClick(pelanggan)
            }

            btnDelete.setOnClickListener {
                listener?.onDeleteClick(pelanggan)
            }
        }
    }
}
