package uk.gov.justice.log.utils;


public class RestConfig {
    private String hostName;
    private String hostScheme;
    private Integer hostPort;
    private String proxyHost;
    private int proxyPort;
    private int maximumConnections;

    public int getMaximumConnections() {
        return maximumConnections;
    }

    public void setMaximumConnections(final int maximumConnections) {
        this.maximumConnections = maximumConnections;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(final Integer hostPort) {
        this.hostPort = hostPort;
    }

    public String getHostScheme() {
        return hostScheme;
    }

    public void setHostScheme(final String hostScheme) {
        this.hostScheme = hostScheme;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
