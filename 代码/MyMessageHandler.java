package multicast;

import java.io.IOException;

public interface MyMessageHandler<S> {
    //消息处理
    void process(Message message, S handler) throws IOException;
}
