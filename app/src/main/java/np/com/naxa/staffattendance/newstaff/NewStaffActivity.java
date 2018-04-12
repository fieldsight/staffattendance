package np.com.naxa.staffattendance.newstaff;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import np.com.naxa.staffattendance.FormCall;
import np.com.naxa.staffattendance.R;
import np.com.naxa.staffattendance.utlils.ToastUtils;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class NewStaffActivity extends AppCompatActivity implements View.OnClickListener {

    private Spinner bank, designation;
    private TextInputLayout firstName, lastName, ethinicity, contactNumber, email, address;
    private EditText dob, contractStartDate, contractEndDate;
    private Button photo, create;
    private List<String> designationList = new ArrayList<>();
    private List<String> bankList = new ArrayList<>();
    private RadioGroup gender;
    private Calendar calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener date;
    private ArrayAdapter<String> spinnerAdapter;
    private File photoFileToUpload;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_staff);

        initUI();

        initCalender();

        initListeners();

        spinnerValues();

        initSpinners();
    }

    private void spinnerValues() {
        designationList.add(getResources().getString(R.string.default_option));
        new FormCall().getDesignation(new FormCall.DesignationListener() {
            @Override
            public void designation(ArrayList<ArrayList<String>> arrayLists) {
                for (ArrayList<String> list : arrayLists) {
                    designationList.add(list.get(1));
                }
            }
        });

        bankList.add(getResources().getString(R.string.default_option));
        new FormCall().getBankList(new FormCall.BankListListener() {
            @Override
            public void bankList(ArrayList<ArrayList<String>> arrayLists) {
                for (ArrayList<String> list : arrayLists) {
                    bankList.add(list.get(1));
                }
            }
        });

    }

    private void initSpinners() {
        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, designationList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        designation.setAdapter(spinnerAdapter);

        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bankList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bank.setAdapter(this.spinnerAdapter);
    }

    private void initCalender() {
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        };
    }

    private void updateDateOnView(final EditText view) {
        String myFormat = "yyyy/MM/dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);
        view.setText(sdf.format(calendar.getTime()));
    }

    private void initUI() {
        designation = findViewById(R.id.staff_designation);
        firstName = findViewById(R.id.staff_first_name);
        lastName = findViewById(R.id.staff_last_name);
        dob = findViewById(R.id.staff_dob_date);
        gender = findViewById(R.id.staff_gender);
        ethinicity = findViewById(R.id.staff_ethinicity);
        bank = findViewById(R.id.staff_bank);
        contactNumber = findViewById(R.id.staff_contact_number);
        email = findViewById(R.id.staff_email);
        address = findViewById(R.id.staff_address);
        contractStartDate = findViewById(R.id.staff_contract_start_date_date);
        contractEndDate = findViewById(R.id.staff_contract_end_date_date);
        photo = findViewById(R.id.staff_photo);
        create = findViewById(R.id.staff_create);
    }

    private void initListeners() {
        dob.setOnClickListener(this);
        contractStartDate.setOnClickListener(this);
        contractEndDate.setOnClickListener(this);
        photo.setOnClickListener(this);
        create.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.staff_dob_date:
                getDatePicker((EditText) view);
                findViewById(R.id.staff_dob_heading).setVisibility(View.VISIBLE);
                break;

            case R.id.staff_contract_start_date_date:
                getDatePicker((EditText) view);
                findViewById(R.id.staff_contract_start_date_heading).setVisibility(View.VISIBLE);
                break;

            case R.id.staff_contract_end_date_date:
                getDatePicker((EditText) view);
                findViewById(R.id.staff_contract_end_date_heading).setVisibility(View.VISIBLE);
                break;

            case R.id.staff_photo:
                photo.setError(null);
                showImageOptionsDialog();
                break;

            case R.id.staff_create:
                if (validate()) {
                    ToastUtils.showShort("Validated");
                }
                break;
        }
    }

    private boolean validate() {
        boolean validtion = false;

        if (designation.getSelectedItem().equals(getResources().getString(R.string.default_option))) {
            ToastUtils.showShort("Select a designation");
        } else if (firstName.getEditText().getText().toString().isEmpty()) {
            ToastUtils.showShort("Enter first name");
        } else if (lastName.getEditText().getText().toString().isEmpty()) {
            ToastUtils.showShort("Enter last name");
        } else if (dob.getText().toString().isEmpty()) {
            ToastUtils.showShort("Choose date of birth");
        } else if (gender.getCheckedRadioButtonId() == -1) {
            ToastUtils.showShort("Select gender");
        } else if (ethinicity.getEditText().getText().toString().isEmpty()) {
            ToastUtils.showShort("Enter ethnicity");
        }
//        else if (bank.getSelectedItem().toString().equals(getResources().getString(R.string.default_option))) {
//            ToastUtils.showShort("Select Bank");
//        }
        else if (contactNumber.getEditText().getText().toString().isEmpty()) {
            ToastUtils.showShort("Enter contact number");
        } else if (email.getEditText().getText().toString().isEmpty()) {
            ToastUtils.showShort("Enter email");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getEditText().getText().toString()).matches()) {
            ToastUtils.showShort("Enter valid email");
        } else if (address.getEditText().getText().toString().isEmpty()) {
            ToastUtils.showShort("Enter address");
        } else if (contractStartDate.getText().toString().isEmpty()) {
            ToastUtils.showShort("Choose contract start date");
        } else if (contractEndDate.getText().toString().isEmpty()) {
            ToastUtils.showShort("Choose contract end date");
        } else {
            validtion = true;
        }
        return validtion;
    }

    private void getDatePicker(final EditText view) {
        new DatePickerDialog(this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
        updateDateOnView((EditText) view);
    }

    private void showImageOptionsDialog() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Dismiss"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int itemId) {
                dialog.dismiss();

                if (options[itemId].equals("Take Photo")) {
                    EasyImage.openCamera(NewStaffActivity.this, 0);
                } else if (options[itemId].equals("Choose from Gallery")) {
                    EasyImage.openChooserWithGallery(NewStaffActivity.this, "Select Staff Photo", 0);
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {

                photo.setError("");
            }

            @Override
            public void onImagePicked(File photoFileToUpload, EasyImage.ImageSource source, int type) {
                NewStaffActivity.this.photoFileToUpload = photoFileToUpload;
                photo.setText("Change Photo");
            }
        });
    }

}