package com.rpc.rpc_demo.communication.server;

import com.rpc.rpc_demo.communication.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * @Description <pre>
 * 如何解决半包
 *      我们在消息头中已经设置了请求体的长度，
 *      在服务端接收的时候，判断每次消息的长度是否符合我们的预期，如果消息不完整，那么我们就留到下一次再读取
 * 如何解决粘包问题
 *      解决思路类似，我们每次只读取指定长度的数据，超过的长度留到下一次接收消息的时候再读取
 *      在Vert.x中，我们可以使用内置的RecordParser来解决半包和粘包问题，
 *      它可以保证下次读取到特定长度的字符，这是我们解决半包粘包问题的基础。
 *
 * 我们封装一个TcpBufferHandlerWrapper类,这里我们使用了设计模式中的装饰者模式,使用RecordParser来对原来的Buffer处理器功能进行增强
 * </pre>
 * @Author veritas
 * @Data 2025/3/9 11:43
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {
    private final RecordParser recordParser;

    /**
     * 构造函数 初始化一个 RecordParser 对象,用于解析接收到的数据
     * @param bufferHandler
     */
    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        this.recordParser = initRecordParser(bufferHandler);
    }

    /**
     * 所谓的处理 就是 将接收到的二进制数据传递给 RecordParser 进行处理
     * @param buffer
     */
    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // 创建一个新的 RecordParser 实例,并设置其固定长度为 ProtocolConstant.MESSAGE_HEADER_LENGTH(消息头长度)
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);
        // 设置 RecordParser 的输出处理器
        parser.setOutput(new Handler<Buffer>() {
            // 初始化
            int size = -1;
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (size == -1) {// 还没有开始读
                    // 首先 读取 消息体长度
                    size = buffer.getInt(13);// 规定了13~16 看消息体定义的类
                    // 设置 RecordParser 的固定长度模式为该长度
                    parser.fixedSizeMode(size);
                    // 写入消息头到结果buffer
                    resultBuffer.appendBuffer(buffer);
                } else {// 已经读取到了一部分消息
                    // 直接把消息体写入到结果
                    resultBuffer.appendBuffer(buffer);
                    // 为已经拼接为完整的消息buffer做处理
                    // 当接收到完整的消息体数据后,将整个消息写入 resultBuffer,并将其传递给外部处理器进行处理
                    bufferHandler.handle(resultBuffer);

                    // 重置 RecordParser 的固定长度模式为消息头长度,并清空 resultBuffer
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });
        return parser;
    }
}
