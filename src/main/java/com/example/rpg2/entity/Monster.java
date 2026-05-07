package com.example.rpg2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table( name = "monster" )
@Data
public class Monster {
	
	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY )
	@Column( name = "id" )
	private Integer id;

	@Column( name = "name" )
	private String name;
		    
	@Column( name = "hp" )
	private Integer hp;
		    
	@Column( name = "mp" )
	private Integer mp;
		    
	@Column( name = "atk" )
	private Integer atk;
		    
	@Column( name = "def" )
	private Integer def;
	
	@Column( name = "spe" )
	private Integer spe;
	
    @Column( name = "pattern" )
    private String pattern;
    
    @Column( name = "actions" )
    private String actions;
    
	@Column( name = "resistance" )
	private Integer resistance;


}
