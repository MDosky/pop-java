package popjava.buffer;

/**
 * This class defined the interface for new buffer factory plug-in
 */
public class BufferFactoryPlugin extends BufferFactory {

	@Override
	public Buffer createBuffer() {
		return null;
	}

	@Override
	public String getBufferName() {
		return null;
	}

}
