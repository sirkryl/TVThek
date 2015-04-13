package fdts.android.tvconnection;

/**
 * Interface for connection from the AnroidApp to a TV.
 * 
 */
public interface TVConnection {

	/**
	 * Starts the search for available tvs.
	 */
	void start();

	/**
	 * Searchs for available tvs.
	 */
	void search();

	/**
	 * Connects to tv device.
	 * @return true - succeed 
	 * 		   false - failed
	 */
	boolean connectToTV(String tvname);

	/**
	 * Disconnects from tv device.
	 * @return
	 */
	boolean disconnectFromTV();

	/**
	 * Send a message to tv device.
	 * @param data
	 * @return
	 */
	boolean sendMessageToTV(String data);

	/**
	 * Get a message from tv device.
	 * @return
	 */
	String getMessageFromTV();
}
