package com.example.bletesting

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BLEReceiveForiOSActivity: AppCompatActivity() {

    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var txtStatus: TextView
    private lateinit var tvDeviceName: TextView
    private lateinit var tvAddress: TextView

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) startScan()
        else Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ble_receiver)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtStatus = findViewById(R.id.tvReceiver)
        tvDeviceName = findViewById(R.id.tvDeviceName)
        tvAddress = findViewById(R.id.tvDeviceAddress)

        requestPermissions()

    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startScan() {

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        val scanFilters = mutableListOf<ScanFilter>()

        // Optional: Add filter for your service UUID

        val filter = ScanFilter.Builder()

            .setServiceUuid(ParcelUuid.fromString("12345678-1234-1234-1234-123456789ABC"))

            .build()

        scanFilters.add(filter)

        val scanSettings = ScanSettings.Builder()

            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)

            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)

            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)

            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)

            .setReportDelay(0)

            .build()

        bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)

        txtStatus.text = "Scanning..."

    }

    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {

            super.onScanResult(callbackType, result)

            val device = result.device

            val scanRecord = result.scanRecord ?: return

            // 1. Get Device Name

            val deviceName = scanRecord.deviceName ?: device.name ?: "Unknown"

            Log.d("BLE", "Device Name: $deviceName")

            // 2. Get Service UUIDs

            val serviceUuids = scanRecord.serviceUuids

            serviceUuids?.forEach { uuid ->

                Log.d("BLE", "Service UUID: $uuid")

            }

            // 3. Get Manufacturer Data

            val manufacturerData = scanRecord.getManufacturerSpecificData(0x1234)

            manufacturerData?.let { data ->

                val message = String(data, Charsets.UTF_8)

                Log.d("BLE", "Manufacturer Data: $message")

            }

            // 4. Get all manufacturer data (debugging)

            val manufacturerDataMap = scanRecord.manufacturerSpecificData

            for (i in 0 until manufacturerDataMap.size()) {
                val key = manufacturerDataMap.keyAt(i)          // Manufacturer ID
                val value = manufacturerDataMap.valueAt(i)     // ByteArray data

                // Convert data to String (skip first 2 bytes if they are ID bytes)
                val message = if (value.size > 2) {
                    value.copyOfRange(2, value.size).toString(Charsets.UTF_8)
                } else {
                    ""
                }

                Log.d("BLE", "Manufacturer ID: 0x${key.toString(16)}, Message: $message")
            }
            // 5. Print all available information

            Log.d("BLE", "=== Full Scan Record ===")

            Log.d("BLE", "Device: ${device.address}")

            Log.d("BLE", "Device Name: $deviceName")

            Log.d("BLE", "RSSI: ${result.rssi}")

            Log.d("BLE", "Service UUIDs: $serviceUuids")

            Log.d("BLE", "Manufacturer Data: $manufacturerDataMap")

            Log.d("BLE", "Service Data: ${scanRecord.serviceData}")

            Log.d("BLE", "TX Power: ${scanRecord.txPowerLevel}")

            txtStatus.text = serviceUuids.toString()
            tvDeviceName.text = deviceName
           // tvAddress.text = manufacturerDataMap.toString()


        }

        override fun onScanFailed(errorCode: Int) {

            Log.e("BLE", "Scan failed with error: $errorCode")

        }

    }


}