package com.google.cdap.sftp.filepicker.plugins.common;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to connect to SFTP server.
 */
public class SftpConnector implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SftpConnector.class);
    private final Session session;
    private final Channel channel;

    //Connector Object to be used for Auth with Password
    public SftpConnector(String host, String port, String userName, String password)
            throws Exception {
        JSch jsch = new JSch();
        this.session = jsch.getSession(userName, host, Integer.parseInt(port));
        session.setPassword(password);
        java.util.Properties properties = new java.util.Properties();
        properties.put("StrictHostKeyChecking", "no");
        session.setConfig(properties);
        LOG.info("Connecting to Host: {}, Port: {}, with User: {}", host, port, userName);
        session.connect(30000);
        channel = session.openChannel("sftp");
        channel.connect();
    }


    /**
     * Get the established sftp channel to perform operations.
     */
    public ChannelSftp getSftpChannel() {
        return (ChannelSftp) channel;
    }


    @Override
    public void close() throws Exception {
        LOG.info("Closing SFTP session.");
        if (channel != null) {
            try {
                channel.disconnect();
            } catch (Throwable t) {
                LOG.warn("Error while disconnecting sftp channel.", t);
            }
        }

        if (session != null) {
            try {
                session.disconnect();
            } catch (Throwable t) {
                LOG.warn("Error while disconnecting sftp session.", t);
            }
        }
    }
}