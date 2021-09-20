package org.gscho;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import software.amazon.awscdk.core.App;

public class GHESStackTest {
	private final static ObjectMapper JSON = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

	@Test
	public void testStack() throws IOException {
		App app = new App();
		GHESStack stack = GHESStack.Builder.create(app, "test")
				.build();

		// synthesize the stack to a CloudFormation template
		JsonNode actual = JSON.valueToTree(app.synth()
				.getStackArtifact(stack.getArtifactId())
				.getTemplate());

		// Update once resources have been added to the stack
		assertThat(actual.get("Resources")).isNull();
	}
}
