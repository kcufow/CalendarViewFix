package com.kcufow.calendardemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kcufow.calendardemo.adapter.CommonAdapter
import com.kcufow.calendardemo.bean.CommonBean
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
        initListener()
    }

    private fun initListener() {
        sml.setOnRefreshListener {
            initData()
        }
        sml.setOnLoadMoreListener {
            loadMoreData()
        }
        expand_toggle_iv.setOnClickListener {
            if (work_calendar_view.isExpand) {
                work_calendar_view.updateExpand(false)
                expand_toggle_iv.setImageResource(R.drawable.arrow_dow)
            }else{
                work_calendar_view.updateExpand(true)
                expand_toggle_iv.setImageResource(R.drawable.arrow_up)
            }
        }
    }

    private fun loadMoreData() {
        val list = ArrayList<CommonBean>()
        repeat(20){
            list.add(CommonBean("name $it",it,"content ==== $it"))
        }
        adapter.dataList.addAll(list)
        adapter.notifyDataSetChanged()
        sml.finishLoadMore(true)
    }

    private fun initData() {
        val list = ArrayList<CommonBean>()
        repeat(20){
            list.add(CommonBean("name $it",it,"content ==== $it"))
        }
        adapter.setData(list)
        sml.finishRefresh(true)
    }

    private val adapter:CommonAdapter by lazy { CommonAdapter() }
    private fun initView() {

        recycle_view.layoutManager = LinearLayoutManager(this)
        recycle_view.adapter=adapter
    }
}