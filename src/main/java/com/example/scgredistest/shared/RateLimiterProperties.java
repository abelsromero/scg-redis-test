package com.example.scgredistest.shared;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.util.StringUtils;

// Copied form scg-k8s
public class RateLimiterProperties implements HasRouteId {

	private int limit = 0;
	private Duration duration = Duration.ofSeconds(1);
	private String routeId;
	private String keyLocation;
	private String claim;
	private String header;
	private int xForwardedForMaxTrustedIndex = DEFAULT_X_FORWARDED_FOR_MAX_TRUSTED_INDEX;
	private final List<String> ipAddresses = new ArrayList<>();

	private static final String CLAIM_KEY = "claim:";
	private static final String HEADER_KEY = "header:";
	private static final String IP_KEY = "IPs:";
	private static final String NUMBER_REGEX = "\\d+";
	private static final int DEFAULT_X_FORWARDED_FOR_MAX_TRUSTED_INDEX = 1;

	public String getClaim() {
		return claim;
	}

	public boolean hasClaim() {
		return StringUtils.hasText(claim);
	}

	public String getHeader() {
		return header;
	}

	public boolean hasHeader() {
		return StringUtils.hasText(header);
	}

	public List<String> getIPs() {
		return ipAddresses;
	}

	public boolean hasIPs() {
		return !ipAddresses.isEmpty();
	}

	public int getXForwardedForMaxTrustedIndex() {
		return xForwardedForMaxTrustedIndex;
	}

	public String getKeyLocation() {
		return keyLocation;
	}

	public void setKeyLocation(String keyLocation) {
		this.keyLocation = keyLocation;
		parseKeyLocation();
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	@Override
	public String getRouteId() {
		return routeId;
	}

	@Override
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	private void parseKeyLocation() {
		claim = null;
		header = null;
		ipAddresses.clear();
		xForwardedForMaxTrustedIndex = DEFAULT_X_FORWARDED_FOR_MAX_TRUSTED_INDEX;
		if (StringUtils.hasText(keyLocation)
				&& keyLocation.startsWith("{")
				&& keyLocation.endsWith("}")) {
			String parsedKeyLocation = keyLocation.substring(1, keyLocation.length() - 1);

			if (parsedKeyLocation.startsWith(CLAIM_KEY)) {
				parseClaim(parsedKeyLocation);
			}
			else if (parsedKeyLocation.startsWith(HEADER_KEY)) {
				parseHeader(parsedKeyLocation);
			}
			else if (parsedKeyLocation.startsWith(IP_KEY)) {
				parseIPs(parsedKeyLocation);
			}
		}
	}

	private void parseClaim(String parsedKeyLocation) {
		String parsedClaim = parsedKeyLocation.substring(CLAIM_KEY.length()).trim();
		if (StringUtils.hasText(parsedClaim)) {
			claim = parsedClaim;
		}
	}

	private void parseHeader(String parsedKeyLocation) {
		String parsedHeader = parsedKeyLocation.substring(HEADER_KEY.length()).trim();
		if (StringUtils.hasText(parsedHeader)) {
			header = parsedHeader;
		}
	}

	private void parseIPs(String parsedKeyLocation) {
		List<String> parsedIpTokens = Arrays.asList(parsedKeyLocation.substring(IP_KEY.length()).split(";"));
		List<String> modifiableIpList = new ArrayList<>(parsedIpTokens);
		if (modifiableIpList.size() > 0 && modifiableIpList.get(0).trim().matches(NUMBER_REGEX)) {
			int parsedXForwardedForMaxTrustedIndex = Integer.parseInt(modifiableIpList.get(0).trim());

			// used in XForwardedRemoteAddressResolver, which expects an index > 0
			if (parsedXForwardedForMaxTrustedIndex > 0) {
				xForwardedForMaxTrustedIndex = parsedXForwardedForMaxTrustedIndex;
				modifiableIpList.remove(modifiableIpList.get(0));
			}
		}
		for (String ip : modifiableIpList) {
			if (StringUtils.hasText(ip)) {
				ipAddresses.add(ip.trim());
			}
		}
	}
}
