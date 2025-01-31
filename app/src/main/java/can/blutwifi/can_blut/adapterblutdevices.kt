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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class adapterblutdevices(private val listofdevices: MutableList<BluetoothDevice>, val context: MainActivity):RecyclerView.Adapter<adapterblutdevices.neVievHolder>() {
    class neVievHolder(ItemView: View) : ViewHolder(ItemView){

        val button=ItemView.findViewById<Button>(R.id.device_name_button)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): neVievHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_buttons, parent, false)

        return neVievHolder(view)
    }

    override fun getItemCount()= listofdevices.size

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: neVievHolder, position: Int) {

       holder.button.text=listofdevices[position].name
        holder.button.setOnClickListener {
            MySingleton.getInstance().BludDevice = listofdevices[holder.adapterPosition]
                val inte = Intent(context, DiagnosticsOBD::class.java).putExtra("IsWifiDevice", false)
                context.startActivity(inte)

        }

    }




}