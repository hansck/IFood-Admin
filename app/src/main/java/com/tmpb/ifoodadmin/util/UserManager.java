package com.tmpb.ifoodadmin.util;

import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tmpb.ifoodadmin.model.Canteen;

/**
 * Created by Hans CK on 14-Feb-18.
 */

public class UserManager {

	private static UserManager instance = new UserManager();
	private FirebaseAuth auth;
	private FirebaseUser firebaseUser;
	private SharedPreferences keyStore;

	public UserManager() {

	}

	public void initAuth() {
		auth = FirebaseAuth.getInstance();
	}

	public static UserManager getInstance() {
		return instance;
	}

	public FirebaseAuth getAuth() {
		return auth;
	}

	public void signOut() {
		clearKeyStore();
		auth.signOut();
	}

	public FirebaseUser getFirebaseUser() {
		return firebaseUser;
	}

	public void setFirebaseUser(FirebaseUser firebaseUser) {
		this.firebaseUser = firebaseUser;
	}

	public String getUserEmail() {
		return firebaseUser.getEmail();
	}

	public SharedPreferences getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(SharedPreferences preferences) {
		this.keyStore = preferences;
	}

	public void setUserRole(boolean isAdmin) {
		SharedPreferences.Editor editor = keyStore.edit();
		if (isAdmin) {
			editor.putString(Constants.User.ROLE, Constants.User.ADMIN);
		} else {
			editor.putString(Constants.User.ROLE, Constants.User.MEMBER);
		}
		editor.apply();
	}

	public void setCanteen(Canteen canteen) {
		SharedPreferences.Editor editor = keyStore.edit();
		editor.putString(Constants.Canteen.KEY, canteen.getKey());
		editor.putString(Constants.Canteen.NAME, canteen.getName());
		editor.putString(Constants.Canteen.LOCATION, canteen.getLocation());
		editor.putString(Constants.Canteen.SCHEDULE, canteen.getSchedule());
		editor.putString(Constants.User.PHONE, canteen.getAccount());
		editor.apply();
	}

	public String getCanteenKey() {
		return keyStore.getString(Constants.Canteen.KEY, "");
	}

	public void clearKeyStore() {
		SharedPreferences.Editor editor = keyStore.edit();
		editor.clear();
		editor.apply();
	}
}
