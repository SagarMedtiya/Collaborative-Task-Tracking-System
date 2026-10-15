package com.airtribe.tasktrackingsystem.repository;

import com.airtribe.tasktrackingsystem.model.Team;
import com.airtribe.tasktrackingsystem.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Page<Team> findByMembersId(Long userId, Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.createdBy.id = :userId OR EXISTS (SELECT 1 FROM t.members m WHERE m.id = :userId)")
    Page<Team> findTeamsByUserAccess(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t.members FROM Team t WHERE t.id = :teamId")
    List<Users> findTeamMembers(@Param("teamId") Long teamId);

    boolean existsByName(String name);
}












