package amazon.framework.util;

/**
 * Constants define the expected loading timeout of components. For complex components, we could compare to standard
 * component, then we may multiple the timeout.
 */
public final class LoadingConstants {
	private LoadingConstants() {
		// to hide constructor
	}

	public static final long RENDER_ELEMENT = 60L;
	public static final long RENDER_ELEMENT_LONG_TIMEOUT = 120L;
	public static final long UPDATE_BUTTON = 60L;
	public static final long LOADING_TIMEOUT = 120L;
	public static final long LOADING_POLLING = 2L;
}
