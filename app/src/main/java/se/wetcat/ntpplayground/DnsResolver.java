package se.wetcat.ntpplayground;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * http://thushw.blogspot.se/2009/11/resolving-domain-names-quickly-with.html
 */
public class DnsResolver implements Runnable {
    private String domain;
    private InetAddress inetAddr;

    public DnsResolver(String domain) {
        this.domain = domain;
    }

    public void run() {
        try {
            InetAddress addr = InetAddress.getByName(domain);
            set(addr);
        } catch (UnknownHostException e) {

        }
    }

    public synchronized void set(InetAddress inetAddr) {
        this.inetAddr = inetAddr;
    }

    public synchronized InetAddress get() {
        return inetAddr;
    }
}
