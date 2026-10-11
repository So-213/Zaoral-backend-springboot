package com.zaoral.backend.controller;

import com.zaoral.backend.dto.CreateProjectRequest;
import com.zaoral.backend.dto.MessageResponse;
import com.zaoral.backend.dto.ProjectResponse;
import com.zaoral.backend.exception.FailureMessage;
import com.zaoral.backend.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @FailureMessage("プロジェクトの取得に失敗しました")
    public List<ProjectResponse> list(@RequestHeader("X-User-Id") String userId) {
        return projectService.list(userId).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @FailureMessage("プロジェクトの作成に失敗しました")
    public ProjectResponse create(@RequestHeader("X-User-Id") String userId,
                                  @RequestBody CreateProjectRequest request) {
        return ProjectResponse.from(projectService.create(userId, request));
    }

    @DeleteMapping("/{id}")
    @FailureMessage("プロジェクトの削除に失敗しました")
    public MessageResponse delete(@RequestHeader("X-User-Id") String userId,
                                  @PathVariable("id") String id) {
        projectService.delete(id, userId);
        return new MessageResponse("プロジェクトが正常に削除されました");
    }

}
