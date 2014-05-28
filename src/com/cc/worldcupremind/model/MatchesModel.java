package com.cc.worldcupremind.model;

public class MatchesModel {

	/* Match Info*/
	private int matchNo;
	private MatchStage matchStage;
	private String groupName;
	private int matchWeek;
	private String matchMonth;
	private String matchDay;
	private String matchHour;
	private String matchMin;
	private String matchTeam1;
	private String matchTeam2;
	private MatchStatus matchStatus;
	private Boolean isRemind;
	
	/* Match Result */
	private int team1Score;
	private int team2Score;
	
	
	public MatchesModel(int matchNo, MatchStage matchStage, String groupName, int matchWeek, String matchMonth, 
			String matchDay, String matchHour, String matchTeam1, String matchTeam2, 
			MatchStatus matchStatus, int team1Score, int team2Score, Boolean isRemind){
		
		this.matchNo = matchNo;
		this.matchStage = matchStage;
		this.groupName = groupName;
		this.matchWeek = matchWeek;
		this.matchMonth = matchMonth;
		this.matchDay = matchDay;
		this.matchHour = matchHour;
		this.matchMin = "00";
		this.matchTeam1 = matchTeam1;
		this.matchTeam2 = matchTeam2;
		this.matchStatus = matchStatus;
		this.team1Score = team1Score;
		this.team2Score = team2Score;
		this.isRemind = isRemind;
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
	 * @return the matchWeek
	 */
	public int getMatchWeek() {
		return matchWeek;
	}


	/**
	 * @return the matchMonth
	 */
	public String getMatchMonth() {
		return matchMonth;
	}


	/**
	 * @return the matchDay
	 */
	public String getMatchDay() {
		return matchDay;
	}


	/**
	 * @return the matchHour
	 */
	public String getMatchHour() {
		return matchHour;
	}


	/**
	 * @return the matchMin
	 */
	public String getMatchMin() {
		return matchMin;
	}


	/**
	 * @return the matchTeam1
	 */
	public String getMatchTeam1() {
		return matchTeam1;
	}


	/**
	 * @return the matchTeam2
	 */
	public String getMatchTeam2() {
		return matchTeam2;
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
	 * @param matchStage the matchStage to set
	 */
	public void setMatchStage(MatchStage matchStage) {
		this.matchStage = matchStage;
	}


	/**
	 * @param matchStatus the matchStatus to set
	 */
	public void setMatchStatus(MatchStatus matchStatus) {
		this.matchStatus = matchStatus;
	}


	/**
	 * @param team1Score the team1Score to set
	 */
	public void setTeam1Score(int team1Score) {
		this.team1Score = team1Score;
	}


	/**
	 * @param tema2Score the tema2Score to set
	 */
	public void setTeam2Score(int team2Score) {
		this.team2Score = team2Score;
	}
	
	/**
	 * @param isRemind the isRemind to set
	 */
	public void setIsRemind(Boolean isRemind) {
		this.isRemind = isRemind;
	}
	
}
