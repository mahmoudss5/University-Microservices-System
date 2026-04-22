package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Dto.Message.MessageRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Message.MessageResponse;

import java.util.List;

public interface MessageService {

    void createMessage(MessageRequest messageRequest);
    List<MessageResponse> getMessagesByCourseId(Long courseId);
    List<MessageResponse> getMessagesBySenderId(Long senderId);
    long countMessagesByCourseId(Long courseId);
    void deleteMessageById(Long messageId);
}
