package tv.formuler.mytvonline.technic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_course_gridview_list_item.view.*

import tv.formuler.mytvonline.R.layout.activity_course_gridview_list_item

class CourseAdapter(val context: Context, var dataList: ArrayList<CourseData>) :
    android.widget.BaseAdapter() {

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        val v: View;
        if (view == null) {
            v = LayoutInflater.from(context).inflate(activity_course_gridview_list_item, null)
        } else {
            v = view
        }
        v.class_title.text = dataList[position].class_lsubname // 제목
        v.class_description.text = dataList[position].class_lname // 소제목
        v.class_percent.text = dataList[position].class_progress.toString() // 진행도

        if (dataList[position].class_progress == 0 || dataList[position].class_lsubname == "") {
            v.class_percent.setVisibility(View.INVISIBLE)
        } else {
            v.class_percent.setVisibility(View.VISIBLE)
        }

        return v
    }

    override fun getItem(position: Int): CourseData {
        return dataList[position]
    }

    override fun getItemId(pos: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return dataList.size
    }
}