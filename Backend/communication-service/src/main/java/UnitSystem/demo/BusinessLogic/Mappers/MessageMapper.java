package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.Message.MessageRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Message.MessageResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Message;
import UnitSystem.demo.DataAccessLayer.Repositories.CourseRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import UnitSystem.demo.ExcHandler.Entites.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SOLID — Single Responsibility:
 * Only responsible for mapping between Message entities and DTOs.
 */
@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public Message mapToMessageEntity(MessageRequest request) {
        userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("User snapshot", request.getSenderId()));
        courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course snapshot", request.getCourseId()));

        return Message.builder()
                .content(request.getContent())
                .senderId(request.getSenderId())
                .courseId(request.getCourseId())
                .build();
    }

    public MessageResponse mapToMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .courseId(message.getCourseId())
                .courseName(courseRepository.findById(message.getCourseId())
                        .map(course -> course.getCourseName()).orElse(null))
                .senderId(message.getSenderId())
                .senderName(userRepository.findById(message.getSenderId())
                        .map(user -> user.getUserName()).orElse(null))
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
