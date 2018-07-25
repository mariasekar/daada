package os.daada.core.cli;

import java.util.List;

public class AndesTableProfile {

	public AndesTableProfile(){
	}

	private List<FieldSpec> piiFieldDetails;

	public List<FieldSpec> getPiiFieldDetails() {
		return piiFieldDetails;
	}

	public void setPiiFieldDetails(List<FieldSpec> piiFieldDetails) {
		this.piiFieldDetails = piiFieldDetails;
	}

	public List<FieldSpec> getPiiFieldDetailsAsArray() {
		return piiFieldDetails;
	}
}
