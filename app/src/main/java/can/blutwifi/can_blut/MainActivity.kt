package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.text.Editable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import java.net.*
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var handler:Handler
    lateinit var list_of_Devices:MutableList<BluetoothDevice>
    lateinit var bluetoothAdapter:BluetoothAdapter
    lateinit var dialogBluetoothDevices:AlertDialog
    lateinit var instructionmanual:AlertDialog
    lateinit var dialogWIFI:AlertDialog
    lateinit var recyclerPairedDevices:RecyclerView
    lateinit var channelP2P: WifiP2pManager.Channel
    lateinit var managerP2P: WifiP2pManager
    lateinit var choosenP2PDevicetoConn:WifiP2pDevice
    lateinit var peerListListener:WifiP2pManager.PeerListListener
    lateinit var activitybackground:ConstraintLayout
    lateinit var textPopUpLaoding:TextView
    lateinit var popuupWindow:PopupWindow
    var flagConnectionWithBlutbyServer=false
    val MESSAGE_BLUETOOTH_PERMISSION_GRANTED=2
    var flagfirstClick=true
    val laucher_permission=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        var granted=true
        it.forEach { string, boolean ->
            if (!boolean)
            {
                granted=false
            }
        }
        if (granted)
        {
            handler.sendMessage(Message.obtain(handler,MESSAGE_BLUETOOTH_PERMISSION_GRANTED))
        }
    }

    val laucher_connect_WIFI=registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        dialogWIFI.show()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_use_manual, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.INstructionUse -> {
                instructionmanual.show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonConnect = findViewById<Button>(R.id.button_connect)
        MySingleton.getInstance().MainActivity=this
        val buttonWIFI=findViewById<Button>(R.id.wifi_device).apply {
            setOnClickListener {
                laucher_connect_WIFI.launch(Intent(Settings.Panel.ACTION_WIFI))
            }
        }
        activitybackground=findViewById(R.id.MainLayoit)


        //aleert dialog for menu instruction
        val builderManual = AlertDialog.Builder(this)
        builderManual.setTitle(getString(R.string.manula_dialog_title))
        val dialogManualLayout = layoutInflater.inflate(R.layout.alertdialogmanual, null)
        val textMnual=dialogManualLayout.findViewById<TextView>(R.id.textMnual)
        if (resources.configuration.locales[0].language=="pl") {
            textMnual.text = "1.Wybierz jedną z opcji:\n" +
                    "Znajdź sparowane urządzenia Bluetooth- po wybraniu tej opcji pojawi się lista urządzeń Bluetooth sparowanych z twoim urządzeniem. Wybierz dostępny skaner OBD (pamietaj aby przedtem sparować swoje urządzenie ze skanerem OBD).\n" +
                    "Znajdź i połącz z bliskim urządzeniem WIFI- po wybraniu tej opcji pojawi się panel umożliwiający połączenie z siecią WIFI. Wybierz tą, generowaną przez skaner OBD (po połaczeniu z siecią WIFI generowaną przez skaner, zostanie zerwane dotychczasowe połączenie internetowe).\n"+
                    "2. W następnym kroku zostaniesz przeniesiony do ekranu w którym możliwe jest wybranie rodzaju diagnostyki. Wybierz interesującą cię opcję. "
        }
        else {
            textMnual.text = "1.Select one of options:\n" +
                    "Find paired Bluetooth devices- after click on this button appear list of paired Bluetooth devices. Choose the already paired OBD scanner (remember to pair earlier your device with OBD scanner).\n" +
                    "Find and connect to close wifi device- after click this option appear panel with available WIFI networks. Chose the one generated by OBD scanner (when you connect with OBD scanner by WIFI, your present network connection will be torn down).\n" +
                    "2. In next step you will be directed to screen with available diagnostics option. Choose the one you are interested in"
        }
        builderManual.setView(dialogManualLayout)
        builderManual.setPositiveButton(getString(R.string.OK))
        {dialogInterface, i ->

        }
        instructionmanual=builderManual.create()





        val builderWIFI = AlertDialog.Builder(this)
        builderWIFI.setTitle(getString(R.string.do_continue))
        builderWIFI.setPositiveButton(getString(R.string.continue_))
        {dialogInterface, i ->
            MySingleton.getInstance().wifiIP="192.168.0.10"
            MySingleton.getInstance().SocketPort=35000
            this.startActivity(Intent(this,DiagnosticsOBD::class.java).putExtra("IsWifiDevice",true))
        }
        dialogWIFI=builderWIFI.create()

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_pair_dev))
        val dialogLayout = layoutInflater.inflate(R.layout.alert_dialog_bluetooth_devices, null)
        recyclerPairedDevices=dialogLayout.findViewById<RecyclerView>(R.id.recyclerpairedBlDev)

        builder.setView(dialogLayout)
        dialogBluetoothDevices=builder.create()

        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == MESSAGE_BLUETOOTH_PERMISSION_GRANTED) {
                    recyclerPairedDevices.layoutManager=LinearLayoutManager(this@MainActivity)
                    recyclerPairedDevices.adapter=adapterblutdevices(getBondedDevices(),this@MainActivity)
                    dialogBluetoothDevices.show()

                }

            }
        }

        buttonConnect.setOnClickListener {
            //dialogBluetoothDevices.dismiss()
            //buttonConnect.isVisible = false
            if (flagfirstClick) {
                flagfirstClick=false
                val bluetoothManager: BluetoothManager =
                    getSystemService(BluetoothManager::class.java)
                try {
                    bluetoothAdapter = bluetoothManager.getAdapter()
                } catch (e: java.lang.Exception) {
                    val text =getString( R.string.cant_find_bluetooth)
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(this, text, duration)
                    toast.show()
                }
            }

            when {
                ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.BLUETOOTH_CONNECT"
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            this,
                            "android.permission.BLUETOOTH"
                        ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                        this,
                "android.permission.ACCESS_FINE_LOCATION"
                    ) == PackageManager.PERMISSION_GRANTED
                -> {

                    recyclerPairedDevices.layoutManager=LinearLayoutManager(this@MainActivity)
                    recyclerPairedDevices.adapter=adapterblutdevices(getBondedDevices(),this@MainActivity)
                    dialogBluetoothDevices.show()


                }

                else -> {
                    laucher_permission.launch(arrayOf("android.permission.BLUETOOTH_CONNECT",
                        "android.permission.BLUETOOTH"))
                }
            }

        }
    }





    @SuppressLint("MissingPermission")
    fun getBondedDevices() : MutableList<BluetoothDevice>
    {
        try {
             list_of_Devices=bluetoothAdapter.bondedDevices!!.toMutableList()
            return list_of_Devices

        }
        catch (e:java.lang.Exception)
        {
            val text = getString(R.string.cant_find_bonded)
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(this, text, duration)
            toast.show()
        }
        return mutableListOf()

    }
fun showPopUp()
{

    val popupView=this.layoutInflater.inflate(R.layout.for_pop_up_window,null)
    textPopUpLaoding =popupView.findViewById<TextView>(R.id.textPopup)
    popuupWindow= PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    popuupWindow.isOutsideTouchable = true
    popuupWindow.isFocusable = true
    textPopUpLaoding.text =getString(R.string.wait)
    popuupWindow.showAsDropDown(activitybackground)
}
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

}