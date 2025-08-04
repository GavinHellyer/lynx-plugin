package com.ultimafurniture.lynx.acts.printer;

public interface IHandler {
    boolean Connect(String addr, int port) throws Exception;

    boolean IsConnected();

    boolean Disconnect() throws Exception;

    boolean SendBytes(int offset, int length, byte[] bytes, int timeoutMs) throws Exception;

    boolean RecvBytes(int offset, int length, byte[] bytes, int timeoutMs) throws Exception;
}