package com.cmpundhir.cm.merataskmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cmpundhir.cm.merataskmanager.R;
import com.cmpundhir.cm.merataskmanager.listeners.TaskListener;
import com.cmpundhir.cm.merataskmanager.model.MyTask;
import com.cmpundhir.cm.merataskmanager.utils.TaskPriority;
import com.cmpundhir.cm.merataskmanager.utils.TaskStatus;
import com.google.android.gms.tasks.Task;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTaskAdapter extends RecyclerView.Adapter<MyTaskAdapter.ViewHolder> {
    Context context;
    List<MyTask> myTaskList;
    Map<TaskPriority,Integer> priorityColorMap = new HashMap<>();
    TaskListener taskListener;
    public MyTaskAdapter(Context context, List<MyTask> myTaskList) {
        this.context = context;
        this.myTaskList = myTaskList;
        this.taskListener = (TaskListener) context;
        priorityColorMap.put(TaskPriority.HIGH,R.color.priorityHigh);
        priorityColorMap.put(TaskPriority.MEDIUM,R.color.priorityMedium);
        priorityColorMap.put(TaskPriority.LOW,R.color.priorityLow);
        priorityColorMap.put(TaskPriority.NONE,R.color.priorityNone);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_task,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final MyTask myTask = myTaskList.get(position);
        holder.t1.setText(myTask.getTask());
        holder.t2.setText(myTask.getDsc());
        holder.status.setBackgroundColor(context.getResources().getColor(priorityColorMap.get(myTask.getTaskPriority())));
        holder.checkBox.setChecked(myTask.getTaskStatus().equals(TaskStatus.COMPLETE));
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myTask.setTaskStatus(holder.checkBox.isChecked()? TaskStatus.COMPLETE:myTask.getTaskStatus());
                taskListener.onTaskStatusUpdate(myTask);
            }
        });
        holder.t3.setText(myTask.getCreateDate()==null? Calendar.getInstance().getTime().toString():myTask.getCreateDate().toString());
    }

    @Override
    public int getItemCount() {
        return myTaskList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView t1,t2,t3;
        View status;
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            t1 = itemView.findViewById(R.id.t1);
            t2 = itemView.findViewById(R.id.t2);
            t3 = itemView.findViewById(R.id.t3);
            status = itemView.findViewById(R.id.status);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}
