package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnuema.android.obd.enums.ObdModes
import com.pnuema.android.obd.models.PID

import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.util.*

@SuppressLint("MissingPermission")
class ErrorActivity : AppCompatActivity() {
    lateinit var BlutDeice:BluetoothDevice
    var listELM237Commnads= mutableListOf<ThreadReadWrite>()
    var nextELM237Command=0
    var startupdates=false
    lateinit var handler1: Handler
    lateinit var titleError:TextView
    val MESSAGE_OBD_RESULT: Int = 5
    var  readwriteThreadFinised=true
    val MESSAGE_RESPONSE_ELM237_COMMAND: Int = 11
    var flaginitiationmessage=true
    lateinit var listDTC:DTCClass
    var flagmoreECUs=false
    var flaggenericfaultshistory=true
    var flaggenericfaultslastCycle=true
    var flagaskCustomFaults=true
    var flagaskCustomFaultshistory=true
    var flagaskCustomBatterysFaults=true
    lateinit var runnablewait:Runnable
    var flagcustomfaults=true
    var flagclaimedmoreECUs=false
    var OBDresult=""

    var listofpresentGenericErrors=mutableListOf<MutableList<String>>()
    var listofstoredGenericErrors= mutableListOf<MutableList<String>>()
    var listofgenericErrorstoRecycler= mutableListOf<String>()
    var listofpresentCustomErrors= mutableListOf<MutableList<String>>()
    var listofCustomErrorstoRecycler= mutableListOf<String>()
    var presentlyChoosenErrorDesc=""
    var flagcheckingNumberOFECus=true
    var numberOFECUs=1
    lateinit var alertdialGenreicErrorDesc:AlertDialog
    lateinit var textViewGenErrDesc:TextView
    lateinit var popuupWindow:PopupWindow
    lateinit var activitybackground:ConstraintLayout
    lateinit var listofGeneralDescriptionofErrorCause:MutableList<String>

    var mmSocket: BluetoothSocket?=null
    var SocketWIFI: Socket?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        val recyclerErrors=findViewById<RecyclerView>(R.id.recyclerErrors)
        val recyclerErrorsCustom=findViewById<RecyclerView>(R.id.recyclerErrorsCustom)
         titleError=findViewById<TextView>(R.id.titleError)
        activitybackground=findViewById(R.id.errorlayout)
        val builderGenErrAlDi = AlertDialog.Builder(this)
        val dialogGenErrLayout = layoutInflater.inflate(R.layout.alert_dialog_error_desc, null)
        textViewGenErrDesc=dialogGenErrLayout.findViewById(R.id.textErorDesc)
        builderGenErrAlDi.setView(dialogGenErrLayout)
        alertdialGenreicErrorDesc=builderGenErrAlDi.create()
        listofGeneralDescriptionofErrorCause= mutableListOf<String>(getString(R.string.err_in_fuel),
            getString(R.string.err_in_injection),
            getString(R.string.err_in_air_meter),
            getString(R.string.err_misfires),
            getString(R.string.err_emission_sys),
            getString(R.string.err_spd_cntrl),
            getString(R.string.err_comp_outp_circ),
            getString(R.string.err_transmiss),
            getString(R.string.err_transmiss))
        runnablewait=object:Runnable{
            override fun run() {
                activitybackground.requestLayout()
                val popupView=this@ErrorActivity.layoutInflater.inflate(R.layout.for_pop_up_window,null)
                val textPopUpLaoding=popupView.findViewById<TextView>(R.id.textPopup)
                popuupWindow= PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                activitybackground=findViewById(R.id.errorlayout)
                popuupWindow.isOutsideTouchable = true
                popuupWindow.isFocusable = true
                textPopUpLaoding.text = getString(R.string.loading_)
                popuupWindow.showAsDropDown(activitybackground)
            }

        }

        val json= Json { ignoreUnknownKeys = true }
        if (resources.configuration.locales[0].language=="pl") {
            val returnStringPIDsforgraphs = StringBuilder()
            InputStreamReader(resources.openRawResource(R.raw.dtccodespl)).use { isr ->
                BufferedReader(isr).use { input ->
                    var line = input.readLine()
                    while (line != null) {
                        returnStringPIDsforgraphs.append(line)
                        line = input.readLine()
                    }
                }
            }
            listDTC = json.decodeFromString<DTCClass>(returnStringPIDsforgraphs.toString())
        }
        else
        {
            val returnStringPIDsforgraphs = StringBuilder()
            InputStreamReader(resources.openRawResource(R.raw.dtccodes)).use { isr ->
                BufferedReader(isr).use { input ->
                    var line = input.readLine()
                    while (line != null) {
                        returnStringPIDsforgraphs.append(line)
                        line = input.readLine()
                    }
                }
            }
            listDTC = json.decodeFromString<DTCClass>(returnStringPIDsforgraphs.toString())
        }

        handler1=object :Handler(Looper.getMainLooper())
        {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                if (msg.what== MESSAGE_OBD_RESULT)
                {
                    OBDresult=msg.obj as String
                    readwriteThreadFinised=true
                    if (flaginitiationmessage)
                    {
                        flaginitiationmessage=false
                        ThreadReadWrite("",PID(ObdModes.MODE_03),SocketWIFI, mmSocket,handler1,0).start()
                    }
                    else if (flaggenericfaultshistory)
                    {
                       flaggenericfaultshistory=false
                       //titleError.text=ParseErrors().findErrorsCustom(OBDresult,0)
                       listofstoredGenericErrors=ParseErrors().findErrorsCustom(OBDresult,0)
                        //listofstoredGenericErrors=ParseErrors().parseGenericErrors1(OBDresult)
                       ThreadReadWrite("",PID(ObdModes.MODE_07),SocketWIFI, mmSocket,handler1,0).start()
                    }
                    else if (flaggenericfaultslastCycle)
                    {

                        flaggenericfaultslastCycle=false

                        listofpresentGenericErrors=ParseErrors().findErrorsCustom(OBDresult,0)
                        //listofpresentGenericErrors=ParseErrors().parseGenericErrors1(OBDresult)
                            for ((index, mutablelist) in listofstoredGenericErrors.withIndex()) {
                                listofgenericErrorstoRecycler.add(getString(R.string.err_from) + " " + (index + 1).toString() + " ECU")
                                listofgenericErrorstoRecycler.add(getString(R.string.stor_err))
                                listofgenericErrorstoRecycler.addAll(mutablelist)
                                listofgenericErrorstoRecycler.add(getString(R.string.pend_err))
                                try {
                                    listofgenericErrorstoRecycler.addAll(listofpresentGenericErrors[index])
                                } catch (e: Exception) {

                                }

                            }

                            recyclerErrors.layoutManager = LinearLayoutManager(this@ErrorActivity)
                            recyclerErrors.adapter = AdapterErrors(this@ErrorActivity)
                        popuupWindow.dismiss()

                    }


                    //
                }
                else if (msg.what==MESSAGE_RESPONSE_ELM237_COMMAND)
                {
                    readwriteThreadFinised=true
                    nextELM237Command++
                    if (nextELM237Command<listELM237Commnads.size)
                    {
                        listELM237Commnads[nextELM237Command].start()
                    }
                    else
                    {
                        ThreadReadWrite("01 00".trim { it <= ' ' },null, SocketWIFI,mmSocket,handler1,0).start()

                    }

                }
            }
        }



        Thread {
            while (this@ErrorActivity.lifecycle.currentState!= Lifecycle.State.STARTED)
            {

            }
            runOnUiThread {
                mainExecutor.execute(runnablewait)
                //ThreadReadWrite(null,PID(ObdModes.MODE_01,"00"), mmSocket!!,handler1,0).start()
            }
        }.start()

        Thread{
            try {
            if (!intent.getBooleanExtra("IsWifiDevice", false)) {
                if (MySingleton.getInstance().BlutSocket==null) {
                    val blutDEV: BluetoothDevice = MySingleton.getInstance().BludDevice!!
                    var uuidGood: UUID = UUID(18695992643584,-9223371485494954757)
                    blutDEV.uuids.forEach {
                            parceluuid->
                        if (parceluuid.uuid!= UUID.fromString("00000000-0000-1000-8000-00805f9b34fb"))
                        {
                            uuidGood= parceluuid.uuid
                        }
                    }
                    /* mmSocket = blutDEV.createRfcommSocketToServiceRecord(
                         blutDEV.uuids[0].uuid
                     )//createRfcommSocketToServiceRecord*/

                    mmSocket = blutDEV.createRfcommSocketToServiceRecord(
                        uuidGood//UUID(18695992643584,-9223371485494954757)
                    )

                    mmSocket?.let { socket ->

                        if (!socket.isConnected) {
                            socket.connect()
                        }

                        if (socket.isConnected) {
                            MySingleton.getInstance().BlutSocket = socket
                            initialATCommands()

                        } else {
                            runOnUiThread {
                                val text =getString(R.string.cant_connect)
                                val duration = Toast.LENGTH_SHORT
                                val toast = Toast.makeText(this, text, duration)
                                toast.show()
                            }
                        }


                    }
                }
                else
                {
                    mmSocket=MySingleton.getInstance().BlutSocket
                    if (!mmSocket!!.isConnected)
                    {
                        mmSocket!!.connect()
                    }
                    runOnUiThread {
                        initialATCommands()
                    }
                }
            }
        else
            {
                if (MySingleton.getInstance().SocketWIFI == null) {
                    SocketWIFI = Socket(MySingleton.getInstance().wifiIP, MySingleton.getInstance().SocketPort)

                    if (SocketWIFI!!.isConnected) {
                        MySingleton.getInstance().SocketWIFI = SocketWIFI
                        runOnUiThread {
                            Thread.sleep(50)
                            initialATCommands()
                        }
                    } else {
                        runOnUiThread {
                            val text = getString(R.string.try_again)
                            val duration = Toast.LENGTH_SHORT
                            val toast = Toast.makeText(this, text, duration)
                            toast.show()
                            popuupWindow.dismiss()
                        }
                    }
                }
                else
                {
                    SocketWIFI=MySingleton.getInstance().SocketWIFI
                    runOnUiThread {
                        initialATCommands()
                    }
                }
            }

        } catch (e: Exception) {

                runOnUiThread {
                    val text = getString(R.string.try_again)
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(this, text, duration)
                    toast.show()
                }
            }
        }.start()



// calback called when back button pressed- stop of connection with ELM 237
        val backCallback=object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                try {
                   // startupdates=false
                    if (mmSocket!=null) {
                        mmSocket!!.close()
                        MySingleton.getInstance().BlutSocket=null
                    }
                    else
                    {
                        if (MySingleton.getInstance().wifiIP=="192.168.0.10") {
                            SocketWIFI!!.close()
                            MySingleton.getInstance().SocketWIFI = null
                        }
                    }
                    Thread.sleep(500)

                }
                catch(e:Exception)
                {

                }
                startActivity(Intent(this@ErrorActivity,DiagnosticsOBD::class.java))
                finish()
            }

        }
        onBackPressedDispatcher.addCallback(backCallback)

    }


 fun initialATCommands()
 { runOnUiThread {

     listELM237Commnads.add(
         ThreadReadWrite(
             "AT Z".trim { it <= ' ' },
             null,
             SocketWIFI,
             mmSocket,
             handler1,
             0
         )
     )
     listELM237Commnads.add(
         ThreadReadWrite(
             "AT SP 0".trim { it <= ' ' },
             null,
             SocketWIFI,
             mmSocket,
             handler1,
             0
         )
     )
     listELM237Commnads.add(
         ThreadReadWrite(
             "AT H1".trim { it <= ' ' },
             null,
             SocketWIFI,
             mmSocket,
             handler1,
             0
         )
     )
     listELM237Commnads.add(
         ThreadReadWrite(
             "AT ST FF".trim { it <= ' ' },
             null,
             SocketWIFI,
             mmSocket,
             handler1,
             0
         )
     )
     listELM237Commnads[nextELM237Command].start()


 }}

    override fun onDestroy() {
        super.onDestroy()
        if (mmSocket!=null) {
            mmSocket!!.close()
            MySingleton.getInstance().BlutSocket=null
        }
        else
        {
            if (MySingleton.getInstance().wifiIP=="192.168.0.10") {
                SocketWIFI!!.close()
                MySingleton.getInstance().SocketWIFI = null
            }
        }

        // finish()
    }
}