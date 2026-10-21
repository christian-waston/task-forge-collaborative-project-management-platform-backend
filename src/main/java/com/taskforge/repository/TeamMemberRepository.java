package com.taskforge.repository;

import com.taskforge.entity.TeamMember;
import com.taskforge.entity.Workstation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    @Query("SELECT t FROM TeamMember t JOIN t.skills s WHERE LOWER(s.name) = LOWER(:name)")
    List<TeamMember> findBySkillName(@Param("name") String name);

    boolean existsByWorkstationAndIdNot(Workstation workstation, UUID id);
}