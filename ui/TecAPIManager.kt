package tv.formuler.mytvonline.technic.ui

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import tv.formuler.mytvonline.technic.data.Class
import tv.formuler.mytvonline.technic.data.School
import tv.formuler.mytvonline.technic.data.User

interface TecAPI {
    @GET("schools")
    fun schools(): Call<List<School>>
    @GET("classes")
    fun classes(): Call<List<Class>>
    @GET("classes/{schoolId}")
    fun classes(@Path("schoolId") schoolId: String): Call<List<Class>>
    @POST("user/register")
    fun register(@Body account: User): Call<User>
}

class TecAPIManager {
    public val BASE_URL: String
    private val api: TecAPI

    init {
        BASE_URL = "http://192.168.5.162:3000/"

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(TecAPI::class.java)
    }

    fun requestSchools(): RestAPI<List<School>> {
        return RestAPI(api.schools())
    }

    fun requestClasses(sId: String): RestAPI<List<Class>> {
        return RestAPI(api.classes(sId))
    }

    fun requestRegister(sId: String, cId: String, number: String, name: String): RestAPI<User> {
        return RestAPI(api.register(User("", number, name, sId, "", cId, "")))
    }
}

class RestAPI<T>(val call: Call<T>) {
    private var failure: OnFailure? = null
    private var response: OnResponse<T>? = null

    fun onFailure(failure: OnFailure): RestAPI<T> {
        this.failure = failure
        return this
    }

    fun onResponse(response: OnResponse<T>): RestAPI<T> {
        this.response = response
        return this
    }

    fun request() {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, res: Response<T>?) {
                response?.let {
                    it.onResponse(res?.body())
                }
            }

            override fun onFailure(call: Call<T>?, t: Throwable?) {
                failure?.let {
                    it.onFailure(-1, t?.message)
                }
            }

        })
    }

}

interface OnResponse<T> {
    fun onResponse(response: T?)
}

interface OnFailure {
    fun onFailure(code: Int, message: String?)
}

