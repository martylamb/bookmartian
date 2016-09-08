package com.martiansoftware.bookmartian;

import com.martiansoftware.boom.Boom;
import com.martiansoftware.boom.BoomResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * helper for generating responses as described at
 * https://labs.omniti.com/labs/jsend
 * 
 * @author mlamb
 */
public class JSend {

    private static final Logger log = LoggerFactory.getLogger(JSend.class);
    
    private enum STATUS {success, fail, error};
    private final STATUS _status;
    private final String _message;
    private final Object _data;
    
    private JSend(STATUS status, String message, Object data) {
        _status = status;
        _message = message;
        _data = data;
    }
    
    private static BoomResponse jsend(STATUS status, String message, Object data) {
        return Boom.json(new JSend(status, message, data));
    }
    
    public static BoomResponse success() {
        return success(null);
    }
    
    public static BoomResponse success(Object data) {
        return jsend(STATUS.success, null, data);
    }
    
    public static BoomResponse fail(String message) {
        return jsend(STATUS.fail, message, null);
    }
    
    public static BoomResponse error(String message) {
        return jsend(STATUS.error, message, null);
    }
    
    public static BoomResponse error(Exception e) {
        return error(e.getMessage());
    }
}
