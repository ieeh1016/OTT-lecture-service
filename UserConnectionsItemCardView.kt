package tv.formuler.mytvonline.technic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import tv.formuler.mytvonline.R

open class UserConnectionsItemCardView : RelativeLayout {
    private var mNameView: TextView? = null
    private var mSchoolView: TextView? = null
    private var mClassView: TextView? = null
    private var mConnectedView: TextView? = null

    //    private ImageView mExpireIconView;
    //    private TextView mExprieTextView;
    private var mBgView: View? = null

    constructor(context: Context?) : super(context) {
        buildConnectionsItemCardView()
    }

    private fun buildConnectionsItemCardView() {
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        val params: ViewGroup.LayoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams = params
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.cardview_user_connections_item, this, false)
        initUI(view)
        addView(view)
    }

    private fun initUI(view: View) {
        mNameView = view.findViewById(R.id.item_name)
        mSchoolView = view.findViewById(R.id.item_school)
        mClassView = view.findViewById(R.id.item_class)
        mBgView = view.findViewById(R.id.connections_item_bg)
        mConnectedView = view.findViewById(R.id.item_connected)
    }

    fun getNameView(): TextView? {
        return mNameView
    }

    fun getSchoolView(): TextView? {
        return mSchoolView
    }

    fun getClassView(): TextView? {
        return mClassView
    }

    //
    fun getBgView(): View? {
        return mBgView
    }

    fun getConnectedView(): TextView? {
        return mConnectedView
    }
}