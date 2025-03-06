package com.example.Flicktionary.domain.actor.repository;

import com.example.Flicktionary.domain.actor.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActorRepository extends JpaRepository<Actor, Long> {
}
