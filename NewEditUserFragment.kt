package tv.formuler.mytvonline.technic

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.room.Room
import kotlinx.android.synthetic.main.dialog_vod_next_play.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import tv.formuler.mytvonline.R
import tv.formuler.mytvonline.common.dialog.BaseButtonDialogFragment
import tv.formuler.mytvonline.common.dialog.ListItemDialogAdapter
import tv.formuler.mytvonline.common.dialog.ListItemDialogFragment
import tv.formuler.mytvonline.common.model.ListDialogItem
import tv.formuler.mytvonline.common.util.Clog
import tv.formuler.mytvonline.common.util.MolUtils
import tv.formuler.mytvonline.common.view.CommonTitleView
import tv.formuler.mytvonline.register.NewEditServerOptionItem
import tv.formuler.mytvonline.technic.data.School
import tv.formuler.mytvonline.technic.data.User
import tv.formuler.mytvonline.technic.data.UserDao
import java.util.*
import kotlin.collections.ArrayList

class NewEditUserFragment : Fragment(), View.OnFocusChangeListener {
    private val TAG = "NewEditUserFragment"
    private lateinit var mCommonTitleView: CommonTitleView

    private lateinit var mSchoolItem: NewEditServerOptionItem
    private lateinit var mGradeItem: NewEditServerOptionItem
    private lateinit var mClassItem: NewEditServerOptionItem
    private lateinit var mNumberItem: NewEditServerOptionItem
    private lateinit var mNameItem: EditText
    private lateinit var mLoading: ProgressBar

    private var schools: ArrayList<School> = ArrayList<School>()
    private var grades: ArrayList<Int> = ArrayList<Int>()
    private var classes: ArrayList<Int> = ArrayList<Int>()
    private var numbers: ArrayList<Int> = ArrayList<Int>()

    private var selectedSchool: School? = null
    private var selectedGrade = -1
    private var selectedClass = -1
    private var selectedNumber = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater
            .inflate(R.layout.fragment_new_edit_user, container, false) as RelativeLayout
        rootView.background = MolUtils.getWallpaper(context)
        initUI(rootView)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        schools.add(School("HwaRangE", "화랑초등학교", "화랑초등학교"))
        for (i in 1..6) {
            grades.add(i)
        }
        for (i in 1..10) {
            classes.add(i)
        }
        for (i in 1..30) {
            numbers.add(i)
        }
    }

    private fun initUI(rootView: View) {
        initCommonTitle(rootView)

        mSchoolItem = rootView.findViewById(R.id.school_layout)
        mGradeItem = rootView.findViewById(R.id.grade_layout)
        mClassItem = rootView.findViewById(R.id.class_layout)
        mNumberItem = rootView.findViewById(R.id.number_layout)
        mNameItem = rootView.findViewById(R.id.user_name_edit)
        mLoading = rootView.findViewById(R.id.login_loading_bar)

        mSchoolItem.initViews(
            resources.getDrawable(R.drawable.ic_school_name, null),
            getString(R.string.school_name), getString(R.string.select_school)
        )

        mGradeItem.initViews(
            resources.getDrawable(R.drawable.ic_school_class, null),
            getString(R.string.grade_number), getString(R.string.select_grade)
        )

        mClassItem.initViews(
            resources.getDrawable(R.drawable.ic_school_class, null),
            getString(R.string.class_number), getString(R.string.select_class)
        )

        mNumberItem.initViews(
            resources.getDrawable(R.drawable.ic_school_class, null),
            getString(R.string.student_number), getString(R.string.select_number)
        )

        mSchoolItem.setOnFocusChangeListener(this)
        mGradeItem.setOnFocusChangeListener(this)
        mClassItem.setOnFocusChangeListener(this)
        mNumberItem.setOnFocusChangeListener(this)
        mSchoolItem.requestFocus();

        mSchoolItem.setOnClickListener {
            val listData = ArrayList<ListDialogItem>()
            schools.forEach { s ->
                listData.add(ListDialogItem(0, ListItemDialogAdapter.LIST_ITEM_TYPE_BTN, s.name))
            }

            val selectedItemIndex = if (selectedSchool == null) {
                0
            } else {
                var idx = 0;
                schools.forEachIndexed { index, school ->
                    if (selectedSchool == school) {
                        idx = index + 1
                        return@forEachIndexed
                    }
                }
                idx
            }
            listData[selectedItemIndex].setSelected(true);

            val dialog = BaseButtonDialogFragment.newListButtonInstance(
                null,
                null,
                getString(R.string.select_school),
                listData,
                selectedItemIndex,
                R.color.black_opacity_fifty,
                object : BaseButtonDialogFragment.IButtonEventListener {
                    override fun onButtonClicked(position: Int) {
                        val school = schools.get(position);
                        if (school == selectedSchool) {
                            return
                        }
                        selectedSchool = school;
                        mSchoolItem.valueTextView.text = selectedSchool?.name;
                    }

                    override fun onButtonFocused(position: Int, hasFocus: Boolean) {}
                })
            dialog.showDialog(
                requireActivity().supportFragmentManager, ListItemDialogFragment.TAG,
                requireActivity()
            )
        }

        mGradeItem.setOnClickListener {
            if (selectedSchool == null) {
                return@setOnClickListener
            }

            val listData = ArrayList<ListDialogItem>()
            grades.forEach { c ->
                listData.add(
                    ListDialogItem(
                        c,
                        ListItemDialogAdapter.LIST_ITEM_TYPE_BTN,
                        getString(R.string.selected_grade, c)
                    )
                )
            }

            val selectedItemIndex = Math.max(0, selectedGrade - 1)
            listData[selectedItemIndex].setSelected(true);

            val dialog = BaseButtonDialogFragment.newListButtonInstance(
                null,
                null,
                getString(R.string.select_grade),
                listData,
                selectedItemIndex,
                R.color.black_opacity_fifty,
                object : BaseButtonDialogFragment.IButtonEventListener {
                    override fun onButtonClicked(position: Int) {
                        selectedGrade = position + 1
                        mGradeItem.valueTextView.text =
                            getString(R.string.selected_grade, selectedGrade)
                    }

                    override fun onButtonFocused(position: Int, hasFocus: Boolean) {}
                })
            dialog.showDialog(
                requireActivity().supportFragmentManager, ListItemDialogFragment.TAG,
                requireActivity()
            )
        }

        mClassItem.setOnClickListener {
            if (selectedSchool == null) {
                return@setOnClickListener
            }

            val listData = ArrayList<ListDialogItem>()
            classes.forEach { c ->
                listData.add(
                    ListDialogItem(
                        c,
                        ListItemDialogAdapter.LIST_ITEM_TYPE_BTN,
                        getString(R.string.selected_class, c)
                    )
                )
            }

            val selectedItemIndex = Math.max(0, selectedClass - 1)
            listData[selectedItemIndex].setSelected(true);

            val dialog = BaseButtonDialogFragment.newListButtonInstance(
                null,
                null,
                getString(R.string.select_class),
                listData,
                selectedItemIndex,
                R.color.black_opacity_fifty,
                object : BaseButtonDialogFragment.IButtonEventListener {
                    override fun onButtonClicked(position: Int) {
                        selectedClass = position + 1
                        mClassItem.valueTextView.text =
                            getString(R.string.selected_class, selectedClass)
                    }

                    override fun onButtonFocused(position: Int, hasFocus: Boolean) {}
                })
            dialog.showDialog(
                requireActivity().supportFragmentManager, ListItemDialogFragment.TAG,
                requireActivity()
            )
        }

        mNumberItem.setOnClickListener {
            if (selectedSchool == null) {
                return@setOnClickListener
            }

            val listData = ArrayList<ListDialogItem>()
            numbers.forEach { c ->
                listData.add(
                    ListDialogItem(
                        c,
                        ListItemDialogAdapter.LIST_ITEM_TYPE_BTN,
                        getString(R.string.selected_number, c)
                    )
                )
            }

            val selectedItemIndex = Math.max(0, selectedNumber - 1)
            listData[selectedItemIndex].setSelected(true);

            val dialog = BaseButtonDialogFragment.newListButtonInstance(
                null,
                null,
                getString(R.string.select_number),
                listData,
                selectedItemIndex,
                R.color.black_opacity_fifty,
                object : BaseButtonDialogFragment.IButtonEventListener {
                    override fun onButtonClicked(position: Int) {
                        selectedNumber = position + 1
                        mNumberItem.valueTextView.text =
                            getString(R.string.selected_number, selectedNumber)
                    }

                    override fun onButtonFocused(position: Int, hasFocus: Boolean) {}
                })
            dialog.showDialog(
                activity!!.supportFragmentManager, ListItemDialogFragment.TAG,
                activity
            )
        }

        rootView.findViewById<Button>(R.id.register_btn).setOnClickListener {
            if (selectedSchool == null || selectedGrade == -1 || selectedClass == -1 || selectedNumber == -1 ||
                mNameItem.text.isEmpty()
            ) {
                return@setOnClickListener
            }

            mLoading.visibility = View.VISIBLE
            TechnicApiManager.instance.login(
                selectedSchool!!.sId,
                mNameItem.text.toString(),
                selectedGrade.toString(),
                selectedClass.toString().padStart(2, '0'),
                selectedNumber.toString().padStart(2, '0'),
                object : retrofit2.Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>?,
                        response: Response<LoginResponse>?
                    ) {
                        mLoading.visibility = View.GONE

                        CoroutineScope(Dispatchers.IO).launch {
                            response?.body()?.apply {
                                if (loginCode == 200) {
                                    val db = Room.databaseBuilder(
                                        requireContext(),
                                        TecDatabase::class.java, TecDatabase.DB_NAME
                                    ).build()
                                    val sGrade = selectedGrade.toString()
                                    val sClass = selectedClass.toString().padStart(2, '0')
                                    val sNumber = selectedNumber.toString().padStart(2, '0')

                                    val curId = "$sGrade $sClass $sNumber ${selectedSchool!!.sId}"

                                    var alreadyExist = false
                                    val allUser = db.userDao().getAllUsers()
                                    for (i in allUser.indices) {
                                        if (curId == allUser[i].id) {
                                            alreadyExist = true
                                            break
                                        }
                                    }

                                    if (!alreadyExist) {
                                        db.userDao().insertUser(
                                            User(
                                                curId,
                                                selectedSchool!!.sId,
                                                selectedSchool!!.name,
                                                sGrade,
                                                sClass,
                                                sNumber,
                                                mNameItem.text.toString()
                                            )
                                        )
                                        //TechnicApiManager 에 로그인한 유저 정보 저장
                                        with(TechnicApiManager.instance) {
                                            isLogin = true
                                            sCode = sGrade + sClass + sNumber
                                            sSchool = selectedSchool!!.sId
                                            sName = mNameItem.text.toString()
                                            lessonInfoList.clear()
                                            weekAllLesson(sSchool, sCode,
                                                object : retrofit2.Callback<WeekAllLessonResponse> {
                                                    override fun onResponse(
                                                        call: Call<WeekAllLessonResponse>?,
                                                        response: Response<WeekAllLessonResponse>?
                                                    ) {
                                                        response?.body()?.lessonInfoList.let {
                                                            for (i in it!!) {
                                                                lessonInfoList.add(i)
                                                            }
                                                        }
                                                        Clog.r(TAG, "WeekAllLesson response ${response?.body().toString()}"
                                                        )
                                                    }

                                                    override fun onFailure(
                                                        call: Call<WeekAllLessonResponse>?,
                                                        t: Throwable?
                                                    ) {
                                                        Clog.r(TAG, "주간 시간표 입력 실패")
                                                    }
                                                })
                                        }

                                        withContext(Dispatchers.Main) {
                                            requireActivity().supportFragmentManager.popBackStack()
                                        }
                                    }
                                    // 이미 존재하는 학생 정보일때
                                    else {
                                        withContext(Dispatchers.Main){
                                            Toast.makeText(requireActivity(), "이미 등록된 학생입니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                // 올바르지 않은 학생 정보일때
                                else {
                                    withContext(Dispatchers.Main){
                                        Toast.makeText(requireActivity(), "올바르지 않은 학생 정보입니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        Clog.r(TAG, "Login response ${response?.body().toString()}")
                    }

                    override fun onFailure(call: Call<LoginResponse>?, t: Throwable?) {
                        Clog.r(TAG, "Login response Fail")
                    }
                }
            )
        }
    }

    protected fun initCommonTitle(rootView: View) {
        mCommonTitleView = rootView.findViewById(R.id.common_title_container)
        mCommonTitleView.setTitleText(R.string.add_user)
        mCommonTitleView.setTitleEventListener {
            activity!!.supportFragmentManager.popBackStack()
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v is NewEditServerOptionItem) {
            val textColor =
                if (hasFocus) R.color.connections_new_option_summary_text_f
                else R.color.connections_new_option_summary_text
            v.valueTextView.setTextColor(resources.getColor(textColor, null))
        }
    }
}