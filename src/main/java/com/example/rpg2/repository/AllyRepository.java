package com.example.rpg2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.rpg2.entity.Ally;

@Repository
public interface AllyRepository extends JpaRepository<Ally, Integer>{

}
