package com.example.rpg2.action.endskill;

public class SortingEndSkill {
	
	public static EndSkill sortingSkill( String endSkillName ) {
		
		EndSkill endSkill = null;
		
		switch( endSkillName ) {
		
			case "HP":
				endSkill = new AutoRecovery();
				
				break;
				
			case "MP":
				endSkill = new AutoRecoveryMp();
				
				break;
		}
		
		return endSkill;
	}

}
