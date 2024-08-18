package org.focus.logmeet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.focus.logmeet.common.exception.BaseException;
import org.focus.logmeet.common.response.BaseExceptionResponseStatus;
import org.focus.logmeet.controller.dto.project.ProjectCreateRequest;
import org.focus.logmeet.controller.dto.project.ProjectCreateResponse;
import org.focus.logmeet.controller.dto.project.ProjectUpdateResponse;
import org.focus.logmeet.controller.dto.project.UserProjectDto;
import org.focus.logmeet.domain.Project;
import org.focus.logmeet.domain.User;
import org.focus.logmeet.domain.UserProject;
import org.focus.logmeet.domain.enums.ProjectColor;
import org.focus.logmeet.repository.ProjectRepository;
import org.focus.logmeet.repository.UserProjectRepository;
import org.focus.logmeet.security.annotation.CurrentUser;
import org.focus.logmeet.security.aspect.CurrentUserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.focus.logmeet.common.response.BaseExceptionResponseStatus.*;
import static org.focus.logmeet.domain.enums.Role.LEADER;
import static org.focus.logmeet.domain.enums.Status.ACTIVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService { //TODO: 인증 과정 중 예외 발생 시 BaseException 으로 처리

    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;

    @Transactional
    @CurrentUser
    public ProjectCreateResponse createProject(ProjectCreateRequest request) {
        log.info("프로젝트 생성 시도: projectName={}, projectContent={}", request.getName(), request.getContent());
        User currentUser = CurrentUserHolder.get();

        if (currentUser == null) {
            throw new BaseException(USER_NOT_AUTHENTICATED);
        }

        Project project = Project.builder()
                .name(request.getName())
                .content(request.getContent())
                .status(ACTIVE)
                .build();

        projectRepository.save(project);

        UserProject userProject = UserProject.builder()
                .user(currentUser)
                .project(project)
                .role(LEADER)
                .color(ProjectColor.valueOf(request.getColor().name()))
                .build();
        userProjectRepository.save(userProject);

        log.info("프로젝트 생성 성공: projectId={}", project.getId());

        return new ProjectCreateResponse(project.getId());
    }

    @Transactional
    @CurrentUser
    public ProjectUpdateResponse getProject(Long projectId) {
        log.info("프로젝트 정보 조회: projectId={}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(PROJECT_NOT_FOUND));

        List<UserProjectDto> userProjectDtos = project.getUserProjects().stream()
                .map(up -> new UserProjectDto(
                        up.getId(),
                        up.getUser().getId(),
                        up.getUser().getName(),
                        up.getRole(),
                        up.getColor()))
                .collect(Collectors.toList());

        return new ProjectUpdateResponse(project.getId(), project.getName(), project.getContent(), userProjectDtos);
    }

    @Transactional
    @CurrentUser
    public void updateProject(Long projectId, String name, String content, ProjectColor color) {
        log.info("프로젝트 수정 시도: projectId={}, projectName={}", projectId, name);
        User currentUser = CurrentUserHolder.get();

        if (currentUser == null) {
            throw new BaseException(USER_NOT_AUTHENTICATED);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseExceptionResponseStatus.PROJECT_NOT_FOUND));

        UserProject userProject = userProjectRepository.findByUserAndProject(currentUser, project)
                .orElseThrow(() -> new BaseException(USER_NOT_IN_PROJECT));

        if (!userProject.getRole().equals(LEADER)) {
            throw new BaseException(BaseExceptionResponseStatus.USER_NOT_LEADER);
        }

        project.setName(name);
        project.setContent(content);
        userProject.setColor(color);

        projectRepository.save(project);
        userProjectRepository.save(userProject);
        log.info("프로젝트 수정 성공: projectId={}", projectId);
    }

    @Transactional
    @CurrentUser
    public void expelMember(Long projectId, Long userId) {
        log.info("참가자 추방 시도: projectId={}, userId={}", projectId, userId);
        User currentUser = CurrentUserHolder.get();

        if (currentUser == null) {
            throw new BaseException(USER_NOT_AUTHENTICATED);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseExceptionResponseStatus.PROJECT_NOT_FOUND));

        UserProject leaderProject = userProjectRepository.findByUserAndProject(currentUser, project)
                .orElseThrow(() -> new BaseException(USER_NOT_IN_PROJECT));

        if (!leaderProject.getRole().equals(LEADER)) {
            throw new BaseException(BaseExceptionResponseStatus.USER_NOT_LEADER);
        }

        UserProject memberToExpel = userProjectRepository.findByUserIdAndProject(userId, project)
                .orElseThrow(() -> new BaseException(USER_NOT_IN_PROJECT));

        userProjectRepository.delete(memberToExpel);

        log.info("참가자 추방 성공: projectId={}, userId={}", projectId, userId);
    }
}
