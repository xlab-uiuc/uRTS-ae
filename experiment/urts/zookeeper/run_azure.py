import os, shutil, time, sys
sys.path.append("../../")
from util import *

cur_path = os.getcwd()
project_url = "https://github.com/apache/zookeeper.git"
project_root_path = os.path.join(cur_path, "zookeeper")
project_module_name = "zookeeper-server"
project_module_path = os.path.join(project_root_path, project_module_name)
module_pom_file_path = os.path.join(project_module_path, "pom.xml")
api_file1_path = os.path.join(project_module_path, "src/main/java/org/apache/zookeeper/server/ServerConfig.java")
api_file2_path = os.path.join(project_module_path, "src/main/java/org/apache/zookeeper/server/quorum/QuorumPeerConfig.java")
api_file3_path = os.path.join(project_module_path, "src/main/java/org/apache/zookeeper/server/quorum/QuorumPeerMain.java")
api_pom_file_path = os.path.join(project_module_path, "pom.xml")
test_copied_path = os.path.join(project_module_path, "src/test/java/org/apache/zookeeper")
time_number_file_path = os.path.join(cur_path, "time_number.txt")
#test_class_num_file_path = os.path.join(cur_path, "test_class_num.txt")
mvn_cmd = "mvn urts:urts -DgetterClass=TestGetConfigValueForConfigAware -DfailIfNoTests=false -Dmaven.test.failure.ignore=true | tee out.txt"
commits = ["acbfb2d78996cbaa29b9dc764d4cb3463193dc45", "9213f7353b1e6ce4d0fdbc1dca963ace1fd32cec", "ad5513b8dd15637c6c95585603a4e055dabad56d", "061438e83e61692fc9e06e057a739db5327d42b9", "7fdadf7273f34dd0552db25a3771cf55b65e9208", "ea75e1f63572fc72fb8520ba8c793523047acb49", "c583a6e79654359b5daad5093d1730e370d3b75b", "1590a424cb7a8768b0ae01f2957856b1834dd68d", "f8ebf1a25281b2c7f48e08011918c72643313209", "96d87e2809f92460f182c66311d83d59f2ab96c3", "f5c29aab9adb49d9d3580074d4d1a805579fa20d", "766e173e9d51b6354920ebc136b246d221b87ec1", "b79abb2014db9684c00afd7d98c0a7cb69ce6c8b", "c499202a2c470e2e365ef109c8e49784cb043367", "5e787c5990091b2d1fc560eba88d3c25b04690a2", "32e40e8cee36ddba1973875ab6637912719271fa", "5e6e15ac40cfd2cadac3e718e494a3c13b934b8d", "525a28bd1cb864a5783593c64908cfe0ddcba65f", "2f00dea17bd94bf43b2f6647f6b08b00ff8b7ece", "9442ce23bade5286459ff78ade1af173f7b69c30", "16187c48a1d9b339866b81e69c522021b031c4c3", "2d3065606a444c0b711c1809ce296db2ba56cb0c", "70f70d821c2c5225edeb54a8af0bd1911a51fc89", "e642a325b91ab829aefa47708c7b4b45811d2d23", "a692cbab92e9bae53c9313241d9668d9de97ed8b", "e7de1cf04925b7e1d06f9add83d90760e5a7a241", "7a368b4b196af02190e6e57f18f56598ee32a626", "9a5da5f9a023e53bf339748b5b7b17278ae36475", "3cdc62c132846a6e5fd06782730a986b3e5ea144", "5f6ec6cc7fd303702dca31edd2317b33c6e66bd1", "06467dc8c20e6c7357c19904f6214bb406262ba2", "726ec30fdd47bccd2a2be021cf6a6b848e9ee28b", "d9d20aa1db311424336a564ae55367b4e20c4c9d", "ce4c3d52e0dfb59ee758b77450ae13b196488c95", "26001aacfeff519ccd6b0991b0cc38ab10ad6564", "c0b19e0c5c2bbf1fb24e154466b6cc0fa6b5e74a", "531bddd5b43d2f0b3afbe0051642830c47030652", "156e682e3d4bb27338418602c1c3c530da6ff7bd", "cb899167421d7a24f1e6c3f06aa4a621391df15f", "864b8a7c8044349b29b0e67952323a8cb2bc2dd4", "01f935cdebd582211e6d4eef9f81da4228412911", "8d82be71dfce6b6752717ca0a68a77ec3c03032b", "3b0603f526fda4715904bfe2d1bba8a79458f00c", "be3c3954e6bd940baf4b8d21b4b6a87a71f76ee6", "1104deeaa589b01548bfa6c411e090e345f132f8", "7b75017bbb70c9f159b35f7399e29dc53bdc3d97", "957f8fc0afbeca638f13f6fb739e49a921da2b9d", "85551f9be5b054fa4aee0636597b12bda2ecb2e8", "d45d5df963cc3f7641d6dec2920bb22cfe8d0a76", "5a02a05eddb59aee6ac762f7ea82e92a68eb9c0f"]
configuration_file = ["core-default.cfg", "prod1.cfg", "prod2.cfg"]

DEBUG_PREFIX="===============[uRTS Evaluation: "

# Clone testing project
def clone():
    if os.path.exists(project_root_path):
        shutil.rmtree(project_root_path)
    clone_cmd = "git clone " + project_url
    os.system(clone_cmd)


# Modify Get/Set API
def modify_api():
    lines = []
    with open(api_file1_path, 'r') as f:
        for line in f:
            lines.append(line)
    
    with open(api_file1_path, 'w') as f:
        for line in lines:
            if 'import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;' in line:
                f.write(line)
                f.write('import org.urts.configAware.*;\n')
            elif 'public InetSocketAddress getClientPortAddress() {' in line:
                f.write(line)
                f.write('        if (clientPortAddress != null) ConfigListener.addGetConfig("clientPortAddress", clientPortAddress.toString());\n')
                f.write('        else ConfigListener.addGetConfig("clientPortAddress", "null");\n')
            elif 'public InetSocketAddress getSecureClientPortAddress() {' in line:
                f.write(line)
                f.write('        if (secureClientPortAddress != null) ConfigListener.addGetConfig("secureClientPortAddress", secureClientPortAddress.toString());\n')
                f.write('        else ConfigListener.addGetConfig("secureClientPortAddress", "null");\n')
            elif 'public File getDataDir() {' in line:
                f.write(line)
                f.write('        if (dataDir != null) ConfigListener.addGetConfig("dataDir", dataDir.toString());\n')
                f.write('        else ConfigListener.addGetConfig("dataDir", "null");\n')
            elif 'public File getDataLogDir() {' in line:
                f.write(line)
                f.write('        if (dataLogDir != null) ConfigListener.addGetConfig("dataLogDir", dataLogDir.toString());\n')
                f.write('        else ConfigListener.addGetConfig("dataLogDir", "null");\n')
            elif 'public int getTickTime() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("tickTime", String.valueOf(tickTime));\n')
            elif 'public int getMaxClientCnxns() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("maxClientCnxns", String.valueOf(maxClientCnxns));\n')
            elif 'public int getMinSessionTimeout() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("minSessionTimeout", String.valueOf(minSessionTimeout == -1 ? tickTime * 2 : minSessionTimeout));\n')
            elif 'public int getMaxSessionTimeout() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("maxSessionTimeout", String.valueOf(maxSessionTimeout == -1 ? tickTime * 20 : maxSessionTimeout));\n')
            elif 'public long getJvmPauseInfoThresholdMs() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("jvm.pause.info-threshold.ms", String.valueOf(jvmPauseInfoThresholdMs));\n')
            elif 'public long getJvmPauseWarnThresholdMs() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("jvm.pause.warn-threshold.ms", String.valueOf(jvmPauseWarnThresholdMs));\n')
            elif 'public long getJvmPauseSleepTimeMs() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("jvm.pause.sleep.time.ms", String.valueOf(jvmPauseSleepTimeMs));\n')
            elif 'public boolean isJvmPauseMonitorToRun() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("jvm.pause.monitor", String.valueOf(jvmPauseMonitorToRun));\n')
            elif 'public String getMetricsProviderClassName() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("metricsProvider.className", String.valueOf(metricsProviderClassName));\n')
            elif 'public int getClientPortListenBacklog() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("listenBacklog", String.valueOf(listenBacklog));\n')
            else:
                f.write(line)

    lines = []
    func = '        if(isFirstThreadAndFlip()) {\n' \
           '            injectError(true);\n' \
           '        } else {\n' \
           '            injectError(false);\n' \
           '        }\n' \
           '    }\n' \
           '\n' \
           '    private synchronized boolean isFirstThreadAndFlip() {\n' \
           '        boolean tmp = isFirstThread;\n' \
           '        isFirstThread = false;\n' \
           '        return tmp;\n' \
           '    }\n' \
           '\n' \
           '    public void injectError(boolean inj_env_param) throws ConfigException {\n' \
           '        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0]; //UNIFY_TESTS\n' \
           '        String injectionFile = "ctest-" + pid + ".cfg";\n' \
           '        try {\n' \
           '            Properties cfg = new Properties();\n' \
           '            FileInputStream in = new FileInputStream(injectionFile);\n' \
           '            try {\n' \
           '                cfg.load(in);\n' \
           '            } finally {\n' \
           '                in.close();\n' \
           '            }\n' \
           '            parseProperties(cfg, true, inj_env_param, false);\n' \
           '        } catch (IOException e) {\n' \
           '            throw new ConfigException("[CTEST] Error injecting from " + injectionFile, e);\n' \
           '        } catch (IllegalArgumentException e) {\n' \
           '            throw new ConfigException("[CTEST] Error injecting from " + injectionFile, e);\n' \
           '        }\n' \
           '    }\n' \
           '\n' \
           '    public void parseProperties(Properties zkProp) throws IOException, ConfigException {\n' \
           '        parseProperties(zkProp, false, true, true);\n' 
    func_counter = 0

    with open(api_file2_path, 'r') as f:
        for line in f:
            lines.append(line)
    
    with open(api_file2_path, 'w') as f:
        for line in lines:
            if 'import org.apache.zookeeper.server.util.VerifyingFileFactory;' in line:
                f.write(line)
                f.write('import org.urts.configAware.ConfigListener;\n')
                f.write("import java.lang.management.ManagementFactory;\n")
            elif 'private final int MIN_SNAP_RETAIN_COUNT = 3;' in line:
                f.write(line)
                f.write('    private static boolean isFirstThread = true;\n')
            elif 'LOG.warn("NextQuorumVerifier is initiated to null");' in line:
                f.write(line)
                func_counter += 1
            elif '    public void parseProperties(Properties zkProp) throws IOException, ConfigException {' in line:
                f.write('    public void parseProperties(Properties zkProp, boolean ctest_inj , boolean inj_env_param, boolean inj_cli_port) throws IOException, ConfigException {\n')
            elif '            if (key.equals("dataDir")) {' in line:
                f.write('            if (key.equals("dataDir") && inj_env_param) {\n')
            elif '            } else if (key.equals("dataLogDir")) {' in line:
                f.write('            } else if (key.equals("dataLogDir") && inj_env_param) {\n')
            elif '            } else if (key.equals("clientPort")) {' in line:
                f.write('            } else if (key.equals("clientPort") && inj_env_param && inj_cli_port) {\n')
            elif '            } else if (key.equals("clientPortAddress")' in line:
                f.write('            } else if (key.equals("clientPortAddress") && inj_env_param && inj_cli_port) {\n')
            elif '            } else if (key.equals("secureClientPort")' in line:
                f.write('            } else if (key.equals("secureClientPort") && inj_env_param && inj_cli_port) {\n')
            elif '            } else if (key.equals("secureClientPortAddress")' in line:
                f.write('            } else if (key.equals("secureClientPortAddress") && inj_env_param && inj_cli_port) {\n')
            elif '            } else if (key.equals("dynamicConfigFile")' in line:
                f.write('            } else if (key.equals("dynamicConfigFile") && inj_env_param) {\n')
            elif 'setStandaloneEnabled(parseBoolean(key, value));' in line:
                f.write('                setStandaloneEnabled2(parseBoolean(key, value));\n')
            elif 'setReconfigEnabled(parseBoolean(key, value));' in line:
                f.write('                setReconfigEnabled2(parseBoolean(key, value));\n')
            elif 'if (dataDir == null) {' in line:
                f.write('        if (inj_env_param) {\n')
                f.write(line)
            elif 'if (clientPort == 0) {' in line:
                f.write('        }\n')
                f.write('        if (inj_env_param && inj_cli_port) {\n')
                f.write(line)
            elif 'if (secureClientPort == 0) {' in line:
                f.write('        }\n')
                f.write('        if (inj_env_param && inj_cli_port) {\n')
                f.write(line)
            elif 'if (observerMasterPort <= 0) {' in line:
                f.write('        }\n')
                f.write(line)
            elif 'if (dynamicConfigFileStr == null) {' in line:
                f.write('        if (ctest_inj) return;\n')
                f.write(line)
            elif 'public InetSocketAddress getClientPortAddress() {' in line:
                f.write(line)
                f.write('        if (clientPortAddress != null) ConfigListener.addGetConfig("clientPortAddress", clientPortAddress.toString());\n')
                f.write('        else ConfigListener.addGetConfig("clientPortAddress", "null");\n')
            elif 'public InetSocketAddress getSecureClientPortAddress() {' in line:
                f.write(line)
                f.write('        if (secureClientPortAddress != null) ConfigListener.addGetConfig("secureClientPortAddress", secureClientPortAddress.toString());\n')
                f.write('        else ConfigListener.addGetConfig("secureClientPortAddress", "null");\n')
            elif 'public int getObserverMasterPort() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("observerMasterPort", String.valueOf(observerMasterPort));\n')
            elif 'public File getDataDir() {' in line:
                f.write(line)
                f.write('        if (dataDir != null) ConfigListener.addGetConfig("dataDir", dataDir.toString());\n')
                f.write('        else ConfigListener.addGetConfig("dataDir", "null");\n')
            elif 'public File getDataLogDir() {' in line:
                f.write(line)
                f.write('        if (dataLogDir != null) ConfigListener.addGetConfig("dataLogDir", dataLogDir.toString());\n')
                f.write('        else ConfigListener.addGetConfig("dataLogDir", "null");\n')
            elif 'public int getTickTime() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("tickTime", String.valueOf(tickTime));\n')
            elif 'public int getMaxClientCnxns() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("maxClientCnxns", String.valueOf(maxClientCnxns));\n')
            elif 'public int getMinSessionTimeout() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("minSessionTimeout", String.valueOf(minSessionTimeout));\n')
            elif 'public int getMaxSessionTimeout() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("maxSessionTimeout", String.valueOf(maxSessionTimeout));\n')
            elif 'public String getMetricsProviderClassName() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("metricsProvider.className", metricsProviderClassName);\n')
            elif 'public boolean areLocalSessionsEnabled() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("localSessionsEnabled", String.valueOf(localSessionsEnabled));\n')
            elif 'public boolean isLocalSessionsUpgradingEnabled() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("localSessionsUpgradingEnabled", String.valueOf(localSessionsUpgradingEnabled));\n')
            elif 'public boolean isSslQuorum() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("sslQuorum", String.valueOf(sslQuorum));\n')
            elif 'public boolean shouldUsePortUnification() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("portUnification", String.valueOf(shouldUsePortUnification));\n')
            elif 'public int getClientPortListenBacklog() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("clientPortListenBacklog", String.valueOf(clientPortListenBacklog));\n')
            elif 'public int getInitLimit() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("initLimit", String.valueOf(initLimit));\n')
            elif 'public int getSyncLimit() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("syncLimit", String.valueOf(syncLimit));\n')
            elif 'public int getConnectToLearnerMasterLimit() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("connectToLearnerMasterLimit", String.valueOf(connectToLearnerMasterLimit));\n')
            elif 'public int getElectionAlg() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("electionAlg", String.valueOf(electionAlg));\n')
            elif 'public int getSnapRetainCount() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("autopurge.snapRetainCount", String.valueOf(snapRetainCount));\n')
            elif 'public int getPurgeInterval() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("autopurge.purgeInterval", String.valueOf(purgeInterval));\n')
            elif 'public boolean getSyncEnabled() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("syncEnabled", String.valueOf(syncEnabled));\n')
            elif 'public long getJvmPauseInfoThresholdMs() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("jvm.pause.info-threshold.ms", String.valueOf(jvmPauseInfoThresholdMs));\n')
            elif 'public long getJvmPauseWarnThresholdMs() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("jvm.pause.warn-threshold.ms", String.valueOf(jvmPauseWarnThresholdMs));\n')
            elif 'public long getJvmPauseSleepTimeMs() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("jvm.pause.sleep.time.ms", String.valueOf(jvmPauseSleepTimeMs));\n')
            elif 'public boolean isJvmPauseMonitorToRun() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("jvm.pause.monitor", String.valueOf(jvmPauseMonitorToRun));\n')
            elif 'public LearnerType getPeerType() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("peerType", String.valueOf(peerType));\n')
            elif 'public Boolean getQuorumListenOnAllIPs() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("quorumListenOnAllIPs", String.valueOf(quorumListenOnAllIPs));\n')
            elif 'public boolean isMultiAddressEnabled() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("multiAddress.enabled", String.valueOf(multiAddressEnabled));\n')
            elif 'public boolean isMultiAddressReachabilityCheckEnabled() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("multiAddress.reachabilityCheckEnabled", String.valueOf(multiAddressReachabilityCheckEnabled));\n')
            elif 'public int getMultiAddressReachabilityCheckTimeoutMs() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("multiAddress.reachabilityCheckTimeoutMs", String.valueOf(multiAddressReachabilityCheckTimeoutMs));\n')
            elif 'public static boolean isStandaloneEnabled() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("standaloneEnabled", String.valueOf(standaloneEnabled));\n')
            elif 'public static void setStandaloneEnabled(boolean enabled) {' in line:
                f.write(line)
                f.write('        ConfigListener.addSetConfig("standaloneEnabled");\n' \
                        '        standaloneEnabled = enabled;\n' \
                        '    }\n' \
                        '    public static void setStandaloneEnabled2(boolean enabled) {\n' ) 
            elif 'public static boolean isReconfigEnabled() {' in line:
                f.write(line)
                f.write('        ConfigListener.addGetConfig("reconfigEnabled", String.valueOf(reconfigEnabled));\n')
            elif 'public static void setReconfigEnabled(boolean enabled) {' in line:
                f.write(line)
                f.write('        ConfigListener.addSetConfig("reconfigEnabled");\n' \
                        '        reconfigEnabled = enabled;\n' \
                        '    }\n' \
                        '    public static void setReconfigEnabled2(boolean enabled) {\n')
            else: 
                f.write(line)

            
            if func_counter > 0:
                func_counter += 1
                if func_counter == 5:
                    f.write(func)
                    func_counter = 0
            
    lines = []
    with open(api_file3_path, 'r') as f:
        for line in f:
            lines.append(line)
    
    with open(api_file3_path, 'w') as f:
        for line in lines:
            if 'import org.apache.zookeeper.util.ServiceUtils;' in line:
                f.write(line)
                f.write('import org.urts.configAware.ConfigListener;\n')
            elif 'quorumPeer.setQuorumListenOnAllIPs(config.getQuorumListenOnAllIPs());' in line:
                f.write(line)
                f.write('            ConfigListener.addGetConfig("sslQuorumReloadCertFiles", String.valueOf(config.sslQuorumReloadCertFiles));\n')
            elif 'quorumPeer.setQuorumSaslEnabled(config.quorumEnableSasl);' in line:
                f.write(line)
                f.write('            ConfigListener.addGetConfig("quorum.auth.enableSasl", String.valueOf(quorumPeer.isQuorumSaslAuthEnabled()));\n')
            elif 'if (quorumPeer.isQuorumSaslAuthEnabled()) {' in line:
                f.write(line)
                f.write('                ConfigListener.addGetConfig("quorum.auth.serverRequireSasl", String.valueOf(config.quorumServerRequireSasl));\n')
            elif 'quorumPeer.setQuorumServerSaslRequired(config.quorumServerRequireSasl);' in line:
                f.write(line)
                f.write('                ConfigListener.addGetConfig("quorum.auth.learnerRequireSasl", String.valueOf(config.quorumLearnerRequireSasl));\n')
            elif 'quorumPeer.setQuorumLearnerSaslRequired(config.quorumLearnerRequireSasl);' in line:
                f.write(line)
                f.write('                ConfigListener.addGetConfig("quorum.auth.kerberos.servicePrincipal", String.valueOf(config.quorumServicePrincipal));\n')
            elif 'quorumPeer.setQuorumServicePrincipal(config.quorumServicePrincipal);' in line:
                f.write(line)
                f.write('                ConfigListener.addGetConfig("quorum.auth.server.saslLoginContext", String.valueOf(config.quorumServerLoginContext));\n')
            elif 'quorumPeer.setQuorumServerLoginContext(config.quorumServerLoginContext);' in line:
                f.write(line)
                f.write('                ConfigListener.addGetConfig("quorum.auth.learner.saslLoginContext", String.valueOf(config.quorumLearnerLoginContext));\n')
            elif 'quorumPeer.setQuorumCnxnThreadsSize(config.quorumCnxnThreadsSize);' in line:
                f.write('            ConfigListener.addGetConfig("quorum.cnxn.threads.size", String.valueOf(config.quorumCnxnThreadsSize));\n')
                f.write(line)
            else:
                f.write(line)
    return


# Add uRTS to pom file
def modify_pom():
    plugin = "<plugin>\n" \
             "<groupId>org.urts</groupId>\n" \
             "<artifactId>urts-maven-plugin</artifactId>\n" \
             "<version>1.0.0-SNAPSHOT</version>\n" \
             "</plugin>\n"
    lines = []
    meet_plugins = False
    with open(module_pom_file_path, 'r') as f:
        for line in f:
            lines.append(line)

    with open(module_pom_file_path, 'w') as f:
        for line in lines:
            f.write(line)
            if "<plugins>" in line:
                meet_plugins = True
                f.write(plugin)

    dependency = "<dependency>\n" \
                 "<groupId>org.urts</groupId>\n" \
                 "<artifactId>org.urts.core</artifactId>\n" \
                 "<version>1.0.0-SNAPSHOT</version>\n" \
                 "<scope>compile</scope>\n" \
                 "</dependency>\n"
    lines = []
    meet_dependencies = False
    with open(api_pom_file_path, 'r') as f:
        for line in f:
            lines.append(line)

    with open(api_pom_file_path, 'w') as f:
        for line in lines:
            f.write(line)
            if "<dependencies>" in line:
                meet_dependencies = True
                f.write(dependency)

    if not meet_plugins or not meet_dependencies:
        raise ValueError("Failed to modify pom file, has <plugins>? " + str(meet_plugins) + " has <dependencies>? " + str(meet_dependencies))


# Run tests
def run_urts(config_file, curConfig, curCommit):
    os.chdir(project_module_path)
    shutil.copy(config_file, "curConfigFile.cfg")
    print(DEBUG_PREFIX + "RUN uRTS]=================", flush=True)
    start = time.time()
    os.system(mvn_cmd)
    end = time.time()
    record_time_and_number("zookeeper", "URTS", time_number_file_path, end - start, curConfig, curCommit)
    os.chdir(cur_path)


# Install dependency module
def maven_install_module():
    os.chdir(project_root_path)
    os.system("mvn install -DskipTests -am -pl " + project_module_name)
    os.chdir(cur_path)


# Prepare config file
def copy_production_config_file(replaced_config_file_path, original_config_file_path):
    shutil.copy(replaced_config_file_path, original_config_file_path)


# Prepare uRTS config file
def prepare_urtsrc_file(config_name):
    source_path = os.path.join(cur_path, ".urtsrc-" + config_name)
    target_path = os.path.join(project_module_path, ".urtsrc")
    shutil.copy(source_path, target_path)


# Checkout to certain version
def checkout_commit(commit):
    os.chdir(project_root_path)
    os.system("git checkout -f " + commit)
    os.chdir(cur_path)


# Prepare test that used to get config values
def copy_get_config_value_test():
    source_path = os.path.join(cur_path, "TestGetConfigValueForConfigAware.java")
    target_path = os.path.join(test_copied_path, "TestGetConfigValueForConfigAware.java")
    shutil.copy(source_path, target_path)


# Prepare mapping
def copy_ctest_mapping():
    source_path = os.path.join(cur_path, "mapping")
    target_path = os.path.join(project_module_path, "mapping")
    shutil.copy(source_path, target_path)


# Prepare environment and all files
def do_preparation(commit):
    checkout_commit(commit)
    modify_pom()
    modify_api()
    copy_ctest_mapping()
    copy_get_config_value_test()
    maven_install_module()


def copy_cofig_value(curCommit, cur_config_name):
    source_path = os.path.join(project_module_path, ".ConfigValue")
    target_path = os.path.join(cur_path, "config_value", "ConfigValue-" + cur_config_name + "-" + curCommit)
    shutil.copy(source_path, target_path)


def run():
    clone()
    for i in range(len(commits)):
        curCommit = commits[i]
        do_preparation(curCommit)
        for curConfig in configuration_file:
            cur_config_name = curConfig.split(".")[0]
            config_file_name = curConfig + "-" + curCommit
            print(DEBUG_PREFIX + curCommit + " Config: " + cur_config_name + " ]===============", flush=True)
            replacedConfigFilePath = os.path.join(cur_path, "../../config_files/zookeeper/", config_file_name)
            targetConfigFilePath = os.path.join(project_module_path, curConfig)
            if not os.path.exists(replacedConfigFilePath):
                ValueError("Does not have configuration file: " + replacedConfigFilePath)
            copy_production_config_file(replacedConfigFilePath, targetConfigFilePath)
            prepare_urtsrc_file(cur_config_name)
            run_urts(replacedConfigFilePath, curConfig, curCommit)
            copy_dependency_folder_urts("zookeeper", project_module_path, cur_path, curCommit, cur_config_name, i)


if __name__ == '__main__':
    run()
