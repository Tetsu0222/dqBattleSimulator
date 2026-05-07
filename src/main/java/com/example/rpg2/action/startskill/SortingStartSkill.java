package com.example.rpg2.action.startskill;

public class SortingStartSkill {
	
	
	public static StartSkill sortingSkill( String startSkillName ) {
		
		StartSkill startSkill = null;
		
		if( startSkillName.equals( "自動防御" )) {
			startSkill = new AutoDefense();
		}
		
		return startSkill;
	}

}
