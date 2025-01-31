package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Paint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Size
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import java.io.File
import java.net.Socket
import java.util.Collections
import java.util.Random
import kotlin.math.absoluteValue


class ActivityOpenGraphFile : AppCompatActivity() {
    var mutablelistOfValues = mutableListOf<MutableList<Float>>()
    val mutablelistOfNames = mutableListOf<String>()
    val muatablelistOfGraphsColors = mutableListOf<Paint>()
    var mutablelistOfShownGraphs= mutableListOf<Boolean>()
    var mutablelistOfTimeValues= mutableListOf<Float>()
    var listofIndexesOfListOfArrraystoShow= mutableListOf<Int>()
    lateinit var recyclerGraphsToChoose:RecyclerView
    lateinit var Graphview: GraphView
    lateinit var switchHideAppear:SwitchCompat
    var YforSpanid0=-1f
    var YforSpanid1=-1f
    var meanYforSpan=0f
    var beginningSpan=0f
    var madeSpan=0f
    var graphScaleFactor=1f
    var graphVerticalScrollFloat=0f
    var startX=0f
    var startY=0f
    var presentX=0f
    var presentY=0f
    lateinit var testtext: TextView
    lateinit var legacySize: Size
    lateinit var popuupWindow: PopupWindow
    var mutablelistofinfotoshowPoint: MutableList<Float>?= mutableListOf()
    var readwriteThreadFinised=true
    var numberOFValuesOnGraph=20
    var maxNumberofSamplesonGraphInOneTime=100
    var minNumberofSamplesonGraphInOneTime=20
    var maxYaxisvalue=0f
    var minYaxisValue=0f
    var flagDefaultYaxisValues=true
    lateinit var touchListener: View.OnTouchListener
    var numberofSamplesTOMOve=0
    var numberOfGraphsTOShow=0

    val laucher_choose_File = registerForActivityResult(ActivityResultContracts.OpenDocument()) {

        if (it != null) {
            val contentres = this.contentResolver
            val fileInputStream = contentres.openInputStream(it)
            var i = 0
            var j = 0

            fileInputStream!!.readBytes().decodeToString().split('\n')
               .forEach { stringwithsemicol ->
                    val arraystringwithoutsemicol=stringwithsemicol.split(';').toMutableList()
                   arraystringwithoutsemicol.removeLast()
                 /*  if (i==0)
                   {
                       testtext.text=arraystringwithoutsemicol.toString()
                   }*/
                   for (stringwithoutsemicol in arraystringwithoutsemicol) {
                       if (i == 0) {
                           mutablelistOfNames.add(stringwithoutsemicol)
                          val paint= Paint().apply {
                               setARGB(
                                   255,
                                   kotlin.random.Random.nextInt(80, 220),
                                   kotlin.random.Random.nextInt(80, 220),
                                   kotlin.random.Random.nextInt(80, 220)
                               )
                               strokeWidth = kotlin.random.Random.nextInt(4, 10).toFloat()
                           }
                           muatablelistOfGraphsColors.add(paint)
                       } else {


                               mutablelistOfValues[j].add(
                                   stringwithoutsemicol.trim().toDouble().toFloat()
                               )


                       }
                       j++
                   }
                    if (i==0)
                    {
                        mutablelistOfValues= MutableList(j){ mutableListOf() }
                    }
                    j = 0
                    i++
                }
            mutablelistOfNames.removeAt(0)
            mutablelistOfTimeValues=mutablelistOfValues[0]
            mutablelistOfValues.removeAt(0)
            mutablelistOfShownGraphs= MutableList(mutablelistOfValues.size){false}
            
            muatablelistOfGraphsColors.removeAt(0)
            recyclerGraphsToChoose.adapter=AdapterGraphsNamesOpenFIle(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_graph_file)
        laucher_choose_File.launch(arrayOf(
            "application/pdf",
            "application/msword",
            "application/ms-doc",
            "application/doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        ))
        Graphview = findViewById(R.id.onegraph)
        Graphview.flagdiffrentcolors=true

        testtext=findViewById(R.id.texttest)
        val metrics=this.windowManager.currentWindowMetrics
        val windowinsets=metrics.windowInsets
        val insets = windowinsets.getInsetsIgnoringVisibility(android.view.WindowInsets.Type.navigationBars() or  android.view.WindowInsets.Type.displayCutout())
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom
        val bounds = metrics.getBounds()
        legacySize = Size(bounds.width() - insetsWidth,
            bounds.height() - insetsHeight)



        recyclerGraphsToChoose=findViewById<RecyclerView>(R.id.recyclergraphsforchoose)

        switchHideAppear=findViewById<SwitchCompat>(R.id.compatSwitchHide).apply{
            setOnCheckedChangeListener { p0, p1 ->
                recyclerGraphsToChoose.adapter=AdapterGraphsNamesOpenFIle(this@ActivityOpenGraphFile)
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

        recyclerGraphsToChoose.layoutManager= LinearLayoutManager(this)
        recyclerGraphsToChoose.adapter=AdapterGraphsNamesOpenFIle(this)

        // scale listener for zoom gestures
        val scaleGestureListener=object: ScaleGestureDetector.OnScaleGestureListener
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

        val scaleGestureDtector= ScaleGestureDetector(this,scaleGestureListener)
        // touch listener responsible for processing graphs moving and zooming
        touchListener=object : View.OnTouchListener {
            @SuppressLint("SuspiciousIndentation")
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {

                if (p0 is GraphView) {

                    scaleGestureDtector.onTouchEvent(p1!!)
                    graphScaleFactor+=madeSpan
                    val index = p1.actionIndex
                    val id=p1.getPointerId(index)
                   /* if (id==0)
                    {
                        YforSpanid0=p1.getY(index)
                    }
                    else if (id==1)
                    {
                        YforSpanid1=p1.getY(index)
                    }
                    else if (YforSpanid0>-1f && YforSpanid1>-1f)
                    {
                        meanYforSpan=(YforSpanid0+YforSpanid1)/2

                    }*/

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
                        if (mutablelistOfTimeValues.size-numberOFValuesOnGraph-numberofSamplesTOMOve>0 && numberOFValuesOnGraph<maxNumberofSamplesonGraphInOneTime)
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

                        presentX = p1.getX(index!!)
                        presentY = p1.getY(index)

                        graphVerticalScrollFloat+=presentX-startX


                        if (graphVerticalScrollFloat>legacySize.width.toFloat()/15f)
                        {
                            startX = p1.getX(index!!)
                            startY = p1.getY(index)
                            graphVerticalScrollFloat=0f
                            if (mutablelistOfTimeValues.size -numberOFValuesOnGraph-numberofSamplesTOMOve>0) {
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
                    numberOfGraphsTOShow=listofIndexesOfListOfArrraystoShow.size
                    p0.arrayofFunctionValues =MutableList(numberOfGraphsTOShow){i->mutablelistOfValues[listofIndexesOfListOfArrraystoShow[i]].subList(mutablelistOfValues[listofIndexesOfListOfArrraystoShow[i]].size-numberofSamplesTOMOve-numberOFValuesOnGraph,mutablelistOfValues[listofIndexesOfListOfArrraystoShow[i]].size-numberofSamplesTOMOve).toFloatArray()}
                    p0.listOFPaints =MutableList(numberOfGraphsTOShow){i->muatablelistOfGraphsColors[listofIndexesOfListOfArrraystoShow[i]]}
                    p0.minOFarrayofTimeValues=mutablelistOfTimeValues[mutablelistOfTimeValues.size-numberofSamplesTOMOve-numberOFValuesOnGraph]
                    p0.maxOFarrayofTimeValues=mutablelistOfTimeValues[mutablelistOfTimeValues.size-numberofSamplesTOMOve-1]
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


        val backCallback=object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
              startActivity(Intent(this@ActivityOpenGraphFile,DiagnosticsOBD::class.java))
                finish()
            }

        }
        onBackPressedDispatcher.addCallback(backCallback)

    }




    @SuppressLint("SuspiciousIndentation")
    fun UpdateAndInvalidateGraph()
    {
        numberOfGraphsTOShow=listofIndexesOfListOfArrraystoShow.size
        Graphview.arrayofFunctionValues =MutableList(numberOfGraphsTOShow){i->mutablelistOfValues[listofIndexesOfListOfArrraystoShow[i]].subList(mutablelistOfValues[listofIndexesOfListOfArrraystoShow[i]].size-numberofSamplesTOMOve-numberOFValuesOnGraph,mutablelistOfValues[listofIndexesOfListOfArrraystoShow[i]].size-numberofSamplesTOMOve).toFloatArray()}
        Graphview.listOFPaints =MutableList(numberOfGraphsTOShow){i->muatablelistOfGraphsColors[listofIndexesOfListOfArrraystoShow[i]]}
        Graphview.minOFarrayofTimeValues=mutablelistOfTimeValues[mutablelistOfTimeValues.size-numberofSamplesTOMOve-numberOFValuesOnGraph]
        Graphview.maxOFarrayofTimeValues=mutablelistOfTimeValues[mutablelistOfTimeValues.size-numberofSamplesTOMOve-1]
        maxYaxisvalue=
            Collections.max(MutableList(numberOfGraphsTOShow) { i -> mutablelistOfValues[listofIndexesOfListOfArrraystoShow[i]].max() })
        minYaxisValue=
            Collections.min(MutableList(numberOfGraphsTOShow) { i -> mutablelistOfValues[listofIndexesOfListOfArrraystoShow[i]].min() })
        Graphview.maxOFarrayofFunctionValues=maxYaxisvalue
        Graphview.minOFarrayofFunctionValues=minYaxisValue
        Graphview.invalidate()

    }

    fun updateVisibleGraphs()
    {
        var i=0
        listofIndexesOfListOfArrraystoShow.clear()
        mutablelistOfShownGraphs.forEach {
            if (it)
            {
                listofIndexesOfListOfArrraystoShow.add(i)
            }
            i++
        }
        UpdateAndInvalidateGraph()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}