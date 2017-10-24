package com.smesh.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimeTable {
	
	List<HashMap<String, String>> mList;

	public TimeTable() {
		mList = new ArrayList<HashMap<String, String>>();
	}
	
	public boolean Check(String _id, String _time) // key = id, value = time;
	{  
		if(mList.size() == 0)
		{
			AddList(_id, _time); // 그럼 해쉬맵에 넣어주세요			
			return false;
		} 
		HashMap<String, String> tHashMap = new HashMap<String, String>();
		tHashMap.put(_id, _time);
		 
		if(mList.contains(tHashMap)) // 이미 내가 가지고 있는 메시지에요.
		{ 
			return true;
		}
		else // 내 리스트에 지금 받는 메시지가 없어용
		{
			AddList(_id, _time); // 그럼 해쉬맵에 넣어주세요
			return false;
		} 
	}
	
	public void AddList(String _id, String _time)
	{
		HashMap<String, String> tHashMap = new HashMap<String, String>();
		tHashMap.put(_id, _time);
		
		mList.add(tHashMap); 
	} 
} // end of class
