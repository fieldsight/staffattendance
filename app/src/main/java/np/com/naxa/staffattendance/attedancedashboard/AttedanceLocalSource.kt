package np.com.naxa.staffattendance.attedancedashboard

import np.com.naxa.staffattendance.attendence.AttendanceResponse
import np.com.naxa.staffattendance.database.AttendanceDao
import org.json.JSONException
import org.json.JSONObject


class AttedanceLocalSource {

    val attedanceDao = AttendanceDao()

    companion object {
        val instance = AttedanceLocalSource()
    }


    fun getAttendanceForDate(date: String?, new_attendance: AttendanceResponse, team_id: String?): AttendanceResponse? {
        val oldAttendance = attedanceDao.getAttedanceByDate(team_id, date)

        val oldAttendanceJSON = JSONObject(if (oldAttendance.idPassProofs != null) oldAttendance.idPassProofs else "{}");
        val newAttendanceJSON = JSONObject(new_attendance.idPassProofs)

//        val hasAttedanceStarted = oldAttendance.presentStaffIds != null && oldAttendance.presentStaffIds.size > 0
        val hasAttedanceStarted = oldAttendanceJSON.length() > 0
//        val attendanceNotTaken = !oldAttendance.presentStaffIds.contains(new_attendance.presentStaffIds[0].toString())//we always add one staff at a time so [0] should be safe
        val attendanceNotTaken = !oldAttendanceJSON.has(new_attendance.idPassProofs)

        if (hasAttedanceStarted && attendanceNotTaken) {
            val mergedJSON = mergeJSONObjects(oldAttendanceJSON, newAttendanceJSON)
            oldAttendance.idPassProofs = mergedJSON.toString()
            return oldAttendance
        }

        return new_attendance
    }

    @Throws(JSONException::class)
    private fun mergeJSONObjects(vararg jsonObjects: JSONObject): JSONObject {

        val jsonObject = JSONObject()

        for (temp in jsonObjects) {
            val keys = temp.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                jsonObject.put(key, temp.get(key))
            }

        }
        return jsonObject;
    }

    fun updateAttendance(date: String?, new_attendance: AttendanceResponse, team_id: String?) {
        val attendanceResponse = getAttendanceForDate(date, new_attendance, team_id)
        attendanceResponse?.dataSyncStatus = AttendanceDao.SyncStatus.FINALIZED
        val contentValues = attedanceDao.getContentValuesForAttedance(attendanceResponse)

        attedanceDao.saveAttedance(contentValues)
    }

}