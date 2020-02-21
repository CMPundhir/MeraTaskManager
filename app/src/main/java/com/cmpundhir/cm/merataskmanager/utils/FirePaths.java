package com.cmpundhir.cm.merataskmanager.utils;

import com.google.firebase.auth.FirebaseAuth;

public class FirePaths {
    public static final String USER = "user/"+ FirebaseAuth.getInstance().getUid()+"/";
    public static final String TASK = USER+"task/";
}
