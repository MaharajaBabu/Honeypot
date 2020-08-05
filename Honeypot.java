/**
 * @Author: Maharaja Babu
 **/

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import com.spotify.docker.client.messages.swarm.Config;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Honeypot
{
	/**
	 *
	 *	@throws DockerCertificateException
	 *	@throws	DockerException
	 *	@throws InterruptedException
	 */
	public void start_cowrie() throws DockerCertificateException, DockerException, InterruptedException {

	final DockerClient docker = DefaultDockerClient.fromEnv().build();

		// lists the available containers
		final List<Container> containers = docker.listContainers();
		for (int i =0; i < containers.size(); i++)
		{
			System.out.println(containers.get(i));
		}

		try {
			ReadJson t = new ReadJson();
			t.getRules();
			String temp_subnet = t.getSubnet() + "/" + t.getSubnet_r();
			String temp_IPrange = t.getIPrange() + "/" + t.getIPrange_r();

			NetworkConfig networkConfig = NetworkConfig.builder()
					.checkDuplicate(true)
					.attachable(true)
					.name("babu")
					.ipam(new Ipam() {
						@Override
						public String driver() {
							return "default";
						}

						@Nullable
						@Override
						public ImmutableList<IpamConfig> config() {
							IpamConfig i = IpamConfig.create(temp_subnet, temp_IPrange, t.getGateway());
							ImmutableList<IpamConfig> config = ImmutableList.of(i);
							return config;
						}

						@Nullable
						@Override
						public ImmutableMap<String, String> options() {
							return null;
						}
					})
					.build();
			docker.createNetwork(networkConfig);
		}
		catch(Exception e)
		{
			System.out.println("EXCEPTION CAUSED RULES: "+ e);
		}


		// pulls docker container cowrie/cowrie
		docker.pull("cowrie/cowrie");

		// Bind container ports to host ports
		final String[] ports = {"2222"};
		final Map<String, List<PortBinding>> portBindings = new HashMap<>();
		for (String port : ports) {
			List<PortBinding> hostPorts = new ArrayList<>();
			hostPorts.add(PortBinding.of("0.0.0.0", port));
			portBindings.put(port, hostPorts);
		}

		// Binds port 443 to allocated host port.
		List<PortBinding> randomPort = new ArrayList<>();
		randomPort.add(PortBinding.randomPort("0.0.0.0"));
		portBindings.put("443", randomPort);

		final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

		// Create container
		final ContainerConfig containerConfig = ContainerConfig.builder()
				.hostConfig(hostConfig)
				.image("cowrie/cowrie").exposedPorts(ports)
				.build();

		final ContainerCreation creation = docker.createContainer(containerConfig);

		// creates id for container
		final String id = creation.id();

		// connects container to docker bridge network babu
		docker.connectToNetwork(id, "babu");

		// starts the container
		docker.startContainer(id);

	}
}