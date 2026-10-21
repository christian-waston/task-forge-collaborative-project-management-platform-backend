package com.taskforge.repository;

import com.taskforge.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {

    // C3: JPQL IS EMPTY on the collection, not a count
    @Query("SELECT s FROM Skill s WHERE s.teamMembers IS EMPTY")
    List<Skill> findUnusedSkills();
}
