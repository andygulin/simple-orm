package simple.orm.exception;

public class DaoException extends Exception {

	private static final long serialVersionUID = 3452988975774050209L;

	private int errorCode;

	public DaoException() {
		super();
	}

	public DaoException(int errorCode) {
		super();
		this.setErrorCode(errorCode);
	}

	public DaoException(int errorCode, String msg) {
		super(msg);
		this.setErrorCode(errorCode);
	}

	public DaoException(Throwable e) {
		super(e);
	}

	public DaoException(String msg) {
		super(msg);
	}

	public DaoException(String msg, Throwable e) {
		super(msg, e);
	}

	public DaoException(int errorCode, Throwable e) {
		super(e);
		this.setErrorCode(errorCode);
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}