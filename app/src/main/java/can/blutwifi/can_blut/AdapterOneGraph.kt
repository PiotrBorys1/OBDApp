package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import java.util.*
import java.util.Collections.max
import java.util.Collections.min
import kotlin.random.Random

class AdapterOneGraph(private val listofGraphsnames: MutableList<String>,private val listOFVisibleGraphs: MutableList<Boolean>, val context: OneGraphActivity):RecyclerView.Adapter<AdapterOneGraph.neVievHolder>() {
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

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: neVievHolder, @SuppressLint("RecyclerView") position: Int) {

        holder.textGraphName.isVisible=context.switchHideAppear.isChecked || context.listOfVisiblegraphs[position]
        holder.checkbox.isVisible=context.switchHideAppear.isChecked || context.listOfVisiblegraphs[position]
        holder.textGraphName.text=listofGraphsnames[position]
        holder.checkbox.isChecked=listOFVisibleGraphs[position]
        if (holder.checkbox.isChecked) {
            holder.textGraphName.setTextColor(context.listOfPaintsForVisibleGraphs[context.additionalVariableUsedOnlyFirstTimeForRecycler].color)
            context.additionalVariableUsedOnlyFirstTimeForRecycler++
        }


        holder.checkbox.setOnCheckedChangeListener(
            object: CompoundButton.OnCheckedChangeListener
            {
                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
            if (!p1 && context.listOfVisiblegraphs.count { predicate-> predicate } < 2)
            {
                        p0!!.isChecked=true

            }
            else
            {
                        //position of listOfValuesArraysVisible, from where remove the elemen
                        context.flagchangeGraphs=true
                        context.relativepositionwithwereInPast=context.listOFGraphswereinPast.subList(0,holder.adapterPosition).count { predicate-> predicate }
                        context.listOfVisiblegraphs[holder.adapterPosition] = p1
                        context.numberOfGraphsTOShow=context.listOfVisiblegraphs.count { predicate-> predicate }

                if (p1)
                {
                   if (!context.listOFGraphswereinPast[holder.adapterPosition])
                   {
                       context.listOFGraphswereinPast[holder.adapterPosition] = true
                        context.listOfValuesArraysVisible.add(context.relativepositionwithwereInPast,FloatArray((MutableList(context.listOfValuesArraysVisible.size){i->context.listOfValuesArraysVisible[i].size}).max()).toMutableList())
                        context.listOFPIdsforVisibleGraphs.add(context.relativepositionwithwereInPast,context.ListOFPID[holder.adapterPosition])
                        context.listOfPaintsForVisibleGraphs.add(context.relativepositionwithwereInPast,Paint().apply{
                            setARGB(255,
                                Random.nextInt(80,220),
                                Random.nextInt(80,220),
                                Random.nextInt(80,220))
                            strokeWidth=7f
                        })
                        context.listofVisisbleformulas.add(context.relativepositionwithwereInPast,context.listofFormulas[holder.adapterPosition])

                           context.listofECUHeaderVisible.add(context.relativepositionwithwereInPast,context.listofECUHeader[holder.adapterPosition])

                   }
                    holder.textGraphName.setTextColor(context.listOfPaintsForVisibleGraphs[context.relativepositionwithwereInPast].color)
                }
                else
                {
                    context.listOFGraphswereinPast[holder.adapterPosition] = true
                    holder.textGraphName.setTextColor(Color.BLACK)
                }
                context.listofIndexesOfListOfArrraystoShow= mutableListOf()
                var i=0
                while (i<context.listOfVisiblegraphs.size)
                {
                    if (context.listOfVisiblegraphs[i])
                    {
                        context.listofIndexesOfListOfArrraystoShow.add(context.listOFGraphswereinPast.subList(0,i).count { predicate-> predicate })
                    }
                    i++
                }

                context.Graphview.arrayofFunctionValues =MutableList(context.numberOfGraphsTOShow){i->context.listOfValuesArraysVisible[context.listofIndexesOfListOfArrraystoShow[i]].subList(context.listOfValuesArraysVisible[context.listofIndexesOfListOfArrraystoShow[i]].size-context.numberofSamplesTOMOve-context.numberOFValuesOnGraph,context.listOfValuesArraysVisible[context.listofIndexesOfListOfArrraystoShow[i]].size-context.numberofSamplesTOMOve).toFloatArray()}

                context.Graphview.listOFPaints =MutableList(context.numberOfGraphsTOShow){i->context.listOfPaintsForVisibleGraphs[context.listofIndexesOfListOfArrraystoShow[i]]}
                context.Graphview.minOFarrayofTimeValues=context.listOfTimeValues[context.listOfTimeValues.size-context.numberofSamplesTOMOve-context.numberOFValuesOnGraph]
                context.Graphview.maxOFarrayofTimeValues=context.listOfTimeValues[context.listOfTimeValues.size-context.numberofSamplesTOMOve-1]
                if (context.flagDefaultYaxisValues)
                {
                    context.maxYaxisvalue=
                        max(MutableList(context.numberOfGraphsTOShow) { i -> context.listOfValuesArraysVisible[context.listofIndexesOfListOfArrraystoShow[i]].max() })
                    context.minYaxisValue=
                        min(MutableList(context.numberOfGraphsTOShow) { i -> context.listOfValuesArraysVisible[context.listofIndexesOfListOfArrraystoShow[i]].min() })
                }
                context.Graphview.maxOFarrayofFunctionValues=context.maxYaxisvalue

                context.Graphview.minOFarrayofFunctionValues=context.minYaxisValue

                context.Graphview.invalidate()
            }
        }

    }
   )



 }




}