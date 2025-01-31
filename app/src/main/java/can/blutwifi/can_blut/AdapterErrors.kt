package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class AdapterErrors(val context: ErrorActivity):RecyclerView.Adapter<AdapterErrors.neVievHolder>() {
    class neVievHolder(ItemView: View) : ViewHolder(ItemView){

        val textName=ItemView.findViewById<TextView>(R.id.textErrorNames)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): neVievHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_errors_names, parent, false)

        return neVievHolder(view)
    }

    override fun getItemCount()= context.listofgenericErrorstoRecycler.size

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: neVievHolder, @SuppressLint("RecyclerView") position: Int) {
        if (context.listofgenericErrorstoRecycler[position].contains("ECU") || context.listofgenericErrorstoRecycler[position].contains("errors")|| context.listofgenericErrorstoRecycler[position].contains("błędy",true) )
        {
            holder.textName.setTextColor(Color.RED)
            holder.textName.textSize=30f
        }
        holder.textName.text=context.listofgenericErrorstoRecycler[position]


        holder.textName.setOnClickListener {
            try {

                    var erorr=context.listDTC.listOfDTC.find { predicate ->
                         context.listofgenericErrorstoRecycler[holder.adapterPosition].contains(predicate.code)
                    }
                if (erorr!=null)
                {
                    context.presentlyChoosenErrorDesc=erorr.description
                    context.textViewGenErrDesc.text=context.presentlyChoosenErrorDesc
                    context.alertdialGenreicErrorDesc.show()
                }
                else
                {
                    context.presentlyChoosenErrorDesc=context.listofGeneralDescriptionofErrorCause[Integer.parseInt(context.listofgenericErrorstoRecycler[position][2].toString())]
                    context.textViewGenErrDesc.text=context.presentlyChoosenErrorDesc
                    context.alertdialGenreicErrorDesc.show()
                }
            }
            catch (e:Exception)
            {

                context.presentlyChoosenErrorDesc=""
            }


        }

    }




}