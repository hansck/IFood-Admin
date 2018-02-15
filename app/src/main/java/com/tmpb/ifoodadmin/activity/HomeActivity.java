package com.tmpb.ifoodadmin.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.fragment.CanteenFragment_;
import com.tmpb.ifoodadmin.fragment.ManageCanteenFragment_;
import com.tmpb.ifoodadmin.fragment.ManageMenuFragment_;
import com.tmpb.ifoodadmin.fragment.MenuFragment_;
import com.tmpb.ifoodadmin.util.UserManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;

@EActivity(R.layout.activity_home)
@WindowFeature(Window.FEATURE_NO_TITLE)
public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	@ViewById
	ProgressBar progressBar;
	@ViewById
	Toolbar toolbar;
	@ViewById
	DrawerLayout content;
	@ViewById
	NavigationView navigationView;

	private FragmentManager fm;
	private ActionBarDrawerToggle toggle;

	@AfterViews
	void initLayout() {
		setSupportActionBar(toolbar);
		toggle = new ActionBarDrawerToggle(this, content, toolbar, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View v) {
			}

			public void onDrawerOpened(View v) {

			}
		};
		content.addDrawerListener(toggle);
		toggle.syncState();
		navigationView.setNavigationItemSelectedListener(this);
		navigationView.setItemIconTintList(null);
		if (UserManager.getInstance().getCanteenKey() != null) {
			navigationView.getMenu().setGroupVisible(R.id.menu_seller, true);
		} else {
			navigationView.getMenu().setGroupVisible(R.id.menu_admin, true);
		}

		setHomeChecked();

		fm = getSupportFragmentManager();
		fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				int nStack = fm.getBackStackEntryCount();
				if (nStack == 0) {
					setHeader();
					setDrawerState(true);
				} else {
					setDrawerState(false);
					getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				}
				invalidateOptionsMenu();
			}
		});
		setHeader();
	}

	public void setHomeChecked() {
		navigationView.getMenu().getItem(0).setChecked(true);
	}

	private void setHeader() {
		View header = navigationView.getHeaderView(0);
		TextView email = (TextView) header.findViewById(R.id.email);
		email.setText(UserManager.getInstance().getUserEmail());
	}

	@Override
	public void onBackPressed() {
		if (content.isDrawerOpen(GravityCompat.START)) {
			content.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_dashboard_drawer, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.nav_menu).setVisible(false);
		menu.findItem(R.id.nav_history_order).setVisible(false);
		menu.findItem(R.id.nav_manage_menu).setVisible(false);
		menu.findItem(R.id.nav_canteen).setVisible(false);
		menu.findItem(R.id.nav_manage_canteen).setVisible(false);
		menu.findItem(R.id.nav_signout).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (toggle.onOptionsItemSelected(item)) {
			return true;
		}
		if (id == android.R.id.home) {
			super.onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.nav_history_order:
				break;
			case R.id.nav_menu:
				goToMenu();
				break;
			case R.id.nav_manage_menu:
				goToManageMenu();
				break;
			case R.id.nav_canteen:
				goToCanteen();
				break;
			case R.id.nav_manage_canteen:
				goToManageCanteen();
				break;
			case R.id.nav_signout:
				onSignOut();
				break;
			default:
				break;
		}
		content.closeDrawer(GravityCompat.START);
		return true;
	}

	//region Private Methods
	private void goToMenu() {
		MenuFragment_ fragment = new MenuFragment_();
		navigateTo(fragment);
	}

//		private void goToHistoryOrder() {
//		Intent intent = new Intent(this, VerificationActivity_.class);
//		startActivity(intent);
//		finish();
//	}

	private void goToManageMenu() {
		ManageMenuFragment_ fragment = new ManageMenuFragment_();
		navigateTo(fragment);
	}

	private void goToCanteen() {
		CanteenFragment_ fragment = new CanteenFragment_();
		navigateTo(fragment);
	}

	private void goToManageCanteen() {
		ManageCanteenFragment_ fragment = new ManageCanteenFragment_();
		navigateTo(fragment);
	}

	private void goToLogin() {
		Intent intent = new Intent(HomeActivity.this, LoginActivity_.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
	}

	private void setLoading(boolean loading) {
		progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
	}

	private void onSignOut() {
		UserManager.getInstance().signOut();
		goToLogin();
	}

	private void setDrawerState(boolean enable) {
		if (enable) {
			content.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		} else {
			content.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
		toggle.setDrawerIndicatorEnabled(enable);
		toggle.syncState();
		setSupportActionBar(toolbar);
	}

	private void navigateTo(Fragment fragment) {
		if (!isFinishing()) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.setCustomAnimations(R.anim.enter_right, R.anim.exit_left);
			ft.replace(R.id.contentFrame, fragment);
			ft.commit();
		}
	}

	private void navigateToWithBackstack(Fragment fragment) {
		if (!isFinishing()) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.setCustomAnimations(R.anim.enter_right, R.anim.exit_left, R.anim.enter_left, R.anim.exit_right);
			ft.replace(R.id.contentFrame, fragment);
			ft.addToBackStack(null);
			ft.commit();
		}
	}
	//endregion
}
