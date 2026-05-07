package com.example.rpg2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.rpg2.entity.Monster;

@Repository
public interface MonsterRepository extends JpaRepository<Monster, Integer>{

}
