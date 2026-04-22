package UnitSystem.demo.Controllers;


import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Controller
public class WebSocketController {

    private final NotificationService notificationService;
}
