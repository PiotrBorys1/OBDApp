package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class AdapterGraphs(private val listOfGraphs: MutableList<String>, val context: GraphsActivity):RecyclerView.Adapter<AdapterGraphs.neVievHolder>() {
    class neVievHolder(ItemView: View) : ViewHolder(ItemView){

        val graph=ItemView.findViewById<GraphView>(R.id.plot_diagnostics)
        val raphname=ItemView.findViewById<TextView>(R.id.Grapname)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): neVievHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_graphs, parent, false)

        return neVievHolder(view)
    }

    override fun getItemCount()= listOfGraphs.size

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: neVievHolder, position: Int) {

        holder.graph.arrayofFunctionValues = MutableList(1){context.listOfValuesArrays[position].subList(context.listOfValuesArrays[position].size-context.numberOFValuesOnGraph,context.listOfValuesArrays[position].size).toFloatArray()}
        holder.raphname.text=listOfGraphs[position]
        holder.graph.setOnTouchListener(context.touchListener)

    }



}