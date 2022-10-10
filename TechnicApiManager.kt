package tv.formuler.mytvonline.technic

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class TechnicApiManager private constructor() {

    companion object {
        const val TAG = "TechnicApiManager"
        val instance = TechnicApiManager()
    }

    var isLogin = false
    var lessonInfoList = mutableListOf<LessonInfo>()
    var sCode : String? = null
    var sSchool: String? = null
    var sName : String? = null

    val retrofit = IRetrofitService.create()

    fun login(sSchool: String, sName : String, sGrade : String, sClass : String, sNumber : String, callback: retrofit2.Callback<LoginResponse>){
        val callResult = retrofit.login(LoginRequest(sSchool, sName, sGrade, sClass, sNumber))
            callResult.enqueue(callback)
    }

    fun weekAllLesson(sSchool : String?, sCode : String?, callback: retrofit2.Callback<WeekAllLessonResponse>){
        retrofit.weekAllLesson(WeekAllLessonRequest(sSchool, sCode)).enqueue(callback)
    }

    fun setLessonProgress(sSchool: String?, sCode: String?, lCode : Int?, runningTime : Int?, callback: retrofit2.Callback<SetProgressResponse>){
        retrofit.setLessonProgress(SetProgressRequest(sSchool, sCode, lCode, runningTime)).enqueue(callback)
    }

    fun getLessonProgress(sSchool: String?, sCode: String?, lCode : Int?, callback : retrofit2.Callback<GetProgressResonse>){
        retrofit.getLessonProgress(GetProgressRequest(sSchool, sCode, lCode)).enqueue(callback)
    }

    fun lessonFeedback(sSchool: String?, sCode: String?, lCode : Int?, fContents : String?, callback : retrofit2.Callback<FeedbackResponse>){
        retrofit.lessonFeedback(FeedbackRequest(sSchool, sCode, lCode, fContents)).enqueue(callback)
    }

    fun test(callback: retrofit2.Callback<WeekAllLessonResponse>){
        ITest.create().test().enqueue(callback)
    }


    interface IRetrofitService {
        @POST("login")
        fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

        @POST("week-all-lesson")
        fun weekAllLesson(@Body weekAllLessonRequest: WeekAllLessonRequest): Call<WeekAllLessonResponse>

        @POST("lesson-progress")
        fun setLessonProgress(@Body setPregressRequest: SetProgressRequest): Call<SetProgressResponse>

        @GET("lesson-progress")
        fun getLessonProgress(@Body getProgressRequest: GetProgressRequest): Call<GetProgressResonse>

        @POST("lesson-feedback")
        fun lessonFeedback(@Body feedbackRequest: FeedbackRequest): Call<FeedbackResponse>


        companion object {
            private const val BASE_URL = "http://3.35.4.229:5006/app/"
            fun create(): IRetrofitService {
                val gson: Gson = GsonBuilder().setLenient().create()

                return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(IRetrofitService::class.java)
            }
        }
    }
    interface ITest{
        @GET("v3/03052971-671a-4e5b-b429-b01979e98471")
        fun test(): Call<WeekAllLessonResponse>

        companion object{
            private const val BASE_URL = "https://run.mocky.io/"
            fun create(): ITest {
                val gson: Gson = GsonBuilder().setLenient().create()

                return Retrofit.Builder()
                    .baseUrl(ITest.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(ITest::class.java)
            }
        }
    }

}
