package com.kcufow.calendardemo.adapter

import android.service.autofill.TextValueSanitizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kcufow.calendardemo.R
import com.kcufow.calendardemo.bean.CommonBean

class CommonAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>() {
      var dataList=ArrayList<CommonBean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        return CommonViewHolder(createItemView(parent, R.layout.item_common))

    }

    private fun createItemView(parent: ViewGroup,resId:Int):View{

        return LayoutInflater.from(parent.context).inflate(resId,parent,false)
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as CommonViewHolder
        holder.tvContent.text = dataList[position].name
    }
    fun setData(dataList:List<CommonBean>){
        this.dataList.clear()
        this.dataList .addAll(dataList)
        notifyDataSetChanged()
    }
    class CommonViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val tvContent :TextView = itemView.findViewById(R.id.content_tv)
    }
}