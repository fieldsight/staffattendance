package np.com.naxa.staffattendance

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.gson.Gson
import np.com.naxa.staffattendance.attedancedashboard.AttedanceLocalSource
import np.com.naxa.staffattendance.attendence.AttendanceResponse
import np.com.naxa.staffattendance.attendence.TeamMemberResposne
import np.com.naxa.staffattendance.attendence.TeamMemberResposneBuilder
import np.com.naxa.staffattendance.database.AttendanceDao
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("np.com.naxa.staffattendance", appContext.packageName)
    }

    @Test
    fun testDatabaseCascade() {

        val signedAction = "CmEKVwpHaWRwYXNzOjIzMjEzMjAwNzY1QjAzM0MzRUZFMERDQ0UxNjBDNDY4ODk0NjUyOTNEQjc0Qzc1RjlGQjNBRThGQzRFMUQ1MEYSCk5pc2hvbiBQaW4gASgBMJL3jOkFEkD5ElwF/Y1GavfBSa+47HBINaCg+McI9bCwz7aTelLeTjlaG2jMN05wvC4fF7KrptOws1K9YF3a7vdeOiIJA4QFGiAjITIAdlsDPD7+DczhYMRoiUZSk9t0x1+fs66PxOHVDyJAuRo8YB0pC6JcVt9bE8ke2KfFLXwVFAnVwGr3Fc53F+TRDoSUZX0Sm2GsbJDCllTnSdjVxkYffVQzvhoYTuEWDSog06VQpjU+dAG90x737gBeJlX+rdiQmRG7R5LDRw0k28U="
        //save attendance
        var staff = TeamMemberResposneBuilder()
                .setId("1562590087018")
                .setTeamID("1")
                .setIDPassDID("1234")
                .createTeamMemberResposne()



        //simualte upload


        //cascade ids

        //validate cascade

    }


    fun saveAttendance(staff: TeamMemberResposne, signedAction: String, loadedDate: String) {
        val attendanceResponse = AttendanceResponse()
        attendanceResponse.setAttendanceDate(loadedDate)
        attendanceResponse.setStaffs(listOf(staff.id))
        attendanceResponse.idPassProofs = Gson().toJson(hashMapOf(staff.id.trim().toBigInteger() to signedAction))
        attendanceResponse.dataSyncStatus = AttendanceDao.SyncStatus.FINALIZED
        AttedanceLocalSource.instance.updateAttendance(loadedDate, attendanceResponse, staff.teamID)
    }


}
