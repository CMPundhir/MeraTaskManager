package com.cmpundhir.cm.merataskmanager.listeners;

import com.cmpundhir.cm.merataskmanager.model.MyTask;

public interface TaskListener {
    public void onTaskStatusUpdate(MyTask task);
}
