package tv.formuler.mytvonline.technic

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.room.Room
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import tv.formuler.formulerlib.model.MolServerInfo
import tv.formuler.mytvonline.IDispatchKeyEvent
import tv.formuler.mytvonline.R
import tv.formuler.mytvonline.common.constant.IPTVKeyEvent
import tv.formuler.mytvonline.common.util.Clog
import tv.formuler.mytvonline.common.util.DialogUtils
import tv.formuler.mytvonline.common.util.MolUtils
import tv.formuler.mytvonline.leanback.presenter.ConnectionsAddPresenter
import tv.formuler.mytvonline.leanback.presenter.ConnectionsListRowPresenter
import tv.formuler.mytvonline.register.ConnectionsTitleView
import tv.formuler.mytvonline.technic.TecSharedPreferences.Companion.setLoginUser
import tv.formuler.mytvonline.technic.data.User


class UserConnectionsFragment : BrowseSupportFragment(), IDispatchKeyEvent {

    private val TAG = "UserConnectionsFragment"
    private val MAX_ITEM = 10

    private val ROW_ID_INSTALLER_PORTALS = 0
    private val ROW_ID_USERS : Long = 1

    private var deleteDialog: Dialog? = null
    private var mRowsAdapter: ArrayObjectAdapter? = null

    private var mUserList: List<User>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        headersState = HEADERS_DISABLED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        val rootView: View? = super.onCreateView(inflater, container, savedInstanceState)
        rootView?.setBackground(MolUtils.getWallpaper(context))
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUIElements()
        setupEventListener()
    }

    override fun onStart() {
        super.onStart()
        buildRows()
        deleteDialog = AlertDialog.Builder(context)
            .setMessage("Do you want to delete the selected user?")
            .setPositiveButton("OK", { dialog, witch ->
                CoroutineScope(Dispatchers.IO).launch {
                    val db = Room.databaseBuilder(
                        context!!,
                        TecDatabase::class.java, "TecDatabase"
                    ).build()
                    db.userDao().delete((selectedItem as User?)!!)
                }
            })
            .setNegativeButton("cancel", null)
            .setOnDismissListener({ dialog -> buildRows() })
            .create()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Clog.r(TAG, "event = $event")
        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                IPTVKeyEvent.KEYCODE_MENU -> {
                    DialogUtils.showMainMenuDialog(requireActivity().supportFragmentManager)
                    return true
                }
                else -> {
                }
            }
        }

        if (event?.keyCode == IPTVKeyEvent.KEYCODE_OK) {
            if (event.isLongPress() && !deleteDialog?.isShowing()!! && selectedItem is User) {
                deleteDialog?.show();
                return true
            } else if (event.action == KeyEvent.ACTION_UP && deleteDialog?.isShowing()!!) {
                return true
            }
        }

        return false
    }


    //////////////////////////////////////////////////////////
    // Common Method
    //////////////////////////////////////////////////////////
    private fun setupUIElements() {
        headersState = HEADERS_DISABLED

        title = ""
        val adapter = titleViewAdapter as ConnectionsTitleView.ConnectionsTitleViewAdapter
        adapter.setIconClickListener { DialogUtils.showMainMenuDialog(activity!!.supportFragmentManager) }
    }

    private fun setupEventListener() {
        onItemViewSelectedListener = ItemViewSelectedListener()
        onItemViewClickedListener = ItemViewClickedListener()
    }

    // Portal, Playlist, Dvb-T2 각 Mode(Installer, Activate)마다 UI출력 되야 하는 Server들이 상이 하므로 데이터 만으로 비교 불가
    // ex) InstallerMode 인 경우에는 mPortalList.getReserve2 값을 확인하여 Row에 추가하지만 mPortalList에 변동 없음
    // 현재 UI출력중인 server 정보와 갱신 할 server의 정보를 직접 비교
    private fun compareAdapter(
        prevAdapter: ArrayObjectAdapter?,
        newAdapter: ArrayObjectAdapter?
    ): Boolean {
        if (null == prevAdapter || 0 == prevAdapter.size() || null == newAdapter || 0 == newAdapter.size()
        ) {
            Clog.r(
                TAG, "compareAdapter failed.  adapter force update."
                        + " prevAdapter: " + prevAdapter + " newAdapter: " + newAdapter
            )
            return false
        } else if (prevAdapter.size() != newAdapter.size()) {
            Clog.r(
                TAG, ("compareAdapter diffrent size."
                        + " prevAdapter: " + prevAdapter.size() + " newAdapter: " + newAdapter.size())
            )
            return false
        } else {
            // same size
            // 반드시 동일한 index 에 동일 한 정보가 있어야 함
            var i = 0
            while (i < prevAdapter.size() && i < newAdapter.size()) {
                if ((prevAdapter[i] is ListRow && newAdapter[i] is ListRow)) {
                    val prevRow = prevAdapter[i] as ListRow
                    val newRow = newAdapter[i] as ListRow
                    if (!compareRow(prevRow.adapter, newRow.adapter)) {
                        return false
                    } else {
                        i++
                        continue
                    }
                }
                i++
            }
            return true
        }
    }

    private fun compareRow(prevRowAdapter: ObjectAdapter?, newRowAdapter: ObjectAdapter?): Boolean {
        if (((null == prevRowAdapter || 0 == prevRowAdapter.size()) || (null == newRowAdapter || 0 == newRowAdapter.size()))) {
            Clog.r(
                TAG,
                "compareRow failed. adapter force update. prevRowAdapter: " + prevRowAdapter + " newRowAdapter: " + newRowAdapter
            )
            return false
        } else if (prevRowAdapter.size() !== newRowAdapter.size()) {
            Clog.r(
                TAG,
                "compareRow diffrent size." + " prevAdapter: " + prevRowAdapter.size() + " newAdapter: " + newRowAdapter.size()
            )
            return false
        } else {
            // same size
            // 반드시 동일한 index 에 동일 한 정보가 있어야 함
            var i = 0
            while (i < prevRowAdapter.size() && i < newRowAdapter.size()) {
                if ((prevRowAdapter[i] is MolServerInfo && newRowAdapter[i] is MolServerInfo)) {
                    val prevRow = prevRowAdapter[i] as MolServerInfo
                    val newRow = newRowAdapter[i] as MolServerInfo
                    if (!compareMolServerInfo(prevRow, newRow)) {
                        return false
                    } else {
                        i++
                        continue
                    }
                }
                i++
            }
            return true
        }
    }

    private fun compareMolServerInfo(prevInfo: MolServerInfo?, newInfo: MolServerInfo?): Boolean {
        if (null == prevInfo || null == newInfo) {
            return false
        } else if (prevInfo.id != newInfo.id) {
            return false
        } else if (prevInfo.type != newInfo.type) {
            return false
        } else if (!compareString(prevInfo.name, newInfo.name)) {
            return false
        } else if (!compareString(prevInfo.url, newInfo.url)) {
            return false
        } else if (!compareString(prevInfo.userId, newInfo.userId)) {
            return false
        } else if (!compareString(prevInfo.password, newInfo.password)) {
            return false
        } else if (!compareString(prevInfo.expiredTime, newInfo.expiredTime)) {
            return false
        } else if (!compareString(prevInfo.pincode, newInfo.pincode)) {
            return false
        } else if (!compareString(prevInfo.epgMode, newInfo.epgMode)) {
            return false
        } else if (prevInfo.epgOffset != newInfo.epgOffset) {
            return false
        } else if (!compareString(prevInfo.groupChannelNumber, newInfo.groupChannelNumber)) {
            return false
        } else if (!compareString(prevInfo.playlistVodUrl, newInfo.playlistVodUrl)) {
            return false
        } else if (!compareString(prevInfo.playlistEpgUrl, newInfo.playlistEpgUrl)) {
            return false
        } else if (!compareString(prevInfo.reserve1, newInfo.reserve1)) {
            return false
        } else if (!compareString(prevInfo.reserve2, newInfo.reserve2)) {
            return false
        } else if (!compareString(prevInfo.reserve3, newInfo.reserve3)) {
            return false
        } else if (!compareString(prevInfo.reserve4, newInfo.reserve4)) {
            return false
        } else if (!compareString(prevInfo.reserve5, newInfo.reserve5)) {
            return false
        }
        return true
    }

    private fun compareString(prevString: String, newString: String): Boolean {
        if (TextUtils.isEmpty(prevString) && !TextUtils.isEmpty(newString)) {
            // empty / !empty
            return false
        } else if (!TextUtils.isEmpty(prevString) && TextUtils.isEmpty(newString)) {
            // !empty / empty
            return false
        } else return if (TextUtils.isEmpty(prevString) && TextUtils.isEmpty(newString)) {
            // empty / empty
            true
        } else {
            // !empty / !empty
            (prevString == newString)
        }
    }

    private fun buildRows() {
        val rowAdapter = ArrayObjectAdapter(ConnectionsListRowPresenter())

        // User
        val userAdapter = ArrayObjectAdapter(mConnectionItemPresenterSelector)
        userAdapter.add(getString(R.string.add_user))

        GlobalScope.launch(Dispatchers.Main) {
            mUserList = async(Dispatchers.IO) {
                val db = Room.databaseBuilder(context!!, TecDatabase::class.java, "TecDatabase").build()
                db.userDao().getAllUsers()
            }.await()

            mUserList?.forEach { userAdapter.add(it) }
            // a  1pplied installer mode + has no user portal + prohibited user portals => hide row
            if (userAdapter.size() > 0) {
                val portalHeadItem = HeaderItem(ROW_ID_USERS, getString(R.string.users))
                val portalListRow = ListRow(portalHeadItem, userAdapter)
                rowAdapter.add(portalListRow)
            }

            if (!compareAdapter(mRowsAdapter, rowAdapter)) {
                Clog.r(TAG, "connections row update")
                mRowsAdapter = rowAdapter
                adapter = mRowsAdapter
                startEntranceTransition()
            } else {
                Clog.r(TAG, "ignore connections  row update cause by deplicated")
            }
        }
    }


    private fun getItemAdapter(rowId: Long): ArrayObjectAdapter? {
        Clog.r(TAG, "getItemAdapter rowId:$rowId")
        if (mRowsAdapter != null && mRowsAdapter!!.size() > 0) {
            val adapter: ObjectAdapter? = null
            for (i in 0 until mRowsAdapter!!.size()) {
                val row = mRowsAdapter!![i] as ListRow
                val itemId = row.headerItem.id
                Clog.r(TAG, "getItemAdapter item id:$itemId")
                if (itemId == rowId.toLong()) {
                    return row.adapter as ArrayObjectAdapter
                }
            }
        }
        return null
    }


    //////////////////////////////////////////////////////////
    // Inner class
    //////////////////////////////////////////////////////////
    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?, row: Row
        ) {
            if (item != null) {
                var user: User? = null
                if (item is User) {
                    user = item
                } else if (item is String) {
                    val transaction: FragmentTransaction? = activity?.getSupportFragmentManager()?.beginTransaction()
                    transaction?.replace(R.id.fragment_container, NewEditUserFragment())
                    transaction?.addToBackStack(null)
                    transaction?.commit()
                    return
                }
                if (row.id == ROW_ID_USERS) {
                    if (user == null) {
                        val adapter: ArrayObjectAdapter? = getItemAdapter(ROW_ID_USERS)
                        if (adapter != null && adapter.size() - 1 >= MAX_ITEM) {
                            Toast.makeText(
                                getActivity(),
                                getString(R.string.connections_server_limited, MAX_ITEM),
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    } else {
                        Log.d(TAG, user.toString())
                        //현재 클릭된 유저를 TechnicApiManager 에 초기화
                        with(TechnicApiManager.instance){
                            isLogin = true
                            sCode = user.gradeNumber + user.classNumber + user.studentNumber
                            sSchool = user.sId
                            sName = user.name
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
                        setLoginUser(context!!, user.id)
                        getActivity()?.finish()
                    }
                }
            }
        }
    }

    private var selectedItem: Any? = null

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            selectedItem = item
        }
    }

    private val mConnectionItemPresenterSelector: PresenterSelector = object : PresenterSelector() {
        var addPresenter = ConnectionsAddPresenter()
        var itemPresenter = UserConnectionsItemPresenter()
        override fun getPresenter(item: Any): Presenter? {
            if (item is String) {
                return addPresenter
            } else if (item is User) {
                return itemPresenter
            }
            return null
        }

        override fun getPresenters(): Array<Presenter> {
            return arrayOf(addPresenter, itemPresenter)
        }
    }
}