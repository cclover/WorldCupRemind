package com.cc.worldcupremind.model;


public class MatchesModel {

	/* Match Info*/
	private int matchNo;
	private MatchStage matchStage;
	private String groupName;
	private MatchDate matchTime;
	private String team1Code;
	private String team2Code;
	private MatchStatus matchStatus;
	private Boolean isRemind;
	private String extInfo;
	private int extType;

	/* Match Result */
	private int team1Score;
	private int team2Score;
	
	public static final int EXT_TYPE_NEWS = 1;
	public static final int EXT_TYPE_VIDEO = 2;
	
	public MatchesModel(int matchNo, MatchStage matchStage, String groupName,
			MatchDate matchTime, String team1Code, String team2Code, 
			MatchStatus matchStatus, int team1Score, int team2Score, Boolean isRemind){
		
		this.matchNo = matchNo;
		this.matchStage = matchStage;
		this.groupName = groupName;
		this.matchTime = matchTime;
		this.team1Code = team1Code;
		this.team2Code = team2Code;
		this.matchStatus = matchStatus;
		this.team1Score = team1Score;
		this.team2Score = team2Score;
		this.isRemind = isRemind;
		this.extInfo = "";
		this.extType = EXT_TYPE_NEWS;
	}



	/**
	 * @return the matchNo
	 */
	public int getMatchNo() {
		return matchNo;
	}


	/**
	 * @return the matchStage
	 */
	public MatchStage getMatchStage() {
		return matchStage;
	}


	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}


	/**
	 * @return the matchTime
	 */
	public MatchDate getMatchTime() {
		return matchTime;
	}
	

	/**
	 * @return the team1Code
	 */
	public String getTeam1Code() {
		return team1Code;
	}


	/**
	 * @return the team2Code
	 */
	public String getTeam2Code() {
		return team2Code;
	}


	/**
	 * @return the matchStatus
	 */
	public MatchStatus getMatchStatus() {
		return matchStatus;
	}


	/**
	 * @return the team1Score
	 */
	public int getTeam1Score() {
		return team1Score;
	}


	/**
	 * @return the tema2Score
	 */
	public int getTeam2Score() {
		return team2Score;
	}

	/**
	 * @return the isRemind
	 */
	public Boolean getIsRemind() {
		return isRemind;
	}

	
	/**
	 * @param isRemind the isRemind to set
	 */
	public void setIsRemind(Boolean isRemind) {
		this.isRemind = isRemind;
	}


	/**
	 * @return the MatchNewsURL
	 */
	public String getExtInfo() {
		return extInfo;
	}
	

	/**
	 * @param extInfo the extInfo to set
	 */
	public void setExtInfo(String extInfo, int type) {
		this.extInfo = extInfo;
		this.extType = type;
	}
	
	
	/**
	 * @return the extType
	 */
	public int getExtType() {
		return extType;
	}
	
}
