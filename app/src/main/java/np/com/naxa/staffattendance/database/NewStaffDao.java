package np.com.naxa.staffattendance.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

import np.com.naxa.staffattendance.pojo.NewStaffPojo;
import np.com.naxa.staffattendance.pojo.NewStaffPojoBuilder;
import timber.log.Timber;


public class NewStaffDao {

    public static String SAVED = "saved";
    public static String FAILED = "failed";
    public static String UPLOADED = "uploaded";


    private static NewStaffDao newStaffDao;

    public static NewStaffDao getInstance() {
        if (newStaffDao == null) {
            return new NewStaffDao();
        }
        return newStaffDao;
    }

    public long saveNewStaff(NewStaffPojo newStaffPojo) {
        Timber.d("saveNewStaff: did: " + newStaffPojo.getIdPassDID());
        return DatabaseHelper.getDatabaseHelper()
                .getWritableDatabase()
                .insert(DatabaseHelper.TABLE_NEW_STAFF,
                        null,
                        getContentValues(newStaffPojo));
    }


    public ArrayList<NewStaffPojo> getOfflineStaffs() {
        Cursor cursor = DatabaseHelper.getDatabaseHelper().getReadableDatabase().rawQuery("SELECT* FROM " + DatabaseHelper.TABLE_NEW_STAFF, null);
        ArrayList<NewStaffPojo> pojos = new ArrayList<>();
        while (cursor.moveToNext()) {
            pojos.add(mapCursorToPojo(cursor));
        }
        return pojos;
    }

    private void delete(String selection, String[] selectionArgs) {
        DatabaseHelper.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_NEW_STAFF, selection, selectionArgs);
    }


    public void deleteStaffById(String staffId) {
        delete(DatabaseHelper.KEY_ID + " =?", new String[]{staffId});
    }

    private NewStaffPojo mapCursorToPojo(Cursor cursor) {
        Timber.i("mapCursorToPojo" + cursor);


        return new NewStaffPojoBuilder()
                .setDesignation(Integer.valueOf(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_DESIGNATION)))
                .setDesignationLabel(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_DESIGNATION_LABEL))
                .setFirstName(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_FIRST_NAME))
                .setLastName(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_LAST_NAME))
                .setDateOfBirth(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_DOB))
                .setGender(Integer.valueOf(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_GENDER)))
                .setEthnicity(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_ETHNICITY))
                .setBank(Integer.valueOf(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_BANK_ID)))
                .setBankName(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_BANK_NAME))
                .setAccountNumber(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_ACCOUNT_NUMBER))
                .setPhoneNumber(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_CONTACT_NUMBER))
                .setEmail(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_EMAIL))
                .setAddress(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_ADDRESS))
                .setContractStart(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_CONTRACT_START_DATE))
                .setContractEnd(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_CONTRACT_END_DATE))
                .setPhoto(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_PHOTO))
                .setStatus(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_STAFF_DETAIL_STATUS))
                .setID(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_ID))
                .setIDPass(DatabaseHelper.getStringFromCursor(cursor, DatabaseHelper.KEY_ID_PASS))
                .createNewStaffPojo();
    }

    private ContentValues getContentValues(NewStaffPojo pojo) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_ID,pojo.getId());
        values.put(DatabaseHelper.KEY_STAFF_DESIGNATION, pojo.getDesignation());
        values.put(DatabaseHelper.KEY_STAFF_DESIGNATION_LABEL, pojo.getDesignationLabel());
        values.put(DatabaseHelper.KEY_STAFF_FIRST_NAME, pojo.getFirstName());
        values.put(DatabaseHelper.KEY_STAFF_LAST_NAME, pojo.getLastName());
        values.put(DatabaseHelper.KEY_STAFF_DOB, pojo.getDateOfBirth());
        values.put(DatabaseHelper.KEY_STAFF_GENDER, pojo.getGender());
        values.put(DatabaseHelper.KEY_STAFF_ETHNICITY, pojo.getEthnicity());
        values.put(DatabaseHelper.KEY_STAFF_BANK_ID, pojo.getBank());
        values.put(DatabaseHelper.KEY_STAFF_BANK_NAME, pojo.getBankName());
        values.put(DatabaseHelper.KEY_STAFF_ACCOUNT_NUMBER, pojo.getAccountNumber());
        values.put(DatabaseHelper.KEY_STAFF_CONTACT_NUMBER, pojo.getPhoneNumber());
        values.put(DatabaseHelper.KEY_STAFF_EMAIL, pojo.getEmail());
        values.put(DatabaseHelper.KEY_STAFF_ADDRESS, pojo.getAddress());
        values.put(DatabaseHelper.KEY_STAFF_CONTRACT_START_DATE, pojo.getContractStart());
        values.put(DatabaseHelper.KEY_STAFF_CONTRACT_END_DATE, pojo.getContractEnd());
        values.put(DatabaseHelper.KEY_STAFF_PHOTO, pojo.getPhoto());
        values.put(DatabaseHelper.KEY_STAFF_DETAIL_STATUS, pojo.getStatus());
        values.put(DatabaseHelper.KEY_ID_PASS, pojo.getIdPassDID());
        return values;
    }


}
