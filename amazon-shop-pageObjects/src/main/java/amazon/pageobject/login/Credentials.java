package amazon.pageobject.login;

public class Credentials {

	private final String login;
	private final String password;

	private static final String DEFAULT_PASSWORD = "P@ssw0rd";

	private Credentials(final String login, final String password) {
		this.login = login;
		this.password = password;
	}

	public Credentials(final String login) {
		this(login, DEFAULT_PASSWORD);
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	private static Credentials getB2CSCredentials(final String login) {
		return new Credentials(login, DEFAULT_PASSWORD);
	}

	public static final Credentials LEPHUONGDY = getB2CSCredentials("dy.lephuong@gmail.com");
}