package com.cmpundhir.cm.merataskmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cmpundhir.cm.merataskmanager.model.MyTask;
import com.cmpundhir.cm.merataskmanager.utils.MyDialog;
import com.cmpundhir.cm.merataskmanager.utils.TaskPriority;
import com.cmpundhir.cm.merataskmanager.utils.TaskStatus;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 111;
    FloatingActionButton fab;
    Map<Integer,TaskPriority> priorityMap = new HashMap<>();
    Map<Integer,TaskStatus> statusMap = new HashMap<>();
    Calendar calendar = Calendar.getInstance();
    int year,month,day;


    private void init(){
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog(0);
            }
        });
        priorityMap.put(R.id.high,TaskPriority.HIGH);
        priorityMap.put(R.id.med,TaskPriority.MEDIUM);
        priorityMap.put(R.id.low,TaskPriority.LOW);

        statusMap.put(R.id.pen,TaskStatus.PENDING);
        statusMap.put(R.id.com,TaskStatus.COMPLETE);
        statusMap.put(R.id.fail,TaskStatus.FAILED);

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUi();
    }

    private void updateUi(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setLogo(R.drawable.thumps_up_emoji)
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.PhoneBuilder().build()))
                            .build(),
                    RC_SIGN_IN);
        }
    }

    private void myDialog(int form){    // form 0 = add new task , 1 = edit old task
        final Dialog dialog = MyDialog.getCustomDialog(this,R.layout.dialog_add_task);
        final Switch ss = dialog.findViewById(R.id.switch1);
        final LinearLayout llDate = dialog.findViewById(R.id.llDate);
        TextView title = dialog.findViewById(R.id.title);
        final EditText taskEd = dialog.findViewById(R.id.taskEd);
        final EditText dscEd = dialog.findViewById(R.id.dscEd);
        final RadioGroup rgP = dialog.findViewById(R.id.rgPriority);
        final RadioGroup rgS = dialog.findViewById(R.id.rgStatus);
        final Button finishDateBtn = dialog.findViewById(R.id.finishDateBtn);
        Button doneBtn = dialog.findViewById(R.id.doneBtn);

        rgS.setVisibility(form==0?View.GONE:View.VISIBLE);
        title.setText(form==0?"Add Task":"Edit Task");

        ss.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                llDate.setVisibility(isChecked?View.VISIBLE:View.GONE);
                ss.setText(isChecked?ss.getTextOn():ss.getTextOff());
            }
        });

        finishDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDatepicker(finishDateBtn);
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task,dsc,date;
                task= taskEd.getText().toString();
                dsc= dscEd.getText().toString();

                RadioButton rpS = findViewById(rgS.getCheckedRadioButtonId());
                date = finishDateBtn.getText().toString();

                MyTask task1 = new MyTask();
                task1.setTask(task);
                task1.setDsc(dsc);
                task1.setTaskPriority(rgP.getCheckedRadioButtonId()==-1? TaskPriority.NONE:priorityMap.get(rgP.getCheckedRadioButtonId()));
                task1.setTaskStatus(rgS.getCheckedRadioButtonId()==-1? TaskStatus.PENDING:statusMap.get(rgS.getCheckedRadioButtonId()));
                //task1.setCreateDate();
                
            }
        });

    }

    private void callDatepicker(final Button btn){

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                btn.setText(String.format("%d-%02d-%02d",year,month+1,dayOfMonth));
            }
        }, year, month,day);

        datePickerDialog.show();
    }
}
