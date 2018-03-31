package com.github.honourednihilist.metrics;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketUtils {

	private SocketUtils() {}

	public static int getFreePort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
