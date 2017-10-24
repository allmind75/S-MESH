package com.smesh.network;
  
//검색된 장비의 이름, 맥 주소, rssi값이 들어가는 클래스
public class SearchStruct implements Comparable<SearchStruct> {

	public String name;
	public String address;
	public int rssi;
	
	public SearchStruct(String _name, String _add, int _rssi)
	{
		name = _name;
		address = _add;
		rssi = _rssi;
	}  
	public SearchStruct(String _add)
	{
		address = _add;
	}  
	
	public String get_name()
	{
		return name;
	}
	
	public String get_address() 
	{
		return address;
	}
	
	public int get_rssi()
	{
		return rssi;
	} 

	@Override
	public int compareTo(SearchStruct another) { // 비교하여 정렬한다. 
		return this.rssi > another.rssi ? -1 : this.rssi < another.rssi ? 1 : 0;
	}
}