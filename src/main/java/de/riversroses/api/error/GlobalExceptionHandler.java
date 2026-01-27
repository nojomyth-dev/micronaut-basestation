package de.riversroses.api.error;

import java.util.List;

import de.riversroses.api.dto.error.ApiErrorResponse;
import de.riversroses.api.dto.error.ErrorCode;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@Singleton
@Produces
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

    // Things like "unknown shipId", "bad parameter", etc.
    if (exception instanceof IllegalArgumentException iae) {
      return HttpResponse.badRequest(ApiErrorResponse.builder()
          .code(ErrorCode.BAD_REQUEST)
          .message(iae.getMessage())
          .build());
    }

    // If throw HttpStatusException explicitly in code
    if (exception instanceof HttpStatusException hse) {
      ErrorCode code = hse.getStatus() == HttpStatus.NOT_FOUND ? ErrorCode.NOT_FOUND : ErrorCode.BAD_REQUEST;
      return HttpResponse.status(hse.getStatus()).body(ApiErrorResponse.builder()
          .code(code)
          .message(hse.getMessage())
          .build());
    }

    // Fallback
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
