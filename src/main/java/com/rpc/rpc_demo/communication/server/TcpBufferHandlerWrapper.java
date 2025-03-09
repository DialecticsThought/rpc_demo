package com.rpc.rpc_demo.communication.server;

import com.rpc.rpc_demo.communication.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * @Description 一个包装类，实现 Handler<Buffer> 接口，用于处理TCP接收到的Buffer数据
 * <pre>
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
    // 内部成员：RecordParser 用于解析连续传输的二进制数据
    // TODO 数据接收方收到数据时，会自动执行这个 handle(Buffer buffer) 方法，也就是收到的数据交给内部的 RecordParser 处理
    private final RecordParser recordParser;

    /**
     * 构造函数 初始化一个 RecordParser 对象,用于解析接收到的数据
     *
     * @param bufferHandler 外部传入的处理器，用于处理完整的消息
     */
    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        // 调用初始化方法，构造并配置RecordParser，将外部bufferHandler作为回调处理完整消息
        this.recordParser = initRecordParser(bufferHandler);
    }

    /**
     * 所谓的处理 就是 将接收到的二进制数据传递给 RecordParser 进行处理
     * 这里是关键：RecordParser 能自动处理粘包和半包问题，
     * 即当数据不够完整时，自动等待后续数据补齐，再将完整的数据交给下层处理器
     * TODO 这个 handle 方法是在 TCP 连接接收到数据时自动调用的，而具体功能：
     *      将收到的Buffer传递给recordParser处理，recordParser内部会管理数据的拼接和分包
     *      但是首先 需要初始化recordParser，也就是执行initRecordParser方案
     * @param buffer TCP接收到的二进制数据
     */
    @Override
    public void handle(Buffer buffer) {
        // 将收到的Buffer传递给RecordParser处理，RecordParser内部会管理数据的拼接和分包
        recordParser.handle(buffer);
    }

    /**
     * 初始化RecordParser，设置其固定长度模式和输出处理器
     * 这里通过设置固定长度为消息头长度，先读取消息头，再根据消息头中指定的消息体长度读取完整消息，
     * 这正是解决粘包和半包问题的常见方法。
     *
     * @param bufferHandler 外部传入的处理器，用于处理解析出的完整消息
     * @return 初始化并配置好的 RecordParser 对象
     */
    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // 创建一个新的 RecordParser 实例,并将其初始模式设置为固定长度模式
        // 并设置其固定长度为 ProtocolConstant.MESSAGE_HEADER_LENGTH(消息头长度)
        // 这样可以保证首先读取完整的消息头，消息头中通常包含消息体的长度信息
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);
        // 设置 RecordParser 的输出处理器，用于处理解析出的数据块
        // 这个匿名内部类负责根据消息头信息调整读取模式
        // TODO 传入了一个匿名内部类（实现了 Handler<Buffer> 接口），它的作用是对 RecordParser 输出的每一块数据进行处理
        // TODO 这个handler是给parser用的
        parser.setOutput(new Handler<Buffer>() {
            // 初始值-1表示还未获取消息体长度
            int size = -1;
            // 定义一个Buffer用于累积读取的数据，初始为空
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                // 当size为-1时，说明当前读取的是消息头数据
                if (size == -1) {
                    // 从消息头中读取消息体长度，假定消息体长度存储在消息头的固定位置，这里读取位置13处的4字节整数
                    size = buffer.getInt(13);// 规定了13~16 看消息体定义的类
                    // 将RecordParser的读取模式切换为固定长度模式，长度为消息体的大小
                    // 这样，RecordParser会等待接收到完整的消息体数据（即半包问题的解决方案）
                    parser.fixedSizeMode(size);
                    // 将当前读取的消息头数据写入累积Buffer
                    resultBuffer.appendBuffer(buffer);
                } else {// 已经读取到了一部分消息
                    // 直接将接收到的消息体数据追加到累积Buffer中
                    resultBuffer.appendBuffer(buffer);
                    // TODO 当累积的Buffer包含了完整的消息（消息头+消息体）后，调用外部处理器处理完整消息
                    // TODO 这个外部处理器 可以看VertxTcpClient.doRequest的completefuture
                    bufferHandler.handle(resultBuffer);
                    // 重置RecordParser，切换回初始的固定长度模式，先读取下一个消息的消息头
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    // 重置size为-1，表示等待下一个消息头中消息体长度的信息
                    size = -1;
                    // 清空累积Buffer，准备存放下一个完整消息的数据
                    resultBuffer = Buffer.buffer();
                }
            }
        });
        // 返回配置好的RecordParser
        return parser;
    }
}
