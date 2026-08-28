package com.unisystem.api_gateway.service;

import com.unisystem.api_gateway.client.caller.AcademicServiceCaller;
import com.unisystem.api_gateway.client.caller.IamServiceCaller;
import com.unisystem.api_gateway.dto.DashboardDtos;
import com.unisystem.api_gateway.service.InternalRequestHeadersFactory.InternalRequestHeaders;
import com.unisystem.api_gateway.util.DashboardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardAggregationService {

    private final IamServiceCaller iamServiceCaller;
    private final AcademicServiceCaller academicServiceCaller;
    private final InternalRequestHeadersFactory headersFactory;

    public Mono<DashboardDtos.StudentDashboardResponseDto> getStudentDashboard(Long studentId, String token) {
        InternalRequestHeaders headers = headersFactory.create(token);
        Mono<DashboardDtos.StudentProfileDto> profile = blockingCall(() ->
                iamServiceCaller.getStudentDetails(
                        studentId, headers.authorization(), headers.userId(), headers.roles()))
                .doOnNext(p -> log.info("BFF Aggregation: Fetched student profile: {}", p));
        Mono<List<DashboardDtos.EnrolledCourseSummaryDto>> courses = blockingCall(() ->
                academicServiceCaller.getStudentCourses(
                        studentId, headers.authorization(), headers.userId(), headers.roles()))
                .onErrorResume(error -> {
                    log.warn("BFF: Failed to fetch enrolled courses for student {}: {}",
                            studentId, error.getMessage());
                    return Mono.just(List.of());
                });

        return Mono.zip(profile, courses)
                .map(result -> new DashboardDtos.StudentDashboardResponseDto(
                        DashboardUtils.mergeStudentCourses(result.getT1(), result.getT2())))
                .doOnNext(dto -> log.info("BFF Aggregation: Aggregated Student Dashboard: {}", dto));
    }

    public Mono<DashboardDtos.TeacherDashboardResponseDto> getTeacherDashboard(Long teacherId, String token) {
        InternalRequestHeaders headers = headersFactory.create(token);
        Mono<DashboardDtos.TeacherProfileDto> profile = blockingCall(() ->
                iamServiceCaller.getTeacherDetails(
                        teacherId, headers.authorization(), headers.userId(), headers.roles()))
                .doOnNext(p -> log.info("BFF Aggregation: Fetched teacher profile: {}", p));
        Mono<List<DashboardDtos.CourseDto>> courses = blockingCall(() ->
                academicServiceCaller.getTeacherCourses(
                        teacherId, headers.authorization(), headers.userId(), headers.roles()))
                .onErrorResume(error -> {
                    log.warn("BFF: Failed to fetch teacher courses for teacher {}: {}",
                            teacherId, error.getMessage());
                    return Mono.just(List.of());
                });

        return Mono.zip(profile, courses).map(result -> {
            List<DashboardDtos.TeacherCourseSummaryDto> mappedCourses =
                    DashboardUtils.mapTeacherCourses(result.getT2(), result.getT1());
            return new DashboardDtos.TeacherDashboardResponseDto(
                    result.getT1(), mappedCourses, mappedCourses.size());
        }).doOnNext(dto -> log.info("BFF Aggregation: Aggregated Teacher Dashboard: {}", dto));
    }

    public Mono<DashboardDtos.UserDashboardResponseDto> getCurrentUserDashboard(String token) {
        InternalRequestHeaders headers = headersFactory.create(token);
        return blockingCall(() -> iamServiceCaller.getCurrentUser(
                headers.authorization(), headers.userId(), headers.roles()))
                .doOnNext(u -> log.info("BFF Aggregation: Fetched current user: {}", u))
                .flatMap(user -> dashboardForRole(user, token))
                .doOnNext(dto -> log.info("BFF Aggregation: Final User Dashboard: {}", dto));
    }

    private Mono<DashboardDtos.UserDashboardResponseDto> dashboardForRole(
            DashboardDtos.UserDto user,
            String token) {
        String role = DashboardUtils.normalizeRole(user.role());
        return switch (role) {
            case "student" -> getStudentDashboard(user.id(), token)
                    .map(dashboard -> new DashboardDtos.UserDashboardResponseDto(
                            user, role, dashboard, null, null));
            case "teacher" -> getTeacherDashboard(user.id(), token)
                    .map(dashboard -> new DashboardDtos.UserDashboardResponseDto(
                            user, role, null, dashboard, null));
            default -> Mono.just(new DashboardDtos.UserDashboardResponseDto(
                    user,
                    role,
                    null,
                    null,
                    new DashboardDtos.AdminDashboardResponseDto(
                            user.id(), user.username(), user.email(), role)));
        };
    }

    private <T> Mono<T> blockingCall(Supplier<T> call) {
        return Mono.fromCallable(call::get).subscribeOn(Schedulers.boundedElastic());
    }
}
