package com.cmpundhir.cm.merataskmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cmpundhir.cm.merataskmanager.adapters.MyTaskAdapter;
import com.cmpundhir.cm.merataskmanager.listeners.TaskListener;
import com.cmpundhir.cm.merataskmanager.model.MyTask;
import com.cmpundhir.cm.merataskmanager.utils.FirePaths;
import com.cmpundhir.cm.merataskmanager.utils.MyDialog;
import com.cmpundhir.cm.merataskmanager.utils.TaskPriority;
import com.cmpundhir.cm.merataskmanager.utils.TaskStatus;
import com.cmpundhir.cm.merataskmanager.utils.TaskType;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity implements TaskListener {


    private ProgressBar list_progressbar;
    private static final int RC_SIGN_IN = 111;
    private static final String TAG = "MainActivity__";
    FloatingActionButton fab;
    Map<Integer,TaskPriority> priorityMap = new HashMap<>();
    Map<Integer,TaskStatus> statusMap = new HashMap<>();
    Calendar calendar = Calendar.getInstance();
    int year,month,day;
    private boolean isFinishDateSet = false;
    Date endDate;
    List<MyTask> myTaskList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    MyTaskAdapter myTaskAdapter;
    RecyclerView recyclerView;

    private void init(){
        list_progressbar = findViewById(R.id.list_progressbar);

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

        myTaskAdapter = new MyTaskAdapter(this,myTaskList);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myTaskAdapter);
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
        getTasks();
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
        final ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        final Button doneBtn = dialog.findViewById(R.id.doneBtn);

        rgS.setVisibility(form==0?View.GONE:View.VISIBLE);
        title.setText(form==0?"Add Task":"Edit Task");

        ss.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                llDate.setVisibility(isChecked?View.VISIBLE:View.GONE);
                ss.setText(isChecked?ss.getTextOn():ss.getTextOff());
                isFinishDateSet = !isChecked;
                finishDateBtn.setText("Select Finish Date");
            }
        });

        finishDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFinishDateSet = false;
                callDatepicker(finishDateBtn);
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task,dsc;
                task= taskEd.getText().toString();
                dsc= dscEd.getText().toString();
                taskEd.setError(null);
                if (taskEd.getText().toString().isEmpty()){
                    taskEd.setError("Required");
                    return;
                }
                MyTask task1 = new MyTask();
                task1.setTask(task);
                task1.setDsc(dsc);
                task1.setTaskPriority(rgP.getCheckedRadioButtonId()==-1? TaskPriority.NONE:priorityMap.get(rgP.getCheckedRadioButtonId()));
                task1.setTaskStatus(rgS.getCheckedRadioButtonId()==-1? TaskStatus.PENDING:statusMap.get(rgS.getCheckedRadioButtonId()));
                task1.setTaskType(ss.isChecked()? TaskType.GENERAL:TaskType.TODAY);
                if (ss.isChecked()){
                    if(isFinishDateSet){
                        task1.setEndDate(endDate);
                        Toast.makeText(MainActivity.this, endDate.toString()+"", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "Please select end date", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                addTaskTOFire(task1,doneBtn,progressBar,dialog);
            }
        });

    }

    private void callDatepicker(final Button btn){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                btn.setText(String.format("%d-%02d-%02d",year,month+1,dayOfMonth));
                String currentDateString = String.format("%02d/%02d/%d 23:59:59",month,dayOfMonth,year);
                SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                try {
                    endDate = sd.parse(currentDateString);
                    isFinishDateSet = true;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, year, month,day);
        datePickerDialog.show();
    }

    private void addTaskTOFire(final MyTask task, final Button button, final ProgressBar progressBar, final Dialog dialog){
        button.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Add a new document with a generated ID
        db.collection(FirePaths.TASK)
                .add(task)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(MainActivity.this, "Task added successfully : "+documentReference.getId(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        button.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        dialog.dismiss();
                        task.setId(documentReference.getId());
                        myTaskList.add(0,task);
                        myTaskAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error adding document", e);
                        button.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    private void getTasks(){
        db.collection(FirePaths.TASK)
                .orderBy("createDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                myTaskList.clear();
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                    MyTask task = documentSnapshot.toObject(MyTask.class);
                    task.setId(documentSnapshot.getId());
                    myTaskList.add(task);
                }
                myTaskAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"error : "+e.getMessage());
                Toast.makeText(MainActivity.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTasksList(){
        db.collection(FirePaths.TASK).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                    myTaskList.add(documentSnapshot.toObject(MyTask.class));
                }
                myTaskAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateTaskStatus(MyTask task){
        list_progressbar.setVisibility(View.VISIBLE);
        db.collection(FirePaths.TASK).document(task.getId())
                .update("taskStatus",task.getTaskStatus().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        list_progressbar.setVisibility(View.GONE);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        list_progressbar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    public void onTaskStatusUpdate(MyTask task) {
        updateTaskStatus(task);
    }
}
