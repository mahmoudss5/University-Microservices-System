package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Entities.User;

public interface UserService {
    String getUserName(Long id);

    void saveUser(User user);

}
