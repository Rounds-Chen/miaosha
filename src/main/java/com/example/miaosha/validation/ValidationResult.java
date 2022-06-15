package com.example.miaosha.validation;


import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
    private boolean hasError=false;
    private Map<String,String> errMaps=new HashMap<>();

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public Map<String, String> getErrMaps() {
        return errMaps;
    }

    public void setErrMaps(Map<String, String> errMaps) {
        this.errMaps = errMaps;
    }

    public String getErrMsgs(){
        return StringUtils.join(this.errMaps.values().toArray(),",");
    }
}
