package com.tmpb.ifoodadmin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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
import com.tmpb.ifoodadmin.util.ItemDecoration;
import com.tmpb.ifoodadmin.util.OnListItemSelected;

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
//	@ViewById
//	SwipeRefreshLayout swipeRefreshLayout;
//	@ViewById
//	RelativeLayout noItemLayout;

	@AfterViews
	void initLayout() {
		((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.home));
		((AppCompatActivity) getActivity()).getSupportActionBar().show();
		RecyclerView.LayoutManager newsLayoutManager = new LinearLayoutManager(getActivity());
		listMenu.setLayoutManager(newsLayoutManager);
		listMenu.addItemDecoration(new ItemDecoration(1, Common.getInstance().dpToPx(getActivity(), 10), true));
		listMenu.setItemAnimator(new DefaultItemAnimator());

		adapter = new MenuAdapter(getActivity(), menus, menuListener);
		listMenu.setAdapter(adapter);

		if (ConnectivityUtil.getInstance().isNetworkConnected()) {
//			swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
//			swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
//			onRefreshListener.onRefresh();
			loadMenu();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
//		if (swipeRefreshLayout != null) {
//			swipeRefreshLayout.setRefreshing(false);
//			swipeRefreshLayout.destroyDrawingCache();
//			swipeRefreshLayout.clearAnimation();
//		}
	}

	@Override
	public void onResume() {
		super.onResume();
//		swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
//		swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
	}

	@IgnoreWhen(IgnoreWhen.State.VIEW_DESTROYED)
	void cancelRefresh() {
//		if (swipeRefreshLayout.isRefreshing()) {
//			swipeRefreshLayout.setRefreshing(false);
//		}
	}

	private void setCanteenList() {
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
		final DatabaseReference ref = FirebaseDB.getInstance().getDbReference(Constants.Menu.MENU);
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
					Menu menu = postSnapshot.getValue(Menu.class);
					menu.setKey(postSnapshot.getKey());
					menus.add(menu);
				}
				setCanteenList();
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
//	SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
//		@Override
//		public void onRefresh() {
//			swipeRefreshLayout.setRefreshing(true);
//			loadMenu();
//		}
//	};

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