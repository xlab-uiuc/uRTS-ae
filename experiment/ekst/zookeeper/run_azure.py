import os, shutil, time, sys
sys.path.append("../../")
from util import *

cur_path = os.getcwd()
non_ctest_list = "nonCtestList"
project_url = "https://github.com/apache/zookeeper.git"
project_path = os.path.join(cur_path, "zookeeper")
project_module = "zookeeper-server"
project_module_path = os.path.join(project_path, project_module)
api_file_path = os.path.join(project_module_path, "src/main/java/org/apache/zookeeper/server/quorum/QuorumPeerConfig.java")
pom_file_path = os.path.join(project_module_path, "pom.xml")
time_number_file_path = os.path.join(cur_path, "time_number.txt")
#test_class_num_file_path = os.path.join(cur_path, "test_class_num.txt")
ctest_configuration_file_path = os.path.join(project_module_path, "ctest.cfg")
production_configuration_file_path = os.path.join(project_module_path, "production-configuration.cfg")
commits = ["acbfb2d78996cbaa29b9dc764d4cb3463193dc45", "9213f7353b1e6ce4d0fdbc1dca963ace1fd32cec", "ad5513b8dd15637c6c95585603a4e055dabad56d", "061438e83e61692fc9e06e057a739db5327d42b9", "7fdadf7273f34dd0552db25a3771cf55b65e9208", "ea75e1f63572fc72fb8520ba8c793523047acb49", "c583a6e79654359b5daad5093d1730e370d3b75b", "1590a424cb7a8768b0ae01f2957856b1834dd68d", "f8ebf1a25281b2c7f48e08011918c72643313209", "96d87e2809f92460f182c66311d83d59f2ab96c3", "f5c29aab9adb49d9d3580074d4d1a805579fa20d", "766e173e9d51b6354920ebc136b246d221b87ec1", "b79abb2014db9684c00afd7d98c0a7cb69ce6c8b", "c499202a2c470e2e365ef109c8e49784cb043367", "5e787c5990091b2d1fc560eba88d3c25b04690a2", "32e40e8cee36ddba1973875ab6637912719271fa", "5e6e15ac40cfd2cadac3e718e494a3c13b934b8d", "525a28bd1cb864a5783593c64908cfe0ddcba65f", "2f00dea17bd94bf43b2f6647f6b08b00ff8b7ece", "9442ce23bade5286459ff78ade1af173f7b69c30", "16187c48a1d9b339866b81e69c522021b031c4c3", "2d3065606a444c0b711c1809ce296db2ba56cb0c", "70f70d821c2c5225edeb54a8af0bd1911a51fc89", "e642a325b91ab829aefa47708c7b4b45811d2d23", "a692cbab92e9bae53c9313241d9668d9de97ed8b", "e7de1cf04925b7e1d06f9add83d90760e5a7a241", "7a368b4b196af02190e6e57f18f56598ee32a626", "9a5da5f9a023e53bf339748b5b7b17278ae36475", "3cdc62c132846a6e5fd06782730a986b3e5ea144", "5f6ec6cc7fd303702dca31edd2317b33c6e66bd1", "06467dc8c20e6c7357c19904f6214bb406262ba2", "726ec30fdd47bccd2a2be021cf6a6b848e9ee28b", "d9d20aa1db311424336a564ae55367b4e20c4c9d", "ce4c3d52e0dfb59ee758b77450ae13b196488c95", "26001aacfeff519ccd6b0991b0cc38ab10ad6564", "c0b19e0c5c2bbf1fb24e154466b6cc0fa6b5e74a", "531bddd5b43d2f0b3afbe0051642830c47030652", "156e682e3d4bb27338418602c1c3c530da6ff7bd", "cb899167421d7a24f1e6c3f06aa4a621391df15f", "864b8a7c8044349b29b0e67952323a8cb2bc2dd4", "01f935cdebd582211e6d4eef9f81da4228412911", "8d82be71dfce6b6752717ca0a68a77ec3c03032b", "3b0603f526fda4715904bfe2d1bba8a79458f00c", "be3c3954e6bd940baf4b8d21b4b6a87a71f76ee6", "1104deeaa589b01548bfa6c411e090e345f132f8", "7b75017bbb70c9f159b35f7399e29dc53bdc3d97", "957f8fc0afbeca638f13f6fb739e49a921da2b9d", "85551f9be5b054fa4aee0636597b12bda2ecb2e8", "d45d5df963cc3f7641d6dec2920bb22cfe8d0a76", "5a02a05eddb59aee6ac762f7ea82e92a68eb9c0f"]
configuration_list = ["core-default.cfg", "prod1.cfg", "prod2.cfg"]
mvn_cmd = "mvn ekstazi:ekstazi -DfailIfNoTests=false | tee out.txt"
mvn_clean_cmd = "mvn ekstazi:clean"
prod1_config_changed_commit = ["061438e83e61692fc9e06e057a739db5327d42b9", "ea75e1f63572fc72fb8520ba8c793523047acb49", "1590a424cb7a8768b0ae01f2957856b1834dd68d", "b79abb2014db9684c00afd7d98c0a7cb69ce6c8b", "70f70d821c2c5225edeb54a8af0bd1911a51fc89", "e642a325b91ab829aefa47708c7b4b45811d2d23", "e7de1cf04925b7e1d06f9add83d90760e5a7a241", "d9d20aa1db311424336a564ae55367b4e20c4c9d", "156e682e3d4bb27338418602c1c3c530da6ff7bd", "85551f9be5b054fa4aee0636597b12bda2ecb2e8"]
prod2_config_changed_commit = ["ad5513b8dd15637c6c95585603a4e055dabad56d", "7fdadf7273f34dd0552db25a3771cf55b65e9208", "f5c29aab9adb49d9d3580074d4d1a805579fa20d", "5e787c5990091b2d1fc560eba88d3c25b04690a2", "32e40e8cee36ddba1973875ab6637912719271fa", "525a28bd1cb864a5783593c64908cfe0ddcba65f", "2f00dea17bd94bf43b2f6647f6b08b00ff8b7ece", "16187c48a1d9b339866b81e69c522021b031c4c3", "726ec30fdd47bccd2a2be021cf6a6b848e9ee28b", "d9d20aa1db311424336a564ae55367b4e20c4c9d"]
config_changed_commit = {"prod1":prod1_config_changed_commit, "prod2":prod2_config_changed_commit}

DEBUG_PREFIX="===============[Ekstazi-ext Evaluation: "

# Clone testing project
def clone():
    if os.path.exists(project_path):
        shutil.rmtree(project_path)
    clone_cmd = "git clone " + project_url
    os.system(clone_cmd)


# Copy the production configuration to the project for configuration tests
def copy_production_config_file(config_file_name):
    replaced_config_file_path = os.path.join(cur_path, "../../config_files/zookeeper/", config_file_name)
    shutil.copy(replaced_config_file_path, production_configuration_file_path)


# Run tests
def run_test(curConfig, curCommit):
    os.chdir(project_module_path)
    start = time.time()
    os.system(mvn_cmd)
    end = time.time()
    record_time_and_number("zookeeper", "EKST", time_number_file_path, end - start, curConfig, curCommit)
    os.chdir(cur_path)


# Checkout Revision
def checkout_commit(commit):
    os.chdir(project_path)
    os.system("git checkout -f " + commit)
    os.chdir(cur_path)


# Install Project
def maven_install():
    os.chdir(project_path)
    os.system("mvn clean install -DskipTests -am -pl " + project_module)
    os.chdir(cur_path)


# Production configuration only execute ctest (i.e., configuration test)
# Exclude non-ctest in maven-surefire for production round
# Default round still runs the whole test suite (regular test + ctest)
def exclude_non_ctest():
    excludeLines = []
    with open(non_ctest_list, 'r') as f:
        excludeLines = f.readlines()
    pom_lines = []
    with open(pom_file_path, 'r') as f:
        for line in f:
            pom_lines.append(line)
    with open(pom_file_path, 'w') as f:
        surefire_pos_flag = False
        for line in pom_lines:
            f.write(line)
            if "<artifactId>maven-surefire-plugin</artifactId>" in line:
                surefire_pos_flag = True
            if surefire_pos_flag and "<configuration>" in line:
                f.write("".join(excludeLines))
                f.write("\n")
                surefire_pos_flag = False


# Add ekstazi maven plugin and use 'mvn ekstazi:ekstazi'
# with instrumented JUnit Runner to dynamically set 
# production configuration value for ctest.
def add_ekstazi_runner_pom():
    plugin = "<plugin>\n" \
             "<groupId>org.ekstazi</groupId>\n" \
             "<artifactId>ekstazi-maven-plugin</artifactId>\n" \
             "<version>5.3.1-SNAPSHOT</version>\n" \
             "</plugin>\n"
    lines = []
    with open(pom_file_path, 'r') as f:
        for line in f:
            lines.append(line)

    with open(pom_file_path, 'w') as f:
        for line in lines:
            f.write(line)
            if "<plugins>" in line:
                f.write(plugin)


# Add ctest configuration file to project API
def modify_config_api_to_add_ctest_file():
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
    with open(api_file_path, 'r') as f:
        for line in f:
            lines.append(line)
    with open(api_file_path, 'w') as f:
        for line in lines:
            if 'package org.apache.zookeeper.server.quorum;' in line:
                f.write(line)
                f.write('import java.lang.management.ManagementFactory;\n')
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
            else: 
                f.write(line)

            if func_counter > 0:
                func_counter += 1
                if func_counter == 5:
                    f.write(func)
                    func_counter = 0               

                
# Prepare injection file
def create_empty_config_file_for_running_ctest():
    source_path = os.path.join(cur_path, "ctest.cfg")
    shutil.copy(source_path, ctest_configuration_file_path)


# Prepare mapping
def copy_config_mapping():
    source_path = os.path.join(cur_path, "mapping")
    target_path = os.path.join(project_module_path, "mapping")
    shutil.copy(source_path, target_path)


# Prepare ekstazi config file
def prepare_ekstazi_config_file():
    source_path = os.path.join(cur_path, ".ekstazirc")
    target_path = os.path.join(project_module_path, ".ekstazirc")
    shutil.copy(source_path, target_path)


def mimic_config_file_change(curCommit, cur_config_name):
    if commits.index(curCommit) < commits.index(config_changed_commit[cur_config_name][0]):
        return
    elif commits.index(curCommit) >= commits.index(config_changed_commit[cur_config_name][-1]):
        added_commit_sha = config_changed_commit[cur_config_name][-1]
    else:  
        for i in range(len(config_changed_commit[cur_config_name]) - 1):
            if (commits.index(curCommit) >= commits.index(config_changed_commit[cur_config_name][i])) and  (commits.index(curCommit) < commits.index(config_changed_commit[cur_config_name][i+1])):
                added_commit_sha = config_changed_commit[cur_config_name][i]
    with open(api_file_path, 'r') as f:
        lines = f.readlines()
    with open(api_file_path, 'w') as f:
        for line in lines:
            stringModified = "        LOG.info(\"Reading configuration from: {}\" + path);\n".format(added_commit_sha)
            if "LOG.info(\"Reading configuration from:" in line:
                f.write(stringModified)
            else:
                f.write(line)


# Prepare environment and all files
def do_preparation(curCommit, cur_config_name):
    checkout_commit(curCommit)
    modify_config_api_to_add_ctest_file()
    create_empty_config_file_for_running_ctest()
    copy_config_mapping()
    prepare_ekstazi_config_file()
    add_ekstazi_runner_pom()
    if cur_config_name != "core-default":
        mimic_config_file_change(curCommit, cur_config_name)
    maven_install()
    

def clean_dependency_folder():
    os.chdir(project_module_path)
    os.system(mvn_clean_cmd)
    os.chdir(cur_path)


def run():
    clone()
    for i in range(len(configuration_list)):
        for curCommit in commits:
            curConfig = configuration_list[i]
            cur_config_name = curConfig.split(".")[0]
            config_file_name = curConfig + "-" + curCommit
            do_preparation(curCommit, cur_config_name)
            if cur_config_name != "core-default":
                exclude_non_ctest()
            copy_production_config_file(config_file_name)
            print(DEBUG_PREFIX + curCommit + " Config: " + cur_config_name + " ===============", flush=True)
            run_test(curConfig, curCommit)
            copy_dependency_folder_ekstazi("zookeeper", "EKST", project_module_path, cur_path, curCommit, cur_config_name, i)
        clean_dependency_folder()


if __name__ == '__main__':
    run()