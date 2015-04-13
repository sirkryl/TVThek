package fdts.android.dragndrop;

/**
 * Interface for RemoveListener, used in Adapters
 *
 */
public interface RemoveListener {

	/**
	 * Called when an item is to be removed
	 * @param which - indicates which item to remove.
	 */
	void onRemove(int which);
}
