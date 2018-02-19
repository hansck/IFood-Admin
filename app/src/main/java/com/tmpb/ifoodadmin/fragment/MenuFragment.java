package com.tmpb.ifoodadmin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.activity.LoginActivity_;
import com.tmpb.ifoodadmin.adapter.MenuAdapter;
import com.tmpb.ifoodadmin.model.Menu;
import com.tmpb.ifoodadmin.util.Common;
import com.tmpb.ifoodadmin.util.ConnectivityUtil;
import com.tmpb.ifoodadmin.util.Constants;
import com.tmpb.ifoodadmin.util.FirebaseDB;
import com.tmpb.ifoodadmin.util.ImageUtil;
import com.tmpb.ifoodadmin.util.ItemDecoration;
import com.tmpb.ifoodadmin.util.OnListItemSelected;
import com.tmpb.ifoodadmin.util.UserManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.IgnoreWhen;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@EFragment(R.layout.fragment_menu)
public class MenuFragment extends BaseFragment {

	private List<Menu> menus = new ArrayList<>();
	private MenuAdapter adapter;

	@ViewById
	RecyclerView listMenu;
	@ViewById
	ImageView picture;
	@ViewById
	TextView name;
	@ViewById
	TextView location;
	@ViewById
	TextView schedule;
	@ViewById
	Toolbar toolbar;
	@ViewById
	AppBarLayout appBarLayout;
	@ViewById
	CollapsingToolbarLayout collapsingToolbar;

	@AfterViews
	void initLayout() {
		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
			@Override
			public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
				if (verticalOffset <= toolbar.getHeight() - collapsingToolbar.getHeight()) {
					collapsingToolbar.setContentScrimColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
					collapsingToolbar.setTitle(UserManager.getInstance().getCanteenName());
				} else {
					collapsingToolbar.setContentScrimColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
					collapsingToolbar.setTitle("");
				}
			}
		});

		setView();
		RecyclerView.LayoutManager newsLayoutManager = new LinearLayoutManager(getActivity());
		listMenu.setLayoutManager(newsLayoutManager);
		listMenu.addItemDecoration(new ItemDecoration(1, Common.getInstance().dpToPx(getActivity(), 10), true));
		listMenu.setItemAnimator(new DefaultItemAnimator());

		adapter = new MenuAdapter(getActivity(), menus, menuListener);
		listMenu.setAdapter(adapter);

		if (ConnectivityUtil.getInstance().isNetworkConnected()) {
			loadMenu();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@IgnoreWhen(IgnoreWhen.State.VIEW_DESTROYED)
	void cancelRefresh() {
	}

	private void setView() {
		name.setText(UserManager.getInstance().getCanteenName());
		location.setText(UserManager.getInstance().getCanteenLocation());
		schedule.setText(UserManager.getInstance().getCanteenSchedule());
		ImageUtil.getInstance().setImageResource(getActivity(), UserManager.getInstance().getCanteenPicture(), picture);
	}

	private void setMenuList() {
		if (listMenu != null) {
			if (menus != null && menus.size() > 0) {
				adapter.notifyDataSetChanged();
				listMenu.setVisibility(VISIBLE);
			} else {
				listMenu.setVisibility(GONE);
			}
		}
		cancelRefresh();
	}

	//region Firebase Call
	@IgnoreWhen(IgnoreWhen.State.VIEW_DESTROYED)
	void loadMenu() {
		String canteenKey = UserManager.getInstance().getCanteenKey();
		final DatabaseReference ref = FirebaseDB.getInstance().getDbReference(Constants.Menu.MENU);
		ref.orderByChild(Constants.Menu.CANTEEN_KEY).equalTo(canteenKey).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
					Menu menu = postSnapshot.getValue(Menu.class);
					menu.setKey(postSnapshot.getKey());
					menus.add(menu);
				}
				setMenuList();
				ref.removeEventListener(this);
			}

			@Override
			public void onCancelled(DatabaseError error) {
				if (isAdded()) Common.getInstance().showAlertToast(getActivity(), getString(R.string.default_failed));
				cancelRefresh();
				ref.removeEventListener(this);
			}
		});
	}
	//endregion

	//region Listeners
	OnListItemSelected menuListener = new OnListItemSelected() {
		@Override
		public void onClick(int position) {
			Intent intent = new Intent(getActivity(), LoginActivity_.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable(Constants.Menu.MENU, menus.get(position));
			intent.putExtras(bundle);
			startActivity(intent);
		}
	};
	//endregion
}