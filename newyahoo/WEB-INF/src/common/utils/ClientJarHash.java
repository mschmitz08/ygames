package common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.ProtectionDomain;

public final class ClientJarHash {

    private static final String AGENT_HASH_MARKER = "|rph-client-hash=";

    private ClientJarHash() {
    }

    public static String appendClientHashToAgent(String agent, String hash) {
        String cleanAgent = stripClientHashFromAgent(agent);
        if (hash == null || hash.length() == 0)
            return cleanAgent;
        return cleanAgent + AGENT_HASH_MARKER + hash.toLowerCase();
    }

    public static String computeFileSha256(File file) {
        if (file == null || !file.exists() || !file.isFile())
            return "";
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            return sha256Hex(input);
        }
        catch (Throwable t) {
            return "";
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (Throwable ignore) {
                }
            }
        }
    }

    public static String computeSelfJarHash(Class anchorClass) {
        if (anchorClass == null)
            return "";

        try {
            URL resourceUrl = anchorClass.getResource(anchorClass.getSimpleName()
                    + ".class");
            URL jarUrl = resolveJarUrl(resourceUrl);
            if (jarUrl != null) {
                String hash = computeUrlSha256(jarUrl);
                if (hash.length() > 0)
                    return hash;
            }
        }
        catch (Throwable t) {
        }

        try {
            ProtectionDomain protectionDomain = anchorClass.getProtectionDomain();
            if (protectionDomain != null) {
                CodeSource codeSource = protectionDomain.getCodeSource();
                if (codeSource != null && codeSource.getLocation() != null)
                    return computeUrlSha256(codeSource.getLocation());
            }
        }
        catch (Throwable t) {
        }

        return "";
    }

    public static String extractClientHashFromAgent(String agent) {
        if (agent == null)
            return "";
        int index = agent.indexOf(AGENT_HASH_MARKER);
        if (index == -1)
            return "";
        String hash = agent.substring(index + AGENT_HASH_MARKER.length())
                .trim();
        int nextSeparator = hash.indexOf('|');
        if (nextSeparator != -1)
            hash = hash.substring(0, nextSeparator);
        return hash.toLowerCase();
    }

    public static String stripClientHashFromAgent(String agent) {
        if (agent == null || agent.length() == 0)
            return "undefined";
        int index = agent.indexOf(AGENT_HASH_MARKER);
        if (index == -1)
            return agent;
        String cleanAgent = agent.substring(0, index);
        if (cleanAgent.length() == 0)
            return "undefined";
        return cleanAgent;
    }

    private static String computeUrlSha256(URL url) {
        if (url == null)
            return "";
        InputStream input = null;
        try {
            input = url.openStream();
            return sha256Hex(input);
        }
        catch (Throwable t) {
            return "";
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (Throwable ignore) {
                }
            }
        }
    }

    private static URL resolveJarUrl(URL resourceUrl) {
        if (resourceUrl == null)
            return null;
        try {
            URLConnection connection = resourceUrl.openConnection();
            if (connection instanceof JarURLConnection)
                return ((JarURLConnection) connection).getJarFileURL();
        }
        catch (Throwable t) {
        }

        try {
            String spec = resourceUrl.toString();
            if (spec.startsWith("jar:")) {
                int separator = spec.indexOf("!/");
                if (separator != -1)
                    return new URL(spec.substring(4, separator));
            }
        }
        catch (Throwable t) {
        }

        return null;
    }

    private static String sha256Hex(InputStream input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1)
            digest.update(buffer, 0, read);
        byte[] bytes = digest.digest();
        StringBuffer hex = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            String part = Integer.toHexString(bytes[i] & 0xff);
            if (part.length() == 1)
                hex.append('0');
            hex.append(part);
        }
        return hex.toString();
    }
}
