package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.text.Editable
import android.util.Size
import android.view.*
import android.view.View.OnScrollChangeListener
import android.view.View.OnTouchListener
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.google.android.material.slider.Slider
import com.pnuema.android.obd.enums.ObdModes
import com.pnuema.android.obd.models.PID

import java.lang.Long
import java.net.Socket
import java.util.*
import kotlin.random.Random

@SuppressLint("MissingPermission")
class GraphsActivity : AppCompatActivity() {
    var beginningSpan=0f
    var madeSpan=0f
    var graphScaleFactor=1f
    var graphVerticalScrollFloat=0f
    var startX=0f
    var startY=0f
    var presentX=0f
    var presentY=0f
    var meanYforSpan=0f
    var presentGraphPos=0
    var initialThreadtime=0L
    var checkingMinAllowedms=3
    var additionalsamplestoMove=0
    var checkingMinAllowedmsHex="03"
    var numberOfSendMessages=0
    var numberOfGoodResponses=0
    lateinit var popuupWindow:PopupWindow
    lateinit var activitybackground:ConstraintLayout
    lateinit var editTextSampleTime:EditText
    lateinit var  editTextMaxSamples:EditText
    lateinit var textPopUpLaoding:TextView
    lateinit var checkboxLinearBuffer:CheckBox
    lateinit var checkboxRingBuffer:CheckBox
    lateinit var runnablewait:Runnable
    var countertoTouch=0
    var OBDresult=""
    var PIDlenghtAdd=0
    var flagRecyclerScrollSettle=false
    var flagRecyclerScrollDrag=false
    var flagRecyclerScrollIdle=false
    var flagfinalDelay=false
    lateinit var alertDialogChangeNumberOfSaaplesOnGraph:AlertDialog
    lateinit var  alertdialogYaxisSettings:AlertDialog
    var startupdates=false
    var sumOfScroll=0
    var flaginitialmesaage=true
    var SampleTime=150
    val maxvalueOfSampleTime=1000
    val mintimeOfSampleTime=20
    var maxYaxisvalue=0f
    var minYaxisValue=0f
    var flagDefaultYaxisValues=true
    lateinit var recyclerView:RecyclerView
    var differenceInListsizeBetweentwoTouches=0
    var oldValueListSize=0
    var flagcalculationsENdCanincrementacualcommandnumber=true
    val MESSAGE_OBD_WRONG_RESULT: Int = 6
    val MESSAGE_OBD_RESULT: Int = 5
    val MESSAGE_START_SAVING=-3
    val MESSAGE_FINISH_SAVING=-4
    val MESSAGE_RESPONSE_ELM237_COMMAND: Int = 11
    var readwriteThreadFinised=true
lateinit var observer:GraphActivityLifecycleObserver
    val FINISH_SEND_THREAD=-99
    val NEXT_MESSAGE_SEND_THREAD=-100
    lateinit var SendThread:ThreadGraphValuesUpdate
    var numberOFValuesOnGraph=20
    var maxnumberofsamplesinArrays=20000
    var maxnumberofmaxnumberofsamplesinArrays=100000
    var numberOfSamplesTocutAfterOverLoad=50
    var minnumberofmaxnumberofsamplesinArrays=10000
    var maxNumberofSamplesonGraphInOneTime=100
    var minNumberofSamplesonGraphInOneTime=20
    lateinit var editTextmaxYvalue:EditText
    lateinit var editTextminYvalue:EditText
    // start sample do disply on graphview
   // var startOFGraph=maxNumberofSamplesonGraphInOneTime-1
    // end sample to display on graph view
    //var endofGraph=maxNumberofSamplesonGraphInOneTime-numberOFValuesOnGraph
    var numberofSamplesTOMOve=0
    var isGraphplay=true
   // var SamplesjumpValueCoef=1f
    lateinit var handler1:Handler

    var mmSocket: BluetoothSocket?=null
    var SocketWIFI: Socket?=null
// Mutable lists with graphs informations

    val listOfGraphs: MutableList<String> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listWithGraphsNames",Array<String>::class.java) as Array<String>).toMutableList()
    }
    val listOfValuesArrays : MutableList<MutableList<Float>> by lazy(LazyThreadSafetyMode.NONE) {
        MutableList(intent.getIntExtra("numberOfGraphs",1)){FloatArray(maxNumberofSamplesonGraphInOneTime).toMutableList()}
    }
    val listOfTimeValues: MutableList<MutableList<Float>> by lazy(LazyThreadSafetyMode.NONE) {
        MutableList(intent.getIntExtra("numberOfGraphs",1)){FloatArray(maxNumberofSamplesonGraphInOneTime).toMutableList()}
    }
    val flagsallowUpdates: MutableList<Boolean> by lazy(LazyThreadSafetyMode.NONE) {
        MutableList(intent.getIntExtra("numberOfGraphs",1)){true}
    }
    val ListOFPID:MutableList<PID> by lazy(LazyThreadSafetyMode.NONE) {

        (intent.getParcelableExtra("listOfPIDs",Array<PID>::class.java) as Array<PID>).toMutableList()
    }
    val listofECUHeader:MutableList<kotlin.Long> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listofECUHeader",Array<kotlin.Long>::class.java) as Array<kotlin.Long>).toMutableList()
    }
    val isMoreThenOneECU:Boolean by lazy(LazyThreadSafetyMode.NONE) {
        intent.getBooleanExtra("ismorethanOneECU",false)
    }
    /*val listOFPIDandModesStrings:MutableList<String>by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listOFPIDandModesStrings",Array<String>::class.java) as Array<String>).toMutableList()
    }*/
    val listofFormulas:MutableList<String> by lazy(LazyThreadSafetyMode.NONE) {
        (intent.getParcelableExtra("listofformulas",Array<String>::class.java) as Array<String>).toMutableList()
    }
    var accuallysendCommandToVehicle=-1
    var accuallysendCommandToCalc=-1
    lateinit var touchListener:OnTouchListener

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
        setContentView(R.layout.activity_graphs)
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
                flagsallowUpdates[presentGraphPos] = true
                UpdateAndInvalidateGraph()
            }
        }
        activitybackground=findViewById<ConstraintLayout>(R.id.contrLayoutGraphs)

        runnablewait=object:Runnable{
            override fun run() {
                activitybackground.requestLayout()
                val popupView=this@GraphsActivity.layoutInflater.inflate(R.layout.for_pop_up_window,null)
                textPopUpLaoding =popupView.findViewById<TextView>(R.id.textPopup)
                popuupWindow= PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                popuupWindow.isOutsideTouchable = true
                popuupWindow.isFocusable = true
                textPopUpLaoding.text =getString(R.string.loading_)
                popuupWindow.showAsDropDown(activitybackground)
            }

        }
        recyclerView=findViewById(R.id.recycler_graphs)
        val text=findViewById<TextView>(R.id.tekstcheck)

        //adding lifecycle observer to launch actions from menu
        observer = GraphActivityLifecycleObserver(this.activityResultRegistry,this)
        lifecycle.addObserver(observer)

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
                            maxYaxisvalue=listOfValuesArrays[presentGraphPos].max()

                            minYaxisValue=listOfValuesArrays[presentGraphPos].min()
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
        //builder.setTitle("Write number of samples (from 20 to 100) and sample time (from 20 to 1000 ms)")
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
                //editTextMaxSamples.text=Editable.Factory.getInstance().newEditable(maxnumberofsamplesinArrays.toString())

                numberofSamplesTOMOve=0
                var i=0
                while (i<flagsallowUpdates.size) {
                    flagsallowUpdates[i] = true
                    i++
                }
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
                  popuupWindow.showAsDropDown(activitybackground)
                  // startupdates=false

                   //cyclicValuesUpdate()
               }
                else if (msg.what==MESSAGE_FINISH_SAVING)
               {
                   popuupWindow.dismiss()
                   //startupdates=true
                   cyclicValuesUpdate()
               }
                else if (msg.what==MESSAGE_OBD_RESULT || msg.what==MESSAGE_RESPONSE_ELM237_COMMAND)
                {

                   // readwriteThreadFinised=true
                    OBDresult =msg.obj as String

                if (flaginitialmesaage)
                    {
                        flaginitialmesaage = false
                        //startupdates = true
                        popuupWindow.dismiss()
                        initialThreadtime = SystemClock.elapsedRealtime()
                        cyclicValuesUpdate()
                    }
                else
                    {
                        accuallysendCommandToCalc=accuallysendCommandToVehicle
                        if (isGraphplay) {
                            SendThread.handlerThread.sendMessage(
                                Message.obtain(
                                    SendThread.handlerThread,
                                    NEXT_MESSAGE_SEND_THREAD
                                )
                            )
                        }
                       // PIDlenghtAdd=((ListOFPID[accuallysendCommandToCalc].PID.length.toFloat())*1.5f -3f).toInt()
                         var w=0
                        ListOFPID.forEach { PID ->
                            if (PID == ListOFPID[accuallysendCommandToCalc] ) {
                                listOfValuesArrays[w].add(
                                    CalculateResponse().calculateNumericwithMoreECUs(
                                        PIDlenghtAdd,
                                        OBDresult, listofFormulas[w],
                                        listofECUHeader[w]
                                    )
                                )
                                listOfTimeValues[w].add((SystemClock.elapsedRealtime().toFloat()-initialThreadtime.toFloat())/1000f)
                            }
                            w++
                        }





                        if (listOfTimeValues[accuallysendCommandToCalc].size>maxnumberofsamplesinArrays)
                        {
                            if (checkboxRingBuffer.isChecked) {
                                numberofSamplesTOMOve = 0
                                var i = 0
                                while (i < flagsallowUpdates.size) {
                                    flagsallowUpdates[i] = true
                                    listOfValuesArrays[i] = listOfValuesArrays[i].subList(
                                        numberOfSamplesTocutAfterOverLoad,
                                        listOfValuesArrays[i].size
                                    )
                                    listOfTimeValues[i] = listOfTimeValues[i].subList(
                                        numberOfSamplesTocutAfterOverLoad,
                                        listOfTimeValues[i].size
                                    )
                                    numberofSamplesTOMOve = 0
                                    i++
                                }
                                UpdateAndInvalidateGraph()
                            }
                            else
                            {
                                SendThread.handlerThread.sendMessage(Message.obtain(SendThread.handlerThread,FINISH_SEND_THREAD))
                                //startupdates=false
                            }


                        }
                        else if (flagsallowUpdates[presentGraphPos] /*&& accuallysendCommandToCalc==presentGraphPos*/)
                        {

                            UpdateAndInvalidateGraph()
                        }
                    }


                }
                else if (msg.what==MESSAGE_OBD_WRONG_RESULT)
                {
                    readwriteThreadFinised=true
                   /* if (!flaginitialmesaage) {
                        listOfValuesArrays[accuallysendCommandToCalc].add(0f)
                    }*/

                }
                flagcalculationsENdCanincrementacualcommandnumber=true
            }
        }




        val metrics=this.windowManager.currentWindowMetrics
        val windowinsets=metrics.windowInsets
        val insets = windowinsets.getInsetsIgnoringVisibility(android.view.WindowInsets.Type.navigationBars() or  android.view.WindowInsets.Type.displayCutout())
        val insetsWidth = insets.right + insets.left;
        val insetsHeight = insets.top + insets.bottom;

        // Legacy size that Display#getSize reports
        val bounds = metrics.getBounds();
        val legacySize = Size(bounds.width() - insetsWidth,
            bounds.height() - insetsHeight)


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
                     if (listOfValuesArrays[presentGraphPos].size-numberOFValuesOnGraph-numberofSamplesTOMOve>0 && numberOFValuesOnGraph<maxNumberofSamplesonGraphInOneTime)
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


                 }
             else if(p1.actionMasked == MotionEvent.ACTION_MOVE) {


                     presentX = p1.getX(index!!)
                     presentY = p1.getY(index)
                     graphVerticalScrollFloat+=presentX-startX


                     if (graphVerticalScrollFloat>legacySize.width.toFloat()/15f)
                     {
                         startX = p1.getX(index!!)
                         startY = p1.getY(index)
                         graphVerticalScrollFloat=0f
                         if (listOfValuesArrays[presentGraphPos].size-numberOFValuesOnGraph-numberofSamplesTOMOve>0) {
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
                 if (oldValueListSize>0 && !flagsallowUpdates[presentGraphPos]) {

                      numberofSamplesTOMOve +=
                         MutableList(listOfValuesArrays.size){i->listOfValuesArrays[i].size}.min() - oldValueListSize
                     if (listOfValuesArrays[presentGraphPos].size-numberOFValuesOnGraph-numberofSamplesTOMOve<1)
                     {
                         numberofSamplesTOMOve-=MutableList(listOfValuesArrays.size){i->listOfValuesArrays[i].size}.min() - oldValueListSize
                     }
                 }

                 oldValueListSize=MutableList(listOfValuesArrays.size){i->listOfValuesArrays[i].size}.min()
                 flagsallowUpdates[presentGraphPos]= numberofSamplesTOMOve==0

                 //[0] because in this case only one line on one graph- always here!!!
                 p0.arrayofFunctionValues[0] =listOfValuesArrays[presentGraphPos].subList(listOfValuesArrays[presentGraphPos].size-numberofSamplesTOMOve-numberOFValuesOnGraph,listOfValuesArrays[presentGraphPos].size-numberofSamplesTOMOve).toFloatArray()
                 p0.minOFarrayofTimeValues=listOfTimeValues[presentGraphPos][listOfTimeValues[presentGraphPos].size-numberofSamplesTOMOve-numberOFValuesOnGraph]
                 p0.maxOFarrayofTimeValues=listOfTimeValues[presentGraphPos][listOfTimeValues[presentGraphPos].size-numberofSamplesTOMOve-1]
                if (flagDefaultYaxisValues) {
                    maxYaxisvalue = listOfValuesArrays[presentGraphPos].max()
                    minYaxisValue = listOfValuesArrays[presentGraphPos].min()
                }
                 p0.maxOFarrayofFunctionValues=maxYaxisvalue
                 p0.minOFarrayofFunctionValues=minYaxisValue
                 p0.invalidate()

             }

                 return false
             }
         }



        val recyclerScrollLostener=object: RecyclerView.OnScrollListener()
        {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                flagRecyclerScrollSettle = newState==SCROLL_STATE_SETTLING
                flagRecyclerScrollDrag = newState== SCROLL_STATE_DRAGGING
                flagRecyclerScrollIdle= newState== SCROLL_STATE_IDLE


                if (newState== SCROLL_STATE_IDLE ) {
                    recyclerView.layoutManager!!.scrollToPosition(presentGraphPos)
                    sumOfScroll=0
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (flagRecyclerScrollDrag) {
                    sumOfScroll += dy
                    if (sumOfScroll <= -legacySize.height.toFloat() / 3f) {
                        flagRecyclerScrollDrag=false
                        sumOfScroll = 0
                        if (presentGraphPos > 0) {
                            presentGraphPos--
                            numberofSamplesTOMOve=0
                            flagsallowUpdates[presentGraphPos]=true
                        }
                    } else if (sumOfScroll >= legacySize.height.toFloat() / 3f) {
                        flagRecyclerScrollDrag=false
                        sumOfScroll = 0
                        if (presentGraphPos < recyclerView.layoutManager!!.itemCount - 1) {
                            presentGraphPos++
                            numberofSamplesTOMOve=0
                            flagsallowUpdates[presentGraphPos]=true

                        }
                    }
                }


            }
        }



       recyclerView.layoutManager=LinearLayoutManager(this)
       recyclerView.adapter=AdapterGraphs(listOfGraphs,this)
       recyclerView.addOnScrollListener(recyclerScrollLostener)

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
                    Thread.sleep(100)

                }
                catch(e:Exception)
                {

                }
                startActivity(Intent(this@GraphsActivity,DiagnosticsOBD::class.java))
                finish()
            }

        }
        onBackPressedDispatcher.addCallback(backCallback)


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


    }


    fun cyclicValuesUpdate()
    {
        SendThread=ThreadGraphValuesUpdate(this,ListOFPID)
        SendThread.start()

    }

    @SuppressLint("SuspiciousIndentation")
    fun UpdateAndInvalidateGraph()
    {
            try {
                val view = recyclerView.layoutManager!!.findViewByPosition(presentGraphPos)!!
                    .findViewById<GraphView>(R.id.plot_diagnostics)
                if (view is GraphView) {
                    view.arrayofFunctionValues[0] = listOfValuesArrays[presentGraphPos].subList(listOfValuesArrays[presentGraphPos].size-numberOFValuesOnGraph,listOfValuesArrays[presentGraphPos].size).toFloatArray()
                    view.minOFarrayofTimeValues=listOfTimeValues[presentGraphPos][listOfTimeValues[presentGraphPos].size-numberOFValuesOnGraph]
                    view.maxOFarrayofTimeValues=listOfTimeValues[presentGraphPos].last()
                    if (flagDefaultYaxisValues) {
                        maxYaxisvalue = listOfValuesArrays[presentGraphPos].max()
                        minYaxisValue = listOfValuesArrays[presentGraphPos].min()
                    }
                    view.maxOFarrayofFunctionValues=maxYaxisvalue
                    view.minOFarrayofFunctionValues=minYaxisValue

                }
                view.invalidate()
            }
            catch (e:Exception)
            {}



        }


    fun initialConn()
    {
        Thread {
            while (this@GraphsActivity.lifecycle.currentState!= Lifecycle.State.STARTED)
            {

            }
            runOnUiThread {
                mainExecutor.execute(runnablewait)
                /*checkingMinAllowedmsHex =
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
                //ThreadReadWrite(null,PID(ObdModes.MODE_01,"00"), mmSocket!!,handler1,0).start()
            }
        }.start()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
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
        //finish()
    }
}