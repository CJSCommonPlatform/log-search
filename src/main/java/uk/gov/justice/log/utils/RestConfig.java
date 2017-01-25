package uk.gov.justice.log.utils;


public class RestConfig {
    private String hostName;
    private String hostScheme;
    private Integer hostPort;
    private String proxyHost = "";
    private int proxyPort;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    public String getHostScheme() {
        return hostScheme;
    }

    public void setHostScheme(String hostScheme) {
        this.hostScheme = hostScheme;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
