package np.com.naxa.staffattendance.attendence;

import java.util.HashMap;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import np.com.naxa.staffattendance.utlils.DateConvertor;

public class AttendanceResponse {

    private List<TeamMemberResposne> teamMemberResposnes = null;

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("attendance_date")
    @Expose
    private String attendanceDate;
    @SerializedName("is_deleted")
    @Expose
    private Boolean isDeleted;
    @SerializedName("staffs")
    @Expose
    private List<String> staffs = null;


    @Expose
    private String IDPassProofs = null;


    private String teamId;

    public AttendanceResponse(String attendanceDate, List<String> staffs) {
        this.attendanceDate = attendanceDate;
        this.staffs = staffs;
    }

    public AttendanceResponse() {

    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    private String dataSyncStatus;

    public String getDataSyncStatus() {
        return dataSyncStatus;
    }

    public void setDataSyncStatus(String dataSyncStatus) {
        this.dataSyncStatus = dataSyncStatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAttendanceDate(boolean stylize) {
        if (stylize) {
            if (attendanceDate.equals(DateConvertor.getCurrentDate())) {
                return "Today";
            }
        }


        return attendanceDate;
    }

    public void setAttendanceDate(String attendanceDate) {

        this.attendanceDate = attendanceDate;

    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public List<String> getPresentStaffIds() {
        return staffs;
    }

    public void setStaffs(List<String> staffs) {
        this.staffs = staffs;
    }

    public void setIDPassProofs(String IDPassProofs) {
        this.IDPassProofs = IDPassProofs;
    }

    public String getIDPassProofs() {
        return IDPassProofs;
    }

    public List<TeamMemberResposne> getTeamMemberResposnes() {
        return teamMemberResposnes;
    }

    public void setTeamMemberResposnes(List<TeamMemberResposne> teamMemberResposnes) {
        this.teamMemberResposnes = teamMemberResposnes;
    }
}
