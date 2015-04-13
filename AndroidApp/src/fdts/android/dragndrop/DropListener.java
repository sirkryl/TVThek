package fdts.android.dragndrop;

/**
 * Interface for DropListener, used in Adapters
 *
 */
public interface DropListener {

	/**
	 * Called when an item is to be dropped.
	 * @param from - index item started at.
	 * @param to - index to place item at.
	 */
	void onDrop(int from, int to);
}
