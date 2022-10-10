package tv.formuler.mytvonline.technic

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.HTTP
import tv.formuler.mytvonline.R
import tv.formuler.mytvonline.TAG
import tv.formuler.mytvonline.common.constant.IPTVKeyEvent

class ApiTestAcitivy : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_test)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP) {
            when (event.keyCode) {
                IPTVKeyEvent.KEYCODE_1 -> {
                    Log.d(TAG, "1 clicked")
                    val userName = "harry"
                    TechnicApiManager.instance.login("HwaRangE", userName, "1", "01", "03", object : retrofit2.Callback<LoginResponse> {
                            override fun onFailure(call: Call<LoginResponse>?, t: Throwable?) {
                                Log.d("login_failed", t?.message)
                            }
                            override fun onResponse(call: Call<LoginResponse>?, response: Response<LoginResponse>?) {
                                val loginResult = response?.body()
                                // 로그인 성공시 loginCode는 200으로 떨어진다
                                if (loginResult!!.loginCode == 200) {
                                    with(TechnicApiManager.instance){
                                        isLogin = true
                                        sSchool = loginResult.sSchool
                                        sCode = loginResult.sCode.toString()
                                        sName = userName
                                    }
                                    Log.d("isLogin", TechnicApiManager.instance.isLogin.toString())
                                    Log.d("login_success", TechnicApiManager.instance.sSchool + TechnicApiManager.instance.sCode + TechnicApiManager.instance.sName)
                                } else {
                                    Log.d(TAG, "login failed")
                                }
                            }
                        })
                }
                IPTVKeyEvent.KEYCODE_2 -> {
                    Log.d(TAG, "2 clicked")
                    TechnicApiManager.instance.weekAllLesson(TechnicApiManager.instance.sSchool, TechnicApiManager.instance.sCode.toString(), object : retrofit2.Callback<WeekAllLessonResponse> {
                            override fun onFailure(call: Call<WeekAllLessonResponse>?, t: Throwable?) {
                                Log.d("week_lesson_failed", t?.message)
                            }

                            override fun onResponse(call: Call<WeekAllLessonResponse>?, response: Response<WeekAllLessonResponse>?) {
                                TechnicApiManager.instance.lessonInfoList.clear()
                                //로그인이 되어있는 상태라면 해당 학생의 week-all-lesosn을 lessonInfoList에 저장한다
                                if (TechnicApiManager.instance.isLogin){
                                    //val weeKAllLessonResult = response?.body()

                                    response?.body()?.lessonInfoList?.let {
                                        for (i in 0..it.size - 1) {
                                            TechnicApiManager.instance.lessonInfoList.add(it[i])
                                            Log.d(TAG, TechnicApiManager.instance.lessonInfoList.toString())
                                        }
                                    }
                                }
                                Log.d("week_lesson_success", TechnicApiManager.instance.lessonInfoList.toString())
                            }
                        })
                }
                IPTVKeyEvent.KEYCODE_3 -> {
                    Log.d(TAG, "3 clicked")

                    TechnicApiManager.instance.setLessonProgress("HwaRangE", "30120", 53, 10, object : retrofit2.Callback<SetProgressResponse> {
                            override fun onFailure(call: Call<SetProgressResponse>?, t: Throwable?) {
                                Log.d("set_progress_failed", t?.message)
                            }
                            override fun onResponse(
                                call: Call<SetProgressResponse>?,
                                response: Response<SetProgressResponse>?
                            ) {
                                if (response?.body()?.successCode == 200){
                                    Log.d("set_progress_success", response?.body().successCode.toString())
                                } else {
                                    Log.d("set_progress_failed", "wrong parameters")
                                }
                            }
                        })
                }
                IPTVKeyEvent.KEYCODE_4 -> {
                    Log.d(TAG, "4 clicked")
                    TechnicApiManager.instance.test(object : retrofit2.Callback<WeekAllLessonResponse>{
                        override fun onFailure(call: Call<WeekAllLessonResponse>?, t: Throwable?) {
                            Log.d("week_lesson_failed", t?.message)
                        }

                        override fun onResponse(call: Call<WeekAllLessonResponse>?, response: Response<WeekAllLessonResponse>?) {
                            TechnicApiManager.instance.lessonInfoList.clear()
                            //로그인이 되어있는 상태라면 해당 학생의 week-all-lesosn을 lessonInfoList에 저장한다
                            if (TechnicApiManager.instance.isLogin){
                                val weeKAllLessonResult = response?.body()

                                weeKAllLessonResult?.lessonInfoList?.let {
                                    for (i in 0..it.size - 1) {
                                        TechnicApiManager.instance.lessonInfoList.add(it[i])
                                        Log.d(TAG, TechnicApiManager.instance.lessonInfoList.toString())
                                    }
                                }
                            }
                            Log.d("week_lesson_success", TechnicApiManager.instance.lessonInfoList.toString())
                        }
                    })

                }
                IPTVKeyEvent.KEYCODE_5 -> {
                    Log.d(TAG, "5 clicked")
                    TechnicApiManager.instance.getLessonProgress("HwaRangE", "30120", 53, object : retrofit2.Callback<GetProgressResonse>{
                        override fun onResponse(call: Call<GetProgressResonse>?, response: Response<GetProgressResonse>?) {
                            Log.d("get_progress_success", response?.body()?.runningTime)
                        }

                        override fun onFailure(call: Call<GetProgressResonse>?, t: Throwable?) {
                            Log.d("get_progress_success", t?.message)
                        }
                    })
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}