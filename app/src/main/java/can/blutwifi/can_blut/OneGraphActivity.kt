package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.text.Editable
import android.util.Size
import android.util.TypedValue
import android.view.*
import android.view.View.OnScrollChangeListener
import android.view.View.OnTouchListener
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.google.android.material.slider.Slider
import com.pnuema.android.obd.enums.ObdModes
import com.pnuema.android.obd.models.PID

import java.lang.Long
import java.lang.Thread.sleep
import java.net.Socket
import java.util.Collections.max
import java.util.Collections.min
import kotlin.math.absoluteValue
import kotlin.random.Random

@SuppressLint("MissingPermission")
class OneGraphActivity : AppCompatActivity() {
    var beginningSpan=0f
    var madeSpan=0f
    var graphScaleFactor=1f
    var graphVerticalScrollFloat=0f
    var startX=0f
    var startY=0f
    var presentX=0f
    var presentY=0f
    var meanYforSpan=0f
    var checkingMinAllowedms=3
    var checkingMinAllowedmsHex="03"
    var numberOfSendMessages=0
    var numberOfGoodResponses=0
    var differenceInListsizeBetweentwoTouches=0
    var oldValueListSize=0
    lateinit var legacySize:Size
    var initialThreadtime=0L
    var relativepositionwithwereInPast=0
    var flagchangeGraphs=false
    val FINISH_SEND_THREAD=-99
    val NEXT_MESSAGE_SEND_THREAD=-100
    lateinit var SendThread:ThreadOneGraphValuesUpdate
lateinit var Graphview:GraphView
    var OBDresult=""
    lateinit var alertDialogChangeNumberOfSaaplesOnGraph:AlertDialog
    lateinit var  alertdialogYaxisSettings:AlertDialog
    var startupdates=false
lateinit var popuupWindow:PopupWindow
lateinit var activitybackground:LinearLayout
lateinit var switchHideAppear:SwitchCompat
lateinit var editTextSampleTime:EditText
lateinit var  editTextMaxSamples:EditText
lateinit var textPopUpLaoding:TextView
lateinit var checkboxLinearBuffer:CheckBox
lateinit var checkboxRingBuffer:CheckBox
    var isRingBuffer=true
    var flaginitialmesaage=true
    var flagfinalDelay=false
    var SampleTime=5
    val maxvalueOfSampleTime=1000
    val mintimeOfSampleTime=0
    var mutablelistofinfotoshowPoint: MutableList<Float>?= mutableListOf()
    val MESSAGE_OBD_WRONG_RESULT: Int = 6
    var flagcalculationsENdCanincrementacualcommandnumber=true
    val MESSAGE_OBD_RESULT: Int = 5
    val MESSAGE_SAVE_TOFILE:Int=-2
    val MESSAGE_START_SAVING=-3
    val MESSAGE_FINISH_SAVING=-4
    val MESSAGE_RESPONSE_ELM237_COMMAND: Int = 11
    var PIDlenghtAdd=0
    var readwriteThreadFinised=true
    lateinit var observer:OneGraphLifecycleObserver
    var numberOFValuesOnGraph=20
    var maxnumberofsamplesinArrays=20000
    var maxnumberofmaxnumberofsamplesinArrays=100000
    var numberOfSamplesTocutAfterOverLoad=2000
    var minnumberofmaxnumberofsamplesinArrays=10000
    var maxNumberofSamplesonGraphInOneTime=100
    var minNumberofSamplesonGraphInOneTime=20
    var maxYaxisvalue=0f
    var minYaxisValue=0f
    var numberOfQueriesbeforeGraphUpdate=0
    var flagDefaultYaxisValues=true
    lateinit var editTextmaxYvalue:EditText
    lateinit var editTextminYvalue:EditText
    lateinit var runnablewait:Runnable
    var countertoTouch=0

    // start sample do disply on graphview
    // var startOFGraph=maxNumberofSamplesonGraphInOneTime-1
    // end sample to display on graph view
    //var endofGraph=maxNumberofSamplesonGraphInOneTime-numberOFValuesOnGraph
    var numberofSamplesTOMOve=0
    // var SamplesjumpValueCoef=1f
    lateinit var handler1:Handler
    var numberOfGraphsTOShow=0
    lateinit var listofIndexesOfListOfArrraystoShow:MutableList<Int>

    var mmSocket: BluetoothSocket?=null
    var SocketWIFI: Socket?=null



// Mutable lists with graphs informations

    //list Of Booleans of witch graph is chosen to show
    val listOfVisiblegraphs: MutableList<Boolean> by lazy(LazyThreadSafetyMode.NONE) {
    (intent.getParcelableExtra("listOfIDsOfChoosengraphs",Array<Boolean>::class.java) as Array<Boolean>).toMutableList()
}
    //Boolean list of all plots that were in past chosen to show on graph
    lateinit var listOFGraphswereinPast:MutableList<Boolean>
    // list of all available graphs (not only chosen)!!!!!
    val listOfGraphs: MutableList<String> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listWithGraphsNames",Array<String>::class.java) as Array<String>).toMutableList()
    }
    // empty list of mutable lists of floats. The amount is number of chosen graphs in previous activity. Each mutable list of float has initial size of maximum number of
    // samples allowed to be shown o graph at one time
    val listOfValuesArrays : MutableList<MutableList<Float>> by lazy(LazyThreadSafetyMode.NONE) {
        MutableList(intent.getIntExtra("numberOfGraphs",1)){FloatArray(maxNumberofSamplesonGraphInOneTime).toMutableList()}
    }
    // list of all variables orders!!!
    val listofFormulas:MutableList<String> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listOfGraphsFormulas",Array<String>::class.java) as Array<String>).toMutableList()
    }

    val listofECUHeader:MutableList<kotlin.Long> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listofECUHeader",Array<kotlin.Long>::class.java) as Array<kotlin.Long>).toMutableList()
    }
    var listofECUHeaderVisible=mutableListOf<kotlin.Long>()
    val isMoreThenOneECU:Boolean by lazy(LazyThreadSafetyMode.NONE) {
        intent.getBooleanExtra("ismorethanOneECU",false)
    }

   var listofVisisbleformulas= mutableListOf<String>()


    // list of values of actually visible graphs
    lateinit var listOfValuesArraysVisible:MutableList<MutableList<Float>>
    //mutable list for arrays with values that were chosen during recording to hide

    // list of time values
    var listOfTimeValues=FloatArray(maxNumberofSamplesonGraphInOneTime).toMutableList()
    // flag to allow updates on graph when arrive response from vehicle (for particular query)
    var flagsallowUpdate=true
    // list of all availble PIDS not only Visible!!!!
    val ListOFPID:MutableList<PID> by lazy(LazyThreadSafetyMode.NONE) {

        (intent.getParcelableExtra("listOfPIDs",Array<PID>::class.java) as Array<PID>).toMutableList()
    }
   var listOFPIdsforVisibleGraphs= mutableListOf<PID>()

    //list of connected in one string modes (+0x40) and PIDs
    /*val listOFPIDandModesStrings:MutableList<String>by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listOFPIDandModesStrings",Array<String>::class.java) as Array<String>).toMutableList()
    }*/
    //list of Paints for VIsible graphs (used for legend and for lines on graph)
    lateinit var listOfPaintsForVisibleGraphs:MutableList<Paint>

    // waiting room for paints assigned to graphs whitch was disabled in one moment

    var accuallysendCommandToVehicle=-1
    var accuallysendCommandToCalc=-1
    var additionalVariableUsedOnlyFirstTimeForRecycler=0
    lateinit var touchListener:OnTouchListener
    var isGraphplay=true


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_graphs, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settingsSamples -> {
                //editTextSampleTime.text=Editable.Factory.getInstance().newEditable(SampleTime.toString())
                editTextMaxSamples.text=Editable.Factory.getInstance().newEditable(maxnumberofsamplesinArrays.toString())
                alertDialogChangeNumberOfSaaplesOnGraph.show()
                true
            }
            R.id.savetoFile-> {

                handler1.obtainMessage(MESSAGE_START_SAVING).sendToTarget()
                observer.saveFile()
                true
            }
            R.id.YaxisSettings-> {
                editTextmaxYvalue.text=Editable.Factory.getInstance().newEditable(maxYaxisvalue.toString())
                editTextminYvalue.text=Editable.Factory.getInstance().newEditable(minYaxisValue.toString())
                alertdialogYaxisSettings.show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("MissingPermission", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_graph)
        //pop up window saving
        val bitmapPlay=resources.getDrawable(R.drawable.baseline_play_arrow_24,null)
        val bitmapPause=resources.getDrawable(R.drawable.baseline_pause_24,null)
        val button_play_pause=findViewById<ImageView>(R.id.play_stopbutton).apply{
            scaleType=ImageView.ScaleType.FIT_XY
            isGraphplay=true
            setImageDrawable(bitmapPlay)
            setOnClickListener {
                if (isGraphplay)
                {
                    isGraphplay=false
                    setImageDrawable(bitmapPause)
                }
                else
                {
                    isGraphplay=true
                    setImageDrawable(bitmapPlay)
                    SendThread.handlerThread.sendMessage(
                        Message.obtain(
                            SendThread.handlerThread,
                            NEXT_MESSAGE_SEND_THREAD
                        )
                    )
                }

            }
        }
        val backtoLiveButton=findViewById<ImageView>(R.id.back_to_startbutton).apply{

            setOnClickListener {
                differenceInListsizeBetweentwoTouches = 0
                numberofSamplesTOMOve = 0
                flagsallowUpdate = true
                UpdateAndInvalidateGraph()
            }
        }
       activitybackground=findViewById(R.id.linerlayou)
        val texttest=findViewById<TextView>(R.id.texttest)

        runnablewait=object:Runnable{
            override fun run() {
                activitybackground.requestLayout()
                val popupView=this@OneGraphActivity.layoutInflater.inflate(R.layout.for_pop_up_window,null)
                textPopUpLaoding =popupView.findViewById<TextView>(R.id.textPopup)
                popuupWindow= PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                popuupWindow.isOutsideTouchable = true
                popuupWindow.isFocusable = true
                textPopUpLaoding.text =getString(R.string.loading_)
                popuupWindow.showAsDropDown(Graphview)
            }

        }



        // standard part of code to find useful screen size (useful mean without action bars)




        val metrics=this.windowManager.currentWindowMetrics
        val windowinsets=metrics.windowInsets
        val insets = windowinsets.getInsetsIgnoringVisibility(android.view.WindowInsets.Type.navigationBars() or  android.view.WindowInsets.Type.displayCutout())
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom




        // Legacy size that Display#getSize reports
        val bounds = metrics.getBounds()
        legacySize = Size(bounds.width() - insetsWidth,
            bounds.height() - insetsHeight)

        listOfValuesArraysVisible=listOfValuesArrays
        listOFGraphswereinPast=MutableList(listOfVisiblegraphs.size){false}
        listOfPaintsForVisibleGraphs= mutableListOf()
        listofIndexesOfListOfArrraystoShow=MutableList(listOfValuesArraysVisible.size){i->i}
        var i=0
        while (i<listOfVisiblegraphs.size) {
            if (listOfVisiblegraphs[i]) {
                listOFPIdsforVisibleGraphs.add(ListOFPID[i])
                listofVisisbleformulas.add(listofFormulas[i])
                listOFGraphswereinPast[i]=true
                listOfPaintsForVisibleGraphs.add(Paint().apply{
                    setARGB(255,Random.nextInt(80,220),Random.nextInt(80,220),Random.nextInt(80,220))
                    strokeWidth=7f
                })
                listofECUHeaderVisible.add(listofECUHeader[i])

            }
            i++
        }

        numberOfGraphsTOShow=listOfValuesArraysVisible.size

        Graphview=findViewById<GraphView?>(R.id.onegraph).apply { activityonegraph=this@OneGraphActivity
            listOFPaints=listOfPaintsForVisibleGraphs
            arrayofFunctionValues= MutableList(listOfValuesArraysVisible.size){ FloatArray(maxNumberofSamplesonGraphInOneTime) }

        }
        Graphview.layoutParams.height=legacySize.height
        observer=OneGraphLifecycleObserver(this.activityResultRegistry,this)
        lifecycle.addObserver(observer)

        val recyclerGraphsToChoose=findViewById<RecyclerView>(R.id.recyclergraphsforchoose)
        recyclerGraphsToChoose.layoutManager=LinearLayoutManager(this)
        recyclerGraphsToChoose.adapter=AdapterOneGraph(listOfGraphs,listOfVisiblegraphs,this)
        switchHideAppear=findViewById<SwitchCompat>(R.id.compatSwitchHide).apply{
            setOnCheckedChangeListener { p0, p1 ->
                additionalVariableUsedOnlyFirstTimeForRecycler=0
                recyclerGraphsToChoose.adapter=AdapterOneGraph(listOfGraphs,listOfVisiblegraphs,this@OneGraphActivity)
                if(p1)
                {
                    setTrackResource(R.drawable.vector_slide_track_on)
                }
                else
                {
                    setTrackResource(R.drawable.vector_slide_track)
                }
            }
            if (this.isChecked)
            {
                setTrackResource(R.drawable.vector_slide_track_on)
            }
            else
            {
                setTrackResource(R.drawable.vector_slide_track)
            }


        }
        //adding lifecycle observer to launch actions from menu
        //observer = GraphActivityLifecycleObserver(this.activityResultRegistry,this)
        //lifecycle.addObserver(observer)

        //alert dialog change Y range
        val builderYaxis = AlertDialog.Builder(this)
        builderYaxis.setTitle(getString(R.string.pu_max_min_Y))
        val dialogYaxisLayout = layoutInflater.inflate(R.layout.alert_dial_y_axis_sett, null)


        editTextmaxYvalue = dialogYaxisLayout.findViewById<EditText>(R.id.maxYvalue)
        editTextminYvalue = dialogYaxisLayout.findViewById<EditText>(R.id.minYvalue)
        editTextmaxYvalue.setFocusable(false)
        editTextminYvalue.setFocusable(false)
        editTextmaxYvalue.isFocusableInTouchMode=false
        editTextminYvalue.isFocusableInTouchMode=false
        val checkBoxDefault=dialogYaxisLayout.findViewById<CheckBox>(R.id.defaultCheckBox).apply {
            setOnCheckedChangeListener(
                object: CompoundButton.OnCheckedChangeListener
                {
                    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                        if(p1)
                        {
                            flagDefaultYaxisValues=true
                            maxYaxisvalue=max(MutableList(listOfValuesArraysVisible.size){i ->listOfValuesArraysVisible[i].max()})
                            minYaxisValue=min(MutableList(listOfValuesArraysVisible.size){i ->listOfValuesArraysVisible[i].min()})
                            editTextmaxYvalue.text=Editable.Factory.getInstance().newEditable(maxYaxisvalue.toString())
                            editTextminYvalue.text=Editable.Factory.getInstance().newEditable(minYaxisValue.toString())
                            editTextmaxYvalue.setFocusable(false)
                            editTextminYvalue.setFocusable(false)
                            editTextmaxYvalue.isFocusableInTouchMode=false
                            editTextminYvalue.isFocusableInTouchMode=false

                        }
                        else
                        {
                            editTextmaxYvalue.text.clear()
                            editTextminYvalue.text.clear()
                            flagDefaultYaxisValues=false
                            editTextmaxYvalue.setFocusable(true)
                            editTextminYvalue.setFocusable(true)
                            editTextmaxYvalue.isFocusableInTouchMode=true
                            editTextminYvalue.isFocusableInTouchMode=true
                        }
                    }

                }
            )
            this.isChecked=true
        }
        builderYaxis.setView(dialogYaxisLayout)
        builderYaxis.setPositiveButton("OK")
        { dialogInterface, i ->
            try{
               if(!checkBoxDefault.isChecked) {
                   var possibleNewMaxYvalue = editTextmaxYvalue.text.toString().toFloat()
                   var possibleNewMinYvalue = editTextminYvalue.text.toString().toFloat()
                   if (possibleNewMaxYvalue >= possibleNewMinYvalue) {
                       maxYaxisvalue = possibleNewMaxYvalue
                       minYaxisValue = possibleNewMinYvalue
                   }
               }

            }
            catch (e:Exception)
            {

                val text = getString(R.string.wrng_num)
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(this, text, duration)
                toast.show()
            }


        }
        alertdialogYaxisSettings=builderYaxis.create()


        //alert dialog to change sample time and number of samples on graph
        val builder = AlertDialog.Builder(this)

        val dialogLayout = layoutInflater.inflate(R.layout.alert_change_samples_graphs, null)
        //editTextSampleTime = dialogLayout.findViewById<EditText>(R.id.sampletime)
        editTextMaxSamples = dialogLayout.findViewById<EditText>(R.id.maxNumberOfSamples)
        val sliderSamples=dialogLayout.findViewById<Slider>(R.id.slider_samples).apply {
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    // Responds to when slider's touch event is being started
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    // Responds to when slider's touch event is being stopped
                }
            })

            addOnChangeListener { slider, value, fromUser ->
                SampleTime=value.toInt()
            }
        }
       checkboxLinearBuffer=dialogLayout.findViewById<CheckBox>(R.id.linearbuffer).apply {
            setOnClickListener{
                checkboxRingBuffer.isChecked=false
                checkboxLinearBuffer.isChecked=true
            }

        }
        checkboxRingBuffer=dialogLayout.findViewById<CheckBox>(R.id.ringbuffer).apply {
            isChecked=true
            setOnClickListener{
                checkboxRingBuffer.isChecked=true
                checkboxLinearBuffer.isChecked=false
            }
        }
        builder.setView(dialogLayout)
        builder.setPositiveButton("OK")
        {dialogInterface, i ->
            try {
                val maxNumberOfSamples=editTextMaxSamples.text.toString().toInt()
                if (maxNumberOfSamples>maxnumberofmaxnumberofsamplesinArrays)
                {
                    maxnumberofsamplesinArrays=maxnumberofmaxnumberofsamplesinArrays
                }
                else if (maxNumberOfSamples<minnumberofmaxnumberofsamplesinArrays)
                {
                    maxnumberofsamplesinArrays=minnumberofmaxnumberofsamplesinArrays
                }
                else
                {
                    maxnumberofsamplesinArrays=maxNumberOfSamples
                }
               // editTextMaxSamples.text=Editable.Factory.getInstance().newEditable(maxnumberofsamplesinArrays.toString())
                numberofSamplesTOMOve=0
                flagsallowUpdate = true
               /* val text = getString(R.string.set_val_sampl_time)+"$SampleTime ms\n"+
                        getString(R.string.max_sampl)+ "$maxnumberofsamplesinArrays "
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(this, text, duration)
                toast.show()*/

            }
            catch (e:Exception)
            {
                val text = getString(R.string.wrng_num)
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(this, text, duration)
                toast.show()
            }
        }
        alertDialogChangeNumberOfSaaplesOnGraph=builder.create()
//processing of responses
        handler1=object:Handler(Looper.getMainLooper())
        {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what==MESSAGE_START_SAVING)
                {
                    SendThread.handlerThread.sendMessage(Message.obtain(SendThread.handlerThread,FINISH_SEND_THREAD))
                    popuupWindow.isOutsideTouchable = true
                    popuupWindow.isFocusable = true
                    textPopUpLaoding.text=getString(R.string.saving_)
                    popuupWindow.showAsDropDown(Graphview)
                    //startupdates=false

                }
                else if (msg.what==MESSAGE_FINISH_SAVING)
                {
                    popuupWindow.dismiss()
                    //startupdates=true
                    cyclicValuesUpdate()
                }
                else if (msg.what==MESSAGE_OBD_RESULT || msg.what==MESSAGE_RESPONSE_ELM237_COMMAND)
                {
                    //readwriteThreadFinised=true
                    OBDresult =msg.obj as String

                    if (flaginitialmesaage)
                    {
                        flaginitialmesaage=false
                        //startupdates = true
                        popuupWindow.dismiss()
                        initialThreadtime = SystemClock.elapsedRealtime()
                        listOfTimeValues.add(0f)
                        cyclicValuesUpdate()
                       /* if(checkingMinAllowedms<36)
                        {
                            if (numberOfSendMessages>=10)
                            {
                                if (numberOfGoodResponses>=9)
                                {
                                    checkingMinAllowedms=(2f*checkingMinAllowedms.toFloat()).toInt()
                                    checkingMinAllowedmsHex=
                                        if (checkingMinAllowedms<16)
                                        {
                                            "0"+Integer.toHexString(checkingMinAllowedms)
                                        }
                                        else
                                        {
                                            Integer.toHexString(checkingMinAllowedms)
                                        }

                                    ThreadReadWrite(
                                        "AT ST $checkingMinAllowedmsHex".trim { it <= ' ' },
                                        null,
                                        SocketWIFI,
                                        mmSocket,
                                        handler1,
                                        0
                                    ).start()
                                    flagfinalDelay=true
                                    flaginitialmesaage=false

                                }
                                else {
                                    numberOfGoodResponses=0
                                    numberOfSendMessages = 0
                                    checkingMinAllowedms+=4
                                    checkingMinAllowedmsHex=
                                        if (checkingMinAllowedms<16)
                                        {
                                            "0"+Integer.toHexString(checkingMinAllowedms)
                                        }
                                    else
                                        {
                                            Integer.toHexString(checkingMinAllowedms)
                                        }

                                    ThreadReadWrite(
                                        "AT ST $checkingMinAllowedmsHex".trim { it <= ' ' },
                                        null,
                                        SocketWIFI,
                                        mmSocket,
                                        handler1,
                                        0
                                    ).start()
                                }
                            }
                            else
                            {

                                if (((OBDresult.length.toFloat()-(MySingleton.getInstance().numberOfDetectedECUs.toFloat()-1f)*2f)/24f).toInt()>=MySingleton.getInstance().numberOfDetectedECUs)
                                {
                                    numberOfGoodResponses++
                                }
                                /*if (!OBDresult.contains("NODATA")) {
                                    numberOfGoodResponses++
                                }*/
                                ThreadReadWrite(null,PID(ObdModes.MODE_01,"00"),SocketWIFI, mmSocket,handler1,0).start()
                                numberOfSendMessages++
                            }
                        }
                        else
                        {
                            flaginitialmesaage=false

                            //startupdates=true
                            popuupWindow.dismiss()
                            initialThreadtime=SystemClock.elapsedRealtime()
                            listOfTimeValues.add(0f)
                            cyclicValuesUpdate()
                        }
                    */


                        //ThreadReadWrite(null,PID(ObdModes.MODE_01,"00"), mmSocket!!,handler1,0).start()
                    }
                    else if (flagchangeGraphs)
                    {
                        flagchangeGraphs=false
                        SendThread.handlerThread.sendMessage(Message.obtain(SendThread.handlerThread,NEXT_MESSAGE_SEND_THREAD))
                    }
                    else
                    {
                       // texttest.text=OBDresult
                        accuallysendCommandToCalc=accuallysendCommandToVehicle
                        if (isGraphplay) {
                            SendThread.handlerThread.sendMessage(
                                Message.obtain(
                                    SendThread.handlerThread,
                                    NEXT_MESSAGE_SEND_THREAD
                                )
                            )
                        }
                        if(listOfTimeValues.size>maxnumberofsamplesinArrays)
                        {
                            if (checkboxRingBuffer.isChecked) {
                                numberofSamplesTOMOve = 0
                                flagsallowUpdate = true
                                listOfTimeValues = listOfTimeValues.subList(
                                    numberOfSamplesTocutAfterOverLoad,
                                    listOfTimeValues.size
                                )
                                listOfValuesArraysVisible =
                                    MutableList(listOfValuesArraysVisible.size) { i ->
                                        listOfValuesArraysVisible[i].subList(
                                            numberOfSamplesTocutAfterOverLoad,
                                            listOfValuesArraysVisible[i].size
                                        )
                                    }
                                UpdateAndInvalidateGraph()
                            }
                            else
                            {
                                SendThread.handlerThread.sendMessage(Message.obtain(SendThread.handlerThread,FINISH_SEND_THREAD))
                                //startupdates=false
                            }
                        }
                        else {
                            // when received response contain nodata there is added 0 to present graph
                            /* to know for whitch graph the respose arrived there is special variable set after each sent message to ELM 237
                            It is set by ThreadOneGraphValuesUpdate*/
                            //PIDlenghtAdd=((listOFPIdsforVisibleGraphs[accuallysendCommandToCalc].PID.length.toFloat())*1.5f -3f).toInt()

             // texttest.text=OBDresult
                         var w=0
                        listOFPIdsforVisibleGraphs.forEach { PID ->
                            if (PID==listOFPIdsforVisibleGraphs[accuallysendCommandToCalc])
                            {
                        listOfValuesArraysVisible[w].add(
                            CalculateResponse().calculateNumericwithMoreECUs(
                                PIDlenghtAdd,
                                OBDresult, listofVisisbleformulas[w],
                                listofECUHeaderVisible[w]))
                            }
                             w++
                        }

                //texttest.text=OBDresult

                            if (accuallysendCommandToCalc==0) {
                                listOfTimeValues.add(
                                    (SystemClock.elapsedRealtime().toFloat()-initialThreadtime.toFloat())/1000f
                                )


                            }

                            // if graph not moved, there is invalidated with new value
                            if (flagsallowUpdate){// && listOfTimeValues.size % 5==0) {
                                UpdateAndInvalidateGraph()
                            }
                        }

                    }



                }
                else if (msg.what==MESSAGE_OBD_WRONG_RESULT)
                {

                    //readwriteThreadFinised=true
                    if (!flaginitialmesaage) {

                        listOfValuesArraysVisible[accuallysendCommandToCalc].add(0f)

                        if (accuallysendCommandToCalc==0) {
                            listOfTimeValues.add(
                                (SystemClock.elapsedRealtime().toFloat()-initialThreadtime.toFloat())/1000f
                            )



                        }
                        if (flagsallowUpdate) {
                            UpdateAndInvalidateGraph()
                        }

                    }

                }
                flagcalculationsENdCanincrementacualcommandnumber=true
            }
        }





// scale listener for zoom gestures
        val scaleGestureListener=object:ScaleGestureDetector.OnScaleGestureListener
        {

            override fun onScale(p0: ScaleGestureDetector): Boolean {
                madeSpan=p0.currentSpan-beginningSpan
                meanYforSpan=p0.focusY
                return true
            }

            override fun onScaleBegin(p0: ScaleGestureDetector): Boolean {
                beginningSpan=p0.currentSpan
                return true
            }

            override fun onScaleEnd(p0: ScaleGestureDetector) {
                madeSpan=0f
            }

        }

        val scaleGestureDtector=ScaleGestureDetector(this,scaleGestureListener)
 // touch listener responsible for processing graphs moving and zooming
        touchListener=object :OnTouchListener {
            @SuppressLint("SuspiciousIndentation")
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {

                if (p0 is GraphView) {

                    scaleGestureDtector.onTouchEvent(p1!!)
                    graphScaleFactor+=madeSpan

                    if (graphScaleFactor>legacySize.width.toFloat()/10000f)
                    {
                        madeSpan=0f
                        graphScaleFactor=0f
                        if (numberOFValuesOnGraph>minNumberofSamplesonGraphInOneTime)
                        {
                            numberOFValuesOnGraph--
                        }
                        try {
                            maxYaxisvalue -= (0.1f * meanYforSpan / (p0.height-p0.height/10).toFloat()) * maxYaxisvalue
                            minYaxisValue += (0.1f * (1-meanYforSpan / (p0.height-p0.height/10).toFloat())) * maxYaxisvalue
                            if (maxYaxisvalue <= minYaxisValue) {
                                maxYaxisvalue += 0.01f * maxYaxisvalue
                                minYaxisValue -= 0.01f * maxYaxisvalue
                            }
                        }
                        catch (e:Exception)
                        {

                        }
                    }
                    else if (graphScaleFactor<-legacySize.width.toFloat()/10000f)
                    {
                        graphScaleFactor=0f
                        madeSpan=0f
                        if (numberofSamplesTOMOve>0)
                        {
                            numberofSamplesTOMOve--
                        }
                        if (MutableList(listOfValuesArraysVisible.size){i->listOfValuesArraysVisible[i].size}.min()-numberOFValuesOnGraph-numberofSamplesTOMOve>0 && numberOFValuesOnGraph<maxNumberofSamplesonGraphInOneTime)
                        {
                            numberOFValuesOnGraph++
                        }
                        try {
                            maxYaxisvalue +=  (0.1f * (1-meanYforSpan / (p0.height-p0.height/10).toFloat())) * maxYaxisvalue
                            minYaxisValue -= (0.1f * meanYforSpan / (p0.height-p0.height/10).toFloat()) * maxYaxisvalue

                        }
                        catch (e:Exception)
                        {

                        }

                    }

                    val index = p1?.actionIndex

                    if (p1!!.actionMasked == MotionEvent.ACTION_DOWN) {

                        startX = p1.getX(index!!)
                        startY = p1.getY(index)

                        mutablelistofinfotoshowPoint=p0.mutablelistOFpointsPositionsOnGraph.find { predicate->((predicate[3]-startX).absoluteValue<50f
                                && (predicate[2]-startY).absoluteValue<50f)}

                        if (mutablelistofinfotoshowPoint!=null)
                        {
                           p0.XtoshowPointValue=mutablelistofinfotoshowPoint!![3]
                           p0.YtoshowPointValue=mutablelistofinfotoshowPoint!![2]
                           p0.TextPointValue=(((p0.arrayofFunctionValues[mutablelistofinfotoshowPoint!![0].toInt()][mutablelistofinfotoshowPoint!![1].toInt()])*10f).toInt().toFloat()/10f).toString()
                        }
                        else
                        {
                            p0.TextPointValue=""
                        }
                    }
                    else if(p1.actionMasked == MotionEvent.ACTION_MOVE) {

                      /*  countertoTouch++
                        if (countertoTouch>5)
                        {
                            countertoTouch=0


                        }*/
                        presentX = p1.getX(index!!)
                        presentY = p1.getY(index)

                        graphVerticalScrollFloat+=presentX-startX


                        if (graphVerticalScrollFloat>legacySize.width.toFloat()/15f)
                        {
                            startX = p1.getX(index!!)
                            startY = p1.getY(index)
                            graphVerticalScrollFloat=0f
                            if (MutableList(listOfValuesArraysVisible.size){i->listOfValuesArraysVisible[i].size}.min() -numberOFValuesOnGraph-numberofSamplesTOMOve>0) {
                                numberofSamplesTOMOve++
                            }
                        }
                        else if (graphVerticalScrollFloat<-legacySize.width.toFloat()/15f)
                        {
                            startX = p1.getX(index!!)
                            startY = p1.getY(index)
                            graphVerticalScrollFloat=0f
                            if (numberofSamplesTOMOve>0) {
                                numberofSamplesTOMOve--
                            }

                        }

                    }
                    // flag allow updates always true when graph not moved, numberofSamplesTOMOve==0
                    if (oldValueListSize>0 && !flagsallowUpdate) {
                        numberofSamplesTOMOve +=
                            MutableList(listOfValuesArraysVisible.size) { i -> listOfValuesArraysVisible[i].size }.min() - oldValueListSize
                        if (MutableList(listOfValuesArraysVisible.size){i->listOfValuesArraysVisible[i].size}.min() -numberOFValuesOnGraph-numberofSamplesTOMOve<1){
                            numberofSamplesTOMOve -=
                                MutableList(listOfValuesArraysVisible.size) { i -> listOfValuesArraysVisible[i].size }.min() - oldValueListSize
                        }
                    }

                        oldValueListSize=MutableList(listOfValuesArraysVisible.size){i->listOfValuesArraysVisible[i].size}.min()

                    flagsallowUpdate= numberofSamplesTOMOve==0


                    p0.arrayofFunctionValues =MutableList(numberOfGraphsTOShow){i->listOfValuesArraysVisible[listofIndexesOfListOfArrraystoShow[i]].subList(listOfValuesArraysVisible[listofIndexesOfListOfArrraystoShow[i]].size-numberofSamplesTOMOve-numberOFValuesOnGraph,listOfValuesArraysVisible[listofIndexesOfListOfArrraystoShow[i]].size-numberofSamplesTOMOve).toFloatArray()}
                    p0.listOFPaints =MutableList(numberOfGraphsTOShow){i->listOfPaintsForVisibleGraphs[listofIndexesOfListOfArrraystoShow[i]]}
                    p0.minOFarrayofTimeValues=listOfTimeValues[listOfTimeValues.size-numberofSamplesTOMOve-numberOFValuesOnGraph]
                    p0.maxOFarrayofTimeValues=listOfTimeValues[listOfTimeValues.size-numberofSamplesTOMOve-1]
                    if (flagDefaultYaxisValues)
                    {
                        maxYaxisvalue=max(MutableList(numberOfGraphsTOShow){i ->listOfValuesArraysVisible[listofIndexesOfListOfArrraystoShow[i]].max()})
                        minYaxisValue=min(MutableList(numberOfGraphsTOShow){i ->listOfValuesArraysVisible[listofIndexesOfListOfArrraystoShow[i]].min()})

                    }
                    p0.maxOFarrayofFunctionValues=maxYaxisvalue
                    p0.minOFarrayofFunctionValues=minYaxisValue
                    p0.invalidate()

                }

                return true
            }
        }


      //few actions before connection with Bluetooth device ELM237
        //adding touch listener to move and zoom graph
        //show "waiting (loading)" pop up window, dismissed after connection with ELM 237
        Graphview.setOnTouchListener(touchListener)

        if (!intent.getBooleanExtra("IsWifiDevice", false)) {
            mmSocket = MySingleton.getInstance().BlutSocket
            if (mmSocket!!.isConnected) {
                initialConn()
            }
        }
        else
        {
            SocketWIFI=MySingleton.getInstance().SocketWIFI
            if (SocketWIFI!!.isConnected) {
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
                startActivity(Intent(this@OneGraphActivity,DiagnosticsOBD::class.java))
                finish()
            }

        }
        onBackPressedDispatcher.addCallback(backCallback)
    }


    //function starting cyclic message sending to update values in graphs
    fun cyclicValuesUpdate()
    {
        SendThread=ThreadOneGraphValuesUpdate(this)
        SendThread.start()
    }

    fun initialConn()
    {
        Thread {
            while (this@OneGraphActivity.lifecycle.currentState != Lifecycle.State.STARTED) {

            }
            runOnUiThread {
                mainExecutor.execute(runnablewait)
              /*  checkingMinAllowedmsHex =
                    if (checkingMinAllowedms < 16) {
                        "0" + Integer.toHexString(checkingMinAllowedms)
                    } else {
                        Integer.toHexString(checkingMinAllowedms)
                    }*/
                ThreadReadWrite(
                    "01 00".trim { it <= ' ' },
                    null,
                    SocketWIFI,
                    mmSocket,
                    handler1,
                    0
                ).start()
                //ThreadReadWrite(null,PID(ObdModes.MODE_01,"00"), mmSocket,handler1,0).start()

            }
        }.start()
    }

    //funcion called after response arrive to invalidate graphview
    @SuppressLint("SuspiciousIndentation")
    fun UpdateAndInvalidateGraph()
    {

        Graphview.arrayofFunctionValues =MutableList(numberOfGraphsTOShow){i->listOfValuesArraysVisible[listofIndexesOfListOfArrraystoShow[i]].subList(listOfValuesArraysVisible[listofIndexesOfListOfArrraystoShow[i]].size-numberOFValuesOnGraph,listOfValuesArraysVisible[listofIndexesOfListOfArrraystoShow[i]].size).toFloatArray()}
        Graphview.listOFPaints =MutableList(numberOfGraphsTOShow){i->listOfPaintsForVisibleGraphs[listofIndexesOfListOfArrraystoShow[i]]}
        Graphview.maxOFarrayofTimeValues=listOfTimeValues.last()
        Graphview.minOFarrayofTimeValues=listOfTimeValues[listOfTimeValues.size-numberOFValuesOnGraph]
        if (flagDefaultYaxisValues)
        {
            maxYaxisvalue=max(MutableList(numberOfGraphsTOShow){i ->listOfValuesArraysVisible[listofIndexesOfListOfArrraystoShow[i]].max()})
            minYaxisValue=min(MutableList(numberOfGraphsTOShow){i ->listOfValuesArraysVisible[listofIndexesOfListOfArrraystoShow[i]].min()})

        }
        Graphview.maxOFarrayofFunctionValues=maxYaxisvalue
        Graphview.minOFarrayofFunctionValues=minYaxisValue
        if (Graphview.XtoshowPointValue>Graphview.width.toFloat()/Graphview.numberofDisplayedTimevalues.toFloat()) {
            Graphview.XtoshowPointValue -= (Graphview.width.toFloat() / numberOFValuesOnGraph.toFloat()) / numberOfGraphsTOShow.toFloat()
        }
        else
        {
            Graphview.TextPointValue=""
        }
        Graphview.invalidate()

    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            Graphview.layoutParams.height=legacySize.width//TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400f, getResources().getDisplayMetrics()).toInt()
        }
        else
        {
            Graphview.layoutParams.width=95*legacySize.height/100//TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 800f, getResources().getDisplayMetrics()).toInt()
        }
    }




// when activity is closed connection with ELM 237 is broken
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