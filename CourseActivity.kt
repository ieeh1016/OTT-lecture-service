package tv.formuler.mytvonline.technic

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_course.*
import kotlinx.coroutines.*
import retrofit2.Response
import tv.formuler.mytvonline.R
import tv.formuler.mytvonline.common.constant.IPTVKeyEvent
import java.util.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import javax.inject.Inject

object testSingle{
    var _testLive =  MutableLiveData<Unit>()
    val testLive: LiveData<Unit>
        get() = _testLive
}

class CourseActivity : AppCompatActivity() {


    // gridview 내에서 선택한 강의의 position
    private var clickedPosition: Int = 0

    //요일별 강의 진행율
    private lateinit var classMonProgress: TextView
    private lateinit var classTuesProgress: TextView
    private lateinit var classWednesgress: TextView
    private lateinit var classThursProgress: TextView
    private lateinit var classFriProgress: TextView
    private var checkData:Int = 0
    //오른족 상단 데일리 강의 진행율
    private lateinit var classDailyProgress: TextView

    private lateinit var db: CourseDBDatabase
    private lateinit var gridviewAdapter: CourseAdapter

    //요일별 progress가 저장될 리스트
    private var courseprogress = arrayListOf<Int>(0,0,0,0,0)
    private var dataList = arrayListOf<CourseData>()

    private val dayOfWeekList = listOf<String>("월요일","화요일","수요일","목요일","금요일")
    private var weekOfDay: Int = 0

    //API에서 sCode(학생의 고유번호) , sSchool(학생의 학교)를 가져옴
    private val sCode: String? = TechnicApiManager.instance.sCode
    private val sSchool: String? = TechnicApiManager.instance.sSchool

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        val clearButton = findViewById<ImageButton>(R.id.back_icon)
        classMonProgress = findViewById<TextView>(R.id.learning_monday_percent)
        classTuesProgress = findViewById<TextView>(R.id.learning_tuesday_percent)
        classWednesgress = findViewById<TextView>(R.id.learning_wednesday_percent)
        classThursProgress = findViewById<TextView>(R.id.learning_thursday_percent)
        classFriProgress = findViewById<TextView>(R.id.learning_friday_percent)

        classDailyProgress = findViewById<TextView>(R.id.learning_percent)

        gridviewAdapter = CourseAdapter(this, dataList)
        gridview.adapter = gridviewAdapter

        val classTime = findViewById<TextView>(R.id.course_time) //수업 시간
        val classSubTime = findViewById<TextView>(R.id.course_name) //수업 교시
        val course_sector = findViewById<TextView>(R.id.course_sector) //왼쪽 상단에 뜨는 수업 부제목
        val course_desc = findViewById<TextView>(R.id.course_desc) //왼쪽 상단에 뜨는 수업 설명

        db = Room.databaseBuilder(applicationContext, CourseDBDatabase::class.java, "CourseData3")
            .build()

        for (i in 0..29) {
            dataList.add(CourseData())
        }

        for (lesson in TechnicApiManager.instance.lessonInfoList) {
            val position = getPosition(lesson.lHour) //수업이 존재하는 요일, 수강시간 별 위치 불러옴

            dataList[position].class_lsubname = lesson.lSubName
            dataList[position].class_lname = lesson.lName
            dataList[position].class_lurl = lesson.lURL
            dataList[position].class_lcode = lesson.lCode
            dataList[position].class_lcontent = lesson.lContent
            dataList[position].class_ldate = lesson.lDate
            dataList[position].class_lhour = lesson.lHour
        }

        clearButton.setOnClickListener {
            onBackPressed()
        }


        //TODO 기존에 API에서 데이터를 받을때의 정보를 oncreate할때마다 비교해서 이전에 받아온 정보와 일치하지않으면,
        //TODO 즉 한 주가 바뀌어서 다른 강의정보를 받아온다면 기존의 db는 초기화 되어야한다.



        gridview.setOnItemClickListener { parent: AdapterView<*>?, v: View?, position: Int, l: Long ->
            if (dataList[position].class_lurl != "") {
                val intent = Intent(this, SubjectActivity::class.java)
                intent.putExtra("video_url", dataList[position].class_lurl)
                intent.putExtra("video_notice", dataList[position].class_lname)
                intent.putExtra("video_lcode", dataList[position].class_lcode)
                intent.putExtra("video_ldate", dataList[position].class_ldate)
                intent.putExtra("video_lhour", dataList[position].class_lhour)
                startActivity(intent)
            }
        }

        gridview.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {

                clickedPosition = position
                course_sector.text = dataList[position].class_lname
                course_desc.text = dataList[position].class_lcontent

                val time = position / 5 + 9
                val timeClass = position / 5 + 1
                classTime.text =
                    "$time${resources.getString(R.string.course_time)} - ${time + 1}${resources.getString(
                        R.string.course_time
                    )}"
                classSubTime.text =
                    "${dataList[position].class_lsubname} ${timeClass}${resources.getString(R.string.course_class)}"

                val weekOfDay = position % 5
                classDailyProgress.text =
                    "${dayOfWeekList[weekOfDay]} - ${resources.getString(R.string.course_progrss)} ${courseprogress[weekOfDay].toString()}${resources.getString(
                        R.string.course_percent
                    )}"
            }
        })


        testSingle.testLive.observe(this, androidx.lifecycle.Observer {
            CoroutineScope(Dispatchers.IO).launch {
                refresh()
            }
        })
    }



    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            var checkData = false

            val all = db.CourseDBDataDao().getCourseData(sCode!!, sSchool!!)

            if(all?.size!! > 0){
                for(i in TechnicApiManager.instance.lessonInfoList){
                    if(all[0].lCode == i.lCode && all[0].lDate == i.lDate){
                        checkData = true
                    }
                }
                if(checkData == false){
                    db.CourseDBDataDao().clearAll()
                }
            }
            refresh()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP) {
            when (event.keyCode) {
                IPTVKeyEvent.KEYCODE_1 -> {

                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    fun focusSection() {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

        val hour = Calendar.getInstance()
        val hourOfDay = hour.get(Calendar.HOUR_OF_DAY)

        var focusSelection: Int = 0
        var dayOfWeekList = arrayOf<Int>(0, 5, 10, 15, 20, 25)

        for (i in 0..5) {
            when (dayOfWeek) {
                3 -> dayOfWeekList[i] += 1
                4 -> dayOfWeekList[i] += 2
                5 -> dayOfWeekList[i] += 3
                6 -> dayOfWeekList[i] += 4
            }
        }

        when (hourOfDay) {
            in 0..10 -> focusSelection = dayOfWeekList[0]
            10 -> focusSelection = dayOfWeekList[1]
            11 -> focusSelection = dayOfWeekList[2]
            12 -> focusSelection = dayOfWeekList[3]
            13 -> focusSelection = dayOfWeekList[4]
            in 14..24 -> focusSelection = dayOfWeekList[5]
        }

        gridview.requestFocus()
        gridview.setSelection(focusSelection)
    }

    fun getPosition(str: String): Int {
        var day = 0
        when (str.substring(0, 3)) {
            "Mon" -> {
                day = 0
            }
            "Tue" -> {
                day = 1
            }
            "Wed" -> {
                day = 2
            }
            "Thu" -> {
                day = 3
            }
            "Fri" -> {
                day = 4
            }
        }
        var classtime = 0
        when (str.substring(3, 5)) {
            "01" -> {
                classtime = 0
            }
            "02" -> {
                classtime = 5
            }
            "03" -> {
                classtime = 10
            }
            "04" -> {
                classtime = 15
            }
            "05" -> {
                classtime = 20
            }
            "06" -> {
                classtime = 25
            }
        }
        val position = day + classtime
        return position
    }

    private suspend fun refresh() = withContext(Dispatchers.IO) {

        val dailyProgress = listOf<TextView>(
            classMonProgress,
            classTuesProgress,
            classWednesgress,
            classThursProgress,
            classFriProgress
        )

        val all = db.CourseDBDataDao().getCourseData(sCode!!, sSchool!!)


        for (i in all!!) {
            val position = getPosition(i.lHour)
            dataList[position].class_progress = i.lProgress
        }

        for (i in 0 until courseprogress.size) {
            var count: Int = 0
            val sum: Int =
                dataList[i].class_progress + dataList[i + 5].class_progress + dataList[i + 10].class_progress + dataList[i + 15].class_progress + dataList[i + 20].class_progress + dataList[i + 25].class_progress
            for (k in 0..5) {
                if (dataList[i + k * 5].class_lcode != 0) {
                    count = count + 1
                }
            }
            var avg: Int = 0
            if (count == 0) {
                avg = 0
            } else {
                avg = sum / count
            }
            courseprogress[i] = avg
        }
        withContext(Dispatchers.Main) {
            gridviewAdapter.dataList = dataList
            gridviewAdapter.notifyDataSetChanged()

            for (i in 0 until dailyProgress.size) {
                dailyProgress[i].text =
                    courseprogress[i].toString() + "${resources.getString(R.string.course_percent)}"
            }
            if(clickedPosition == 0){
                focusSection()
            }
            weekOfDay = clickedPosition % 5
            classDailyProgress.text =
                "${dayOfWeekList[weekOfDay]} - ${resources.getString(R.string.course_progrss)} ${courseprogress[weekOfDay].toString()}${resources.getString(
                    R.string.course_percent
                )}"
        }
    }
}