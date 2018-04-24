package com.tmpb.ifoodadmin.fragment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.adapter.MenuAdapter;
import com.tmpb.ifoodadmin.model.Menu;
import com.tmpb.ifoodadmin.util.Common;
import com.tmpb.ifoodadmin.util.ConnectivityUtil;
import com.tmpb.ifoodadmin.util.Constants;
import com.tmpb.ifoodadmin.util.FirebaseDB;
import com.tmpb.ifoodadmin.util.ImageUtil;
import com.tmpb.ifoodadmin.util.ItemDecoration;
import com.tmpb.ifoodadmin.util.OnListItemSelected;
import com.tmpb.ifoodadmin.util.manager.UserManager;

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

	@AfterViews
	void initLayout() {
		((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.menu));

		setView();
		RecyclerView.LayoutManager newsLayoutManager = new LinearLayoutManager(getActivity());
		listMenu.setLayoutManager(newsLayoutManager);
		listMenu.addItemDecoration(new ItemDecoration(1, Common.getInstance().dpToPx(getActivity(), 10), true));
		listMenu.setItemAnimator(new DefaultItemAnimator());

		adapter = new MenuAdapter(getActivity(), menus, menuListener);
		listMenu.setAdapter(adapter);
	}

	@Override
	public void onResume() {
		if (ConnectivityUtil.getInstance().isNetworkConnected()) {
			loadMenu();
		}
		super.onResume();
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
	}

	//region Firebase Call
	@IgnoreWhen(IgnoreWhen.State.VIEW_DESTROYED)
	void loadMenu() {
		menus.clear();
		String canteenKey = UserManager.getInstance().getCanteenKey();
		final DatabaseReference ref = FirebaseDB.getInstance().getDbReference(Constants.Menu.MENU);
		ref.orderByChild(Constants.Menu.CANTEEN_KEY).equalTo(canteenKey).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				menus.clear();
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
					Menu menu = postSnapshot.getValue(Menu.class);
					menu.setKey(postSnapshot.getKey());
					menus.add(menu);
				}
				setMenuList();
			}

			@Override
			public void onCancelled(DatabaseError error) {
				if (isAdded()) Common.getInstance().showAlertToast(getActivity(), getString(R.string.default_failed));
			}
		});
	}
	//endregion

	//region Listeners
	OnListItemSelected menuListener = new OnListItemSelected() {
		@Override
		public void onClick(int position) {
			ManageMenuFragment_ fragment = new ManageMenuFragment_();
			Bundle bundle = new Bundle();
			bundle.putParcelable(Constants.Menu.MENU, menus.get(position));
			fragment.setArguments(bundle);
			navigateFragment(R.id.contentFrame, fragment);
		}
	};
	//endregion
}