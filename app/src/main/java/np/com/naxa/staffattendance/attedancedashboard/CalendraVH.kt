package np.com.naxa.staffattendance.attedancedashboard

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import np.com.naxa.staffattendance.R
import np.com.naxa.staffattendance.attendence.v2.AttedanceActivity


class CalendarVH(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.grid_item_calendra, parent, false)) {
    private var tvDay: TextView? = null
    private var tvDate: TextView? = null
    private var tvMonthYear: TextView? = null
    private var tvAbsentMessage: TextView? = null
    private var tvPresentMessage: TextView? = null
    private var rootLayout: View? = null
    private var card: CardView? = null


    init {
        tvDay = itemView.findViewById(R.id.tv_day)
        tvDate = itemView.findViewById(R.id.tv_date)
        tvMonthYear = itemView.findViewById(R.id.tv_month_year)
        tvAbsentMessage = itemView.findViewById(R.id.tv_absent_message)
        tvPresentMessage = itemView.findViewById(R.id.tv_present_message)
        rootLayout = itemView.findViewById(R.id.root_layout)
        card = itemView.findViewById(R.id.card_view)
    }

    fun bind(day: AttendanceDay) {

        if(day.isAttendanceForToday){
            card?.cardElevation = 6.0F;
        }

        tvDay?.text = day.dayOfWeek
        tvDate?.text = day.dayOfMonth
        tvMonthYear?.text = day.date
        rootLayout?.setOnClickListener {
            val intent = AttedanceActivity.newIntent(itemView.context,
                    date = day.fullDate,
                    teamId = day.teamId,
                    teamName = day.teamName,
                    monthDay= "${day.dayOfMonth} ${day.date}")

            itemView.context.startActivity(intent);
        }

    }

    fun setAbsentPresentMessage(day: AttendanceDay) {
        if (isNullOrEmpty(day.absentNoOfStaff)) {

        }
    }


    fun isNullOrEmpty(str: String?): Boolean {
        if (str != null && !str.isEmpty())
            return false
        return true
    }
}