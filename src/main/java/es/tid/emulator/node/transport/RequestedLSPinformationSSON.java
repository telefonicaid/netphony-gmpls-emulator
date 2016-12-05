package es.tid.emulator.node.transport;

public class RequestedLSPinformationSSON extends RequestedLSPinformation{
	//toda la información de entrada para el lsp en la request
	private int suggestedN;
	private int suggestedM;
	private long bandwidth;
	
	public RequestedLSPinformationSSON (){
		
	}

	public int getSuggestedN() {
		return suggestedN;
	}

	public void setSuggestedN(int suggestedN) {
		this.suggestedN = suggestedN;
	}

	public int getSuggestedM() {
		return suggestedM;
	}

	public void setSuggestedM(int suggestedM) {
		this.suggestedM = suggestedM;
	}

	public long getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(long bandwidth) {
		this.bandwidth = bandwidth;
	}
}
