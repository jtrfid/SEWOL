package writer;

public class PerspectiveException extends Exception {

	private static final long serialVersionUID = 1L;
	private PerspectiveError perspectiveError;
	
	public PerspectiveException(PerspectiveError error){
		this.perspectiveError = error;
	}

	@Override
	public String getMessage() {
		switch(perspectiveError){
		case WRITE_ACTIVITY_IN_TRACE_PERSPECTIVE:
			return "Cannot write activity in trace perspective!";
		case WRITE_TRACE_IN_ACTIVITY_PERSPECTIVE:
			return "Cannot write trace in activity perspective!";
		case INCOMPATIBLE_LOGFORMAT:
			return "Logformat does not support the log perspective!";
		default:
			//Cannot happen since there are only two error types and both are considered.
			return "Unknown perspective error.";		
		}
	}
	
	public enum PerspectiveError {WRITE_TRACE_IN_ACTIVITY_PERSPECTIVE, WRITE_ACTIVITY_IN_TRACE_PERSPECTIVE, INCOMPATIBLE_LOGFORMAT}

}
