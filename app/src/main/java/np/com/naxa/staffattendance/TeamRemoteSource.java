package np.com.naxa.staffattendance;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import np.com.naxa.staffattendance.application.StaffAttendance;
import np.com.naxa.staffattendance.attendence.AttendanceResponse;
import np.com.naxa.staffattendance.attendence.AttendanceViewPagerActivity;
import np.com.naxa.staffattendance.attendence.TeamMemberResposne;
import np.com.naxa.staffattendance.data.APIClient;
import np.com.naxa.staffattendance.data.ApiInterface;
import np.com.naxa.staffattendance.data.MyTeamResponse;
import np.com.naxa.staffattendance.data.TokenMananger;
import np.com.naxa.staffattendance.database.AttendanceDao;
import np.com.naxa.staffattendance.database.DatabaseHelper;
import np.com.naxa.staffattendance.database.NewStaffDao;
import np.com.naxa.staffattendance.database.StaffDao;
import np.com.naxa.staffattendance.database.TeamDao;
import np.com.naxa.staffattendance.login.LoginActivity;
import np.com.naxa.staffattendance.newstaff.NewStaffCall;
import np.com.naxa.staffattendance.pojo.NewStaffPojo;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import timber.log.Timber;


public class TeamRemoteSource {


    private static TeamRemoteSource teamRemoteSource;

    public static TeamRemoteSource getInstance() {
        if (teamRemoteSource == null) {
            teamRemoteSource = new TeamRemoteSource();
        }

        return teamRemoteSource;
    }


    public void test() {
        Observable<Integer> a = Observable.just(11, 22, 33)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer s) {
                        if (true) throw new RuntimeException("We have an error");
                        return s;
                    }
                });
        Observable<Integer> b = Observable.just(1, 2, 3, 4);

        Observable.concat(a, b).subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                Timber.i("Completed");
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Error: %s", e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer integer) {
                Timber.i("Integer: %s", integer);
            }
        });


        Observable.concat(b, a).subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                Timber.i("Completed");
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Error: %s", e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer integer) {
                Timber.i("Integer: %s", integer);
            }
        });
    }

    public Observable<Object> syncAll() {
        final ApiInterface api = APIClient.getUploadClient()
                .create(ApiInterface.class);

        NewStaffCall newStaffCall = new NewStaffCall();


        ArrayList<NewStaffPojo> unsyncedStaffList = new NewStaffDao().getOfflineStaffs();
        Observable<Object> uploadNewStaff = Observable.just(unsyncedStaffList)
                .flatMapIterable((Func1<ArrayList<NewStaffPojo>, Iterable<NewStaffPojo>>) newStaffPojos -> newStaffPojos)
                .flatMap(new Func1<NewStaffPojo, Observable<?>>() {
                    @Override
                    public Observable<?> call(NewStaffPojo newStaffPojo) {
                        String filePath = newStaffPojo.getPhoto();
                        File file = null;
                        if (!TextUtils.isEmpty(filePath)) {
                            file = new File(filePath);
                        }

                        return newStaffCall.newStaffObservable(newStaffPojo, file)
                                .map(new Func1<NewStaffPojo, Pair<String, String>>() {
                                    @Override
                                    public Pair<String, String> call(NewStaffPojo uploadedStaff) {
                                        NewStaffDao.getInstance().deleteStaffById(String.valueOf(newStaffPojo.getId()));
                                        return Pair.create(newStaffPojo.getId(), uploadedStaff.getId());
                                    }
                                })
                                .toList()
                                .flatMap(new Func1<List<Pair<String, String>>, Observable<AttendanceResponse>>() {
                                    @Override
                                    public Observable<AttendanceResponse> call(List<Pair<String, String>> pairs) {
                                        return AttendanceDao.getInstance().updateStaffIdObservable(pairs);
                                    }
                                });
                    }
                });
        ;


        Observable<List<TeamMemberResposne>> teamlist = api.getMyTeam()
                .map(new Func1<ArrayList<MyTeamResponse>, ArrayList<MyTeamResponse>>() {
                    @Override
                    public ArrayList<MyTeamResponse> call(ArrayList<MyTeamResponse> myTeamResponses) {
                        if (myTeamResponses.isEmpty()) {
                            Context context = StaffAttendance.getStaffAttendance().getApplicationContext();
                            TokenMananger.clearToken();
                            SharedPreferenceUtils.purge(StaffAttendance.getStaffAttendance().getApplicationContext());
                            DatabaseHelper.getDatabaseHelper().delteAllRows(DatabaseHelper.getDatabaseHelper().getWritableDatabase());
                            LoginActivity.startNonActivity(context);

                        }
                        return myTeamResponses;
                    }
                })
                .flatMapIterable((Func1<ArrayList<MyTeamResponse>, Iterable<MyTeamResponse>>) myTeamResponses -> myTeamResponses)
                .flatMap((Func1<MyTeamResponse, Observable<List<TeamMemberResposne>>>) myTeamResponse -> api.getTeamMember(myTeamResponse.getId())
                        .flatMapIterable((Func1<ArrayList<TeamMemberResposne>, Iterable<TeamMemberResposne>>) teamMemberResposnes -> teamMemberResposnes)
                        .doOnSubscribe(() -> {
                            StaffDao.getInstance().removeAllStaffList();
                        })
                        .doOnNext(teamMemberResposne -> {

                            teamMemberResposne.setTeamID(myTeamResponse.getId());
                            teamMemberResposne.setTeamName(myTeamResponse.getName());
                            long status = StaffDao.getInstance().saveStaff(teamMemberResposne);
                            Timber.d("Staff insert status %s", status);
                        })
                        .toList());


        Observable<Object> pastAttendance = api.getMyTeam()
                .flatMapIterable((Func1<ArrayList<MyTeamResponse>, Iterable<MyTeamResponse>>) myTeamResponses -> myTeamResponses)
                .flatMap((Func1<MyTeamResponse, Observable<ArrayList<AttendanceResponse>>>) myTeamResponse -> api.getPastAttendanceList(myTeamResponse.getId()))
                .flatMap(new Func1<ArrayList<AttendanceResponse>, Observable<?>>() {
                    @Override
                    public Observable<?> call(ArrayList<AttendanceResponse> attendanceResponses) {
                        AttendanceDao.getInstance().removeAllAttedance();

                        return AttendanceDao.getInstance().saveAttendance(attendanceResponses);
                    }
                });


        final String teamId = new TeamDao().getOneTeamIdForDemo();
        ArrayList<AttendanceResponse> attendanceResponses = AttendanceDao.getInstance().getFinalizedAttendanceSheet();


        Observable<AttendanceResponse> attendanceSheet = AttendanceDao.getInstance().getFinalizedAttedanceSheetObservable()
                .map(new Func1<ArrayList<AttendanceResponse>, ArrayList<AttendanceResponse>>() {
                    @Override
                    public ArrayList<AttendanceResponse> call(ArrayList<AttendanceResponse> attendanceResponses) {
                        if (TextUtils.isEmpty(teamId))
                            throw new RuntimeException("Team ID is missing");
                        return attendanceResponses;
                    }
                })
                .flatMapIterable((Func1<ArrayList<AttendanceResponse>, Iterable<AttendanceResponse>>) attendanceRespons -> attendanceRespons)
                .flatMap((Func1<AttendanceResponse, Observable<AttendanceResponse>>) attendanceResponse -> api.postAttendanceForTeam(teamId,
                        attendanceResponse.getAttendanceDate(false),
                        attendanceResponse.getIDPassProofs(),
                        attendanceResponse.getPresentStaffIds()));


        return Observable.concat(uploadNewStaff, teamlist, pastAttendance, attendanceSheet, pastAttendance);

    }


}
