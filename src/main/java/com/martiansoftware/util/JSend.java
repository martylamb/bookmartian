package com.martiansoftware.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * helper for generating responses as described at
 * https://labs.omniti.com/labs/jsend
 * 
 * @author mlamb
 */
public class JSend {

    private static final Logger LOG = LoggerFactory.getLogger(JSend.class);
    
    private enum STATUS {success, fail, error};
    private final STATUS _status;
    private final String _message;
    private final Object _data;
    
    private JSend(STATUS status, String message, Object data) {
        _status = status;
        _message = message;
        _data = data;
    }
    
    
    public static JSend success() {
        return success(null);
    }
    
    public static JSend success(Object data) {
        return new JSend(STATUS.success, null, data);
    }
    
//    public static BoomResponse rawSuccess(String jsonData) {
//        String result = String.format("{\n\t\"status\": \"success\",\n\t\"data\": %s\n}", jsonData);
//        return new BoomResponse(result).as(MimeType.JSON);
//    }
    
    public static JSend fail(String message) {
        return new JSend(STATUS.fail, message, null);
    }
    
    public static JSend error(String message) {
        return new JSend(STATUS.error, message, null);
    }
    
    public static JSend error(Exception e) {
        LOG.error(e.getMessage(), e);
        return error(e.getMessage());
    }
}
