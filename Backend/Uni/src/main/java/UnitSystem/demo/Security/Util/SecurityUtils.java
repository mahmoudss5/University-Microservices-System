package UnitSystem.demo.Security.Util;
import UnitSystem.demo.Security.User.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}


    public static Long getCurrentUserId() {
      Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
      if (authentication!=null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.getUserId();
      }
        return null;
    }


    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "Anonymous";
    }
}