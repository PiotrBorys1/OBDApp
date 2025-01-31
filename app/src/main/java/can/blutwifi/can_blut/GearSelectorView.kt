package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView

class GearSelectorView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
):androidx.appcompat.widget.AppCompatImageView(context,attrs,defStyleAttr) {
    init{
        isClickable = true
        isScrollContainer=true

    }

    var gearValue=0
    var bitmaptoDraw:Bitmap?=null
    var bitmaptomove:Bitmap?=null
    var rectForStableBitmap=Rect()
    var rectForMoveableBitmap=Rect()
    val paint1= Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style=Paint.Style.FILL
    }
    var ViewHeight:Int=0
    var ViewWidth:Int=0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (bitmaptoDraw != null) {
            setMeasuredDimension(bitmaptoDraw!!.width,bitmaptoDraw!!.height)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas!=null) {
            if (bitmaptoDraw != null && bitmaptomove != null) {
                ViewHeight = height
                ViewWidth = width
                var matrix=Matrix()
                canvas.drawBitmap(bitmaptoDraw!!, matrix, paint1)
                var meitrix=Matrix()
                meitrix.setTranslate((width/18).toFloat(),(gearValue*height/4+height/20).toFloat())
                canvas.drawBitmap(bitmaptomove!!, meitrix, paint1)

            }
        }
    }



}