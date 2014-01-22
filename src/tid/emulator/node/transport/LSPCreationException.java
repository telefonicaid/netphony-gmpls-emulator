package tid.emulator.node.transport;

public class LSPCreationException extends Exception {
	private static final long serialVersionUID = 0;
	private int errorType;
	private long lspID;
	
	
	public LSPCreationException(String msg){
		super(msg);
	}
	public LSPCreationException(int errorType){
		this.errorType = errorType;
	}
	public int getErrorType() {
		return errorType;
	}
	public void setErrorType(int errorType) {
		this.errorType = errorType;
	}
	public long getLspID() {
		return lspID;
	}
	public void setLspID(long lspID) {
		this.lspID = lspID;
	}
}
