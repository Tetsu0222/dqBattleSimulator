package com.example.rpg2.status;

public interface Status {
	
	public String statusMessageBefore();
	
	public Integer actionStatusBefore();
	
	public String statusMessageAfter();
	
	public Integer actionStatusAfter();
	
	public String recoverymessage();

	public Integer countDown();
	
	public String getName();
	
	public Integer getCount();

}
