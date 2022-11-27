package net.auoeke.gronk;

import org.gradle.api.artifacts.repositories.AuthenticationSupported;

public class PasswordExtension extends ClosureExtension<AuthenticationSupported, Void> {
	public PasswordExtension(AuthenticationSupported owner) {
		super(owner);
	}

	public static void inject(AuthenticationSupported owner) {
		inject(owner, "password");
	}

	public void doCall(Object password) {
		this.owner().getCredentials().setPassword(String.valueOf(password));
	}
}
