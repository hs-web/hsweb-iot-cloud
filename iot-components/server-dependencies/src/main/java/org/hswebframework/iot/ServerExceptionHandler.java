package org.hswebframework.iot;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.validate.SimpleValidateResults;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author wangzheng
 * @see
 * @since 1.0
 */
@RestControllerAdvice
public class ServerExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Object> handleException(DuplicateKeyException e) {
        MySQLIntegrityConstraintViolationException exception = null;
        Throwable tmp = e.getCause();
        while (tmp != null) {
            if (tmp instanceof MySQLIntegrityConstraintViolationException) {
                exception = ((MySQLIntegrityConstraintViolationException) tmp);
                break;
            }
            tmp = tmp.getCause();
        }

        if (exception != null) {
            String message = exception.getMessage();
            String value = message.substring(17, message.indexOf(" for key") - 1);
            String field = message.substring(message.indexOf(" for key") + 10, message.lastIndexOf("'"));
            if(field.contains("_")){
                field=field.substring(field.lastIndexOf("__")+2,field.length());
            }
            SimpleValidateResults results = new SimpleValidateResults();
            results.addResult(StringUtils.underScoreCase2CamelCase(field), "存在重复数据:" + value);
            return ResponseMessage.error(400, "存在重复数据:" + value).result(results.getResults());
        }
        return ResponseMessage.error(400, "存在重复数据");
    }
}
