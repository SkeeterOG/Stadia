package com.sachtech.stadia

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_custom_fragment.view.*

class CustomFragmentAdapter(
    var deviceItemList: ArrayList<BluetoothDevice>,
    val onItemClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<CustomFragmentAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_custom_fragment, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return deviceItemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = deviceItemList[position]
        holder.itemView.tv_name.text = item.name
        holder.itemView.tv_address.text = item.address
        holder.itemView.setOnClickListener {
            onItemClick(deviceItemList[position])
        }
    }
}