package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.CourseService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UserService;
import UnitSystem.demo.DataAccessLayer.Dto.Message.MessageRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Message.MessageResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final UserService userService;
    private final CourseService courseService;

    public Message mapToMessageEntity(MessageRequest messageRequest) {
        var user = userService.findUserById(messageRequest.getSenderId());
        var course = courseService.getCourseEntityById(messageRequest.getCourseId());

        return Message.builder()
                .content(messageRequest.getContent())
                .sender(user)
                .course(course)
                .build();
    }

    public MessageResponse mapToMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .courseId(message.getCourse().getId())
                .courseName(message.getCourse().getName())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getUserName())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
