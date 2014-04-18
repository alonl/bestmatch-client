package com.bestmatch.helpers;

import android.graphics.Bitmap;


public class User {

	String uid, name, sex, age, location;
	
	Bitmap profilePic;
	
	public Bitmap getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(Bitmap profilePic) {
		this.profilePic = profilePic;
	}

	public User(String uid, String name, String sex, String age, String location) {
		this.age = age;
		this.location = location;
		this.name = name;
		this.sex = sex;
		this.uid = uid;	
	}
	

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
}
