package server.encoder;

import command.CommandRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

public class CommandDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final Charset charset;

    public CommandDecoder(Charset charset) {
        this.charset = charset;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        String rawMsg = msg.toString(this.charset);
        if (rawMsg.equals("")) {
            return;
        }

        //On Command request
        if (rawMsg.charAt(0) == '/') {
            out.add(buildCommandRequest(rawMsg));

            return;
        }

        // On Publish
        out.add(buildPublishRequest(rawMsg));
    }

    private CommandRequest buildCommandRequest(String rawMsg) {
        String cmd = rawMsg.substring(1);
        String[] cmdParts = cmd.split(" ");
        int size = cmdParts.length - 1;
        String[] args = new String[(size < 0) ? 0 : size];
        System.arraycopy(cmdParts, 1, args, 0, cmdParts.length - 1);

        return new CommandRequest(cmdParts[0], args);
    }

    private CommandRequest buildPublishRequest(String rawMsg) {
        String[] args = new String[1];
        args[0] = rawMsg;

        return new CommandRequest("publish", args);
    }
}