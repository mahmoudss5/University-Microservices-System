package UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpcomingEventResponse {

    private Long id;
    private String title;
    private String subtitle;
    private LocalDateTime eventDate;
    private String type;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
}
