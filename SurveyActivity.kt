package tv.formuler.mytvonline.technic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.api24.activity_survey.*
import tv.formuler.mytvonline.BaseActivity
import tv.formuler.mytvonline.MyTvOnlineApp
import tv.formuler.mytvonline.R
import tv.formuler.recommends.IVideoRecommends


class SurveyActivity : BaseActivity() {
    val MAX_SELECTED_COUNT = 3
    internal var iVideoRecommends : IVideoRecommends? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        iVideoRecommends = (applicationContext as MyTvOnlineApp).videoRecommends
        val need: Boolean = iVideoRecommends?.needSurvey() == true

        if (!need) {
            finish()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        val intent = Intent()
        intent.setComponent(
            ComponentName(
                "kr.co.aloys.videorecommends",
                "kr.co.aloys.videorecommends.VodRecommends"
            )
        )

        val assetManager = resources.assets
        val inputStream = assetManager.open("survey.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val surveyInfos = Gson().fromJson(jsonString, Array<Survey>::class.java)
        recycler_view.layoutManager = GridLayoutManager(this, 2)
        recycler_view.adapter = SurveyAdapter(this, surveyInfos, MAX_SELECTED_COUNT) {}
        recycler_view.addItemDecoration(Spacing(20, 15))
        recycler_view.requestFocus()
    }

    fun onPass(v: View) {
        finish()
    }

    fun onContinue(v: View) {
        val selectedItems = (recycler_view.adapter as SurveyAdapter).getSeletedItems()
        val streamIds = selectedItems.map { it.stremId }.toIntArray()
        iVideoRecommends?.setSurveyResult(streamIds)
        finish()
    }
}

data class Survey(
    val name: String,
    val director: String,
    val screenplay: String,
    val genre: String,
    @SerializedName("image") val imageUrl: String,
    @SerializedName("stream_id") val stremId: Int,
    @SerializedName("tmdb_id") val tmdbId: String,
    val release: String,
    val cast: List<Cast>,
    var selected: Boolean = false
)

class SurveyAdapter(
    private val context: Context,
    private val movies: Array<Survey>,
    private val maxCount: Int,
    private val selectedCountListener: (count: Int) -> Unit
) : RecyclerView.Adapter<SurveyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.survey_item, parent, false)
        return SurveyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        val context = holder.itemView.context
        val info = movies[position]

        val title = "${info.name} (${info.release})"
        holder.title.text = title
        val span = holder.title.text as Spannable
        span.setSpan(RelativeSizeSpan(0.75f), title.lastIndexOf("("), title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        holder.director.text = "${context.getString(R.string.director)} : ${info.director}"
        holder.screenplay.text = "${context.getString(R.string.screenplay)} : ${info.screenplay}"
        holder.genre.text = "${context.getString(R.string.genre)} : ${info.genre}"


        Glide.with(context).load(info.imageUrl).into(holder.image)

        val cast1 = info.cast.get(0)
        val cast2 = info.cast.get(1)
        val cast3 = info.cast.get(2)

        holder.castName1.text = cast1.name
        holder.castName2.text = cast2.name
        holder.castName3.text = cast3.name

        Glide.with(context).load(cast1.imageUrl).into(holder.castImage1)
        Glide.with(context).load(cast2.imageUrl).into(holder.castImage2)
        Glide.with(context).load(cast3.imageUrl).into(holder.castImage3)

        holder.selected.setImageResource(if (info.selected) R.drawable.check_box_type_3 else R.drawable.check_box_type_2)
        holder.itemView.setOnClickListener({
            val selectedCount = movies.count { it.selected }
            info.selected = if (selectedCount < maxCount) {
                !info.selected
            } else if (info.selected) {
                false
            } else {
                Toast.makeText(context, "Can't choose more items", Toast.LENGTH_SHORT).show()
                false
            }
            selectedCountListener.invoke(movies.count { it.selected })
            holder.selected.setImageResource(if (info.selected) R.drawable.check_box_type_3 else R.drawable.check_box_type_2)
        })
        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            if (info.selected) return@setOnFocusChangeListener
            else holder.selected.setImageResource(if (hasFocus) R.drawable.check_box_type_2 else R.drawable.check_box_type_1)
        }
    }

    override fun getItemCount() = movies.size

    fun getSeletedItems() = movies.filter { it.selected }
}

class SurveyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val image: ImageView
    val title: TextView
    val director: TextView
    val screenplay: TextView
    val genre: TextView
    val selected: ImageView

    val castName1: TextView
    val castName2: TextView
    val castName3: TextView
    val castImage1: ImageView
    val castImage2: ImageView
    val castImage3: ImageView

    init {
        image = itemView.findViewById(R.id.image)
        title = itemView.findViewById(R.id.title)
        genre = itemView.findViewById(R.id.genre)
        selected = itemView.findViewById(R.id.selected)
        director = itemView.findViewById(R.id.director)
        screenplay = itemView.findViewById(R.id.screenplay)

        castName1 = itemView.findViewById(R.id.cast_name_1)
        castName2 = itemView.findViewById(R.id.cast_name_2)
        castName3 = itemView.findViewById(R.id.cast_name_3)

        castImage1 = itemView.findViewById(R.id.cast_image_1)
        castImage2 = itemView.findViewById(R.id.cast_image_2)
        castImage3 = itemView.findViewById(R.id.cast_image_3)
    }
}

class Spacing(val spacing: Int, val topSpacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val index = (view.layoutParams as GridLayoutManager.LayoutParams).spanIndex
        val position = parent.getChildLayoutPosition(view)

        if (position > 1) {
            outRect.top = topSpacing
        }
    }
}

data class Cast(
    val name: String,
    @SerializedName("image") val imageUrl: String
)