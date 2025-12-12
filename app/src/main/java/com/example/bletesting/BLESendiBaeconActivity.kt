package com.example.bletesting

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

class BLESendiBaeconActivity: AppCompatActivity() {


    companion object {
        private const val TAG = "iBeacon"
        private const val APPLE_MANUFACTURER_ID = 0x004C // Apple Company ID
    }

    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            //startIBeaconAdvertising()
            startIBeaconServiceDataAdvertising(this)
        } else {
            Log.e(TAG, "Permissions not granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Bluetooth + Location permissions
        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        permissionLauncher.launch(permissions.toTypedArray())
    }


    //Manufacture data
  /*  @SuppressLint("MissingPermission")
    private fun startIBeaconAdvertising() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth not available or disabled")
            return
        }

        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "BLE advertising not supported on this device")
            return
        }

        // 1️⃣ Define your iBeacon parameters
        val uuid = UUID.fromString("12345678-1234-1234-1234-123456789ABC")
        val major = 99
        val minor = 2
        val txPower = (-59).toByte()

        // 2️⃣ Build iBeacon manufacturer data
        val manufacturerData = ByteBuffer.allocate(23)
        manufacturerData.order(ByteOrder.BIG_ENDIAN)
        manufacturerData.put(0x02) // iBeacon indicator
        manufacturerData.put(0x15) // length = 21 bytes

        // Split UUID into bytes manually to avoid any potential Android issues
        val uuidBytes = ByteArray(16)
        val msb = uuid.mostSignificantBits
        val lsb = uuid.leastSignificantBits
        for (i in 0 until 8) uuidBytes[i] = ((msb shr (56 - i * 8)) and 0xFF).toByte()
        for (i in 8 until 16) uuidBytes[i] = ((lsb shr (120 - i * 8)) and 0xFF).toByte()
        manufacturerData.put(uuidBytes)

        manufacturerData.putShort(major.toShort())
        manufacturerData.putShort(minor.toShort())
        manufacturerData.put(txPower)

        // 3️⃣ Create AdvertiseData WITHOUT device name or TX power level
        val advertiseData = AdvertiseData.Builder()
            .addManufacturerData(APPLE_MANUFACTURER_ID, manufacturerData.array())
            .setIncludeDeviceName(false) // Must be false to avoid Android truncation
            .setIncludeTxPowerLevel(false)
            .build()

        Log.d(TAG, "iBeacon Data: ${manufacturerData.array().joinToString { "%02X".format(it) }}")

        // 4️⃣ Configure advertise settings
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false) // Non-connectable for iBeacon
            .build()

        // 5️⃣ Start advertising
        bluetoothLeAdvertiser?.startAdvertising(settings, advertiseData, advertiseCallback)
    }*/

    @SuppressLint("MissingPermission")
    fun startIBeaconServiceDataAdvertising(context: Context) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("BLE", "Bluetooth not available or disabled")
            return
        }

        val bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (bluetoothLeAdvertiser == null) {
            Log.e("BLE", "BLE advertising not supported on this device")
            return
        }

        // 1️⃣ Define your iBeacon payload
        val uuid = UUID.fromString("12345678-1234-1234-1234-123456789ABC")
        val major = 99
        val minor = 2
        val txPower = (-59).toByte()

        // 2️⃣ Build iBeacon bytes (same as manufacturer payload)
        val manufacturerData = ByteBuffer.allocate(23)
        manufacturerData.order(ByteOrder.BIG_ENDIAN)
        manufacturerData.put(0x02) // iBeacon indicator
        manufacturerData.put(0x15) // length = 21 bytes

        val uuidBytes = ByteArray(16)
        val msb = uuid.mostSignificantBits
        val lsb = uuid.leastSignificantBits
        for (i in 0 until 8) uuidBytes[i] = ((msb shr (56 - i * 8)) and 0xFF).toByte()
        for (i in 8 until 16) uuidBytes[i] = ((lsb shr (120 - i * 8)) and 0xFF).toByte()
        manufacturerData.put(uuidBytes)

        manufacturerData.putShort(major.toShort())
        manufacturerData.putShort(minor.toShort())
        manufacturerData.put(txPower)

        Log.d("BLE", "Service Data Payload: ${manufacturerData.array().joinToString { "%02X".format(it) }}")

        // 3️⃣ Use a custom 128-bit Service UUID to send as Service Data
        val serviceUuid = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB") // Example: Eddystone UUID
        val advertiseData = AdvertiseData.Builder()
            .addServiceUuid(serviceUuid)
            .addServiceData(serviceUuid, manufacturerData.array())
            .setIncludeDeviceName(false) // Optional, helps visibility on Xiaomi
            .build()

        // 4️⃣ Configure advertising settings
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        // 5️⃣ Define callback
        val advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                super.onStartSuccess(settingsInEffect)
                Log.d("BLE", "Advertising started successfully")
                Toast.makeText(this@BLESendiBaeconActivity,"✅ iBeacon advertising started successfully!",
                    Toast.LENGTH_SHORT).show()
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                Log.e("BLE", "Advertising failed with error code $errorCode")
                Toast.makeText(this@BLESendiBaeconActivity,"❌ Advertising failed with error code: $errorCode",
                    Toast.LENGTH_SHORT).show()
            }
        }

        // 6️⃣ Start advertising
        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.i(TAG, "✅ iBeacon advertising started successfully!")
            Toast.makeText(this@BLESendiBaeconActivity,"✅ iBeacon advertising started successfully!",
                Toast.LENGTH_SHORT).show()
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e(TAG, "❌ Advertising failed with error code: $errorCode")
            Toast.makeText(this@BLESendiBaeconActivity,"❌ Advertising failed with error code: $errorCode",
                Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
    }


}