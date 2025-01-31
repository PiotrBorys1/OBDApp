package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class Adapter_General_Values(val context: ActivityDiagnostics):RecyclerView.Adapter<Adapter_General_Values.neVievHolder>() {
    class neVievHolder(ItemView: View) : ViewHolder(ItemView){

        val textName=ItemView.findViewById<TextView>(R.id.textNameOfValue)
        val textValue=ItemView.findViewById<TextView>(R.id.textValueOfGeneralInfo)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): neVievHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_general_values, parent, false)

        return neVievHolder(view)
    }

    override fun getItemCount()= context.listOfPIDs.size

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: neVievHolder, @SuppressLint("RecyclerView") position: Int) {

        holder.textName.text=context.listOfNamesOfGeneralInfos[position]
        holder.textValue.text=";"

    }




}