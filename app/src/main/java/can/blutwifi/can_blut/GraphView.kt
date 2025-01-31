package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import kotlin.math.absoluteValue

class GraphView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
):androidx.appcompat.widget.AppCompatImageView(context,attrs,defStyleAttr) {
    init{
        isClickable = true
        isScrollContainer=true

    }

    var numberofLinesonGraphs=1
    var arrayofFunctionValues= MutableList(numberofLinesonGraphs){FloatArray(50)}
    val numberofDisplayedTimevalues=10
    //var arrayofTimeValues= FloatArray(numberofDisplayedTimevalues)
    var nameOfGraph=""
    var maxOFarrayofTimeValues=0f
    var minOFarrayofTimeValues=0f
    var maxOFarrayofFunctionValues=0f
    var minOFarrayofFunctionValues=0f
    var flagnotdrawing=false
    var startYlineposition=0f
    var endYlineposition=0f
    var flagdiffrentcolors=false
    var activityonegraph :OneGraphActivity?=null
    var listOFPaints= mutableListOf<Paint>()
    var mutablelistOFpointsPositionsOnGraph= mutableListOf<MutableList<Float>>()
    var flagChangeLook=true
    var startGraphdivision=12
    var XtoshowPointValue=0f
    var YtoshowPointValue=0f
    var TextPointValue=""
    //(startGraphdivision-valuesubstract)*(width or height)/startGraphdivision
    var valuesubstract=1
    val paint1= Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style=Paint.Style.FILL
        color=Color.BLACK
    }
    val paint2= Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style=Paint.Style.FILL_AND_STROKE
        color=Color.BLACK
        textSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10f,context.resources.displayMetrics)
    }
    val paint3= Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style=Paint.Style.FILL_AND_STROKE
        color=Color.RED
        strokeWidth=5f
        textSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10f,context.resources.displayMetrics)
    }
    val paint4= Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style=Paint.Style.FILL_AND_STROKE
        color=Color.BLUE
        strokeWidth=7f
        textSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,15f,context.resources.displayMetrics)
        textAlign=Align.CENTER
    }
    val paint5= Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style=Paint.Style.FILL_AND_STROKE
        color=Color.BLUE
        strokeWidth=4f
        textSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,20f,context.resources.displayMetrics)
        textAlign=Align.RIGHT
    }
    val paintPoitValue= Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style=Paint.Style.STROKE
        color=Color.BLACK
        strokeWidth=3f
        textSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,20f,context.resources.displayMetrics)
        textAlign=Align.LEFT
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {

        super.onDraw(canvas)

        if (canvas!=null) {

           // canvas.drawText(nameOfGraph,width.toFloat()/2f,(height/(numberofDisplayedTimevalues*4)).toFloat(),paint4)
            canvas.drawText("Time [s]",width.toFloat()-width.toFloat()/20f,height-height/40f,paint5)
                val linesFloatarray1 = arrayOf((width/startGraphdivision).toFloat(),((startGraphdivision-valuesubstract)*height/startGraphdivision).toFloat(),width.toFloat(),((startGraphdivision-valuesubstract)*height/startGraphdivision).toFloat(),
                    (width/startGraphdivision).toFloat(),((startGraphdivision-valuesubstract)*height/startGraphdivision).toFloat(),(width/startGraphdivision).toFloat(),0f,
                    (width/startGraphdivision-width/25f),height/25f,(width/startGraphdivision).toFloat(),0f,
                    (width/startGraphdivision).toFloat(),0f,(width/startGraphdivision+width/25f),height/25f,
                    (width-width/25f),(height-height/startGraphdivision-height/25f),width.toFloat(),(height-height/startGraphdivision).toFloat(),
                    width.toFloat(),(height-height/startGraphdivision).toFloat(),(width-width/25f),(height-height/startGraphdivision+height/25f)
                )

                canvas.drawLines(linesFloatarray1.toFloatArray(),0,linesFloatarray1.size,paint1)
                val linesfloatarray2= mutableListOf<Float>()
                var i=0
                while (i<numberofDisplayedTimevalues)
                {
                    linesfloatarray2.add(0f)
                    linesfloatarray2.add(((height-(2+i)*height/numberofDisplayedTimevalues)).toFloat())
                    linesfloatarray2.add(width.toFloat())
                    linesfloatarray2.add(((height-(2+i)*height/numberofDisplayedTimevalues)).toFloat())

                    linesfloatarray2.add(((2+i)*(width/numberofDisplayedTimevalues)).toFloat())
                    linesfloatarray2.add((height-height/(numberofDisplayedTimevalues*2)).toFloat())
                    linesfloatarray2.add(((2+i)*(width/numberofDisplayedTimevalues)).toFloat())
                    linesfloatarray2.add((height/(numberofDisplayedTimevalues*2)).toFloat())
                    i++
                }

                canvas.drawLines(linesfloatarray2.toFloatArray(),0,linesfloatarray2.size,paint1)
                //flagChangeLook=false

/*i=0
 var maxOFarrayofFunctionValues=0f
var minOFarrayofFunctionValues=Float.MAX_VALUE

while (i<arrayofFunctionValues.size)
{
    if (arrayofFunctionValues[i].max()>maxOFarrayofFunctionValues)
    {
        maxOFarrayofFunctionValues=arrayofFunctionValues[i].max()
    }
    if (arrayofFunctionValues[i].min()<minOFarrayofFunctionValues)
    {
        minOFarrayofFunctionValues=arrayofFunctionValues[i].min()
    }
    i++
}*/


i=0
while (i<numberofDisplayedTimevalues-1)
{
    if(minOFarrayofFunctionValues!=maxOFarrayofFunctionValues) {
        canvas.drawText(
            (((((minOFarrayofFunctionValues + (((maxOFarrayofFunctionValues - minOFarrayofFunctionValues)) / (numberofDisplayedTimevalues.toFloat()-3f)) * i.toFloat())*10f).toInt()).toFloat())/10f).toString(),
            0f,
            (((height - height / (numberofDisplayedTimevalues * 20)) - (2 + i) * height / numberofDisplayedTimevalues)).toFloat(),
            paint2
        )
    }
    else
    {
        canvas.drawText(minOFarrayofFunctionValues.toString(),0f,(height/2).toFloat(),paint2)
    }
    canvas.drawText((((((minOFarrayofTimeValues + ((maxOFarrayofTimeValues-minOFarrayofTimeValues)/numberofDisplayedTimevalues.toFloat())*(i.toFloat()))*100f).toInt()).toFloat())/100f).toString(),
        (i*(width/numberofDisplayedTimevalues)+width/(numberofDisplayedTimevalues)).toFloat(),(height-height/(numberofDisplayedTimevalues*2)).toFloat(),paint2
    )
    i++
}

mutablelistOFpointsPositionsOnGraph.clear()
i=0
var j=0

while (j<arrayofFunctionValues.size) {
    while (i < arrayofFunctionValues[j].size) {

        if (i > 0) {
            if (minOFarrayofFunctionValues!=maxOFarrayofFunctionValues) {
                startYlineposition =
                    (height - 2 * height / numberofDisplayedTimevalues).toFloat() - (((arrayofFunctionValues[j][i - 1] - minOFarrayofFunctionValues) / (maxOFarrayofFunctionValues - minOFarrayofFunctionValues)) * ((numberofDisplayedTimevalues - 3) * height / numberofDisplayedTimevalues))
                endYlineposition =
                    (height - 2 * height / numberofDisplayedTimevalues).toFloat() - (((arrayofFunctionValues[j][i] - minOFarrayofFunctionValues) / (maxOFarrayofFunctionValues - minOFarrayofFunctionValues)) * ((numberofDisplayedTimevalues - 3) * height / numberofDisplayedTimevalues))

            }
            else
            {
                startYlineposition=(height/2).toFloat()
               endYlineposition=(height/2).toFloat()
            }

            flagnotdrawing = (startYlineposition>(height -  height / numberofDisplayedTimevalues).toFloat() &&
                    endYlineposition>(height -  height / numberofDisplayedTimevalues).toFloat()) ||
                    (startYlineposition<(height / numberofDisplayedTimevalues).toFloat() &&
                            endYlineposition<(height / numberofDisplayedTimevalues).toFloat())

            startYlineposition =
                if (startYlineposition>(height -  height / numberofDisplayedTimevalues).toFloat()) {
                    (height -  height / numberofDisplayedTimevalues).toFloat()
                } else if (startYlineposition<(height / numberofDisplayedTimevalues).toFloat()) {
                    (height / numberofDisplayedTimevalues).toFloat()
                } else {
                    startYlineposition
                }

            endYlineposition =
                if (endYlineposition>(height - height / numberofDisplayedTimevalues).toFloat()) {
                    (height - height / numberofDisplayedTimevalues).toFloat()
                } else if (endYlineposition<(height / numberofDisplayedTimevalues).toFloat()) {
                    (height / numberofDisplayedTimevalues).toFloat()
                } else {
                    endYlineposition
                }


        if (!flagnotdrawing) {
            if (activityonegraph == null  && !flagdiffrentcolors) {
                canvas.drawLine(
                    ((i - 1) * (width / arrayofFunctionValues[j].size) + (width / numberofDisplayedTimevalues)).toFloat(),
                    startYlineposition,
                    (i * (width / arrayofFunctionValues[j].size) + (width / numberofDisplayedTimevalues)).toFloat(),
                    endYlineposition,
                    paint3
                )

            } else {

                if (listOFPaints.isNotEmpty()) {
                    canvas.drawLine(
                        ((i - 1) * (width / arrayofFunctionValues[j].size) + (width / numberofDisplayedTimevalues)).toFloat(),
                        startYlineposition,
                        (i * (width / arrayofFunctionValues[j].size) + (width / numberofDisplayedTimevalues)).toFloat(),
                        endYlineposition,
                        listOFPaints[j]
                    )
                }
                mutablelistOFpointsPositionsOnGraph.add(mutableListOf(j.toFloat(),i.toFloat(),startYlineposition,
                    ((i - 1) * (width / arrayofFunctionValues[j].size) + (width / numberofDisplayedTimevalues)).toFloat()))

            }
        }
        }

        i++
    }
    i=0
    j++
}

            canvas.drawText(TextPointValue,XtoshowPointValue,YtoshowPointValue,paintPoitValue)

}

}

}