package com.cc.worldcupremind.model;

public enum MatchStatus {
	
	MATCH_STATUS_WAIT_START(0),
	MATCH_STATUS_PLAYING(1),
	MATCH_STATUS_OVER(2);
	
	private int value = 0;

    private MatchStatus(int value) {
        this.value = value;
    }
    
    public static MatchStatus valueOf(int value) {
    	switch(value)
    	{
    		case 0:
    			return MATCH_STATUS_WAIT_START;
        	case 1:
        		return MATCH_STATUS_PLAYING;
        	case 2:
        		return MATCH_STATUS_OVER;
           	default:
            	return MATCH_STATUS_WAIT_START;
        }
    }
}
