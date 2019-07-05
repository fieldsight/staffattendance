package np.com.naxa.staffattendance.attedancedashboard

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_dashboard_attedance.*
import np.com.naxa.staffattendance.R
import np.com.naxa.staffattendance.SharedPreferenceUtils
import np.com.naxa.staffattendance.TeamRemoteSource
import np.com.naxa.staffattendance.attendence.AttendanceViewPagerActivity
import np.com.naxa.staffattendance.common.UIConstants
import np.com.naxa.staffattendance.data.MyTeamResponse
import np.com.naxa.staffattendance.database.StaffDao
import np.com.naxa.staffattendance.database.TeamDao
import np.com.naxa.staffattendance.settings.SettingsActivity
import np.com.naxa.staffattendance.utlils.DateConvertor
import np.com.naxa.staffattendance.utlils.DialogFactory
import np.com.naxa.staffattendance.utlils.NetworkUtils
import np.com.naxa.staffattendance.utlils.ToastUtils
import retrofit2.adapter.rxjava.HttpException
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


class AttendancesDashboardActivity : AppCompatActivity() {

    private lateinit var listAdapter: ListAdapter
    private var exitOnBackPress: Boolean = false;
    private val backPressHandler = Handler()
    private val runnable = { exitOnBackPress = false }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_attedance);
        DialogFactory.createProgressDialogHorizontal(this@AttendancesDashboardActivity, "Please Wait")
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        setupSwipeToRefresh()


    }

    private fun setupSwipeToRefresh() {
        swiperefresh.setOnRefreshListener {
            if (!NetworkUtils.isInternetAvailable()) {
                showMessage(getString(R.string.no_internet))
                return@setOnRefreshListener
            }

            TeamRemoteSource.getInstance()
                    .syncAll()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe({ this.showPleaseWaitDialog() })
                    .subscribe(object : Observer<Any> {
                        override fun onCompleted() {
                            closePleaseWaitDialog()
                            showMessage("Everything is Up-to-date")
                            Timber.i("onCompleted")
                        }

                        override fun onError(e: Throwable) {
                            closePleaseWaitDialog()
                            if (e is HttpException) {
                                try {
                                    val responseBody = e.response().errorBody()
                                    showMessage(responseBody!!.string())
                                } catch (e1: NullPointerException) {
                                    showMessage("")
                                    e1.printStackTrace()
                                } catch (e1: IOException) {
                                    showMessage("")
                                    e1.printStackTrace()
                                }

                            } else if (e is SocketTimeoutException) {
                                showMessage("Server took too long to respond")
                            } else if (e is IOException) {
                                showMessage(e.message.toString())
                            } else {
                                showMessage(e.message.toString())
                            }
                        }

                        override fun onNext(o: Any) {

                        }
                    })


        }
    }

    private fun showPleaseWaitDialog() {
        swiperefresh.isRefreshing = true
    }

    private fun showMessage(message: String) {
        ToastUtils.showLong(message)
        swiperefresh.isRefreshing = false
    }

    private fun closePleaseWaitDialog() {
        swiperefresh.isRefreshing = false
    }

    private fun generateGridItems(): ArrayList<Any> {
        val teamId = TeamDao().oneTeamIdForDemo
        val list = arrayListOf<Any>()

        val staffs = StaffDao().getStaffByTeamId(teamId)
        list.add(getString(R.string.title_team))
        list.add("")

        var teamName = ""
        var teamMembersCount = "0"
        val type = object : TypeToken<List<MyTeamResponse>>() {}.type
        val teams = Gson().fromJson<List<MyTeamResponse>>(SharedPreferenceUtils.getFromPrefs(this, SharedPreferenceUtils.KEY.teams, "[]"), type)
        if (teams.isNotEmpty()) {
            teamName = teams[0].name;
        }
        if (staffs.size > 0) {
            teamMembersCount = staffs.count().toString()
        }

        list.add(TeamStats(teamName, teamMembersCount))


        list.add(AddItemButton(UIConstants.UUID_GRID_ITEM_TEAM_MEMBER))
        list.add(getString(R.string.title_attedance))
        list.add("")


        for (x in 0 downTo -6 step 1) {
            val date = DateConvertor.getPastDate(x)
            val yearMonthDay = DateConvertor.getYearMonthDay(date);
            list.add(element = AttendanceDay(dayOfWeek = yearMonthDay[2],
                    dayOfMonth = yearMonthDay[1],
                    date = yearMonthDay[0],
                    absentNoOfStaff = "",
                    presentNoOfStaff = "",
                    teamId = teamId,
                    teamName = teamName,
                    fullDate = DateConvertor.formatDate(DateConvertor.getDateForPosition(x))));
        }

        return list;
    }

    override fun onResume() {
        super.onResume()
        setupListAdapter(generateGridItems())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.main_dashboard_setting -> {
                val toSettings = SettingsActivity.newIntent(this@AttendancesDashboardActivity)
                startActivity(toSettings)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        if (exitOnBackPress) {
            finish()
            return
        }

        exitOnBackPress = true
        ToastUtils.showShort(getString(R.string.msg_backpress_to_exit))
        backPressHandler.postDelayed(runnable, TimeUnit.SECONDS.toMillis(2))
    }

    private fun setupListAdapter(days: List<Any>) {

        val manager = LinearLayoutManager(this)
        listAdapter = ListAdapter(days)
        recycler_view.setLayoutManager(manager)
        recycler_view.setItemAnimator(DefaultItemAnimator())
        recycler_view.apply {
            layoutManager = GridLayoutManager(this@AttendancesDashboardActivity, 2)
            adapter = listAdapter
        }
        recycler_view.addItemDecoration(ItemOffsetDecoration(this, R.dimen.spacing_small))

    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, AttendancesDashboardActivity::class.java)
            return intent
        }
    }
}