package com.tmpb.ifoodadmin.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.adapter.CanteenAdapter;
import com.tmpb.ifoodadmin.model.Canteen;
import com.tmpb.ifoodadmin.util.Common;
import com.tmpb.ifoodadmin.util.ConnectivityUtil;
import com.tmpb.ifoodadmin.util.Constants;
import com.tmpb.ifoodadmin.util.FirebaseDB;
import com.tmpb.ifoodadmin.util.ItemDecoration;
import com.tmpb.ifoodadmin.util.OnListItemSelected;
import com.tmpb.ifoodadmin.util.manager.CanteenManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.IgnoreWhen;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@EFragment(R.layout.fragment_canteen)
public class CanteenFragment extends BaseFragment {

	private List<Canteen> canteens = new ArrayList<>();
	private CanteenAdapter adapter;

	@ViewById
	RecyclerView listCanteen;
	@ViewById
	SwipeRefreshLayout swipeRefreshLayout;

	@AfterViews
	void initLayout() {
		((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.canteen));
		((AppCompatActivity) getActivity()).getSupportActionBar().show();

		RecyclerView.LayoutManager newsLayoutManager = new LinearLayoutManager(getActivity());
		listCanteen.setLayoutManager(newsLayoutManager);
		listCanteen.addItemDecoration(new ItemDecoration(1, Common.getInstance().dpToPx(getActivity(), 10), true));
		listCanteen.setItemAnimator(new DefaultItemAnimator());

		adapter = new CanteenAdapter(getActivity(), canteens, canteenListener);
		listCanteen.setAdapter(adapter);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setRefreshing(false);
			swipeRefreshLayout.destroyDrawingCache();
			swipeRefreshLayout.clearAnimation();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (ConnectivityUtil.getInstance().isNetworkConnected()) {
			swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
			swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
			onRefreshListener.onRefresh();
		}
	}

	@IgnoreWhen(IgnoreWhen.State.VIEW_DESTROYED)
	void cancelRefresh() {
		if (swipeRefreshLayout.isRefreshing()) {
			swipeRefreshLayout.setRefreshing(false);
		}
	}

	private void setCanteenList() {
		if (listCanteen != null) {
			if (canteens != null && canteens.size() > 0) {
				adapter.notifyDataSetChanged();
				listCanteen.setVisibility(VISIBLE);
			} else {
				listCanteen.setVisibility(GONE);
			}
		}
		CanteenManager.getInstance().setCanteens(canteens);
		cancelRefresh();
	}

	//region Firebase Call
	@IgnoreWhen(IgnoreWhen.State.VIEW_DESTROYED)
	void loadCanteen() {
		canteens.clear();
		final DatabaseReference ref = FirebaseDB.getInstance().getDbReference(Constants.Canteen.CANTEEN);
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				canteens.clear();
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
					Canteen canteen = postSnapshot.getValue(Canteen.class);
					canteen.setKey(postSnapshot.getKey());
					canteens.add(canteen);
				}
				setCanteenList();
			}

			@Override
			public void onCancelled(DatabaseError error) {
				if (isAdded()) Common.getInstance().showAlertToast(getActivity(), getString(R.string.default_failed));
				cancelRefresh();
			}
		});
	}
	//endregion

	//region Listeners
	SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
		@Override
		public void onRefresh() {
			swipeRefreshLayout.setRefreshing(true);
			loadCanteen();
		}
	};

	OnListItemSelected canteenListener = new OnListItemSelected() {
		@Override
		public void onClick(int position) {
			ManageCanteenFragment_ fragment = new ManageCanteenFragment_();
			Bundle bundle = new Bundle();
			bundle.putParcelable(Constants.Canteen.CANTEEN, canteens.get(position));
			fragment.setArguments(bundle);
			navigateFragment(R.id.contentFrame, fragment);
		}
	};
	//endregion
}