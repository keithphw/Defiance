/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine.network;

import org.apache.mina.transport.socket.nio.*;
import org.apache.mina.common.*;
import org.apache.mina.filter.*;
import org.apache.mina.filter.codec.*;

/**
 *
 * @author CommanderKeith
 */
public class ByteArrayCodecFactory implements ProtocolCodecFactory {

	private ProtocolEncoder encoder;
	private ProtocolDecoder decoder;

	public ByteArrayCodecFactory() {
		encoder = new ByteArrayEncoder();
		decoder = new ByteArrayDecoder();
	}

	public ProtocolEncoder getEncoder() throws Exception {
		return encoder;
	}

	public ProtocolDecoder getDecoder() throws Exception {
		return decoder;
	}

	public class ByteArrayEncoder extends ProtocolEncoderAdapter {

		public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
			byte[] bytes = (byte[]) message;
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 4, false);
			buffer.putInt(bytes.length);
			buffer.put(bytes);
			buffer.flip();
			out.write(buffer);
		}
	}

	public class ByteArrayDecoder extends CumulativeProtocolDecoder {

		protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
			if (in.prefixedDataAvailable(4)) {
				int length = in.getInt();
				byte[] bytes = new byte[length];
				in.get(bytes);
				out.write(bytes);
				return true;
			} else {
				return false;
			}
		}
	}
}
