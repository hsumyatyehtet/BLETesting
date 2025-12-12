package com.example.bletesting

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.uuid.Uuid

class BLESendActivity: AppCompatActivity() {

    private var advertiser: BluetoothLeAdvertiser? = null
    private lateinit var bluetoothAdapter: BluetoothAdapter

//    "0000abcd-0000-1000-8000-00805f9b34fb"
    private val SERVICE_UUID = UUID.fromString("12345678-1234-1234-1234-123456789ABC")

    private val TAG = "BLE_SENDER"

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) startAdvertising()
        else Toast.makeText(this, "Permissions denied ❌", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported ❌", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun startAdvertising() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Enable Bluetooth first", Toast.LENGTH_SHORT).show()
            return
        }

        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            Toast.makeText(this, "BLE advertising not supported ❌", Toast.LENGTH_LONG).show()
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        // For Android 8: small payload, no name
        val message = "Hi"
        val manufacturerId = 0x004C

        val dataBuilder = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addServiceData(ParcelUuid(SERVICE_UUID), "Hi".toByteArray(Charsets.UTF_8))
            //.addManufacturerData(manufacturerId, message.toByteArray(Charsets.UTF_8))

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            dataBuilder.setIncludeDeviceName(true)
        }

        val advertiseData = dataBuilder.build()

        Handler(Looper.getMainLooper()).postDelayed({
            advertiser?.startAdvertising(settings, advertiseData, advertiseCallback)
        }, 1000)

    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.i(TAG, "Advertising started successfully ✅")
            Toast.makeText(this@BLESendActivity, "Advertising started successfully ✅", Toast.LENGTH_SHORT).show()
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Advertising failed: $errorCode")
            Toast.makeText(this@BLESendActivity, "Advertising failed: Code: $errorCode", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    override fun onDestroy() {
        super.onDestroy()
        advertiser?.stopAdvertising(advertiseCallback)
        Log.i(TAG, "Advertising stopped")
    }

}