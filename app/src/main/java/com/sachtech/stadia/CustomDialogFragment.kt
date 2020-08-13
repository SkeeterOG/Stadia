package com.sachtech.stadia

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sachtech.stadia.BluetoothHelper.bReciever
import kotlinx.android.synthetic.main.custom_fragment.*
import kotlinx.android.synthetic.main.custom_fragment.view.*
import kotlinx.android.synthetic.main.item_custom_fragment.*
import kotlinx.android.synthetic.main.item_custom_fragment.view.*


class CustomDialogFragment : DialogFragment() {
    var deviceItemList = ArrayList<BluetoothDevice>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.custom_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPairingDevices()
        recyclerView_custom_fragment.layoutManager = LinearLayoutManager(context)
        var customFragmentAdapter = CustomFragmentAdapter(deviceItemList){
            BluetoothHelper.connectDevice(it)
        }
        recyclerView_custom_fragment.adapter = customFragmentAdapter


        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity()?.registerReceiver(bReciever, filter);

        BluetoothHelper.startScan {
            if (!checkDeviceInList(it)) {
                deviceItemList.add(it)
                customFragmentAdapter.notifyItemInserted(deviceItemList.size - 1)
            }
        }

        setupClickListeners(view)
    }

    private fun checkDeviceInList(device: BluetoothDevice): Boolean {
        var isAvailable = false
        deviceItemList.forEach {
            if (it.address == device.address) {
                isAvailable = true
                return@forEach
            }
        }
        return isAvailable

    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothHelper.cancelSacn()
//        activity?.unregisterReceiver(bReciever)
    }

    fun setupClickListeners(view: View) {
        view.btn_cancel.setOnClickListener {
            BluetoothHelper.cancelSacn()
            activity?.unregisterReceiver(bReciever)
            dismiss()
        }

    }


    fun getPairingDevices() {
        val pairedDevices: Array<BluetoothDevice> = BluetoothHelper.getPairedDevice()
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                deviceItemList.add(device)
            }
        }
    }
}