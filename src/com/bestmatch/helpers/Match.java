package com.bestmatch.helpers;


import java.util.ArrayList;

public class Match {

	String matchID;
	User user1, user2;
	String matchRating;
	ArrayList<User> recommenders;
	
	public Match(String matchID, User user1, User user2) {
		this.matchID = matchID;
		this.user1 = user1;
		this.user2 = user2;
		
		recommenders = new ArrayList<User>();
	}
	
	public String getMatchID() {
		return matchID;
	}
	public void setMatchID(String matchID) {
		this.matchID = matchID;
	}

	public User getUser1() {
		return user1;
	}

	public void setUser1(User user1) {
		this.user1 = user1;
	}

	public User getUser2() {
		return user2;
	}

	public void setUser2(User user2) {
		this.user2 = user2;
	}

	public String getMatchRating() {
		return matchRating;
	}

	public void setMatchRating(String matchRating) {
		this.matchRating = matchRating;
	}

	public ArrayList<User> getRecommenders() {
		return recommenders;
	}

	public void setRecommenders(ArrayList<User> recommenders) {
		this.recommenders = recommenders;
	}
	
}
