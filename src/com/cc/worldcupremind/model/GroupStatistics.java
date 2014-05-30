package com.cc.worldcupremind.model;

public class GroupStatistics {

	private String teamCode;
	private String teamGroup;
	private int winCount;
	private int drawCount;
	private int loseCount;
	private int goalFor;
	private int goalAgainst;
	private int point;
	private int position;
	
	public GroupStatistics(String code, String group, int win, int draw,
			int lose, int gf, int ga, int pts, int pos){
		
		teamCode = code;
		teamGroup = group;
		winCount = win;
		drawCount = draw;
		loseCount = lose;
		goalFor = gf;
		goalAgainst = ga;
		point = pts;
		position = pos;
	}
	
	/**
	 * @return the teamCode
	 */
	public String getTeamCode() {
		return teamCode;
	}
	
	/**
	 * @return the teamGroup
	 */
	public String getTeamGroup() {
		return teamGroup;
	}
	/**
	 * @return the winCount
	 */
	public int getWinCount() {
		return winCount;
	}
	
	/**
	 * @return the drawCount
	 */
	public int getDrawCount() {
		return drawCount;
	}
	
	/**
	 * @return the loseCount
	 */
	public int getLoseCount() {
		return loseCount;
	}
	/**
	 * @return the goalFor
	 */
	public int getGoalFor() {
		return goalFor;
	}
	
	/**
	 * @return the goalAgainst
	 */
	public int getGoalAgainst() {
		return goalAgainst;
	}
	
	/**
	 * @return the point
	 */
	public int getPoint() {
		return point;
	}
	
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}
}
