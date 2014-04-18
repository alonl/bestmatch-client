package com.bestmatch.helpers;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Data {
	public static final String SERVER_ADDRESS = "http://10.10.8.196:3001/";
	public static String currentUserID = "";
	public static String currentFBToken = "";
	public static int heightBackUp = 0;
	public static int widthBackUp = 0;
	public static int widthRsultBackUp = 0;
	public static Bitmap myPic = null;
	public static Match currentMatch = null;
	public static ArrayList<Match> MainData = null;
	public static ArrayList<Match> ResultsData = null;
	
}
