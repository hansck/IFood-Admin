package com.tmpb.ifoodadmin.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.model.Menu;
import com.tmpb.ifoodadmin.util.Common;
import com.tmpb.ifoodadmin.util.Constants;
import com.tmpb.ifoodadmin.util.FirebaseDB;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.IgnoreWhen;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@EFragment(R.layout.fragment_manage_menu)
public class ManageMenuFragment extends ImageCaptureFragment {

	private Menu menu;
	private String name;
	private int price;
	private StorageReference storageRef;
	private File resizedFile;
	private static final int REQUEST_PERMISSIONS = 1;

	@ViewById
	EditText editName;
	@ViewById
	EditText editPrice;
	@ViewById
	TextView imageLabel;
	@ViewById
	Button btnAdd;
	@ViewById
	Button btnUpdate;
	@ViewById
	ProgressBar progressBar;

	@AfterViews
	void initLayout() {
		((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.manage_canteen));
		storageRef = FirebaseStorage.getInstance().getReference();
		Bundle data = this.getArguments();
		if (data != null) {
			menu = data.getParcelable(Constants.Menu.MENU);
		}
		if (menu != null) {
			editName.setText(menu.getName());
			editPrice.setText(menu.getPrice());
			if (menu.getPicture() != null) {
				int start = menu.getPicture().indexOf("F") + 1;
				int end = menu.getPicture().indexOf("?");
				imageLabel.setText(menu.getPicture().substring(start, end));
			}
			btnAdd.setVisibility(GONE);
			btnUpdate.setVisibility(VISIBLE);
		}
	}

	@Click(R.id.btnAddImage)
	void onImageClick() {
		if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager
			.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission
			.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			Common.getInstance().requestRequiredPermissions(getActivity());
		} else {
			openImageIntent(getString(R.string.menu));
		}
	}

	@Click(R.id.btnAdd)
	void onAdd() {
		if (getData()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(getString(R.string.dialog_add))
				.setPositiveButton(getString(R.string.yes), addNewsListener)
				.setNegativeButton(getString(R.string.no), addNewsListener).show();
		} else {
			Common.getInstance().showAlertToast(getActivity(), getString(R.string.field_empty));
		}
	}

	@Click(R.id.btnUpdate)
	void onUpdate() {
		if (getData()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(getString(R.string.dialog_update))
				.setPositiveButton(getString(R.string.yes), updateNewsListener)
				.setNegativeButton(getString(R.string.no), updateNewsListener).show();
		} else {
			Common.getInstance().showAlertToast(getActivity(), getString(R.string.field_empty));
		}
	}

	@IgnoreWhen(IgnoreWhen.State.VIEW_DESTROYED)
	void setLoading(boolean loading) {
		progressBar.setVisibility(loading ? VISIBLE : GONE);
		btnAdd.setVisibility(loading ? GONE : VISIBLE);
		btnUpdate.setVisibility(loading ? GONE : VISIBLE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
		if (requestCode == REQUEST_PERMISSIONS) {
			if (Common.getInstance().verifyPermission(results)) {
				openImageIntent(getString(R.string.menu));
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, results);
		}
	}

	@Override
	public void onImageCaptured() {
		resizedFile = createResizedFile(getString(R.string.menu));
		imageLabel.setText(resizedFile.getName());
	}

	//region Private methods

	private boolean getData() {
		if (!editName.getText().toString().isEmpty() && !editPrice.getText().toString().isEmpty()) {
			name = editName.getText().toString();
			price = Integer.parseInt(editPrice.getText().toString());
			return true;
		}
		return false;
	}

	private Menu prepareNews(Uri downloadUrl) {
		Menu menu = new Menu(name, price);
		if (downloadUrl != null) {
			menu.setPicture(downloadUrl.toString());
		}
		return menu;
	}
	//endregion

	//region Firebase Calls
	@SuppressWarnings("VisibleForTests")
	private void uploadImage(final boolean isNew) {
		storageRef.child(Constants.Storage.IMAGES + "/" + resizedFile.getName()).putFile(outputFileUri)
			.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					Uri downloadUrl = taskSnapshot.getDownloadUrl();
					if (isNew) {
						uploadContent(downloadUrl);
					} else {
						updateContent(downloadUrl);
					}
				}
			})
			.addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception exception) {
					setLoading(false);
					Common.getInstance().showAlertToast(getActivity(), getString(R.string.default_failed));
				}
			});
	}

	private void uploadContent(Uri downloadUrl) {
		String newsId = FirebaseDB.getInstance().getKey(Constants.Menu.MENU);
		FirebaseDB.getInstance().getDbReference(Constants.Menu.MENU).child(newsId).setValue(prepareNews(downloadUrl));
		setLoading(false);
		Common.getInstance().showAlertToast(getActivity(), getString(R.string.success_add));
		navigateFragmentWithoutBackstack(R.id.contentFrame, new MenuFragment_());
	}

	private void updateContent(Uri downloadUrl) {
		DatabaseReference ref = FirebaseDB.getInstance().getDbReference(Constants.Menu.MENU);
		Map<String, Object> taskMap = new HashMap<>();
		taskMap.put("name", name);
		taskMap.put("price", price);
		if (downloadUrl != null) taskMap.put("picture", downloadUrl.toString());
		ref.child(menu.getKey()).updateChildren(taskMap);
		setLoading(false);
		Common.getInstance().showAlertToast(getActivity(), getString(R.string.success_update));
		getActivity().getSupportFragmentManager().popBackStack();
	}
	//endregion

	//region Listeners
	DialogInterface.OnClickListener addNewsListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int choice) {
			switch (choice) {
				case DialogInterface.BUTTON_POSITIVE:
					setLoading(true);
					if (outputFileUri != null) {
						uploadImage(true);
					} else {
						uploadContent(null);
					}
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
			}
		}
	};

	DialogInterface.OnClickListener updateNewsListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int choice) {
			switch (choice) {
				case DialogInterface.BUTTON_POSITIVE:
					setLoading(true);
					if (outputFileUri != null) {
						uploadImage(false);
					} else {
						updateContent(null);
					}
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
			}
		}
	};
	//endregion
}