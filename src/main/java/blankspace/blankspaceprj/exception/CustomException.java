package blankspace.blankspaceprj.exception;

public class CustomException extends RuntimeException{
    public CustomException(String code, String message){
        super(message);
    }
}
