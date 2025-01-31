package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.*
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnuema.android.obd.enums.ObdModes
import com.pnuema.android.obd.models.PID
import kotlinx.serialization.json.Json
import pl.droidsonroids.gif.GifImageView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Thread.sleep
import java.net.Socket
import java.util.concurrent.Executor

@SuppressLint("MissingPermission")
class ActivityDiagnostics : AppCompatActivity() {
    lateinit var BlutDeice:BluetoothDevice
    var listELM237Commnads= mutableListOf<ThreadReadWrite>()
    var nextELM237Command=0
    var startupdates=false
    lateinit var handler1: Handler
    lateinit var OBDresult:String
    val MESSAGE_OBD_RESULT: Int = 5
   var  readwriteThreadFinised=true
    var flagNotNextFreezeFrameFault=false
    val MESSAGE_RESPONSE_CYCLIC_PASSWORD: Int = 9
    val MESSAGE_RESPONSE_ELM237_COMMAND: Int = 11
    val MESSAGE_QUERY_ERROR_DTC_FREEZE=-50
    val MESSAGE_ENTER_BACK_MODE01=-51
    lateinit var activitybackground:LinearLayout
    lateinit var freezeActualDataSwitch:SwitchCompat
    lateinit var popuupWindow:PopupWindow
    lateinit var textPopUpLaoding:TextView
    lateinit var runnablewaitGiff:Runnable
    var presentMode=1
    var numberOfFreezeFrametoAsk=0
    val FINISH_SEND_THREAD=-99
    val NEXT_MESSAGE_SEND_THREAD=-100
    val MESSAGE_CHANGE_MODE_01_02=-101
    val FIRST_MESSAGE_AFTER_CHANGE_OF_MODE=-102
    var flaginitialmesaage=true
    var flagfinalDelay=false
    var accuallysendcommandtoCalc=0
    var flaginitiationmessage=true
    var actualsendCommnad=0
    var PIDlenghtAdd=0
    var checkingMinAllowedms=3
    var checkingMinAllowedmsHex="03"
    var numberOfSendMessages=0
    var numberOfGoodResponses=0
    val MESSAGE_OBD_WRONG_RESULT: Int = 6
    var flagMode02ErrotThatCausedQUery=false
    var flagCheckifFreezeFramesExists=false
    var flagchangefromMode02toMode01=false
    var flagSentNextQuery=true
    var flagcalculationsENdCanincrementacualcommandnumber=true
    var firstVisibleRecyclerElement=0
    var lastVisibleRecyclerElement=0
    var flag_wothSendMessagesMode02=false
    var mapofPidswithPID01orMode0x22= mutableMapOf<String,String>()
    var mapofPidswithPID02= mutableMapOf<String,String>()
    var listofindexestoremovePIDs01or02= mutableListOf<Int>()
    lateinit var SendThread:ThreadSendGenralDiagnostic
    lateinit var listDTC:DTCClass
    var listOfNamesOfGeneralInfos= mutableListOf<String>()
    var listOfPIDs= mutableListOf<PID>()
    var listOfFormulas= mutableListOf<String>()
    var listofECUHeader= mutableListOf<kotlin.Long>()
    val listOfNamesOfGeneralInfosNormal: MutableList<String> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listOfGeneralInfosNames",Array<String>::class.java) as Array<String>).toMutableList()
    }
    val listOfPIDsNormal: MutableList<PID> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listOfGeneralInfoPIDs",Array<PID>::class.java) as Array<PID>).toMutableList()
    }
    val listOfFormulasNormal: MutableList<String> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listOfValuesForGeneralFormulas",Array<String>::class.java) as Array<String>).toMutableList()
    }
    val listofECUHeaderNormal:MutableList<kotlin.Long> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listofECUHeader",Array<kotlin.Long>::class.java) as Array<kotlin.Long>).toMutableList()
    }
    val listOfNamesOfGeneralInfosFreeze: MutableList<String> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listOfGeneralInfosNamesFreeze",Array<String>::class.java) as Array<String>).toMutableList()
    }
    val listOfPIDsFreeze: MutableList<PID> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listOfGeneralInfoPIDsFreeze",Array<PID>::class.java) as Array<PID>).toMutableList()
    }
    val listOfFormulasFreeze: MutableList<String> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listOfValuesForGeneralFormulasFreeze",Array<String>::class.java) as Array<String>).toMutableList()
    }
    val listofECUHeaderFreeze:MutableList<kotlin.Long> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listofECUHeaderFreeze",Array<kotlin.Long>::class.java) as Array<kotlin.Long>).toMutableList()
    }
    val isMoreThenOneECU:Boolean by lazy(LazyThreadSafetyMode.NONE) {
        intent.getBooleanExtra("ismorethanOneECU",false)
    }
    var mmSocket: BluetoothSocket?=null
    var SocketWIFI: Socket?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)
        val texttest=findViewById<TextView>(R.id.texttest)
        val recyclerViewGeneralVal=findViewById<RecyclerView>(R.id.recyclerGeneralValues).apply {
            layoutManager=LinearLayoutManager(this@ActivityDiagnostics)
            adapter=Adapter_General_Values(this@ActivityDiagnostics)
        }
        parseJsonWithDTCcodes()
        //set queries to Normal not freeze frame
        listOfNamesOfGeneralInfos= listOfNamesOfGeneralInfosNormal
        listOfPIDs= listOfPIDsNormal
        listOfFormulas= listOfFormulasNormal
        listofECUHeader= listofECUHeaderNormal
        val executor= Executor{ function->function.run()}
        val buttonNextErrorCausedFreeze=findViewById<Button>(R.id.NextErrorCausedFreeze).apply{
            isVisible=false
            setOnClickListener{
                button->
                flagMode02ErrotThatCausedQUery=true
                numberOfFreezeFrametoAsk++
                if ( flagNotNextFreezeFrameFault)
                {
                    numberOfFreezeFrametoAsk=0
                    flagNotNextFreezeFrameFault=false
                }
              /*  listOfPIDs= listOfPIDsNormal
                listOfPIDs.replaceAll { pid->PID(ObdModes.MODE_02,pid.PID+" 0$numberOfFreezeFrametoAsk") }*/
                //listOfPIDs.replaceAll{ pid->PID(ObdModes.MODE_02,pid.PID.substring(0,pid.PID.length-(numberOfFreezeFrametoAsk.toString().length+2))+" 0$numberOfFreezeFrametoAsk") }
                popuupWindow.showAsDropDown(activitybackground)
                   // handler1.sendMessage(Message.obtain(handler1,MESSAGE_QUERY_ERROR_DTC_FREEZE))


            }
        }
       /*waitingLAyout=findViewById<ConstraintLayout>(R.id.ACTVDIAGwaiting).apply{isVisible=false
        val text=findViewById<TextView>(R.id.TEXTWAITACTIVDIAGN).apply{text=getString(R.string.retrv_data)}}*/
        activitybackground=findViewById(R.id.constrLAuActDiagn)
        runnablewaitGiff=object :Runnable
        {
            override fun run() {
                activitybackground.requestLayout()
                val popupView = this@ActivityDiagnostics.layoutInflater.inflate(R.layout.for_pop_up_window, null)
                textPopUpLaoding = popupView.findViewById<TextView>(R.id.textPopup)
                popuupWindow = PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                popuupWindow.isOutsideTouchable = true
                popuupWindow.isFocusable = true
                textPopUpLaoding.text = "Connection with ECU..."
                popuupWindow.showAsDropDown(activitybackground)
            }

        }
        val textDTCCausedFreezeFrame=findViewById<TextView>(R.id.DTC_caused_freeze_frame).apply { isVisible=false }
         freezeActualDataSwitch=findViewById<SwitchCompat>(R.id.freezeActualData).apply {
            setOnCheckedChangeListener(object :CompoundButton.OnCheckedChangeListener
            {
                @SuppressLint("SuspiciousIndentation")
                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    if (p1)
                    {
                      /*  var i=0
                        while (i<listOfPIDs.size) {
                            if ((listOfPIDs[i].PID == "01" && listOfPIDs[i].mode=="01") ||  listOfPIDs[i].mode=="22") {
                                mapofPidswithPID01orMode0x22.put(
                                    listOfNamesOfGeneralInfos[i],
                                    listOfFormulas[i]
                                )
                                listOfPIDs.removeAt(i)
                                listOfNamesOfGeneralInfos.removeAt(i)
                                listOfFormulas.removeAt(i)
                            } else {
                                i++
                            }

                        }
                        listOfPIDs.replaceAll { pid->PID(ObdModes.MODE_02,pid.PID+" 0$numberOfFreezeFrametoAsk") }*/
                        //listOfNamesOfGeneralInfos= listOfNamesOfGeneralInfosFreeze
                        //listOfPIDs= listOfPIDsNormal
                        //listOfPIDs.replaceAll { pid->PID(ObdModes.MODE_02,pid.PID+" 0$numberOfFreezeFrametoAsk") }
                        //listOfFormulas= listOfFormulasFreeze
                        //listofECUHeader= listofECUHeaderFreeze
                        //startupdates=false
                        //recyclerViewGeneralVal.layoutManager=LinearLayoutManager(this@ActivityDiagnostics)
                        //recyclerViewGeneralVal.adapter=Adapter_General_Values(this@ActivityDiagnostics)

                           // handler1.sendMessage(Message.obtain(handler1,MESSAGE_QUERY_ERROR_DTC_FREEZE))
                        flagMode02ErrotThatCausedQUery=true
                       // presentMode=2
                        popuupWindow.showAsDropDown(activitybackground)

                    }
                    else
                    {

                       /* listOfPIDs.replaceAll { pid->PID(ObdModes.MODE_01,pid.PID.substring(0,pid.PID.length-(numberOfFreezeFrametoAsk.toString().length+2))) }
                        for ((i, key) in mapofPidswithPID01orMode0x22.keys.withIndex())
                        {
                            listOfPIDs.add(i,PID(ObdModes.MODE_01,"01"))
                            listOfNamesOfGeneralInfos.add(i,key)
                            listOfFormulas.add(i,mapofPidswithPID01orMode0x22[key]!!)

                        }*/
                        flagchangefromMode02toMode01=true

                        /*listOfNamesOfGeneralInfos= listOfNamesOfGeneralInfosNormal
                        listOfPIDs= listOfPIDsNormal

                        listOfFormulas= listOfFormulasNormal
                        listofECUHeader= listofECUHeaderNormal
                        */
                        //recyclerViewGeneralVal.layoutManager=LinearLayoutManager(this@ActivityDiagnostics)
                        //recyclerViewGeneralVal.adapter=Adapter_General_Values(this@ActivityDiagnostics)

                         /*   handler1.sendMessage(
                                Message.obtain(
                                    handler1,
                                    MESSAGE_ENTER_BACK_MODE01
                                )
                            )*/




                    }
                }

            })
        }





        handler1=object :Handler(Looper.getMainLooper())
        {
            @SuppressLint("SuspiciousIndentation")
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                if (msg.what== MESSAGE_OBD_RESULT || msg.what==MESSAGE_RESPONSE_ELM237_COMMAND)
                {

                    readwriteThreadFinised=true
                    OBDresult =msg.obj as String

                    if (flaginitiationmessage) {
                        flaginitiationmessage = false
                        popuupWindow.dismiss()
                        startGeneralInfoUpdates()
                    }
                    else if (flagCheckifFreezeFramesExists )
                    {
                        popuupWindow.dismiss()
                        buttonNextErrorCausedFreeze.isVisible=true
                        textDTCCausedFreezeFrame.isVisible=true
                        flagCheckifFreezeFramesExists=false
                        //texttest.text=OBDresult
                        if (OBDresult.length>7)
                        {
                            //texttest.text=ParseErrors().findErrorsCustom(OBDresult,3)[0][0]
                            try {
                                val errorcodecausedFreezeframe=ParseErrors().findErrorsCustom(OBDresult,3)
                                //val errorcodecausedFreezeframe=ParseErrors().parseGenericErrors1(OBDresult)
                                var textErrorsCausedFreeze=""
                                for (list in errorcodecausedFreezeframe) {
                                    if (list.size>0) {
                                        textErrorsCausedFreeze += list[0]+" "+listDTC.listOfDTC.find { predicate->predicate.code==list[0] }!!.description+"\n"
                                    }
                                }
                                textDTCCausedFreezeFrame.text=textErrorsCausedFreeze

                            }
                            catch (e:Exception)
                            {
                                try {
                                    val errorcodecausedFreezeframe=ParseErrors().findErrorsCustom(OBDresult,3)
                                    var textErrorsCausedFreeze=""
                                    for (list in errorcodecausedFreezeframe) {
                                        if (list.size>0) {
                                            textErrorsCausedFreeze += list[0]  }
                                    }
                                    textDTCCausedFreezeFrame.text=textErrorsCausedFreeze
                                }
                                catch (e:Exception)
                                {
                                    textDTCCausedFreezeFrame.text = getString(R.string.unknwn_fault)
                                }

                            }
                            //startupdates=true

                        }
                        else {
                            textDTCCausedFreezeFrame.text=getString(R.string.no_Fault)
                            flagNotNextFreezeFrameFault=true
                        }

                        //recyclerViewGeneralVal.requestLayout()
                        SendThread.handlerThread.sendMessage(
                            Message.obtain(
                                SendThread.handlerThread,
                                NEXT_MESSAGE_SEND_THREAD
                            )
                        )
                    }
                    else
                    {
                      //texttest.text=listOfPIDsFreeze.size.toString()

                        if (!flagMode02ErrotThatCausedQUery && !flagchangefromMode02toMode01) {
                            accuallysendcommandtoCalc=actualsendCommnad
                            SendThread.handlerThread.sendMessage(
                                Message.obtain(
                                    SendThread.handlerThread,
                                    NEXT_MESSAGE_SEND_THREAD
                                )
                            )
                            try {

                                val layoutmanagerforRecycler=recyclerViewGeneralVal.layoutManager
                                if (layoutmanagerforRecycler is LinearLayoutManager) {
                                    firstVisibleRecyclerElement =
                                        layoutmanagerforRecycler.findFirstVisibleItemPosition()
                                    lastVisibleRecyclerElement=layoutmanagerforRecycler.findLastVisibleItemPosition()
                                }
                                if (presentMode==1) {
                                    PIDlenghtAdd = 0//((listOfPIDs[actualsendCommnad].PID.length.toFloat())*1.5f -3f).toInt()
                                }
                                else if (presentMode==2)
                                {
                                    PIDlenghtAdd = 3
                                }


                                var k=0
                                listOfPIDs.forEach { PID->
                                    if (PID==listOfPIDs[accuallysendcommandtoCalc] && k>=firstVisibleRecyclerElement && k<=lastVisibleRecyclerElement)
                                    {
                                        if (listOfFormulas[k].contains("nonNumeric")) {

                                            recyclerViewGeneralVal.get(k - firstVisibleRecyclerElement)
                                                .findViewById<TextView>(R.id.textValueOfGeneralInfo).text =
                                                CalculateResponse().calculateNonNumericwithMoreECUs(
                                                    OBDresult,
                                                    listOfFormulas[k],
                                                    listofECUHeader[k]
                                                )
                                        }
                                        else
                                        {
                                            recyclerViewGeneralVal.get(k - firstVisibleRecyclerElement)
                                                .findViewById<TextView>(R.id.textValueOfGeneralInfo).text =
                                                (CalculateResponse().calculateNumericwithMoreECUs(
                                                    PIDlenghtAdd,
                                                    OBDresult,
                                                    listOfFormulas[k],
                                                    listofECUHeader[k]
                                                )).toString()

                                        }
                                    }
                                    k++
                                }



                            }
                            catch (e:Exception)
                            {

                            }

                        }
                        else
                        {
                            SendThread.handlerThread.sendMessage(Message.obtain(SendThread.handlerThread,MESSAGE_CHANGE_MODE_01_02))
                        }
                       /* else if (flagchangefromMode02toMode01)
                        {
                            flagchangefromMode02toMode01=false
                            buttonNextErrorCausedFreeze.isVisible=false
                            textDTCCausedFreezeFrame.isVisible=false
                            SendThread.handlerThread.sendMessage(
                                Message.obtain(
                                    SendThread.handlerThread,
                                    FIRST_MESSAGE_AFTER_CHANGE_OF_MODE
                                )
                            )
                        }*/


                    }

                }
                else if (msg.what==MESSAGE_CHANGE_MODE_01_02)
                {
                    if (flagMode02ErrotThatCausedQUery) {
                        flagMode02ErrotThatCausedQUery = false
                        flagCheckifFreezeFramesExists = true
                        if (listOfPIDsFreeze.size > 0){
                            listOfNamesOfGeneralInfos = listOfNamesOfGeneralInfosFreeze
                        listOfPIDs = listOfPIDsFreeze
                        listOfPIDs.replaceAll { pid ->
                            PID(
                                ObdModes.MODE_02,
                                pid.PID.substring(0,2) + " 0$numberOfFreezeFrametoAsk"
                            )
                        }
                        listOfFormulas = listOfFormulasFreeze
                        listofECUHeader = listofECUHeaderFreeze
                        recyclerViewGeneralVal.layoutManager =
                            LinearLayoutManager(this@ActivityDiagnostics)
                        recyclerViewGeneralVal.adapter =
                            Adapter_General_Values(this@ActivityDiagnostics)
                    }
                    else {
                            listOfPIDs.replaceAll { pid ->
                                PID(
                                    ObdModes.MODE_02,
                                    pid.PID.substring(0, 2) + " 0$numberOfFreezeFrametoAsk"
                                )
                            }
                        }
                        presentMode=2
                        ThreadReadWrite(null,PID(ObdModes.MODE_02,"02 0$numberOfFreezeFrametoAsk"), SocketWIFI,mmSocket,handler1,0).start()
                    }
                    else if (flagchangefromMode02toMode01)
                    {
                        flagchangefromMode02toMode01=false
                        if (listOfPIDsFreeze.size>0) { listOfNamesOfGeneralInfos= listOfNamesOfGeneralInfosNormal
                        listOfPIDs= listOfPIDsNormal
                        listOfFormulas= listOfFormulasNormal
                        listofECUHeader= listofECUHeaderNormal
                            recyclerViewGeneralVal.layoutManager=LinearLayoutManager(this@ActivityDiagnostics)
                            recyclerViewGeneralVal.adapter=Adapter_General_Values(this@ActivityDiagnostics)
                        }
                        else
                        {
                            listOfPIDs.replaceAll { pid->PID(ObdModes.MODE_01,pid.PID.substring(0,2)) }
                        }
                        buttonNextErrorCausedFreeze.isVisible=false
                        textDTCCausedFreezeFrame.isVisible=false
                        presentMode=1
                        //sleep(100)
                        SendThread.handlerThread.sendMessage(
                            Message.obtain(
                                SendThread.handlerThread,
                                NEXT_MESSAGE_SEND_THREAD
                            )
                        )

                    }

                }

               // flagcalculationsENdCanincrementacualcommandnumber=true
            }
        }


        if (!intent.getBooleanExtra("IsWifiDevice", false)) {
            mmSocket = MySingleton.getInstance().BlutSocket
            if (mmSocket!!.isConnected)
            {
                initialConn()


            }
        }
        else
        {
            SocketWIFI=MySingleton.getInstance().SocketWIFI
            if (SocketWIFI!!.isConnected)
            {
                initialConn()

            }
        }




// calback called when back button pressed- stop of connection with ELM 237
        val backCallback=object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    startupdates=false
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
                startActivity(Intent(this@ActivityDiagnostics,DiagnosticsOBD::class.java))
                finish()
            }

        }
        onBackPressedDispatcher.addCallback(backCallback)

    }

    fun initialConn()
    {
        Thread {
            while (this@ActivityDiagnostics.lifecycle.currentState != Lifecycle.State.STARTED) {

            }
            runOnUiThread {

               /* checkingMinAllowedmsHex =
                    if (checkingMinAllowedms < 16) {
                        "0" + Integer.toHexString(checkingMinAllowedms)
                    } else {
                        Integer.toHexString(checkingMinAllowedms)
                    }*/
                ThreadReadWrite(
                    "01 00 $checkingMinAllowedmsHex".trim { it <= ' ' },
                    null,
                    SocketWIFI,
                    mmSocket,
                    handler1,
                    0
                ).start()
                mainExecutor.execute(runnablewaitGiff)
                //ThreadReadWrite(null,PID(ObdModes.MODE_01,"00"), mmSocket,handler1,0).start()
                //waitingLAyout.isVisible=true
               //popuupWindow.showAsDropDown(activitybackground)
           }
        }.start()
    }




    fun startGeneralInfoUpdates()
    {
        SendThread=ThreadSendGenralDiagnostic(this)
        SendThread.start()
    }

    fun parseJsonWithDTCcodes()
    {
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
            listDTC =
                json.decodeFromString<DTCClass>(returnStringPIDsforgraphs.toString())
        }
        else{
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
            listDTC =
                json.decodeFromString<DTCClass>(returnStringPIDsforgraphs.toString())
        }
    }


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