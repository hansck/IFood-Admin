package com.tmpb.ifoodadmin.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.adapter.OrderItemsAdapter;
import com.tmpb.ifoodadmin.model.Menu;
import com.tmpb.ifoodadmin.model.Order;
import com.tmpb.ifoodadmin.model.OrderItem;
import com.tmpb.ifoodadmin.model.OrderStatus;
import com.tmpb.ifoodadmin.util.Common;
import com.tmpb.ifoodadmin.util.Constants;
import com.tmpb.ifoodadmin.util.FirebaseDB;
import com.tmpb.ifoodadmin.util.ItemDecoration;
import com.tmpb.ifoodadmin.util.manager.MenuManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.tmpb.ifoodadmin.model.OrderStatus.COMPLETED;
import static com.tmpb.ifoodadmin.model.OrderStatus.IN_PROGRESS;
import static com.tmpb.ifoodadmin.model.OrderStatus.OPEN;
import static com.tmpb.ifoodadmin.model.OrderStatus.REJECTED;

/**
 * Created by Hans CK on 19-Feb-18.
 */

@EActivity(R.layout.activity_order_detail)
public class DetailOrderActivity extends AppCompatActivity {

	private Order order;
	private List<OrderItem> items;
	private OrderItemsAdapter adapter;

	@ViewById
	RecyclerView listOrderItems;
	@ViewById
	EditText editReceiver;
	@ViewById
	EditText editDeliver;
	@ViewById
	EditText editNotes;
	@ViewById
	TextView price;
	@ViewById
	TextView fee;
	@ViewById
	TextView total;
	@ViewById
	TextView status;
	@ViewById
	Button btnCompleted;
	@ViewById
	Button btnInProgress;
	@ViewById
	Button btnRejected;
	@ViewById
	ProgressBar progressBar;
	@ViewById
	RelativeLayout statusContainer;
	@ViewById
	RelativeLayout buttonContainer;

	@AfterViews
	void initLayout() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle data = getIntent().getExtras();
		if (data != null) {
			order = data.getParcelable(Constants.Order.ORDER);
		}
		items = order.getItems();
		loadMenu();
	}

	@Click(R.id.btnInProgress)
	void onInProgress() {
		updateStatus(IN_PROGRESS);
	}

	@Click(R.id.btnRejected)
	void onRejected() {
		updateStatus(REJECTED);
	}

	@Click(R.id.btnCompleted)
	void onCompleted() {
		updateStatus(COMPLETED);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
	}

	//region Private methods
	private void setOrderItemList() {
		RecyclerView.LayoutManager newsLayoutManager = new LinearLayoutManager(this);
		listOrderItems.setLayoutManager(newsLayoutManager);
		listOrderItems.addItemDecoration(new ItemDecoration(1, Common.getInstance().dpToPx(this, 10), true));
		listOrderItems.setItemAnimator(new DefaultItemAnimator());
		adapter = new OrderItemsAdapter(this, items, this);
		listOrderItems.setAdapter(adapter);
	}

	private void setView() {
		int subTotal;
		subTotal = calculateSubTotal();
		editReceiver.setEnabled(false);
		editReceiver.setText(order.getReceiver());
		editDeliver.setEnabled(false);
		editDeliver.setText(order.getDeliverTo());
		editNotes.setEnabled(false);
		editNotes.setText(order.getNotes());
		statusContainer.setVisibility(VISIBLE);
		setStatus(OrderStatus.fromInt(order.getStatus()));
		int deliveryFee = (int) (subTotal * 0.05);
		price.setText(Common.getInstance().getFormattedPrice(this, subTotal));
		fee.setText(Common.getInstance().getFormattedPrice(this, deliveryFee));
		total.setText(Common.getInstance().getFormattedPrice(this, subTotal + deliveryFee));
	}

	private void setStatus(OrderStatus orderStatus) {
		status.setText(getString(R.string.order_status, OrderStatus.getTitleFromStatus(orderStatus)));
		int color = 0;
		switch (orderStatus) {
			case OPEN:
				color = ContextCompat.getColor(this, R.color.ic_open);
				break;
			case IN_PROGRESS:
				color = ContextCompat.getColor(this, R.color.ic_in_progress);
				break;
			case CANCELLED:
			case REJECTED:
				color = ContextCompat.getColor(this, R.color.ic_cancel);
				break;
			case COMPLETED:
				color = ContextCompat.getColor(this, R.color.ic_done);
				break;
		}
		statusContainer.setBackgroundColor(color);
		if (orderStatus == OPEN) {
			btnInProgress.setVisibility(VISIBLE);
			btnRejected.setVisibility(VISIBLE);
		} else if (orderStatus == IN_PROGRESS) {
			btnCompleted.setVisibility(VISIBLE);
			btnRejected.setVisibility(GONE);
			btnInProgress.setVisibility(GONE);
		} else {
			buttonContainer.setVisibility(GONE);
		}
	}

	private void setLoading(boolean loading) {
		progressBar.setVisibility(loading ? VISIBLE : GONE);
		btnCompleted.setVisibility(loading ? GONE : VISIBLE);
		btnInProgress.setVisibility(loading ? GONE : VISIBLE);
		btnRejected.setVisibility(loading ? GONE : VISIBLE);
	}

	private void goToLogin() {
		Intent intent = new Intent(this, LoginActivity_.class);
		startActivity(intent);
	}

	private int calculateSubTotal() {
		int subTotal = 0;
		if (MenuManager.getInstance().getMenus().size() > 0) {
			for (Menu temp : MenuManager.getInstance().getMenus()) {
				for (OrderItem item : items) {
					if (temp.getKey().equals(item.getMenuKey())) {
						subTotal += temp.getPrice() * item.getQuantity();
					}
				}
			}
		}
		return subTotal;
	}
	//endregion

	//region Firebase Calls
	private void updateStatus(OrderStatus orderStatus) {
		setLoading(true);
		DatabaseReference ref = FirebaseDB.getInstance().getDbReference(Constants.Order.ORDER);
		Map<String, Object> taskMap = new HashMap<>();
		taskMap.put("status", OrderStatus.toInt(orderStatus));
		ref.child(order.getKey()).updateChildren(taskMap);
		setLoading(false);
		setStatus(orderStatus);
	}

	private void loadMenu() {
		final DatabaseReference ref = FirebaseDB.getInstance().getDbReference(Constants.Menu.MENU);
		ref.orderByChild(Constants.Menu.CANTEEN_KEY).equalTo(order.getCanteenKey()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				List<Menu> menus = new ArrayList<>();
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
					Menu menu = postSnapshot.getValue(Menu.class);
					menu.setKey(postSnapshot.getKey());
					menus.add(menu);
				}
				MenuManager.getInstance().setMenus(menus);
				setOrderItemList();
				setView();
				ref.removeEventListener(this);
			}

			@Override
			public void onCancelled(DatabaseError error) {
				Common.getInstance().showAlertToast(DetailOrderActivity.this, getString(R.string.default_failed));
				ref.removeEventListener(this);
			}
		});
	}
	//endregion

	//region Listeners
	DialogInterface.OnClickListener loginListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int choice) {
			switch (choice) {
				case DialogInterface.BUTTON_POSITIVE:
					setLoading(true);
					goToLogin();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
			}
		}
	};
	//endregion
}
