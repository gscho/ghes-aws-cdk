package org.gscho;

import java.util.HashMap;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;

public class GHESApp {

	@SuppressWarnings("serial")
	public static void main(final String[] args) {
		App app = new App();
		StackProps props = StackProps.builder()
				.env(Environment.builder()
						.account(System.getenv("CDK_DEFAULT_ACCOUNT"))
						.region(System.getenv("CDK_DEFAULT_REGION"))
						.build())
				.tags(new HashMap<String, String>() {
					{
						put("OwnerContact", "gscho");
					}
				})
				.build();
		GHESStack stack = GHESStack.Builder.create(app, "GHES-Stack", props)
				.ownerContact("gscho")
				.serverVersion("3.1.5")
				.instanceType("r5a.xlarge")
				.build();
		stack.prepareStack();
		app.synth();
	}
}
