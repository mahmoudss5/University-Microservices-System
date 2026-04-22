package UnitSystem.demo.DataAccessLayer.Dto.UserDetails;

import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import UnitSystem.demo.DataAccessLayer.Dto.EnrolledCourse.EnrolledCourseResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentDetailsResponse {

    private Long id;
    private String email;
    private String username;
    private BigDecimal gpa;
    private Long totalCredits;
    private Set<EnrolledCourseResponse> enrolledCourses;
    private int enrolledCoursesCount;
    private int enrollmentYear;
    private String academicStanding;
    private List<AnnouncementResponse> announcements;
    private List<UpcomingEventResponse> upcomingEvents;
}
