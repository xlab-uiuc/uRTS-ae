package org.apache.zookeeper;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class TestGetConfigValueForConfigAware {

    @BeforeAll
    public static void before() {
      String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
      File src = new File("curConfigFile.cfg");
      File dest = new File("ctest-" + pid + ".cfg");
      try {
          Files.copy(src.toPath(), dest.toPath());
      } catch (Exception e) {
          e.printStackTrace();
      }
    }

    @AfterAll
    public static void after() {
      String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
      File src = new File("ctest-" + pid + ".cfg");
      if (src.exists()) {
          src.delete();
      }
    }

    @Test
    public void testAllConfig() throws QuorumPeerConfig.ConfigException {
        Map<String, String> quorumPeerMap = new HashMap<String, String>();
        QuorumPeerConfig quorumPeerConfig = new QuorumPeerConfig();
        Map<String, String> serverMap = new HashMap<String, String>();
        ServerConfig serverConfig = new ServerConfig();
        try {
            quorumPeerConfig.injectError(true);
        } catch (QuorumPeerConfig.ConfigException e) {
            quorumPeerConfig.injectError(false);
        }

        // serverConfig
        if(serverConfig.getClientPortAddress() != null) {
            String clientPortAddress = serverConfig.getClientPortAddress().toString();
            serverMap.put("clientPortAddress", clientPortAddress);
        } else {
            serverMap.put("clientPortAddress", "null");
        }
        if (serverConfig.getSecureClientPortAddress() != null) {
            String secureClientPortAddress = serverConfig.getSecureClientPortAddress().toString();
            serverMap.put("secureClientPortAddress", secureClientPortAddress);
        } else {
            serverMap.put("secureClientPortAddress", "null");
        }
        if (serverConfig.getDataDir() != null ){
            String dataDir = serverConfig.getDataDir().toString();
            serverMap.put("dataDir", dataDir);
        } else {
            serverMap.put("dataDir", "null");
        }
        if (serverConfig.getDataLogDir() != null) {
            String dataLogDir = serverConfig.getDataLogDir().toString();
            serverMap.put("dataLogDir", dataLogDir);
        } else {
            serverMap.put("dataLogDir", "null");
        }

        String tickTime = String.valueOf(serverConfig.getTickTime());
        serverMap.put("tickTime", tickTime);
        String maxClientCnxns = String.valueOf(serverConfig.getMaxClientCnxns());
        serverMap.put("maxClientCnxns", maxClientCnxns);
        String minSessionTimeout = String.valueOf(serverConfig.getMinSessionTimeout());
        serverMap.put("minSessionTimeout", minSessionTimeout);
        String maxSessionTimeout = String.valueOf(serverConfig.getMaxSessionTimeout());
        serverMap.put("maxSessionTimeout", maxSessionTimeout);
        String jvmPauseInfoThresholdMs = String.valueOf(serverConfig.getJvmPauseInfoThresholdMs());
        serverMap.put("jvm.pause.info-threshold.ms", jvmPauseInfoThresholdMs);
        String jvmPauseWarnThresholdMs = String.valueOf(serverConfig.getJvmPauseWarnThresholdMs());
        serverMap.put("jvm.pause.warn-threshold.ms", jvmPauseWarnThresholdMs);
        String jvmPauseSleepTimeMs = String.valueOf(serverConfig.getJvmPauseSleepTimeMs());
        serverMap.put("jvm.pause.sleep.time.ms", jvmPauseSleepTimeMs);
        String jvmPauseMonitorToRun = String.valueOf(serverConfig.isJvmPauseMonitorToRun());
        serverMap.put("jvm.pause.monitor", jvmPauseMonitorToRun);
        String metricsProviderClassName = serverConfig.getMetricsProviderClassName();
        serverMap.put("metricsProvider.className", metricsProviderClassName);
        String listenBacklog = String.valueOf(serverConfig.getClientPortListenBacklog());
        serverMap.put("listenBacklog", listenBacklog);

        // quorumPeerConfig
        if (quorumPeerConfig.getClientPortAddress() != null) {
            String clientPortAddress2 = quorumPeerConfig.getClientPortAddress().toString();
            quorumPeerMap.put("clientPortAddress", clientPortAddress2);
        } else {
            quorumPeerMap.put("clientPortAddress", "null");
        }

        if (quorumPeerConfig.getSecureClientPortAddress() != null) {
            String secureClientPortAddress2 = quorumPeerConfig.getSecureClientPortAddress().toString();
            quorumPeerMap.put("secureClientPortAddress", secureClientPortAddress2);
        } else {
            quorumPeerMap.put("secureClientPortAddress", "null");
        }


        String observerMasterPort = String.valueOf(quorumPeerConfig.getObserverMasterPort());
        quorumPeerMap.put("observerMasterPort", observerMasterPort);

        if (quorumPeerConfig.getDataDir() != null ) {
            String dataDir2 = quorumPeerConfig.getDataDir().toString();
            quorumPeerMap.put("dataDir", dataDir2);
        } else {
            quorumPeerMap.put("dataDir", "null");
        }

        if (quorumPeerConfig.getDataLogDir() != null ) {
            String dataLogDir = quorumPeerConfig.getDataLogDir().toString();
            quorumPeerMap.put("dataLogDir", dataLogDir);
        } else {
            quorumPeerMap.put("dataLogDir", "null");
        }

        String tickTime2 = String.valueOf(quorumPeerConfig.getTickTime());
        quorumPeerMap.put("tickTime", tickTime2);
        String maxClientCnxns2 = String.valueOf(quorumPeerConfig.getMaxClientCnxns());
        quorumPeerMap.put("maxClientCnxns", maxClientCnxns2);
        String minSessionTimeout2 = String.valueOf(quorumPeerConfig.getMinSessionTimeout() == -1 ? quorumPeerConfig.getTickTime() * 2 : quorumPeerConfig.getMinSessionTimeout());
        quorumPeerMap.put("minSessionTimeout", minSessionTimeout2);
        String maxSessionTimeout2 = String.valueOf(quorumPeerConfig.getMaxSessionTimeout() == -1 ? quorumPeerConfig.getTickTime() * 20 : quorumPeerConfig.getMaxSessionTimeout());
        quorumPeerMap.put("maxSessionTimeout", maxSessionTimeout2);
        String metricsProviderClassName2 = quorumPeerConfig.getMetricsProviderClassName();
        quorumPeerMap.put("metricsProvider.className", metricsProviderClassName2);
        String localSessionsEnabled = String.valueOf(quorumPeerConfig.areLocalSessionsEnabled());
        quorumPeerMap.put("localSessionsEnabled", localSessionsEnabled);
        String localSessionsUpgradingEnabled = String.valueOf(quorumPeerConfig.isLocalSessionsUpgradingEnabled());
        quorumPeerMap.put("localSessionsUpgradingEnabled", localSessionsUpgradingEnabled);
        String sslQuorum = String.valueOf(quorumPeerConfig.isSslQuorum());
        quorumPeerMap.put("sslQuorum", sslQuorum);
        String portUnification = String.valueOf(quorumPeerConfig.shouldUsePortUnification());
        quorumPeerMap.put("portUnification", portUnification);
        String clientPortListenBacklog = String.valueOf(quorumPeerConfig.getClientPortListenBacklog());
        quorumPeerMap.put("clientPortListenBacklog", clientPortListenBacklog);
        String initLimit = String.valueOf(quorumPeerConfig.getInitLimit());
        quorumPeerMap.put("initLimit", initLimit);
        String syncLimit = String.valueOf(quorumPeerConfig.getSyncLimit());
        quorumPeerMap.put("syncLimit", syncLimit);
        String connectToLearnerMasterLimit = String.valueOf(quorumPeerConfig.getConnectToLearnerMasterLimit());
        quorumPeerMap.put("connectToLearnerMasterLimit", connectToLearnerMasterLimit);
        String electionAlg = String.valueOf(quorumPeerConfig.getElectionAlg());
        quorumPeerMap.put("electionAlg", electionAlg);
        String snapRetainCount = String.valueOf(quorumPeerConfig.getSnapRetainCount());
        quorumPeerMap.put("autopurge.snapRetainCount", snapRetainCount);
        String purgeInterval = String.valueOf(quorumPeerConfig.getPurgeInterval());
        quorumPeerMap.put("autopurge.purgeInterval", purgeInterval);
        String syncEnabled = String.valueOf(quorumPeerConfig.getSyncEnabled());
        quorumPeerMap.put("syncEnabled", syncEnabled);
        String jvmPauseInfoThresholdMs2 = String.valueOf(quorumPeerConfig.getJvmPauseInfoThresholdMs());
        quorumPeerMap.put("jvm.pause.info-threshold.ms", jvmPauseInfoThresholdMs2);
        String jvmPauseWarnThresholdMs2 = String.valueOf(quorumPeerConfig.getJvmPauseWarnThresholdMs());
        quorumPeerMap.put("jvm.pause.warn-threshold.ms", jvmPauseWarnThresholdMs2);
        String jvmPauseSleepTimeMs2 = String.valueOf(quorumPeerConfig.getJvmPauseSleepTimeMs());
        quorumPeerMap.put("jvm.pause.sleep.time.ms", jvmPauseSleepTimeMs2);
        String jvmPauseMonitorToRun2 = String.valueOf(quorumPeerConfig.isJvmPauseMonitorToRun());
        quorumPeerMap.put("jvm.pause.monitor", jvmPauseMonitorToRun2);
        String peerType = String.valueOf(quorumPeerConfig.getPeerType());
        quorumPeerMap.put("peerType", peerType);
        String quorumListenOnAllIPs = String.valueOf(quorumPeerConfig.getQuorumListenOnAllIPs());
        quorumPeerMap.put("quorumListenOnAllIPs", quorumListenOnAllIPs);
        String multiAddressEnabled = String.valueOf(quorumPeerConfig.isMultiAddressEnabled());
        quorumPeerMap.put("multiAddress.enabled", multiAddressEnabled);
        String multiAddressReachabilityCheckEnabled = String.valueOf(quorumPeerConfig.isMultiAddressReachabilityCheckEnabled());
        quorumPeerMap.put("multiAddress.reachabilityCheckEnabled", multiAddressReachabilityCheckEnabled);
        String multiAddressReachabilityCheckTimeoutMs = String.valueOf(quorumPeerConfig.getMultiAddressReachabilityCheckTimeoutMs());
        quorumPeerMap.put("multiAddress.reachabilityCheckTimeoutMs", multiAddressReachabilityCheckTimeoutMs);
        String standaloneEnabled = String.valueOf(quorumPeerConfig.isStandaloneEnabled());
        quorumPeerMap.put("standaloneEnabled", standaloneEnabled);
        String reconfigEnabled = String.valueOf(quorumPeerConfig.isReconfigEnabled());
        quorumPeerMap.put("reconfigEnabled", reconfigEnabled);

        Map<String, String> configMap = new HashMap<String, String>();
        configMap.putAll(quorumPeerMap);
        for (Map.Entry<String, String> entry : serverMap.entrySet()) {
            if (configMap.containsKey(entry.getKey())) {
                String serverValue = entry.getValue();
                String quorumPeerValue = configMap.get(entry.getKey());
                if (!serverValue.equals(quorumPeerValue)) {
                    configMap.put(entry.getKey(), serverValue + "@CONFIGAWARESEPARATOR@" + quorumPeerValue);
                }
            } else {
                configMap.put(entry.getKey(), entry.getValue());
            }
        }

        try {
            File f = new File(".ConfigValue");
            if (f.exists()) {
                File oldFile = new File(".OldConfigValue");
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                f.renameTo(oldFile);
                f.delete();
            }
            FileWriter myWriter = new FileWriter(".ConfigValue", true);
            BufferedWriter bufferWriter = new BufferedWriter(myWriter);
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                bufferWriter.write(entry.getKey() + "=CONFIGAWARE=" + entry.getValue() + "@CONFIGAWARE@\n");
            }
            bufferWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
