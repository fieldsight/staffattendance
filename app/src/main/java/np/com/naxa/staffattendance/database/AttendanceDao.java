package np.com.naxa.staffattendance.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import np.com.naxa.staffattendance.attendence.AttendanceResponse;
import np.com.naxa.staffattendance.utlils.DateConvertor;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class AttendanceDao {

    private static AttendanceDao attendanceDao;

    public static AttendanceDao getInstance() {
        if (attendanceDao == null) {
            return new AttendanceDao();
        }

        return attendanceDao;
    }


    public int updateAttendance(String date, String teamId) {
//        String selection = DatabaseHelper.KEY_ATTENDACE_DATE + "=? AND " + DatabaseHelper.KEY_STAFF_TEAM_ID + "=?";
        String selection = DatabaseHelper.KEY_ATTENDACE_DATE;
        String[] selectionArgs = new String[]{date};
        return updateAttendance(getContentValuesForStatusUpdate(), selection, selectionArgs);
    }


    public ContentValues getContentValuesForStatusUpdate() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_SYNC_STATUS, SyncStatus.UPLOADED);
        return contentValues;
    }

    private int updateAttendance(ContentValues contentValues, String selection, String[] selectionArgs) {
        return DatabaseHelper.getDatabaseHelper().getWritableDatabase().update(TABLE_NAME, contentValues, selection, selectionArgs);
    }

    public void changeAttendanceStatus(String date, String status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_SYNC_STATUS, status);
        updateAttendance(contentValues, DatabaseHelper.KEY_ATTENDACE_DATE + "=?", new String[]{date});
    }

    public void removeAllAttedance() {
        SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getWritableDatabase();
        db.execSQL("DELETE from " + TABLE_NAME + " WHERE " + DatabaseHelper.KEY_SYNC_STATUS + " != '" + SyncStatus.FINALIZED + "'");
        db.close();
    }


    public Observable<Object> updateStaffIdObservable(List<Pair<String, String>> pairs) {

        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                Cursor cursor = null;

                try {

                    cursor = DatabaseHelper
                            .getDatabaseHelper()
                            .getReadableDatabase()
                            .query(TABLE_NAME, null, DatabaseHelper.KEY_SYNC_STATUS + "=?", new String[]{SyncStatus.FINALIZED}, null, null, null);

                    while (cursor.moveToNext()) {
                        String attendanceDate = DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_ATTENDACE_DATE);
                        String idpassproofs = DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_ID_PASS_PROOFS);

                        try {
                            JSONObject json = new JSONObject(idpassproofs);
                            Iterator<String> iter = json.keys();
                            JSONObject jsonObject = new JSONObject();
                            while (iter.hasNext()) {
                                String key = iter.next();
                                Object value = json.get(key);
                                for (Pair<String, String> p : pairs) {
                                    boolean doesOfflineKeyMatch = key.equals(p.first);
                                    Timber.i("Checking if %s and %s match", key, p.first);
                                    if (doesOfflineKeyMatch) {
                                        jsonObject.put(p.second, value);
                                    }
                                }
                            }

                            if (jsonObject.length() == 0) {
                                Timber.i(json.toString());
                                throw new RuntimeException("Opps");
                            }

                            ContentValues values = new ContentValues();
                            values.put(DatabaseHelper.KEY_ID_PASS_PROOFS, jsonObject.toString());
                            int rowsAffected = update(values, DatabaseHelper.KEY_ATTENDACE_DATE + "=?", new String[]{attendanceDate});

                            if (rowsAffected == 0) {
                                throw new RuntimeException("Mismatch no date exists to update");
                            }


                            subscriber.onNext(jsonObject);
                            subscriber.onCompleted();
                        } catch (JSONException e) {
                            throw new RuntimeException("Failed to cascade date");
                        }

                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

            }
        });
    }

    private List<String> matchAndReplaceIds(List<String> offlineIds, List<Pair<String, String>> offlineOnlineIdMap) {

        List<String> updatedIds = new ArrayList<>();

        for (String offlineId : offlineIds) {
            for (Pair pair : offlineOnlineIdMap) {
                String keyOfflineId = (String) pair.first;
                String keyOnlineId = (String) pair.second;

                if (offlineId.equals(keyOfflineId)) {
                    updatedIds.add(keyOnlineId);
                } else {
                    updatedIds.add(offlineId);
                }
            }
        }
        return updatedIds;
    }

    private List<String> convertStaffIdsToList(String staffIds) {
        Type type = new TypeToken<List<String>>() {
        }.getType();

        return new Gson().fromJson(staffIds, type);
    }


    public void updateStaffId(String oldStaffId, String newStaffId) {

        Cursor cursor = DatabaseHelper.getDatabaseHelper().getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME, null);

        while (cursor.moveToNext()) {

            String staffIds = DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFFS_IDS);
            String attendanceDate = DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_ATTENDACE_DATE);


            Type type = new TypeToken<List<String>>() {
            }.getType();
            List<String> staffIdList = new Gson().fromJson(staffIds, type);

            if (!staffIdList.isEmpty()) {
                Timber.i("updateStaffId old list %s", staffIds);
            }

            for (int i = 0; i < staffIdList.size(); i++) {
                String curStaffId = staffIdList.get(i);

                if (oldStaffId.equals(curStaffId)) {
                    staffIdList.set(i, newStaffId);
                }
            }

            AttendanceResponse attendanceResponse = new AttendanceResponse(attendanceDate, staffIdList);
            attendanceResponse.setDataSyncStatus(SyncStatus.FINALIZED);
            ContentValues values = getContentValuesForAttedance(attendanceResponse);
            update(values, DatabaseHelper.KEY_ATTENDACE_DATE + "=?", new String[]{attendanceDate});

            if (!staffIdList.isEmpty()) {
                Timber.i("updateStaffId new list %s", staffIds);
            }
        }

        closeCursor(cursor);
    }

    private int update(ContentValues values, String selection, String[] selectionArgs) {
        return DatabaseHelper.getDatabaseHelper().getWritableDatabase().update(TABLE_NAME, values, selection, selectionArgs);
    }


    public final static class SyncStatus {
        public static String FINALIZED = "finalized";
        public static String UPLOADED = "uploaded";
    }


    private final String TABLE_NAME = DatabaseHelper.TABLE_ATTENDANCE;

    public ContentValues getContentValuesForAttedance(AttendanceResponse attedance) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_ID, attedance.getId());

        String date;
        if (attedance.getAttendanceDate(false).equalsIgnoreCase("Today")) {
            date = DateConvertor.getCurrentDate();
        } else {
            date = attedance.getAttendanceDate(false);
        }

        contentValues.put(DatabaseHelper.KEY_ATTENDACE_DATE, date);
        contentValues.put(DatabaseHelper.KEY_SYNC_STATUS, attedance.getDataSyncStatus());

//        try {
//            JSONArray jsonArray = new JSONObject(attedance.getIDPassProofs()).names();
//            ArrayList<Integer> ids = new ArrayList<>();
//            for (int n = 0; n < jsonArray.length(); n++) {
//                int id = jsonArray.getInt(n);
//                ids.add(id);
//            }
//
//            contentValues.put(DatabaseHelper.KEY_STAFFS_IDS, ids.toString());
//        } catch (NumberFormatException | JSONException e) {
//            Timber.e(e);
//        }


        contentValues.put(DatabaseHelper.KEY_ID_PASS_PROOFS, attedance.getIDPassProofs());
        return contentValues;
    }


    public Observable<?> saveAttendance(List<AttendanceResponse> attendanceRespons) {
        SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getWritableDatabase();
        try {
            db.beginTransaction();
            for (AttendanceResponse staff : attendanceRespons) {
                staff.setDataSyncStatus(SyncStatus.UPLOADED);
                ContentValues values = getContentValuesForAttedance(staff);
                long i = saveAttedance(db, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return null;
    }

    private long saveAttedance(SQLiteDatabase database, ContentValues contentValues) {
        return database.replace(TABLE_NAME, null, contentValues);
    }

    public long saveAttedance(ContentValues contentValues) {
        return DatabaseHelper.getDatabaseHelper().getWritableDatabase().replace(TABLE_NAME, null, contentValues);
    }


    public AttendanceResponse getSingleAttedanceFromCusor(Cursor cursor) {
        ArrayList<AttendanceResponse> list = getAttendanceFromCursor(cursor);
        if (list != null && list.size() >= 1) {
            return list.get(0);
        }

        return new AttendanceResponse();
    }

    public ArrayList<AttendanceResponse> getAttendanceFromCursor(Cursor cursor) {
        ArrayList<AttendanceResponse> attendanceRespons = new ArrayList<>();

        if (cursor == null || cursor.getCount() <= 0) {
            return attendanceRespons;
        }

        while (cursor.moveToNext()) {
            AttendanceResponse attendanceResponse = new AttendanceResponse();
            String attendanceDate = DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_ATTENDACE_DATE);
            attendanceResponse.setAttendanceDate(attendanceDate);


            String idpassproofs = DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_ID_PASS_PROOFS);
            attendanceResponse.setIDPassProofs(idpassproofs);


            attendanceRespons.add(attendanceResponse);

        }

        return attendanceRespons;
    }


    public Cursor getCursor(String selection, String[] selectionArgs) {
        SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getWritableDatabase();
        return db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
    }

    public AttendanceResponse getTodaysAddedance(String teamId) {
        return getAttedanceByDate(teamId, DateConvertor.getCurrentDate());
    }

    public AttendanceResponse getAttedanceByDate(String teamId, String date) {
        Cursor cursor = getCursor(DatabaseHelper.KEY_ATTENDACE_DATE + "=?", new String[]{date});
        return getSingleAttedanceFromCusor(cursor);
    }


    public ArrayList<AttendanceResponse> getAttendanceSheetForTeam(String teamId) {
        Cursor cursor = getCursor(null, null);
        ArrayList<AttendanceResponse> list = getAttendanceFromCursor(cursor);
        closeCursor(cursor);
        return list;
    }

    public ArrayList<AttendanceResponse> getFinalizedAttendanceSheet() {
        Cursor cursor = getCursor(DatabaseHelper.KEY_SYNC_STATUS + "=?", new String[]{SyncStatus.FINALIZED});
        ArrayList<AttendanceResponse> list = getAttendanceFromCursor(cursor);
        closeCursor(cursor);
        return list;
    }

    public Observable<ArrayList<AttendanceResponse>> getFinalizedAttedanceSheetObservable() {
        return Observable.create(subscriber -> {
            try {
                Cursor cursor = getCursor(DatabaseHelper.KEY_SYNC_STATUS + "=?", new String[]{SyncStatus.FINALIZED});
                ArrayList<AttendanceResponse> list = getAttendanceFromCursor(cursor);
                closeCursor(cursor);
                subscriber.onNext(list);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }


        });
    }


    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    private void closeDB(SQLiteDatabase db) {
        if (db != null) {
            db.close();
        }
    }

    public List<Pair<Integer, String>> getAllUnfinilizedAttendanceListInPair() {

        List<Pair<Integer, String>> dateAndAttedancePair = new ArrayList<>();
        //todo need to add where to get unfililized from
        Cursor cursor = DatabaseHelper.getDatabaseHelper().getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
        while (cursor.moveToNext()) {
            Pair<Integer, String> pair = new Pair<>(cursor.getInt(0), cursor.getString(1));
            dateAndAttedancePair.add(pair);
        }
        closeCursor(cursor);
        return dateAndAttedancePair;
    }

    public void updateTableAfterFinilizingForm(List<Pair<Integer, String>> pairList) {
        SQLiteDatabase writer = DatabaseHelper.getDatabaseHelper().getWritableDatabase();
        for (Pair<Integer, String> pair : pairList) {
            writer.rawQuery("UPDATE "
                    + TABLE_NAME
                    + " SET "
                    + DatabaseHelper.KEY_STAFFS_IDS
                    + " = "
                    + pair.second
                    + " WHERE "
                    + DatabaseHelper.KEY_ID
                    + " = "
                    + pair.first, null);
        }
        closeDB(writer);
    }

}
