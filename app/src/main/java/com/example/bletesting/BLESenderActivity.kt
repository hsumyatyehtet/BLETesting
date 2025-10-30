package com.example.bletesting

import android.Manifest
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
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.nio.charset.StandardCharsets
import java.util.UUID


class BLESenderActivity : AppCompatActivity() {

    private var advertiser: BluetoothLeAdvertiser? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->

        if (result.values.all { it }) startAdvertising()
        else Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestPermissions()

    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
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


        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(false)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SharedConstants.SERVICE_UUID))
          //  .addManufacturerData(1,SharedConstants.ADVERTISING_MESSAGE.toByteArray(StandardCharsets.UTF_8))
            .addServiceData(
                ParcelUuid(SharedConstants.SERVICE_UUID),
                SharedConstants.ADVERTISING_MESSAGE.toByteArray(StandardCharsets.UTF_8)
            )
            .setIncludeDeviceName(false)
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)

        Toast.makeText(this, "Advertising Started ✅", Toast.LENGTH_LONG).show()
    }

    private val advertiseCallback = object : android.bluetooth.le.AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: android.bluetooth.le.AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Toast.makeText(this@BLESenderActivity, "Advertising Started ✅", Toast.LENGTH_SHORT).show()
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Toast.makeText(this@BLESenderActivity, "Advertising Failed ❌ Code: $errorCode", Toast.LENGTH_LONG).show()
        }
    }


}