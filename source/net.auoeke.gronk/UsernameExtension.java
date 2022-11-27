package net.auoeke.gronk;

import org.gradle.api.artifacts.repositories.AuthenticationSupported;

public class UsernameExtension extends ClosureExtension<AuthenticationSupported, Void> {
	public UsernameExtension(AuthenticationSupported owner) {
		super(owner);
	}

	public static void inject(AuthenticationSupported owner) {
		inject(owner, "username");
	}

	public void doCall(Object username) {
		this.owner().getCredentials().setUsername(String.valueOf(username));
	}
}
