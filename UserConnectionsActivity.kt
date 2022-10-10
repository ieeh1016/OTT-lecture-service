package tv.formuler.mytvonline.technic

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_connections_list.*
import tv.formuler.mytvonline.BaseActivity
import tv.formuler.mytvonline.IDispatchKeyEvent
import tv.formuler.mytvonline.R
import tv.formuler.mytvonline.common.constant.Constants
import tv.formuler.mytvonline.common.constant.IPTVKeyEvent
import tv.formuler.mytvonline.common.manager.NetworkStatusManager
import tv.formuler.mytvonline.common.util.Clog
import tv.formuler.mytvonline.tvservice.MOLTvManager

class UserConnectionsActivity : BaseActivity() {

    private val TAG = "UserConnectionsActivity"
    val REQ_PORTAL = 1
    val REQ_PLAYLIST = 2
    val REQ_DVB = 3

    private lateinit var mUserConnectionsFragment: UserConnectionsFragment
    private lateinit var mIpTextView: TextView
    private lateinit var mMacAddressView: TextView
    private lateinit var mSnView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connections_list)
        initIpAndMac()
        initFragment()
    }

    private fun initIpAndMac() {
        mIpTextView = ip_text
        val ipAddr = NetworkStatusManager.getConnectedIpAddr(this)
        Clog.r(TAG, "initIpAndMac ipAddr = $ipAddr")
        if (TextUtils.isEmpty(ipAddr)) {
            mIpTextView.setText("")
        } else {
            mIpTextView.setText(getString(R.string.ip) + " " + ipAddr)
        }
        mMacAddressView = real_mac_text
        var mac = MOLTvManager.getInstance().macAddress
        if (TextUtils.isEmpty(mac)) {
            mac = getString(R.string.empty)
        } else {
            if (!mac.startsWith(Constants.FIXED_MAC_PREF)) {
                val tempStr = mac.substring(Constants.FIXED_MAC_PREF.length)
                mac = Constants.FIXED_MAC_PREF + tempStr
            }
        }
        mMacAddressView.setText(getString(R.string.mac) + " " + mac)

        // redmine #21972
        mSnView = real_sn_text
        var sn = MOLTvManager.getInstance().serialNumber
        if (TextUtils.isEmpty(sn)) {
            sn = getString(R.string.empty)
        }
        mSnView.setText("SN $sn")
        mSnView.setVisibility(View.VISIBLE)
    }

    private fun initFragment() {
        val ft = supportFragmentManager.beginTransaction()
        mUserConnectionsFragment = UserConnectionsFragment()
        ft.add(R.id.fragment_container, mUserConnectionsFragment)
        ft.commit()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Clog.r(TAG, "event = $event")
        val fragment: Fragment? = getCurrentFragment()
        if (fragment != null && fragment is IDispatchKeyEvent) {
            if ((fragment as IDispatchKeyEvent).dispatchKeyEvent(event)) {
                return true
            }
        }

        if(event?.action == KeyEvent.ACTION_UP){
            when(event.keyCode){
                IPTVKeyEvent.KEYCODE_INFO -> {
                    val i = Intent(this, ApiTestAcitivy::class.java)
                    startActivity(i)
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragment_container)
    }
}