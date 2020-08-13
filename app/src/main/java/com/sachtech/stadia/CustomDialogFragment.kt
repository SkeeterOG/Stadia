package com.sachtech.stadia

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sachtech.stadia.BluetoothHelper.bReciever
import com.sachtech.stadia.utils.BluetoothConnector
import kotlinx.android.synthetic.main.custom_fragment.*
import kotlinx.android.synthetic.main.custom_fragment.view.*
import java.io.IOException

class CustomDialogFragment : DialogFragment(), NextViewListener {
    var deviceItemList = ArrayList<BluetoothDevice>()
    private var nextViewListener: NextViewListener? = null

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private val pairedBluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val mDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                //3 cases:
                //case1: bonded already
                if (mDevice?.bondState == BluetoothDevice.BOND_BONDED) {
                    Log.d("", "BroadcastReceiver: BOND_BONDED.")
                    // bleUtils.run(mDevice,getActivity());
                    val connector =
                        BluetoothConnector(
                            mDevice, true,
                            BluetoothHelper.bluetoothAdapter, null, nextViewListener
                        )
                    try {
                        connector.connect()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                //case2: creating a bone
                if (mDevice?.bondState == BluetoothDevice.BOND_BONDING) {
                    Log.d("", "BroadcastReceiver: BOND_BONDING.")
                }
                //case3: breaking a bond
                if (mDevice?.bondState == BluetoothDevice.BOND_NONE) {
                    Log.d("", "BroadcastReceiver: BOND_NONE.")
                }
            } else if (action == BluetoothDevice.ACTION_ACL_CONNECTED) {
                Log.d("", "BroadcastReceiver: ACTION_ACL_CONNECTED.")
            }
        }
    }

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
        val customFragmentAdapter = CustomFragmentAdapter(deviceItemList) {
            if (it.createBond()) {
                // bleUtils.run(mDevice,getActivity());
                val connector =
                    BluetoothConnector(
                        it,
                        true,
                        BluetoothHelper.bluetoothAdapter,
                        null,
                        nextViewListener
                    )
                try {
                    connector.connect()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            //    BleUtils().pairDevice(it)
            //   BluetoothHelper.connectDevice(it)
        }
        nextViewListener = this
        recyclerView_custom_fragment.adapter = customFragmentAdapter


        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context?.registerReceiver(bReciever, filter)

        BluetoothHelper.startScan {
            if (!checkDeviceInList(it)) {
                deviceItemList.add(it)
                customFragmentAdapter.notifyItemInserted(deviceItemList.size - 1)
            }
        }

        //Broadcasts when bond state changes (ie:pairing)
        val filter1 = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        /* filter1.addAction()
         filter1.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)*/
        context?.registerReceiver(pairedBluetoothReceiver, filter1)

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
        context?.unregisterReceiver(pairedBluetoothReceiver)
    }

    private fun setupClickListeners(view: View) {
        view.btn_cancel.setOnClickListener {
            BluetoothHelper.cancelSacn()
            context?.unregisterReceiver(bReciever)
            dismiss()
        }

    }


    private fun getPairingDevices() {
        val pairedDevices: Array<BluetoothDevice> = BluetoothHelper.getPairedDevice()
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                deviceItemList.add(device)
            }
        }
    }

    override fun moveToNextFragment(device: BluetoothDevice?) {
        Toast.makeText(context, "Connected          " + device?.address, Toast.LENGTH_LONG).show()
    }

    override fun bluetoothPairError(eConnectException: Exception?, device: BluetoothDevice?) {
        Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show()
    }


}