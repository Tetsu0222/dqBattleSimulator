package com.example.rpg2.status;


public class Dead implements Status{
	
	private String name = "死亡";
	private Integer count = 1;
	
	public int hashCode() {
		return name.hashCode();
	}
	
	public boolean equals( Object obj ) {
		
		return this.hashCode() == obj.hashCode();
	}
	
	@Override
	public Integer actionStatusBefore() {
		//no
		
		return 1;
	}
	
	@Override
	public Integer actionStatusAfter() {
		//no

		return 0;
	}
	
	@Override
	public String statusMessageAfter() {
		//no
		
		return "no";
	}
	
	@Override
	public String statusMessageBefore() {
	//no
	
	return "no";
	}
	
	@Override
	public Integer countDown() {
		//永続効果
		return count;
	}
	
	@Override
	public String recoverymessage() {
		
		return "no";
	}
	
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	

}
