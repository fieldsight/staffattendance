package np.com.naxa.staffattendance.data;


import java.util.ArrayList;
import java.util.List;

import np.com.naxa.staffattendance.attendence.AttendanceResponse;
import np.com.naxa.staffattendance.attendence.TeamMemberResposne;
import np.com.naxa.staffattendance.login.LoginResponse;
import np.com.naxa.staffattendance.pojo.BankPojo;
import np.com.naxa.staffattendance.pojo.NewStaffPojo;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by samir on 4/2/2018.
 */

public interface ApiInterface {


    @FormUrlEncoded
    @POST("users/api/get-auth-token/")
    Call<LoginResponse> getLoginDetails(
            @Field("email_or_username") String username,
            @Field("password") String password
    );


    @FormUrlEncoded
    @POST("users/api/get-auth-token/")
    Observable<LoginResponse> getLoginDetailsObservable(
            @Field("email_or_username") String username,
            @Field("password") String password
    );


    @Multipart
    @POST("staff/api/staff/{team_id}/")
    Observable<NewStaffPojo> uploadNewStaff(
            @Path(value = "team_id", encoded = true) String teamID,
            @Part("designation") Integer designation,
            @Part("first_name") RequestBody firstName,
            @Part("last_name") RequestBody lastName,
            @Part("date_of_birth") RequestBody dob,
            @Part("gender") Integer gender,
            @Part("ethnicity") RequestBody ethnicity,
            @Part("bank") Integer bankId,
            @Part("bank_name") RequestBody bankName,
            @Part("account_number") RequestBody accountNumber,
            @Part("phone_number") RequestBody phoneNumber,
            @Part("email") RequestBody email,
            @Part("address") RequestBody address,
            @Part("contract_start") RequestBody contractStartDate,
            @Part("contract_end") RequestBody contractEndDate,
            @Part("IdPassDID") RequestBody idPassDID,
            @Part MultipartBody.Part photo
    );

    @GET("/staff/api/designations/")
    Observable<ArrayList<ArrayList<String>>> getDesignation();

    @GET("staff/api/banks/")
    Observable<ArrayList<BankPojo>> getBankist();

    @GET("/staff/api/myteam/")
    Observable<ArrayList<MyTeamResponse>> getMyTeam();


    @GET("/staff/api/staff/{team_id}")
    Observable<ArrayList<TeamMemberResposne>> getTeamMember(@Path(value = "team_id", encoded = true) String teamID);

    @FormUrlEncoded
    @POST("/staff/api/attendance/{team_id}/")
    Observable<AttendanceResponse> postAttendanceForTeam(@Path(value = "team_id", encoded = true) String teamID,
                                                         @Field("attendance_date") String date,
                                                         @Field("id_pass_proof") String idPassProof,
                                                         @Field("staffs") List<String> staff_ids
    );


    @GET("/staff/api/attendance/{team_id}/")
    Observable<ArrayList<AttendanceResponse>> getPastAttendanceList(@Path(value = "team_id", encoded = true) String teamID
    );
}
