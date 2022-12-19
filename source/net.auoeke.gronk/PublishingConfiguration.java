package net.auoeke.gronk;

public class PublishingConfiguration {
	public String url;
	public String scm;
	public String license;
	public String email;

	public String url() {
		return this.url == null ? this.scm : this.url;
	}
}
