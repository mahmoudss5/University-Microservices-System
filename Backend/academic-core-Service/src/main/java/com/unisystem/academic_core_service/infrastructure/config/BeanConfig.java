package com.unisystem.academic_core_service.infrastructure.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.unisystem.academic_core_service.application.port.in.CreateAnnouncementUseCase;
import com.unisystem.academic_core_service.application.port.in.CreateCourseUseCase;
import com.unisystem.academic_core_service.application.port.in.EnrollStudentUseCase;
import com.unisystem.academic_core_service.application.port.in.GetAnnouncementsQuery;
import com.unisystem.academic_core_service.application.port.in.GetFeedBackQuery;
import com.unisystem.academic_core_service.application.port.in.GetCoursesQuery;
import com.unisystem.academic_core_service.application.port.in.GetEnrollmentQuery;
import com.unisystem.academic_core_service.application.port.in.SubmitFeedbackUseCase;
import com.unisystem.academic_core_service.application.port.in.SynchronizeUserSnapshotUseCase;
import com.unisystem.academic_core_service.application.port.in.ManageCoursePrerequisitesUseCase;
import com.unisystem.academic_core_service.application.port.in.GetCoursePrerequisitesQuery;
import com.unisystem.academic_core_service.application.port.in.PublishPendingOutboxEventsUseCase;
import com.unisystem.academic_core_service.application.port.out.AnnouncementRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EnrollmentRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.application.port.out.FeedbackRepsitoryPort;
import com.unisystem.academic_core_service.application.port.out.UserSnapshotRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.CoursePrerequisiteRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.OutboxRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.MessageBrokerPort;
import com.unisystem.academic_core_service.application.services.CreateAnnouncementService;
import com.unisystem.academic_core_service.application.services.CreateCourseService;
import com.unisystem.academic_core_service.application.services.EnrollStudentService;
import com.unisystem.academic_core_service.application.services.GetAnnouncementsService;
import com.unisystem.academic_core_service.application.services.GetCoursesService;
import com.unisystem.academic_core_service.application.services.GetEnrollmentsService;
import com.unisystem.academic_core_service.application.services.GetFeedbackService;
import com.unisystem.academic_core_service.application.services.SubmitFeedbackService;
import com.unisystem.academic_core_service.application.services.SynchronizeUserSnapshotService;
import com.unisystem.academic_core_service.application.services.ManageCoursePrerequisitesService;
import com.unisystem.academic_core_service.application.services.PublishPendingOutboxEventsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfig {

    @Bean
    public CreateCourseUseCase createCourseUseCase(CourseRepositoryPort courseRepository, EventPublisherPort eventPublisherPort,
                                                    UserSnapshotRepositoryPort users) {
        return new CreateCourseService(courseRepository, eventPublisherPort, users);
    }

    @Bean
    public GetCoursesQuery getCoursesQuery(CourseRepositoryPort courseRepository) {
        return new GetCoursesService(courseRepository);
    }

    @Bean
    public EnrollStudentUseCase enrollStudentUseCase(CourseRepositoryPort courseRepository, EnrollmentRepositoryPort enrollmentRepository,
                                                      EventPublisherPort eventPublisher, UserSnapshotRepositoryPort users,
                                                      CoursePrerequisiteRepositoryPort prerequisites) {
        return new EnrollStudentService(courseRepository, enrollmentRepository, eventPublisher, users, prerequisites);
    }

    @Bean
    public GetEnrollmentQuery getEnrollmentQuery(EnrollmentRepositoryPort enrollmentRepositoryPort) {
        return new GetEnrollmentsService(enrollmentRepositoryPort);
    }

    @Bean
    public SubmitFeedbackUseCase submitFeedbackUseCase(FeedbackRepsitoryPort feedbackRepsitoryPort,
                                                        UserSnapshotRepositoryPort users, EventPublisherPort events) {
        return new SubmitFeedbackService(feedbackRepsitoryPort, users, events);
    }

    @Bean
    public GetFeedBackQuery getFeedBackQuery(FeedbackRepsitoryPort feedbackRepsitoryPort) {
        return new GetFeedbackService(feedbackRepsitoryPort);
    }

    @Bean
    public CreateAnnouncementUseCase createAnnouncementUseCase(
            AnnouncementRepositoryPort announcementRepositoryPort,
            CourseRepositoryPort courseRepositoryPort,
            EventPublisherPort eventPublisherPort
    ) {
        return new CreateAnnouncementService(announcementRepositoryPort, courseRepositoryPort, eventPublisherPort);
    }

    @Bean
    public GetAnnouncementsQuery getAnnouncementsQuery(
            AnnouncementRepositoryPort announcementRepositoryPort,
            EnrollmentRepositoryPort enrollmentRepositoryPort,
            CourseRepositoryPort courseRepositoryPort) {
        return new GetAnnouncementsService(
                announcementRepositoryPort,
                enrollmentRepositoryPort,
                courseRepositoryPort);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SynchronizeUserSnapshotUseCase synchronizeUserSnapshotUseCase(UserSnapshotRepositoryPort repository) {
        return new SynchronizeUserSnapshotService(repository);
    }

    @Bean
    public ManageCoursePrerequisitesService coursePrerequisitesService(CourseRepositoryPort courses,
                                                                        CoursePrerequisiteRepositoryPort prerequisites) {
        return new ManageCoursePrerequisitesService(courses, prerequisites);
    }

    @Bean
    public PublishPendingOutboxEventsUseCase publishPendingOutboxEventsUseCase(
            OutboxRepositoryPort repository, MessageBrokerPort broker,
            @Value("${outbox.relay.batch-size:50}") int batchSize,
            @Value("${outbox.relay.max-retries:5}") int maxRetries,
            @Value("${outbox.relay.claim-timeout-seconds:60}") long claimTimeoutSeconds) {
        return new PublishPendingOutboxEventsService(repository, broker, batchSize, maxRetries, claimTimeoutSeconds);
    }

}
