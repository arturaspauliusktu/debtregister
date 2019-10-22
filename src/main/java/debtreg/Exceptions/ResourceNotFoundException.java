package debtreg.Exceptions;

public class ResourceNotFoundException extends RuntimeException {
    /**
     * Generated serialVersion
     */
    private static final long serialVersionUID = 7414730579400480765L;

    public ResourceNotFoundException() {
        super();
    }
    
    public ResourceNotFoundException(String message){
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable throwable){
        super(message, throwable);
    }
}