package com.phone.uin.model;

import java.io.Serializable;

/**
 * Created by zhangxingsheng on 2017/6/25.
 * javaBean的基类
 */

public class BaseResponse implements Serializable {
    private String total;
    private String message;
    private String result;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
