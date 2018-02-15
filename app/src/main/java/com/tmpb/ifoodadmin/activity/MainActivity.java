package com.tmpb.ifoodadmin.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.util.ConnectivityUtil;
import com.tmpb.ifoodadmin.util.Constants;
import com.tmpb.ifoodadmin.util.UserManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

	private FirebaseAuth.AuthStateListener listener;
	private boolean first = true;

	@AfterViews
	void initLayout() {
		UserManager.getInstance().initAuth();
		ConnectivityUtil.getInstance().setConnectivityManager(getApplicationContext());
		SharedPreferences preference = getSharedPreferences(Constants.General.PREFERENCE, Context.MODE_PRIVATE);
		UserManager.getInstance().setKeyStore(preference);

		listener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				UserManager.getInstance().setFirebaseUser(firebaseAuth.getCurrentUser());
				if (first) {
					first = false;
					if (UserManager.getInstance().getFirebaseUser() != null) {
						Intent intent = new Intent(MainActivity.this, HomeActivity_.class);
						startActivity(intent);
					} else {
						Intent intent = new Intent(MainActivity.this, LoginActivity_.class);
						startActivity(intent);
					}
				}
				finish();
			}
		};
	}

	@Override
	public void onStart() {
		super.onStart();
		UserManager.getInstance().getAuth().addAuthStateListener(listener);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (listener != null) {
			UserManager.getInstance().getAuth().removeAuthStateListener(listener);
		}
	}
}