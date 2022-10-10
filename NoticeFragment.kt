package tv.formuler.mytvonline.technic

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.api24.fragment_subject_notice.*
import retrofit2.Call
import retrofit2.Response
import tv.formuler.mytvonline.R
import tv.formuler.mytvonline.TAG


class NoticeFragment(val lCode: Int) : SubjectFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subject_notice, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        common_title_container.titleText = getString(R.string.notice_title)
        common_title_container.setTitleEventListener { activity?.finish() }
        notice.text = (activity as SubjectActivity).notice
        complete.setOnClickListener {
            // 질문 사항에 text가 적혀있다면 내용을 서버로 전송한다
            if (!TextUtils.isEmpty(et_qustions.text)) {
                TechnicApiManager.instance.lessonFeedback(
                    TechnicApiManager.instance.sSchool,
                    TechnicApiManager.instance.sCode,
                    lCode,
                    et_qustions.text.toString(),
                    object : retrofit2.Callback<FeedbackResponse> {
                        override fun onResponse(
                            call: Call<FeedbackResponse>?,
                            response: Response<FeedbackResponse>?
                        ) {
                            response?.body()?.let {
                                if (it.successCode == 200) {
                                    Log.d(TAG, "feedback success")
                                }
                            }
                        }

                        override fun onFailure(call: Call<FeedbackResponse>?, t: Throwable?) {
                            Log.d(TAG, "feedback failed ${t?.message}")
                        }
                    })
            }
            activity?.finish()
        }
        et_qustions.requestFocus()
    }

    override fun onKeyUp(keyCode: Int): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            activity?.finish()
            return true
        }
        return false
    }
}