package com.taskforge.repository;

import com.taskforge.entity.Workstation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkstationRepository extends JpaRepository<Workstation, UUID> {

    // C2: JPQL NOT EXISTS subquery, sorted by stationNumber
    @Query("SELECT l FROM Workstation l WHERE NOT EXISTS " +
           "(SELECT p FROM TeamMember p WHERE p.workstation = l) ORDER BY l.stationNumber")
    List<Workstation> findFreeWorkstations();

    // D4 used later
    Optional<Workstation> findByStationNumber(int stationNumber);
}
