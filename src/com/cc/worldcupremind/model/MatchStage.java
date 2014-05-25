package com.cc.worldcupremind.model;

public enum MatchStage {

	STAGE_NONE(-1),
	STAGE_GROUP(0),
	STAGE_ROUND_OF_16(1),
	STAGE_QUARTER_FINAL(2),
	STAGE_SEMI_FINAL(3),
	STAGE_LOSER_FINAL(4),
	STAGE_FINAL(5);
	
	private int value = -1;

    private MatchStage(int value) {
        this.value = value;
    }
    
    public static MatchStage valueOf(int value) {
    	switch(value)
    	{
    		case 0:
    			return STAGE_GROUP;
        	case 1:
        		return STAGE_ROUND_OF_16;
        	case 2:
        		return STAGE_QUARTER_FINAL;
        	case 3:
        		return STAGE_SEMI_FINAL;
        	case 4:
        		return STAGE_LOSER_FINAL;
        	case 5:
        		return STAGE_FINAL;
           	default:
            	return STAGE_NONE;
        }
    }
}
