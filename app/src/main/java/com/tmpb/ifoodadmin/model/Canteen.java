package com.tmpb.ifoodadmin.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hans CK on 14-Feb-18.
 */

public class Canteen implements Parcelable {

	private String key;
	private String name;
	private String location;
	private String schedule;
	private String picture;
	private String userKey;

	public Canteen(String key, String name, String location, String schedule, String picture, String userKey) {
		this.key = key;
		this.name = name;
		this.location = location;
		this.schedule = schedule;
		this.picture = picture;
		this.userKey = userKey;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	//region Parcelable
	public static final Creator<Canteen> CREATOR = new Creator<Canteen>() {
		@Override
		public Canteen createFromParcel(Parcel in) {
			return new Canteen(in);
		}

		@Override
		public Canteen[] newArray(int size) {
			return new Canteen[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(key);
		dest.writeString(name);
		dest.writeString(location);
		dest.writeString(schedule);
		dest.writeString(picture);
		dest.writeString(userKey);
	}


	protected Canteen(Parcel in) {
		key = in.readString();
		name = in.readString();
		location = in.readString();
		schedule = in.readString();
		picture = in.readString();
		userKey = in.readString();
	}
	//endregion
}
