package com.cc.worldcupremind.model;

public class PlayerStatistics {
	
	public enum STATISTICS_TYPE{
		STATISTICS_GOAL,
		STATISTICS_ASSIST
	}
	
	private String playerName;
	private String playerTeamCode;
	private int count;
	private STATISTICS_TYPE type;
	
	public PlayerStatistics(String name, String team, int count, STATISTICS_TYPE type){
		
		this.playerName = name;
		this.playerTeamCode = team;
		this.count = count;
		this.type = type;
	}

	/**
	 * @return the playerName
	 */
	public String getPlayerName() {
		return playerName;
	}
	
	/**
	 * @return the playerTeamCode
	 */
	public String getPlayerTeamCode() {
		return playerTeamCode;
	}
	
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * @return the type
	 */
	public STATISTICS_TYPE getType() {
		return type;
	}
}
