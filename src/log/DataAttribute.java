package log;

public class DataAttribute {
	public String name;
	public Object value = null;
	private final String toStringFormat = "[%s = %s]";
	
	public DataAttribute(String name){
		this.name = name;
	}
	
	public DataAttribute(String name, Object value){
		this(name);
		this.value = value;
	}
	
	@Override
	public String toString(){
		return String.format(toStringFormat, name, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataAttribute other = (DataAttribute) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
	
//	@Override
//	public boolean equals(Object o){
//		if(o==null || !(o instanceof DataAttribute))
//			return false;
//		if(!((DataAttribute) o).name.equals(this.name))
//			return false;
//		return true;
//	}
}