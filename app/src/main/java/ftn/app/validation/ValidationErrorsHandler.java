package ftn.app.validation;

import ftn.app.model.ResponseMessage;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;

@RestControllerAdvice
public class ValidationErrorsHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<ResponseMessage> handleConstraintViolationException(MethodArgumentNotValidException e) {
        List<ObjectError> errorList = e.getBindingResult().getAllErrors();
        StringBuilder sb = new StringBuilder();

        for (ObjectError error : errorList ) {
            FieldError fe = (FieldError) error;
            sb.append("Field ");
            sb.append(fe.getField()).append(" ");
            sb.append(error.getDefaultMessage()).append("!\n");
        }
        return new ResponseEntity<>(new ResponseMessage(sb.toString()), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler({MissingServletRequestPartException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<ResponseMessage> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        return new ResponseEntity<>(new ResponseMessage("Field " + e.getRequestPartName() + " is required!"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<ResponseMessage> handleHttpMediaTypeNotSupportedException() {
        return new ResponseEntity<>(new ResponseMessage("Missing required fields!"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<ResponseMessage> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return new ResponseEntity<>(new ResponseMessage("Field " + e.getName() + " format is not valid!"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({PropertyReferenceException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<ResponseMessage> handlePropertyReferenceException(PropertyReferenceException e) {
        return new ResponseEntity<>(new ResponseMessage("Field " + e.getPropertyName() + " does not exist!"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<ResponseMessage> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String fieldName = e.getCause().toString().substring(e.getCause().toString().lastIndexOf("[") + 2, e.getCause().toString().lastIndexOf("]") - 1);
        return new ResponseEntity<>(new ResponseMessage("Field " + fieldName + " format is not valid!"), HttpStatus.BAD_REQUEST);
    }
}
