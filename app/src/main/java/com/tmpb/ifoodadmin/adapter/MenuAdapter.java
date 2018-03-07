package com.tmpb.ifoodadmin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tmpb.ifoodadmin.R;
import com.tmpb.ifoodadmin.model.Menu;
import com.tmpb.ifoodadmin.util.Common;
import com.tmpb.ifoodadmin.util.ImageUtil;
import com.tmpb.ifoodadmin.util.OnListItemSelected;

import java.util.List;

import static android.view.View.GONE;

/**
 * Created by Hans CK on 13-Jan-17.
 */

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

	private Context context;
	private List<Menu> list;
	private OnListItemSelected listener;

	public class MenuViewHolder extends RecyclerView.ViewHolder {
		public TextView name, price, count;
		public ImageView image;
		public RelativeLayout container;

		public MenuViewHolder(View view) {
			super(view);
			name = (TextView) view.findViewById(R.id.name);
			price = (TextView) view.findViewById(R.id.price);
			image = (ImageView) view.findViewById(R.id.image);
			count = (TextView) view.findViewById(R.id.count);
			container = (RelativeLayout) view.findViewById(R.id.container);
		}
	}

	public MenuAdapter(Context context, List<Menu> list, OnListItemSelected listener) {
		this.context = context;
		this.list = list;
		this.listener = listener;
	}

	@Override
	public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
		return new MenuViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final MenuViewHolder holder, int position) {
		Menu menu = list.get(position);
		holder.name.setText(menu.getName());
		holder.price.setText(Common.getInstance().getFormattedPrice(context, menu.getPrice()));
		ImageUtil.getInstance().setImageResource(context, menu.getPicture(), holder.image);
		holder.container.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(holder.getAdapterPosition());
			}
		});
		holder.count.setVisibility(GONE);
	}

	@Override
	public int getItemCount() {
		return list.size();
	}
}