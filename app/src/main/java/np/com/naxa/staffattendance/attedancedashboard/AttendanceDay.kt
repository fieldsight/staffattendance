package np.com.naxa.staffattendance.attedancedashboard

import np.com.naxa.staffattendance.attedancedashboard.base.IRow

data class AttendanceDay(val dayOfWeek: String,
                         val dayOfMonth: String,
                         val date: String,
                         val absentNoOfStaff: String,
                         val presentNoOfStaff: String,
                         val teamId: String,
                         val teamName: String,
                         val isAttendanceForToday: Boolean,
                         val fullDate: String) : IRow