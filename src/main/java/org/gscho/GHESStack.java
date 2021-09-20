package org.gscho;

import java.util.Arrays;
import java.util.stream.Stream;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.BlockDevice;
import software.amazon.awscdk.services.ec2.BlockDeviceVolume;
import software.amazon.awscdk.services.ec2.EbsDeviceOptions;
import software.amazon.awscdk.services.ec2.EbsDeviceVolumeType;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.LookupMachineImage;
import software.amazon.awscdk.services.ec2.LookupMachineImageProps;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;

public class GHESStack extends Stack {

	private String instanceType;
	private String serverVersion;
	private String ownerContact;

	private GHESStack(final Construct scope, final String id) {
		super(scope, id, null);
	}

	private GHESStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);
	}

	public void prepareStack() {
		IVpc vpc = Vpc.fromLookup(this, "GHES-VPC", VpcLookupOptions.builder()
				.isDefault(true)
				.build());

		SecurityGroup securityGroup = SecurityGroup.Builder.create(this, "GHES-SecurityGroup")
				.vpc(vpc)
				.securityGroupName(String.format("ghes-sg-%s-%s", getOwnerContact(), getServerVersion()))
				.build();

		Stream.of(22, 25, 80, 122, 443, 1194, 9418, 8080, 8443)
				.forEach(port -> securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(port)));

		Stream.of(22, 25, 80, 122, 443, 1194, 9418, 8080, 8443)
				.forEach(port -> securityGroup.addEgressRule(Peer.anyIpv4(), Port.tcp(port)));

		securityGroup.addEgressRule(Peer.anyIpv4(), Port.allIcmp());

		LookupMachineImage ami = (LookupMachineImage) MachineImage.lookup(LookupMachineImageProps.builder()
				.name(String.format("GitHub Enterprise Server %s", getServerVersion()))
				.build());

		BlockDeviceVolume dataVolume = BlockDeviceVolume.ebs(46, EbsDeviceOptions.builder()
				.deleteOnTermination(true)
				.volumeType(EbsDeviceVolumeType.GP2)
				.build());

		BlockDeviceVolume rootVolume = BlockDeviceVolume.ebs(250, EbsDeviceOptions.builder()
				.deleteOnTermination(true)
				.volumeType(EbsDeviceVolumeType.GP2)
				.build());

		BlockDevice dataBlockDevice = BlockDevice.builder()
				.deviceName("/dev/sdb")
				.volume(dataVolume)
				.build();

		BlockDevice rootBlockDevice = BlockDevice.builder()
				.deviceName("/dev/xvda")
				.volume(rootVolume)
				.build();

		InstanceType type = new InstanceType(getInstanceType());

		Instance.Builder.create(this, "GHES-EC2")
				.instanceType(type)
				.machineImage(ami)
				.vpc(vpc)
				.securityGroup(securityGroup)
				.blockDevices(Arrays.asList(rootBlockDevice, dataBlockDevice))
				.keyName("csa-keypair")
				.instanceName(String.format("%s-%s-ges-ec2", getOwnerContact(), getServerVersion()))
				.build();
	}

	public static class Builder {

		private Construct scope;
		private String id;
		private StackProps props;

		private String instanceType = "r5a.xlarge";
		private String serverVersion = "3.1.5";
		private String ownerContact;

		public static Builder create(final Construct scope, final String id) {
			return new Builder(scope, id, null);
		}

		public static Builder create(final Construct scope, final String id, final StackProps props) {
			return new Builder(scope, id, props);
		}

		private Builder(final Construct scope, final String id, final StackProps props) {
			this.scope = scope;
			this.id = id;
			this.props = props;
		}

		public Builder instanceType(String instanceType) {
			this.instanceType = instanceType;

			return this;
		}

		public Builder serverVersion(String serverVersion) {
			this.serverVersion = serverVersion;

			return this;
		}

		public Builder ownerContact(String ownerContact) {
			this.ownerContact = ownerContact;

			return this;
		}

		public GHESStack build() {

			GHESStack stack = new GHESStack(this.scope, this.id, this.props);
			stack.setInstanceType(this.instanceType);
			stack.setServerVersion(this.serverVersion);
			stack.setOwnerContact(this.ownerContact);

			return stack;
		}
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getServerVersion() {
		return serverVersion;
	}

	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}

	public String getOwnerContact() {
		return ownerContact;
	}

	public void setOwnerContact(String ownerContact) {
		this.ownerContact = ownerContact;
	}
}
