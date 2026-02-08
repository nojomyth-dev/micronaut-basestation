package de.riversroses.infra.error;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import java.util.List;

@Singleton
@Produces
@AllArgsConstructor
public class GlobalExceptionHandler implements ExceptionHandler<Exception, HttpResponse<?>> {
  @SuppressWarnings("rawtypes")
  @Override
  public HttpResponse<?> handle(HttpRequest request, Exception exception) {
    if (exception instanceof ConstraintViolationException cve) {
      List<ApiErrorResponse.FieldError> fields = cve.getConstraintViolations().stream()
          .map(this::toFieldError)
          .toList();
      return HttpResponse.badRequest(ApiErrorResponse.builder()
          .code(ErrorCode.VALIDATION_ERROR)
          .message("Validation failed")
          .fieldErrors(fields)
          .build());
    }
    if (exception instanceof DomainException de) {
      HttpStatus status = switch (de.getCode()) {
        case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
        case FORBIDDEN -> HttpStatus.FORBIDDEN;
        case NOT_FOUND -> HttpStatus.NOT_FOUND;
        case BAD_REQUEST, VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
        default -> HttpStatus.INTERNAL_SERVER_ERROR;
      };
      return HttpResponse.status(status).body(ApiErrorResponse.builder()
          .code(de.getCode())
          .message(de.getMessage())
          .build());
    }
    if (exception instanceof IllegalArgumentException iae) {
      return HttpResponse.badRequest(ApiErrorResponse.builder()
          .code(ErrorCode.BAD_REQUEST)
          .message(iae.getMessage())
          .build());
    }
    exception.printStackTrace();
    return HttpResponse.serverError(ApiErrorResponse.builder()
        .code(ErrorCode.INTERNAL_ERROR)
        .message("Unexpected server error")
        .build());
  }

  private ApiErrorResponse.FieldError toFieldError(ConstraintViolation<?> v) {
    String field = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
    return ApiErrorResponse.FieldError.builder()
        .field(field)
        .message(v.getMessage())
        .build();
  }
}
