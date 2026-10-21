package com.taskforge.repository;

import com.taskforge.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    // C1: JPQL with a JOIN on the many-to-many skills collection and LOWER
    // so the letter case of the skill name does not matter.
    @Query("SELECT t FROM TeamMember t JOIN t.skills s WHERE LOWER(s.name) = LOWER(:name)")
    List<TeamMember> findBySkillName(@Param("name") String name);
}