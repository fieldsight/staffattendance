package np.com.naxa.staffattendance.attedancedashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.support.annotation.Nullable
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import kotlinx.android.synthetic.main.fragment_take_attedance_dialog.*
import kotlinx.android.synthetic.main.fragment_take_attedance_dialog.view.*
import kotlinx.android.synthetic.main.fragment_take_attedance_dialog.view.tv_take_attedance_frag_desc
import np.com.naxa.staffattendance.R
import np.com.naxa.staffattendance.attendence.AttendanceResponse
import np.com.naxa.staffattendance.attendence.TeamMemberResposne
import np.com.naxa.staffattendance.common.IntentConstants
import np.com.naxa.staffattendance.database.AttendanceDao
import org.idpass.mobile.api.IDPassConstants
import org.idpass.mobile.api.IDPassIntent


class AttedanceBottomFragment : BottomSheetDialogFragment() {

    private val IDENTIFY_RESULT_INTENT = 1
    var staff: TeamMemberResposne? = null
    var statusDesc: Array<String>? = null
    var loadedDate: String? = null
    lateinit var listener: OnAttedanceTakenListener
    private var nfcAdapter: NfcAdapter? = null
    val enablePersonSelection = true


    interface OnAttedanceTakenListener {
        fun onAttedanceTaken(position: Int)
    }


    fun onClickListener(listener: OnAttedanceTakenListener) {
        this.listener = listener
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments?.getSerializable(IntentConstants.EXTRA_OBJECT)?.let {
            staff = it as TeamMemberResposne
        }
        arguments?.getString(IntentConstants.ATTENDANCE_DATE)?.let {
            loadedDate = it
        }

    }

    @Nullable
    override fun onCreateView(inflater: LayoutInflater,
                              @Nullable container: ViewGroup?,
                              @Nullable savedInstanceState: Bundle?): View? {


        val view = inflater.inflate(R.layout.fragment_take_attedance_dialog, container,
                false)

        view.tv_take_attedance_frag_staff_name.text = staff?.firstName
        statusDesc = resources.getStringArray(R.array.attedance_status_desc);

        if (enablePersonSelection && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        }


        return view
    }

    private fun goToNextStep(view: View) {
        val currentCount: Int = view.statusViewScroller.statusView.currentCount
        if (currentCount < 4) {
            setMessage(currentCount - 1)
            view.statusViewScroller.statusView.currentCount = currentCount + 1

        } else {
            dismiss()
            listener.onAttedanceTaken(1)
        }

    }

    override fun onPause() {
        super.onPause()
        if (enablePersonSelection && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nfcAdapter?.disableReaderMode(this.activity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IDENTIFY_RESULT_INTENT && resultCode == Activity.RESULT_OK) {
            val signedActionBase64 = data!!.getStringExtra(IDPassConstants.IDPASS_SIGNED_ACTION_RESULT_EXTRA)
            saveAttedanceWithIDPass(signedActionBase64)
            setMessage(2)
        } else {
            //todo: something happened? show error in UI
        }
    }


    private fun saveAttedanceWithIDPass(signedActionBase64: String) {
        val attendanceResponse = AttendanceResponse()
        attendanceResponse.setAttendanceDate(loadedDate)
        attendanceResponse.setStaffs(listOf(staff?.id))
        attendanceResponse.setStaffProofs(listOf(signedActionBase64))
        attendanceResponse.dataSyncStatus = AttendanceDao.SyncStatus.FINALIZED
        AttedanceLocalSource.instance.updateAttendance(loadedDate, attendanceResponse, staff?.teamID)
    }


    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nfcAdapter?.enableReaderMode(this.activity, { tag ->
                val intent = IDPassIntent.intentIdentify(
                        IDPassConstants.IDPASS_TYPE_MIFARE,
                        true,
                        true,
                        tag)
                startActivityForResult(intent, IDENTIFY_RESULT_INTENT)
            }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
        }
    }


    private fun loadFirstMessage(view: View) {
        setMessage(0)
    }

    private fun setMessage(currentCount: Int) {
        tv_take_attedance_frag_desc.text = statusDesc?.get(currentCount)
    }

    companion object {

        fun newInstance(): AttedanceBottomFragment {
            return AttedanceBottomFragment()
        }
    }
}