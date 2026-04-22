package UnitSystem.demo.ExcHandler.Entites;

import java.time.LocalDateTime;

public record ErrorResponse(
        int statusCode,
        String error,
        String message,
        LocalDateTime timestamp
) {}