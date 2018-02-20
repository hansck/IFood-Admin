package com.tmpb.ifoodadmin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.activity.DetailOrderActivity;
import com.tmpb.ifoodadmin.model.Menu;
import com.tmpb.ifoodadmin.model.OrderItem;
import com.tmpb.ifoodadmin.util.Common;
import com.tmpb.ifoodadmin.util.ImageUtil;
import com.tmpb.ifoodadmin.util.manager.MenuManager;

import java.util.List;

/**
 * Created by Hans CK on 13-Jan-17.
 */

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> {

	private Context context;
	private List<OrderItem> list;
	private DetailOrderActivity activity;

	public class OrderItemViewHolder extends RecyclerView.ViewHolder {
		public TextView name, price, count;
		public ImageView image;

		public OrderItemViewHolder(View view) {
			super(view);
			name = (TextView) view.findViewById(R.id.name);
			price = (TextView) view.findViewById(R.id.price);
			count = (TextView) view.findViewById(R.id.count);
			image = (ImageView) view.findViewById(R.id.image);
		}
	}

	public OrderItemsAdapter(Context context, List<OrderItem> list, DetailOrderActivity activity) {
		this.context = context;
		this.list = list;
		this.activity = activity;
	}

	@Override
	public OrderItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
		return new OrderItemViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final OrderItemViewHolder holder, int position) {
		final OrderItem item = list.get(position);
		Menu menu = new Menu();
		if (MenuManager.getInstance().getMenus().size() > 0) {
			for (Menu temp : MenuManager.getInstance().getMenus()) {
				if (temp.getKey().equals(item.getMenuKey())) {
					menu = temp;
				}
			}
		}
		holder.name.setText(menu.getName());
		holder.price.setText(Common.getInstance().getFormattedPrice(context, menu.getPrice()));
		holder.count.setText(String.valueOf(item.getQuantity()));
		ImageUtil.getInstance().setImageResource(context, menu.getPicture(), holder.image);
	}

	@Override
	public int getItemCount() {
		return list.size();
	}
}