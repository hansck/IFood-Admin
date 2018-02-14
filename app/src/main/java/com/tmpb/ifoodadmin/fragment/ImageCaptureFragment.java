package com.tmpb.ifoodadmin.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.util.Constants;
import com.tmpb.ifoodadmin.util.ImageUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class ImageCaptureFragment extends BaseFragment {

	private static final String IMAGE_EXT = ".jpg";
	protected static final int SELECT_IMAGE = 0;
	protected static Uri outputFileUri;

	public abstract void onImageCaptured();

	protected void openImageIntent(String caption) {
		// Determine Uri of camera image to save
		File root = new File(Environment.getExternalStorageDirectory() + File.separator + getString(R.string.app_name)
			+ File.separator);
		if (!root.exists()) {
			root.mkdirs();
		}
		String fname = createFileName(false, caption);
		final File sdImageMainDirectory = new File(root, fname);
		outputFileUri = Uri.fromFile(sdImageMainDirectory);

		// Camera
		final List<Intent> camIntent = new ArrayList<>();
		final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		final PackageManager packageManager = getActivity().getPackageManager();
		final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
		for (ResolveInfo res : listCam) {
			final String packageName = res.activityInfo.packageName;
			final Intent intent = new Intent(captureIntent);
			intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
			intent.setPackage(packageName);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			camIntent.add(intent);
		}

		// Filesystem
		final Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");

		Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_source));
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, camIntent.toArray(new Parcelable[camIntent.size()]));
		startActivityForResult(chooserIntent, SELECT_IMAGE);
	}

	private void addToGallery() {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(outputFileUri.getPath());
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		getActivity().sendBroadcast(mediaScanIntent);
	}

	protected File createResizedFile(String caption) {
		Bitmap resized = ImageUtil.getInstance().getResizedBitmap(getContext(), outputFileUri, 1000);
		File root = new File(Environment.getExternalStorageDirectory() + File.separator + getString(R.string.app_name) + File.separator);
		if (!root.exists()) {
			root.mkdirs();
		}
		String name = createFileName(true, caption);
		File resizedFile = new File(root, name);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		resized.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] array = stream.toByteArray();
		FileOutputStream out;
		try {
			out = new FileOutputStream(resizedFile);
			out.write(array);
			out.close();
			return resizedFile;
		} catch (Exception e) {
			return null;
		}
	}

	private String createFileName(boolean resized, String caption) {
		StringBuilder builder = new StringBuilder();
		if (resized) {
			builder.append(caption + "_");
		}
		builder.append(new SimpleDateFormat(Constants.DateFormat.DATETIME, Locale.ENGLISH).format(new Date()));
		builder.append(IMAGE_EXT);
		return new String(builder);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SELECT_IMAGE) {
				boolean isCamera;
				if (data == null) {
					isCamera = true;
				} else {
					String action = data.getAction();
					isCamera = (action != null) && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
				}
				if (isCamera) {
					addToGallery();
				} else {
					if (data.getData() != null) {
						outputFileUri = data.getData();
					} else {
						addToGallery();
					}
				}
				onImageCaptured();
			}
		}
	}
}
