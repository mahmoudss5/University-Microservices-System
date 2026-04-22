package UnitSystem.demo.DataAccessLayer.Dto.UserDetails;

import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseResponse;
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
public class TeacherDetailsResponse {
    private Long teacherId;
    private String name;
    private String email;
    private String department;
    private BigDecimal salary;
    private Set<String> roles;
    private Set<CourseResponse> courses;
    private List<AnnouncementResponse> announcements;
    private List<UpcomingEventResponse> upcomingEvents;
    private int coursesCount;
    private Long numberOfStudents;
}
