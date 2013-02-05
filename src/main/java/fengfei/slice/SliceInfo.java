package fengfei.slice;

public class SliceInfo {

	protected final String host;
	protected final int port;
	protected final int timeout;
	protected String password;
	protected boolean isMaster = true;
	protected Status status = Status.Normal;

	public SliceInfo(String host, int port, int timeout) {
		super();
		this.host = host;
		this.port = port;
		this.timeout = timeout;

	}

	public SliceInfo(String host, int port, int timeout, String password) {
		this(host, port, timeout);
		this.password = password;
	}

	public SliceInfo(String host, int port, int timeout, boolean isMaster) {
		this(host, port, timeout);
		this.isMaster = isMaster;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getPassword() {
		return password;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
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
		SliceInfo other = (SliceInfo) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SliceInfo [host=" + host + ", port=" + port + ", timeout="
				+ timeout + ", password=" + password + ", isMaster=" + isMaster
				+ ", status=" + status + "]";
	}

}
