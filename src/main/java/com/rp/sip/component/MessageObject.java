package com.rp.sip.component;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

public interface MessageObject {

    Object get(String key);

    void set(String key, Object value);

    String getString(String key);

    void setString(String key, String value);

    Integer getInteger(String key);

    void setInteger(String key, Integer value);

    Long getLong(String key);

    void setLong(String key, Long value);

    Float getFloat(String key);

    void setFloat(String key, Float value);

    Double getDouble(String key);

    void setDouble(String key, Float value);

    Date getDate(String key);

    void setDate(String key, Date value);

    Boolean getBoolean(String key);

    void setBoolean(String key, Boolean value);

    byte[] getBytes(String key);

    void setBytes(String key, byte[] value);

    ByteBuf getByteBuf(String key);

    void setByteBuf(String key, ByteBuf value);

    List getList(String key);

    void setList(String key, List value);

    MessageObject getMessageObject(String key);

    void setMessageObject(String key, MessageObject value);

    Object getSipMessagePojo();

    String toString();

    String toString(Charset charset);

}
