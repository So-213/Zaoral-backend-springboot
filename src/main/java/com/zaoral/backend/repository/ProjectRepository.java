package com.zaoral.backend.repository;

import com.zaoral.backend.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, String> {

    @EntityGraph(attributePaths = {"projectMessage", "projectPicture"})
    List<Project> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Project> findByIdAndUserId(String id, String userId);

}
