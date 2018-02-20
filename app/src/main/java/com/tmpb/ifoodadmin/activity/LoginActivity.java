package com.tmpb.ifoodadmin.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.model.Canteen;
import com.tmpb.ifoodadmin.util.Common;
import com.tmpb.ifoodadmin.util.Constants;
import com.tmpb.ifoodadmin.util.FirebaseDB;
import com.tmpb.ifoodadmin.util.manager.UserManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;

@EActivity(R.layout.activity_login)
@WindowFeature(Window.FEATURE_NO_TITLE)
public class LoginActivity extends AppCompatActivity {

	@ViewById
	TextInputEditText editEmail;
	@ViewById
	TextInputEditText editPassword;
	@ViewById
	ProgressBar progressBar;
	@ViewById
	Button btnSignIn;

	private FirebaseAuth.AuthStateListener listener;
	private FirebaseAuth auth;

	@AfterViews
	void initLayout() {
		auth = UserManager.getInstance().getAuth();
	}

	@Click(R.id.btnSignIn)
	void signIn() {
		setLoading(true);
		String email = editEmail.getText().toString();
		String password = editPassword.getText().toString();
		if (!email.isEmpty() && !password.isEmpty()) {
			signIn(email, password);
		} else {
			setLoading(false);
			Common.getInstance().showAlertToast(this, getString(R.string.field_empty));
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	//region Private Methods
	private void setLoading(boolean loading) {
		progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
		btnSignIn.setVisibility(loading ? View.GONE : View.VISIBLE);
	}

	private void goToHome() {
		Intent intent = new Intent(this, HomeActivity_.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.enter_right, R.anim.exit_left);
	}
	//endregion

	//region Firebase Call
	private void signIn(String email, String password) {
		auth.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					if (task.isSuccessful()) {
						UserManager.getInstance().setFirebaseUser(auth.getCurrentUser());
						checkUserRole();
					} else {
						setLoading(false);
						Common.getInstance().showAlertToast(LoginActivity.this, getString(R.string.sign_in_failed));
					}
				}
			});
	}

	public void checkUserRole() {
		String email = UserManager.getInstance().getUserEmail();
		Log.e("email key", email);
		final DatabaseReference ref = FirebaseDB.getInstance().getDbReference(Constants.Canteen.CANTEEN);
		ref.orderByChild(Constants.Canteen.ACCOUNT).equalTo(email).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				Log.e("email key", "MASUK SINI 1");
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
					Log.e("email key", "MASUK SINI 2");
					Canteen canteen = postSnapshot.getValue(Canteen.class);
					canteen.setKey(postSnapshot.getKey());
					UserManager.getInstance().setCanteen(canteen);
				}
				ref.removeEventListener(this);
				goToHome();
			}

			@Override
			public void onCancelled(DatabaseError error) {
				setLoading(false);
				ref.removeEventListener(this);
			}
		});
	}
	//endregion
}

