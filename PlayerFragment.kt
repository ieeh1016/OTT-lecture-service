package tv.formuler.mytvonline.technic

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.room.Room
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.api24.fragment_subject_player.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import tv.formuler.formulerlib.config.TAppState
import tv.formuler.mytvonline.R
import tv.formuler.mytvonline.live.manager.LiveManager
import tv.formuler.mytvonline.tvservice.MOLTvManager


class PlayerFragment(val url: String, val lcode: Int, val lhour: String, val ldate: String) :
    SubjectFragment() {

    private lateinit var db: CourseDBDatabase

    //API에서 sCode(학생의 고유번호), sSchool(학생의 학교)를 가져온다.
    private val sCode: String? = TechnicApiManager.instance.sCode
    private val sSchool: String? = TechnicApiManager.instance.sSchool

    private var player: SimpleExoPlayer? = null
    private val playbackStateListener: Player.EventListener = playbackStateListener()

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private fun playbackStateListener() = object : Player.EventListener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY"
                ExoPlayer.STATE_ENDED -> {
                    val fragment = NoticeFragment(lcode)
                    (activity as SubjectActivity).fragment = fragment
                    val ft = activity!!.supportFragmentManager.beginTransaction()
                    ft.replace(R.id.container, fragment)
                    ft.addToBackStack(null)
                    ft.commit()
                    "ExoPlayer.STATE_ENDED"
                }
                else -> "UNKNOWN_STATE"
            }
            Log.d("ExoPlayerActivity", "changed state to $stateString")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subject_player, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // 선택한 강의의 진도율
        var progress: Float = 0F

        // Room Data
        db = Room.databaseBuilder(
            requireActivity().applicationContext,
            CourseDBDatabase::class.java,
            "CourseData3"
        ).build()

        with(db.CourseDBDataDao()) {
            CoroutineScope(Dispatchers.IO).launch {

                //선택한 강의의 지금까지의 진도율
                var currentVideoProgress = 0

                //선택한 강의의 전체 강의 길이
                var currentVideoDuration = 0

                //선택한 강의에서 이전에 들었던 강의 진도율이 존재하는지 체크
                if (getlProgress(lcode) != null) {
                    //만약 선택한 강의에 이전에 들었던 진도율이 존재하면 lcode(고유번호)를 통해 db에 저장되어있는 선택된 강의의 진도율을 가져온다.
                    currentVideoProgress = getlProgress(lcode)!!
                }

                if (getlDuration(lcode) != null) {
                    //만약 선택한 강의에 강의 길이가 존재하면 lcode(고유번호)를 통해 db에 저장되어있는 선택된 강의의 전체 길이을 가져온다.
                    currentVideoDuration = getlDuration(lcode)!!
                }

                //이미 진도가 나간 이후 부터로 진도율을 계산한다.
                progress = currentVideoProgress.toFloat() * 0.01F * currentVideoDuration

                //플레이지점을 마지막 진도를 나간 이후로 수정한다. 만약 처음 플레이를 한다면 모든 값은 0이다.
                playbackPosition = progress.toLong()
            }


        }
    }

    public override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (MOLTvManager.getInstance().appState != TAppState.STATE_AndroidApp) {
            LiveManager.getInstance().changeAppState(TAppState.STATE_AndroidApp)
        }

        if (player == null) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onStop() {
        super.onStop()
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }

        var progress = (100.0 * player?.contentPosition!! / player?.duration!!).toInt()
        var duration = player?.duration!!.toInt()

        val course = arrayListOf<CourseDBData>()
        course.add(CourseDBData(ldate, lcode, sSchool!!, sCode!!, progress, duration, lhour))

        CoroutineScope(Dispatchers.IO).launch {
            with(db.CourseDBDataDao()) {
                //수강한 진도율과 db에 저장되어있는 진도율과 비교해서 수강한 진도율이 db에 저장되어 있는 진도율보다 클때 수강한 진도율을 갱신한다.
                var courseVODProgress: Int = 0
                if (getlProgress(lcode) != null) {
                    courseVODProgress = getlProgress(lcode)!!
                }
                if (courseVODProgress <= progress) {
                    TechnicApiManager.instance.setLessonProgress(
                        sSchool,
                        sCode,
                        lcode,
                        progress,
                        object : retrofit2.Callback<SetProgressResponse> {
                            override fun onFailure(
                                call: Call<SetProgressResponse>?,
                                t: Throwable?
                            ) {
                                Log.d("실패했다면?", "실패")
                            }

                            override fun onResponse(
                                call: Call<SetProgressResponse>?,
                                response: Response<SetProgressResponse>?
                            ) {
                                Log.d("성공했다면?", "성공 ${response?.body()?.successCode}")
                            }
                        })
                    insertAll(*course.toTypedArray()) //ROOM DB에 데이터 삽입(갱신) 한다
                    testSingle._testLive.postValue(Unit)
                } else {
                }
            }
        }
        releasePlayer()
    }


    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(context!!)
            .build()
            .also { exoPlayer ->
                video_view.player = exoPlayer
                val mediaItem = MediaItem.fromUri(url)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.prepare()
                exoPlayer.addListener(playbackStateListener)
            }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        video_view.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        player = null
    }

    override fun onKeyUp(keyCode: Int): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (!video_view.isControllerVisible) {
                video_view.showController()
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (video_view.isControllerVisible) {
                video_view.hideController()
                return true
            }
        }
        return false
    }


}