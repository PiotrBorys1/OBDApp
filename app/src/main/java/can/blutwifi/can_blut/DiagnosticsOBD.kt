package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.pnuema.android.obd.enums.ObdModes
import com.pnuema.android.obd.models.PID

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import net.objecthunter.exp4j.ExpressionBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Long
import java.net.*
import java.net.Socket
import java.util.*
import javax.net.ssl.HandshakeCompletedListener
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.random.Random

@SuppressLint("MissingPermission")
class DiagnosticsOBD : AppCompatActivity() {
    lateinit var intent1:Intent
    var listOFGraphsNames= mutableListOf<String>()
    var PIDsList= mutableListOf<PID>()
    var ResultPrimary=""
    var NumberOfECUs=1
    var NumberOfECUsinParticularquery=1
    var OBDresultforPutResponsesinOrder=""
    var flagcheckifmorethanOneECU=true
    var numberofBytesinoneResponse=0
    var flagCountNumberOfECUs=false
    var flagClearErrorCodes=false
    var listofECUHeader= mutableListOf<kotlin.Long>()
    var listofECUHeaderFreeze= mutableListOf<kotlin.Long>()
    var listofECUHEADERForward=mutableListOf<kotlin.Long>()
    var ECUSequenceNumber=1
    var ECUHeaderLong=1L
    var flagtestvideoarrived=true
    var listofECUsHeaderes= mutableListOf<kotlin.Long>()
    var listofECUsHeaderesForParticularQuery= mutableListOf<kotlin.Long>()
    val mutablelistOfVar = mutableListOf('A', 'B', 'C', 'D', 'E', 'F','G','H','I','J','K','L','M','N','O','U','P','R','S','T','V','W','X','Y','Z')
    val mutablelistOfVarForOneECU = mutableListOf('A', 'B', 'C', 'D', 'E', 'F')
    var flagmoreECUs=false
    //respone  mode=mode+0x40
   //var listOfModesAndPIDsStrings= mutableListOf("410C","410D","4111","419A","41AA","419A","41AA")
    var listOfGraphsFormulas=mutableListOf<String>()
    var stringResulfFromPIDquery=""
    var flagGeneralInfoChoosen=false
    var flagGraphsCHoosen=false
    var listOfPidsQueryingForSupportedPIDS= mutableListOf(//PID(ObdModes.MODE_09,"04"),
        PID(ObdModes.MODE_01,"00"),
        PID(ObdModes.MODE_01,"20"),
        PID(ObdModes.MODE_01,"40"),
        PID(ObdModes.MODE_01,"60"),
        PID(ObdModes.MODE_01,"80"),
        PID(ObdModes.MODE_01,"A0"),
        PID(ObdModes.MODE_01,"C0"),
        PID(ObdModes.MODE_02,"00"),
        PID(ObdModes.MODE_02,"20"),
        PID(ObdModes.MODE_02,"40"),
        PID(ObdModes.MODE_02,"60"),
        PID(ObdModes.MODE_02,"80"),
        PID(ObdModes.MODE_02,"A0"),
        PID(ObdModes.MODE_02,"C0")
    )
    lateinit var listOfAllPIdsforGraphsFromJson:AllAvailblaPIDsGraphs
    lateinit var listOfAllPIdsforGeneralInfoFromJson:AllAvailblaPIDsGraphs
    lateinit var textTitleDiadnosticOBD:TextView
    var listOfSupportedPIDshexnumbers= mutableListOf<String>()
    var flagaskingforSuppotedPIDs=true
    var nextPIDsupportedQuery=0
    var nextPIDFreezesupportedQuery=0
    var checkBoxStatesTable=MutableList(listOFGraphsNames.size){false}
    var listOFGraphsNamesForward= mutableListOf<String>()
    var listOfFormulasforward=mutableListOf<String>()
    var PIDsListForward= mutableListOf<PID>()
    // var listOfModesAndPIDsStringsForward= mutableListOf<String>()
    var numberOfGraphs=0
    var listOfGeneralInfosNames= mutableListOf<String>()
    var listOfGeneralInfoPIDs= mutableListOf<PID>()
    var listOfValuesForGeneralFormulas= mutableListOf<String>()
    var listOfGeneralInfosNamesFreeze= mutableListOf<String>()
    var listOfGeneralInfoPIDsFreeze= mutableListOf<PID>()
    var listOfValuesForGeneralFormulasFreeze= mutableListOf<String>()
    val MESSAGE_RESPONSE_ELM237_COMMAND: Int = 11
    val MESSAGE_OBD_RESULT: Int = 5
    val MESSAGE_OBD_WRONG_RESULT: Int = 6
    val MESSAGE_RESPONSE_STREAM_VIDEO: Int = 13
    var readwriteThreadFinised=true
    lateinit var instructionmanual: AlertDialog
    lateinit var popuupWindow:PopupWindow
    lateinit var textPopUpLaoding:TextView
    lateinit var layoutGraphsSelect:ConstraintLayout
    lateinit var runnablewait:Runnable
    var listELM237Commnads= mutableListOf<ThreadReadWrite>()
    lateinit var handler1: Handler
    var nextELM237Command=0
    var OBDresult=""
    var flaginitialmesaage=true
    lateinit var BlutDeice:BluetoothDevice

    var mmSocket: BluetoothSocket?=null
    var SocketWIFI: Socket?=null
    var SSLSocketWIFI: Socket?=null
    var SocketWiFIKTOR:io.ktor.network.sockets.Socket?=null


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

    @SuppressLint("MissingPermission", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics_obd)



        val buttonGraphs=findViewById<Button>(R.id.button_go_to_graphs)
        val recyclergraphs=findViewById<RecyclerView>(R.id.recyclergraphsnames)
        layoutGraphsSelect=findViewById<ConstraintLayout>(R.id.layout_for_graphs)
        layoutGraphsSelect.isVisible=false

        val buttonSeparatedGraphs=findViewById<Button>(R.id.buttonSeparatedGraphs)
        val buttonAllGraphsOnOne=findViewById<Button>(R.id.buttonAllGraphsOnOne)
        val buttonGeneralInfo=findViewById<Button>(R.id.buttonGeneralInfo)
        val buttonErrrorCodes=findViewById<Button>(R.id.buttonErrorCodes)
        val buttonClearErrorCodes=findViewById<Button>(R.id.buttonclearErrorCodes)
        val buttonOpenGraphFile=findViewById<Button>(R.id.buttonOpengraphFile).apply {
            setOnClickListener {
                this@DiagnosticsOBD.startActivity(Intent(this@DiagnosticsOBD,ActivityOpenGraphFile::class.java))
                finish()
            }
         }
        textTitleDiadnosticOBD=findViewById<TextView>(R.id.textTitleDiadnosticOBD)
        val graphChooseTitle=findViewById<TextView>(R.id.graphChooseTitle)
        val checkAllGraphsTextView=findViewById<TextView>(R.id.checkAllGraphs)
        val switchCompatCheckAll=findViewById<SwitchCompat>(R.id.compatSwitchCheckall).apply {
            setOnCheckedChangeListener(object:CompoundButton.OnCheckedChangeListener
            {
                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    if (p1)
                    {
                        checkBoxStatesTable.fill(true)
                        recyclergraphs.layoutManager = LinearLayoutManager(this@DiagnosticsOBD)
                        recyclergraphs.adapter =
                            adapterGraphsnames(listOFGraphsNames, this@DiagnosticsOBD)
                    }
                    else
                    {
                        checkBoxStatesTable.fill(false)
                        recyclergraphs.layoutManager = LinearLayoutManager(this@DiagnosticsOBD)
                        recyclergraphs.adapter =
                            adapterGraphsnames(listOFGraphsNames, this@DiagnosticsOBD)
                    }
                }

            })
        }

        val topdrawable= ContextCompat.getDrawable(this,R.drawable.car3ff)?.apply {
            setBounds(0, 0, ((this.intrinsicWidth.toFloat()/this.intrinsicHeight.toFloat())*400f).toInt(), 400)
        }
        textTitleDiadnosticOBD.setCompoundDrawables(null,topdrawable,null,null)
        val popupView=this.layoutInflater.inflate(R.layout.for_pop_up_window,null)
       textPopUpLaoding=popupView.findViewById<TextView>(R.id.textPopup)


        popuupWindow= PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        runnablewait=object :Runnable{
            override fun run() {
                popuupWindow.isOutsideTouchable = true
                popuupWindow.isFocusable = true
                textPopUpLaoding.text = "Connecting..."
                popuupWindow.showAsDropDown(layoutGraphsSelect)
            }
        }


        //aleert dialog for menu instruction


        val builderManual = AlertDialog.Builder(this)
        builderManual.setTitle(getString(R.string.option_dialog_title))
        val dialogManualLayout = layoutInflater.inflate(R.layout.alertdialogmanual, null)
        val textMnual=dialogManualLayout.findViewById<TextView>(R.id.textMnual)
        if (resources.configuration.locales[0].language=="pl") {
            textMnual.text = "1. Wykresy- wybierz tą opcję, aby znaleźć oraz pokazać na wykresie dostępne wartości, wysyłane przez jednostę ECU (Electronic Central Unit) pojazdu.\n" +
                    "2. Ogólne informacje- wybierz tą opcję aby zobaczyć aktualne wartości rejestrowane przez czujniki pokładowe.\n" +
                    "3. Kody błędów- wybierz tą opcję aby odczytać zapisane w ECU kody błędów.\n" +
                    "4. Wyczyść kody błędów- wybierz tą opcję aby wyczyścić kody błędów zapisane przez ECU pojazdu.\n" +
                    "5. Otwórz plik z danymi wykresu- wybierz tą opcję aby otworzyć zapisany wcześniej plik .txt, zawierający zarejestowane dane z czujników pokładowych."
        }
        else {
            textMnual.text ="1.Graphs- choose this option to record vehicle data on graph. After clicking on this button, the connection with ECU (Electronic Central Unit) will be established and available values to record will appear.\n" +
                    "2. General info- choose this option to see current values obtained from vehicle sensors.\n" +
                    "3. Error codes- choose this option to print on screen errors stored in ECU.\n" +
                    "4. Clear error codes- choose this option to erase errors from ECU memory..\n" +
                    "5. Open graph data file- choose this option to open saved earlier .txt file with graph records."
        }
        builderManual.setView(dialogManualLayout)
        builderManual.setPositiveButton(getString(R.string.OK))
        {dialogInterface, i ->

        }
        instructionmanual=builderManual.create()


        handler1=object:Handler(Looper.getMainLooper()) {
            @SuppressLint("SuspiciousIndentation")
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                if (msg.what == MESSAGE_RESPONSE_ELM237_COMMAND) {
                    readwriteThreadFinised = true
                    nextELM237Command++
                    if (nextELM237Command < listELM237Commnads.size) {
                        listELM237Commnads[nextELM237Command].start()
                        readwriteThreadFinised = false
                    } else {
                        if (!flagClearErrorCodes) {
                            ThreadReadWrite(
                                null,
                                PID(ObdModes.MODE_01, "00"),
                                SocketWIFI,
                                mmSocket,
                                handler1,
                                0
                            ).start()

                        }
                        else
                        {
                            ThreadReadWrite(
                                null,
                                PID(ObdModes.MODE_04),
                                SocketWIFI,
                                mmSocket,
                                handler1,
                                0
                            ).start()
                        }
                        readwriteThreadFinised = false
                    }

                } else if (msg.what == MESSAGE_OBD_RESULT) {
                    readwriteThreadFinised = true
                    //OBDresult = msg.obj as String

                    if (flagClearErrorCodes)
                    {
                        flagClearErrorCodes=false
                        popuupWindow.dismiss()
                        val text = getString(R.string.err_cleared)
                        val duration = Toast.LENGTH_SHORT
                        val toast = Toast.makeText(this@DiagnosticsOBD, text, duration)
                        toast.show()
                        try {
                            if (mmSocket != null) {
                                mmSocket!!.close()
                                MySingleton.getInstance().BlutSocket = null
                            } else {
                                if (MySingleton.getInstance().wifiIP == "192.168.0.10") {
                                    SocketWIFI!!.close()
                                    MySingleton.getInstance().SocketWIFI = null
                                }
                            }
                            listELM237Commnads.clear()
                        }
                        catch (e:Exception)
                        {

                        }

                    }
                    else if (flaginitialmesaage) {
                       flaginitialmesaage=false

                        val json=Json { ignoreUnknownKeys = true }

                        if (resources.configuration.locales[0].language=="pl") {
                            val returnStringPIDsforgraphs = StringBuilder()
                            InputStreamReader(resources.openRawResource(R.raw.pidsforgraphspl)).use { isr ->
                                BufferedReader(isr).use { input ->
                                    var line = input.readLine()
                                    while (line != null) {
                                        returnStringPIDsforgraphs.append(line)
                                        line = input.readLine()
                                    }
                                }
                            }
                            listOfAllPIdsforGraphsFromJson=json.decodeFromString<AllAvailblaPIDsGraphs>(returnStringPIDsforgraphs.toString())
                            listOfAllPIdsforGeneralInfoFromJson= json.decodeFromString<AllAvailblaPIDsGraphs>(returnStringPIDsforgraphs.toString())
                        }
                        else{
                            val returnStringPIDsforgraphs = StringBuilder()
                            InputStreamReader(resources.openRawResource(R.raw.pidsforgraphs)).use { isr ->
                                BufferedReader(isr).use { input ->
                                    var line = input.readLine()
                                    while (line != null) {
                                        returnStringPIDsforgraphs.append(line)
                                        line = input.readLine()
                                    }
                                }
                            }
                            listOfAllPIdsforGraphsFromJson=json.decodeFromString<AllAvailblaPIDsGraphs>(returnStringPIDsforgraphs.toString())
                            listOfAllPIdsforGeneralInfoFromJson= json.decodeFromString<AllAvailblaPIDsGraphs>(returnStringPIDsforgraphs.toString())
                        }


                       /* val returnStringPIDsforGeneralInfo = StringBuilder()
                        InputStreamReader(resources.openRawResource(R.raw.pidsforgeneralinfo)).use { isr ->
                            BufferedReader(isr).use { input ->
                                var line = input.readLine()
                                while (line != null) {
                                    returnStringPIDsforGeneralInfo.append(line)
                                    line = input.readLine()
                                }
                            }
                        }
                        listOfAllPIdsforGeneralInfoFromJson= json.decodeFromString<AllAvailblaPIDsGraphs>(returnStringPIDsforGeneralInfo.toString())*/



                            ThreadReadWrite(
                                null,
                                listOfPidsQueryingForSupportedPIDS[nextPIDsupportedQuery],
                                SocketWIFI,
                                mmSocket,
                                handler1,
                                0
                            ).start()
                            nextPIDsupportedQuery++


                        //ThreadReadWrite(null,PID(ObdModes.MODE_01,"00"), mmSocket,handler1,0).start()
                    }

                    else if (flagaskingforSuppotedPIDs)
                    {
                       // OBDresult=msg.obj as String
                        //textTitleDiadnosticOBD.text=nextPIDsupportedQuery.toString()
                       try {
                            OBDresult=msg.obj as String
                               var flagfoundfirstspace=false

                               var i=0
                               var j=0
                               OBDresult.forEach { char-> if (char<=' ' && !flagfoundfirstspace)
                               {
                                   flagfoundfirstspace=true
                               }
                               else if (flagfoundfirstspace && char<=' ')
                               {
                                   NumberOfECUs++
                                   if (i==0)
                                   {
                                       numberofBytesinoneResponse=j-1
                                   }
                                   i++
                               }else
                               {
                                   flagfoundfirstspace=false
                               }
                                   j++
                               }
                        if (numberofBytesinoneResponse==0)
                        {
                            numberofBytesinoneResponse=OBDresult.length
                        }
                           if (nextPIDsupportedQuery==1)
                           {
                               listofECUsHeaderes= MutableList(NumberOfECUs){index->
                                   Long.parseLong(OBDresult.substring(0+index*(numberofBytesinoneResponse+2),OBDresult.indexOf(' ')+index*(numberofBytesinoneResponse+2)),16)
                               }
                               listofECUsHeaderes.sort()
                               MySingleton.getInstance().numberOfDetectedECUs=NumberOfECUs

                           }
                           //.text=NumberOfECUs.toString()
                            OBDresultforPutResponsesinOrder=OBDresult
                            var k=0
                            while (k<NumberOfECUs)
                            {
                             ECUHeaderLong=Long.parseLong(OBDresultforPutResponsesinOrder.substring(0+k*(numberofBytesinoneResponse+2),3+k*(numberofBytesinoneResponse+2)),16)
                             ECUSequenceNumber=listofECUsHeaderes.indexOf(ECUHeaderLong)
                             OBDresult=OBDresultforPutResponsesinOrder.substring(7+k*(numberofBytesinoneResponse+2),numberofBytesinoneResponse+k*(numberofBytesinoneResponse+2))
                               if(nextPIDsupportedQuery<=7) {
                                   FindsupprortedPIDsMoreECUs()
                               }
                                else
                               {

                                   FindsupprortedPIDsFreezeFrameMoreECUs()
                                   nextPIDFreezesupportedQuery++
                               }
                             k++
                            }
                           NumberOfECUs=1


                                    if (nextPIDsupportedQuery<=6 || (flagGeneralInfoChoosen && nextPIDsupportedQuery<listOfPidsQueryingForSupportedPIDS.size)) {
                                        ThreadReadWrite(
                                            null,
                                            listOfPidsQueryingForSupportedPIDS[nextPIDsupportedQuery],
                                            SocketWIFI,
                                            mmSocket,
                                            handler1,
                                            0
                                        ).start()
                                        nextPIDsupportedQuery++
                                    } else {

                                       /* listOfAllPIdsforGraphsFromJson.pids.filter { pidandmore->pidandmore.Mode=="09" }.forEach { pidandmore->
                                            listOFGraphsNames.add(pidandmore.Description!!+" ["+pidandmore.Units+"]")
                                            PIDsList.add(
                                                PID(
                                                    pidandmore.Mode!!,
                                                    pidandmore.PID!!
                                                )
                                            )
                                            listOfGraphsFormulas.add(pidandmore.Formula!!)
                                            listofECUHeader.add(listofECUsHeaderes[0])
                                        }*/


                                        //adding UDS read data service 0x22 to OBD data for melex vehicles

                                        /* if (!flagmoreECUs)
                                         {
                                             AddUDSReadDataServiceOneECU()
                                         }
                                         else
                                         {
                                             ECUSequenceNumber=0
                                             while (ECUSequenceNumber<listofECUsHeaderes.size) {
                                                 ECUHeaderLong=listofECUsHeaderes[ECUSequenceNumber]
                                                 AddUDSReadDataServiceMoreECUs()
                                                 ECUSequenceNumber++
                                             }
                                         }*/
                                        flagaskingforSuppotedPIDs = false
                                        ThreadReadWrite(
                                            null,
                                            PID(ObdModes.MODE_01, "00"),
                                            SocketWIFI,
                                            mmSocket,
                                            handler1,
                                            0
                                        ).start()
                                    }

                                }
                                catch (e:Exception)
                                {
                                    if (nextPIDsupportedQuery<=6 || (flagGeneralInfoChoosen && nextPIDsupportedQuery<listOfPidsQueryingForSupportedPIDS.size)) {
                                        ThreadReadWrite(
                                            null,
                                            listOfPidsQueryingForSupportedPIDS[nextPIDsupportedQuery],
                                            SocketWIFI,
                                            mmSocket,
                                            handler1,
                                            0
                                        ).start()
                                        nextPIDsupportedQuery++


                                    } else {

                                        /*listOfAllPIdsforGraphsFromJson.pids.filter { pidandmore->pidandmore.Mode=="09" }.forEach { pidandmore->
                                            listOFGraphsNames.add(pidandmore.Description!!+" ["+pidandmore.Units+"]")
                                            PIDsList.add(
                                                PID(
                                                    pidandmore.Mode!!,
                                                    pidandmore.PID!!
                                                )
                                            )
                                            listOfGraphsFormulas.add(pidandmore.Formula!!)
                                            // listofECUHeader.add(listofECUsHeaderes[0])
                                        }*/

                                        //adding UDS read data service 0x22 to OBD data for melex vehicles

                                        /* if (!flagmoreECUs)
                                            {
                                                AddUDSReadDataServiceOneECU()
                                            }
                                            else
                                            {
                                                ECUSequenceNumber=0
                                                while (ECUSequenceNumber<listofECUsHeaderes.size) {
                                                    ECUHeaderLong=listofECUsHeaderes[ECUSequenceNumber]
                                                    AddUDSReadDataServiceMoreECUs()
                                                    ECUSequenceNumber++
                                                }
                                            }*/

                                        flagaskingforSuppotedPIDs = false
                                        ThreadReadWrite(
                                            null,
                                            PID(ObdModes.MODE_01, "00"),
                                            SocketWIFI,
                                            mmSocket,
                                            handler1,
                                            0
                                        ).start()
                                    }
                                }
                            //}.start()

                    }
                    else
                    {
                        checkBoxStatesTable=MutableList(listOFGraphsNames.size){false}
                        popuupWindow.dismiss()
                        if (flagGeneralInfoChoosen)
                        {
                            flagGeneralInfoChoosen=false

                            startGeneralInfo()
                        }
                        else if (flagGraphsCHoosen) {
                            flagGraphsCHoosen=false
                            recyclergraphs.layoutManager = LinearLayoutManager(this@DiagnosticsOBD)
                            recyclergraphs.adapter =
                                adapterGraphsnames(listOFGraphsNames, this@DiagnosticsOBD)
                            switchCompatCheckAll.isVisible=true
                            checkAllGraphsTextView.isVisible=true
                        }
                    }
                }
                else if (msg.what==MESSAGE_OBD_WRONG_RESULT)
                {
                    readwriteThreadFinised=true

                    if (flagClearErrorCodes) {
                        popuupWindow.dismiss()
                        val text = getString(R.string.cannot_clr_err)
                        val duration = Toast.LENGTH_SHORT
                        val toast = Toast.makeText(this@DiagnosticsOBD, text, duration)
                        toast.show()
                        flagClearErrorCodes=false
                    }
                }
            }
        }




        buttonClearErrorCodes.setOnClickListener {
            flagClearErrorCodes=true
            StartConnection()

        }
        buttonGeneralInfo.setOnClickListener {
            flagGeneralInfoChoosen=true
            StartConnection()

        }

        buttonErrrorCodes.setOnClickListener {
            intent1= Intent(this,ErrorActivity::class.java).putExtra("IsWifiDevice",intent.getBooleanExtra("IsWifiDevice",false))
            startActivity(intent1)
            finish()
        }






        // buttons visible when buttonGraphs pressed
        buttonSeparatedGraphs.setOnClickListener {
            var i=0
            while (i<listOFGraphsNames.size)
            {
                if (checkBoxStatesTable[i]) {
                    listOFGraphsNamesForward.add(listOFGraphsNames[i])
                    PIDsListForward.add(PIDsList[i])
                    //listOfModesAndPIDsStringsForward.add(listOfModesAndPIDsStrings[i])
                    listOfFormulasforward.add(listOfGraphsFormulas[i])
                     listofECUHEADERForward.add(listofECUHeader[i])

                }
                i++
            }
            if (listOFGraphsNamesForward.size>0) {
                numberOfGraphs=listOFGraphsNamesForward.size
                intent1 = Intent(this, GraphsActivity::class.java).putExtra("listWithGraphsNames",listOFGraphsNamesForward.toTypedArray())
                   /* .putExtra(
                    "listOFPIDandModesStrings",listOfModesAndPIDsStringsForward.toTypedArray())*/.
                    putExtra("listOfPIDs",PIDsListForward.toTypedArray())
                    .putExtra("numberOfGraphs",numberOfGraphs).putExtra(
                        "listofformulas",listOfFormulasforward.toTypedArray()
                    ).putExtra(
                        "listofECUHeader",listofECUHEADERForward.toTypedArray()
                    ).putExtra("ismorethanOneECU",flagmoreECUs).putExtra("IsWifiDevice",intent.getBooleanExtra("IsWifiDevice",false))
               /* val instance = MySingleton.getInstance()
                instance.BlutSocket=mmSocket*/

                startActivity(intent1)
                finish()
            }
        }
        buttonAllGraphsOnOne.setOnClickListener {
            /*listOFGraphsNames= mutableListOf("Motor RPM")
            PIDsList= mutableListOf(PID(ObdModes.MODE_01,"0C"))
            listOfOrderofariable= mutableListOf("((A*256)+B)/4")*/
            if (checkBoxStatesTable.contains(true)) {
                intent1 = Intent(this, OneGraphActivity::class.java).putExtra(
                    "listOfIDsOfChoosengraphs",checkBoxStatesTable.toTypedArray()
                ).putExtra("listWithGraphsNames",listOFGraphsNames.toTypedArray()).putExtra(
                    "listOfPIDs",PIDsList.toTypedArray()
                ).putExtra("numberOfGraphs",checkBoxStatesTable.count { predicate->predicate } ).putExtra(
                    "listOfGraphsFormulas",listOfGraphsFormulas.toTypedArray())
                    .putExtra(
                        "listofECUHeader",listofECUHeader.toTypedArray()
                    ).putExtra("ismorethanOneECU",flagmoreECUs).putExtra("IsWifiDevice",intent.getBooleanExtra("IsWifiDevice",false))
                /*val instance = MySingleton.getInstance()
                instance.BlutSocket=mmSocket*/

                    startActivity(intent1)
                finish()

            }


        }



        buttonGraphs.setOnClickListener {
            it.isVisible=false
            buttonGeneralInfo.isVisible=false
            buttonErrrorCodes.isVisible=false
            layoutGraphsSelect.isVisible=true
            switchCompatCheckAll.isVisible=false
            checkAllGraphsTextView.isVisible=false
            flagGraphsCHoosen=true
            StartConnection()
        }
        val backCallback=object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                try {

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
                finish()
            }

        }
        onBackPressedDispatcher.addCallback(backCallback)
    }


    fun StartConnection()
    {
        Thread {
            try {
                runOnUiThread {
                    mainExecutor.execute(runnablewait)
                }
            if (!intent.getBooleanExtra("IsWifiDevice", false)) {

                if (MySingleton.getInstance().BlutSocket==null) {
                    val blutDEV: BluetoothDevice = MySingleton.getInstance().BludDevice!!
                    var uuidGood:UUID=UUID(18695992643584,-9223371485494954757)
                    blutDEV.uuids.forEach {
                        parceluuid->
                        if (parceluuid.uuid!=UUID.fromString("00000000-0000-1000-8000-00805f9b34fb"))
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
                    //runOnUiThread { textTitleDiadnosticOBD.text=MySingleton.getInstance().BludDevice!!.uuids.size.toString() }

                    mmSocket?.let { socket ->


                            socket.connect()


                        if (socket.isConnected) {
                            MySingleton.getInstance().BlutSocket=mmSocket
                            runOnUiThread {
                                addATcommands()
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
                }
                else
                {
                    mmSocket=MySingleton.getInstance().BlutSocket
                    if (!mmSocket!!.isConnected)
                    {
                        mmSocket!!.connect()
                    }
                    runOnUiThread {
                        addATcommands()
                    }
                }
            }
         else {

                if (MySingleton.getInstance().SocketWIFI == null) {

                        SocketWIFI = Socket(MySingleton.getInstance().wifiIP, MySingleton.getInstance().SocketPort,null,0)

                    if (SocketWIFI!!.isConnected) {
                        MySingleton.getInstance().SocketWIFI = SocketWIFI
                        runOnUiThread {addATcommands()}

                    } else {
                        runOnUiThread {
                            val text =getString(R.string.try_again)
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
                        addATcommands()
                    }
                }

            }


        } catch (e: Exception) {
                runOnUiThread {
                    val text =getString(R.string.cant_connect)
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(this, text, duration)
                    toast.show()
                   // textTitleDiadnosticOBD.text=MySingleton.getInstance().BludDevice!!.uuids[0].uuid.mostSignificantBits.toString()+"   "+
                   // MySingleton.getInstance().BludDevice!!.uuids[0].uuid.leastSignificantBits.toString()
                }

            }

        }.start()
    }


fun startGeneralInfo()
    {intent1 = Intent(this@DiagnosticsOBD, ActivityDiagnostics::class.java).putExtra(
        "listOfGeneralInfosNames",listOfGeneralInfosNames.toTypedArray()
    ).putExtra(
        "listOfGeneralInfoPIDs",listOfGeneralInfoPIDs.toTypedArray()
    ).putExtra(
        "listOfValuesForGeneralFormulas",listOfValuesForGeneralFormulas.toTypedArray()
    )
        .putExtra(
            "listofECUHeader",listofECUHeader.toTypedArray()
        ).putExtra(
            "listOfGeneralInfosNamesFreeze",listOfGeneralInfosNamesFreeze.toTypedArray()
        ).putExtra(
            "listOfGeneralInfoPIDsFreeze",listOfGeneralInfoPIDsFreeze.toTypedArray()
        ).putExtra(
            "listOfValuesForGeneralFormulasFreeze",listOfValuesForGeneralFormulasFreeze.toTypedArray()
        )
        .putExtra(
            "listofECUHeaderFreeze",listofECUHeaderFreeze.toTypedArray()
        ).putExtra("ismorethanOneECU",flagmoreECUs).putExtra("IsWifiDevice",intent.getBooleanExtra("IsWifiDevice",false))
        startActivity(intent1)
        finish()
}

    fun addATcommands()
    { //initial commands to ELM237 to reset present protocol and try to find the new one
        //protocol mean communication protocol with vehicle
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

       /* listELM237Commnads.add(
            ThreadReadWrite(
                "AT ST FF".trim { it <= ' ' },
                null,
                SocketWIFI,
                mmSocket,
                handler1,
                0
            )
        )*/
       /* listELM237Commnads.add(
            ThreadReadWrite(
                "AT CFC0".trim { it <= ' ' },
                null,
                SocketWIFI,
                mmSocket,
                handler1,
                0
            )
        )*/
        //start of fisrst command, what is start of ThreadReadWrite responsible for sending messages to ELM237 and receiving responses
        listELM237Commnads[nextELM237Command].start()
    }


    fun FindsupprortedPIDsMoreECUs()
    {

            ResultPrimary = OBDresult.substring(6, 8) + OBDresult.substring(9, 11) + OBDresult.substring(12, 14) + OBDresult.substring(15, 17)
            val receivedValue = Long.parseLong(ResultPrimary, 16)
            MutableList(32) { index ->
                //searching for supported PIDs from initial queries (queries responding with coded supported PIDs)
                if (receivedValue.and(2147483648L.shr(index)) > 0) {
                    (32 * (nextPIDsupportedQuery - 1) + index + 1).toLong()
                } else {
                    0L
                }
            }.filter { predicate ->
                predicate > 0
            }.forEach { pid ->
                if (flagGraphsCHoosen) {
                    listOfAllPIdsforGraphsFromJson.pids.filter { pidandmore ->
                        ( Long.parseLong(pidandmore.PID!!, 16) == pid && pidandmore.Mode=="01")
                    }.forEach { pidandmore ->
                        listOFGraphsNames.add(pidandmore.Description!! + " [" + pidandmore.Units + "]"+" "+ (ECUSequenceNumber+1).toString()+" ECU")
                        PIDsList.add(
                            PID(
                                pidandmore.Mode!!,
                                pidandmore.PID!!
                            )
                        )
                        listOfGraphsFormulas.add(pidandmore.Formula!!)
                        listofECUHeader.add(ECUHeaderLong)

                    }
                } else if (flagGeneralInfoChoosen) {

                    listOfAllPIdsforGeneralInfoFromJson.pids.filter { pidandmore ->
                        ( Long.parseLong(pidandmore.PID!!, 16) == pid && pidandmore.Mode=="01")
                    }.forEach { pidandmore ->
                        listOfGeneralInfosNames.add(pidandmore.Description!! + " [" + pidandmore.Units + "]"+" "+ (ECUSequenceNumber+1).toString()+" ECU")
                        listOfGeneralInfoPIDs.add(
                            PID(

                                pidandmore.Mode!!,
                                pidandmore.PID!!
                            )
                        )
                        listOfValuesForGeneralFormulas.add(pidandmore.Formula!!)
                        listofECUHeader.add(ECUHeaderLong)

                    }
                }
            }

    }

    fun FindsupprortedPIDsFreezeFrameMoreECUs()
    {
        ResultPrimary = OBDresult.substring(6, 8) + OBDresult.substring(9, 11) + OBDresult.substring(12, 14) + OBDresult.substring(15, 17)

        //ResultPrimary =OBDresult.substring(9, 11) + OBDresult.substring(12, 14) + OBDresult.substring(15, 17)+ OBDresult.substring(18, 20)
        val receivedValue = Long.parseLong(ResultPrimary, 16)
        MutableList(32) { index ->
            //searching for supported PIDs from initial queries (queries responding with coded supported PIDs)
            if (receivedValue.and(2147483648L.shr(index)) > 0) {
                (32 * (nextPIDFreezesupportedQuery) + index + 1).toLong()
            } else {
                0L
            }
        }.filter { predicate ->
            predicate > 0
        }.forEach { pid ->
                listOfAllPIdsforGeneralInfoFromJson.pids.filter { pidandmore ->
                    //must be mode 01, the same pids like in mode 02
                    ( Long.parseLong(pidandmore.PID!!, 16) == pid && pidandmore.Mode=="01")
                }.forEach { pidandmore ->
                    listOfGeneralInfosNamesFreeze.add(pidandmore.Description!! + " [" + pidandmore.Units + "]"+" "+ (ECUSequenceNumber+1).toString()+" ECU")
                    listOfGeneralInfoPIDsFreeze.add(
                        PID(

                            "02",
                            pidandmore.PID!!
                        )
                    )
                    listOfValuesForGeneralFormulasFreeze.add(pidandmore.Formula!!)
                    listofECUHeaderFreeze.add(ECUHeaderLong)

                }

        }

    }


    fun AddUDSReadDataServiceMoreECUs()
    {
        if (flagGraphsCHoosen) {
            listOfAllPIdsforGraphsFromJson.pids.filter { pidandmore ->
                Long.parseLong(pidandmore.PID!!, 16) ==34L
            }.forEach { pidandmore ->
                listOFGraphsNames.add(pidandmore.Description!!+" ["+pidandmore.Units+"]"+" "+ (ECUSequenceNumber+1).toString()+" ECU")
                PIDsList.add(
                    PID(
                        pidandmore.Mode!!,
                        pidandmore.PID!!
                    )
                )
                listOfGraphsFormulas.add(pidandmore.Formula!!)
                listofECUHeader.add(ECUHeaderLong)
            }
        } else if (flagGeneralInfoChoosen) {

            listOfAllPIdsforGeneralInfoFromJson.pids.filter { pidandmore ->
                Long.parseLong(pidandmore.PID!!, 16) == 34L
            }.forEach { pidandmore ->
                listOfGeneralInfosNames.add(pidandmore.Description!!+" ["+pidandmore.Units+"]"+ " "+(ECUSequenceNumber+1).toString()+" ECU")
                listOfGeneralInfoPIDs.add(
                    PID(

                        pidandmore.Mode!!,
                        pidandmore.PID!!
                    )
                )
                listOfValuesForGeneralFormulas.add(pidandmore.Formula!!)
                listofECUHeader.add(ECUHeaderLong)
            }
        }
    }


    /*override fun onStop() {
          super.onStop()
          finish()
      }*/

   /* override fun onDestroy() {
        super.onDestroy()

        //threadreadwrite.cancel()
        try {
            mmSocket!!.close()
            Thread.sleep(500)

        }
        catch(e:Exception)
        {

        }
        finish()

    }*/
}