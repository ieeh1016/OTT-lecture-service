package tv.formuler.mytvonline.technic

import android.content.res.Resources
import android.nfc.Tag
import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import tv.formuler.mytvonline.R
import tv.formuler.mytvonline.common.util.Clog
import tv.formuler.mytvonline.technic.data.User


class UserConnectionsItemPresenter : Presenter() {
    private val TAG = "ConnectionsItemPresenter"

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder? {
        val cardView: UserConnectionsItemCardView =
            object : UserConnectionsItemCardView(parent.context) {
                override fun setSelected(selected: Boolean) {
                    super.setSelected(selected)
                    getSchoolView()?.isSelected = selected
                    getBgView()?.isSelected = selected
                }
            }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        if (item != null) {
            val cardView = viewHolder.view as UserConnectionsItemCardView
            val user: User = item as User
            cardView.getNameView()?.text = user.name
            cardView.getSchoolView()?.text = user.sName
            cardView.getClassView()?.apply {
                text = context.getString(R.string.selected_grade, user.gradeNumber.toInt()) + " " +
                        context.getString(R.string.selected_class, user.classNumber.toInt()) + " " +
                        context.getString(R.string.selected_number, user.studentNumber.toInt())
            }
            // 현재 학생이 로그인된 학생이라면 connectedView 출력
            if (TechnicApiManager.instance.sSchool == user.sId &&
                TechnicApiManager.instance.sCode?.get(0).toString() == user.gradeNumber.toString() &&
                TechnicApiManager.instance.sCode?.substring(1, 3) == user.classNumber.toString() &&
                TechnicApiManager.instance.sCode?.substring(3, 5) == user.studentNumber.toString() &&
                TechnicApiManager.instance.sName == user.name) {
                cardView.getConnectedView()?.visibility = View.VISIBLE
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {}
}