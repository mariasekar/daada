package org.adroit.andes.core.cli;

import java.util.ArrayList;
import java.util.List;

import org.adroit.andes.core.IAndes.PiiFieldType;

/**
 * <p>
 * The <code>FieldSpec</code> class represents a field specification. A field specification specifies
 * the field's position in the input (CSV) file and its piiField
 * </p>
 * @author sekar
 *
 */
public class FieldSpec {

    /** The field position */
    private List<Integer> columnIndices;
    
    /** The field piiField */
    private PiiFieldType piiField;

    public FieldSpec(){
    }

    public FieldSpec(PiiFieldType piiField) {
        this.piiField = piiField;
        this.columnIndices = new ArrayList<>();
    }

    public FieldSpec(PiiFieldType type, int...columnIndices) {
        this.piiField = type;
        this.columnIndices = new ArrayList<>();
        for(int index : columnIndices) {
        	this.columnIndices.add(index);
        }
    }

    public void setColumnIndices(List<Integer> columnIndices) {
		this.columnIndices = columnIndices;
	}

    public List<Integer> getColumnIndices() {
        return this.columnIndices;
    }

    public void setPiiField(PiiFieldType piiField) {
		this.piiField = piiField;
	}

    public PiiFieldType getPiiField() {
        return this.piiField;
    }

    @Override
    public String toString() {
        return getColumnIndices() + ":" + getPiiField().name();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (! (o instanceof FieldSpec)) {
            return false;
        } else {
            FieldSpec f = (FieldSpec) o;
            return this.piiField.equals(f.piiField) && f.columnIndices.equals(this.columnIndices) && f.getPiiField().equals(this.getPiiField());
        }
    }

}
