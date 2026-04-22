package UnitSystem.demo.DataAccessLayer.Dto.Announcement;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private Long courseId;
    private LocalDateTime createdDate;
}
