package org.sevenstar.web.util;

import java.util.ArrayList;

public class SSList extends ArrayList {
	private Class elementType;
	
	private SSList(){
		//pass
	}

	public SSList(Class klass) {
		this.elementType = klass;
	}

	public Class getElementType() {
		return elementType;
	}

	public void setElementType(Class elementType) {
		this.elementType = elementType;
	}

}
