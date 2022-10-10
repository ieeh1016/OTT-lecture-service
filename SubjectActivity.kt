package tv.formuler.mytvonline.technic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.EventListener
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.api24.fragment_subject_notice.*
import kotlinx.android.synthetic.api24.fragment_subject_player.*
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import retrofit2.Call
import retrofit2.Response
import tv.formuler.formulerlib.config.TAppState
import tv.formuler.mytvonline.R
import tv.formuler.mytvonline.TAG
import tv.formuler.mytvonline.common.util.Clog
import tv.formuler.mytvonline.live.manager.LiveManager
import tv.formuler.mytvonline.tvservice.MOLTvManager

class SubjectActivity : AppCompatActivity() {
    var fragment: Fragment? = null;
    val url by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra("video_url")
    }
    val notice by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra("video_notice")
    }
    val lcode by lazy(LazyThreadSafetyMode.NONE) {
        intent.getIntExtra("video_lcode", 0)
    }
    val lhour by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra("video_lhour")
    }
    val ldate by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra("video_ldate")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_player)

        val fragment = PlayerFragment(url,lcode,lhour,ldate)
        val bundle: Bundle = Bundle()
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().add(R.id.container, fragment as PlayerFragment)
            .commit()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (fragment is SubjectFragment) {
            if ((fragment as SubjectFragment).onKeyUp(keyCode)) {
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

abstract class SubjectFragment : Fragment() {
    abstract fun onKeyUp(keyCode: Int): Boolean
}
