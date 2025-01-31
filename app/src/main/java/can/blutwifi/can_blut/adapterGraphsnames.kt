package can.blutwifi.car_obd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class adapterGraphsnames(private val listofGraphsnames: MutableList<String>, val context: DiagnosticsOBD):RecyclerView.Adapter<adapterGraphsnames.neVievHolder>() {
    class neVievHolder(ItemView: View) : ViewHolder(ItemView){

        val checkbox=ItemView.findViewById<CheckBox>(R.id.graphcheckBox)
        val textGraphName=ItemView.findViewById<TextView>(R.id.textgraphname)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): neVievHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_choose_graphs, parent, false)

        return neVievHolder(view)
    }

    override fun getItemCount()= listofGraphsnames.size


    override fun onBindViewHolder(holder: neVievHolder, position: Int) {

        holder.textGraphName.text=listofGraphsnames[position]
        holder.checkbox.isChecked=context.checkBoxStatesTable[position]
        holder.checkbox.setOnCheckedChangeListener(
            object: CompoundButton.OnCheckedChangeListener
            {
                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    context.checkBoxStatesTable[holder.adapterPosition]=p1
                }

            }
        )

    }




}