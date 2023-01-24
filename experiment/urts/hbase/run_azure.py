import os, shutil, time, sys
sys.path.append("../../")
from util import *

cur_path = os.getcwd()
hadoop_url = "https://github.com/apache/hadoop.git"
hbase_url = "https://github.com/apache/hbase.git"
hadoop_root_path = os.path.join(cur_path, "hadoop")
hbase_root_path = os.path.join(cur_path, "hbase")
project_module_name = "hbase-server"
project_module_path = os.path.join(hbase_root_path, project_module_name)
module_pom_file_path = os.path.join(project_module_path, "pom.xml")
hadoop_api_file_path = os.path.join(hadoop_root_path, "hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/conf/Configuration.java")
hadoop_api_pom_file_path = os.path.join(hadoop_root_path, "hadoop-common-project/hadoop-common/pom.xml")
hbase_api_file_path = os.path.join(hbase_root_path, "hbase-common/src/main/java/org/apache/hadoop/hbase/HBaseConfiguration.java")
test_copied_path = os.path.join(project_module_path, "src/test/java/org/apache/hadoop/hbase")
time_number_file_path = os.path.join(cur_path, "time_number.txt")
#test_class_num_file_path = os.path.join(cur_path, "test_class_num.txt")
regular_test_list = os.path.join(cur_path, "regTestList")
mvn_cmd = "mvn urts:urts -DgetterClass=TestGetConfigValueForConfigAware -DfailIfNoTests=false -Dmaven.test.failure.ignore=true | tee out.txt"
commits = ["a6329385a012aa8009ed0561a340e8a752b77a6a", "43455061d3ee2ce3152932c771e6572586819d26", "3cc539a561c4d35dc661c155768ffbfe2d2f073f", "c42d6ccc1a8950c14b349acfdca376040a5ce052", "52d1dc09153f732750977d90a068147834b79cba", "a372d437b3fd82b2965620b1eb31c1b24cd0dd51", "09ffa49df8c50e8278e2bfd070b4f6723dd74f20", "4149459ddef47b877d1c1f2aa1d4f4aebcd11ece", "67967f265f62d0f5319f11f822c485eb3299c802", "446630b71323d60e0fa5e54616cf9449d299575b", "d23a5b5aac4fd2b9bf7d0b4e4b46a8a68b9f5195", "35264037efda5be27ebadc3755a92e1bc9988ebb", "8392dc0ad2039805262354e3335f528961f82094", "1487d62d2119b4da3a2d71da2244d27e70ff92e9", "f382086ca294475804b2417457172dfed1e647ac", "2a318009db48dc6c8ce6fd1180cfed951b8550b1", "98259c679a19277ff0b918a18bff1c1734d7181f", "cc701e2c2859f224a8329d0e4e6f95d256cc3ef0", "0b7630bc1f392832eba36a62ef273d603fe75b56", "8ea548441c347590baab25538a88126e9523a44a", "71bd65c1f28a3815770a7e2d17f0f571237600ad", "a2c4cb67a21186f41fc7f9be3b05be7f88f3ea8a", "6057265388240b49273bc8e32633c2073dea060a", "9ccbd8c6682159881cb39d202dc138b082b54425", "563a404ac152541931b96cdcffc7c446b668c26b", "230ce3e6b58347c30e792e4cb8902fc56286f874", "455f7bfd7b1ba1d3e3e1afd99c6c5182cdf1056d", "5dd23e5b8c7bc965e8a079dd025bd4c904998b2d", "801f9926c54d93b29f4a6b6bbb710390f1b0e73b", "7ce597d34f8ccf3fbc2fa713cac4c0ff32c295ad", "24b700848f2c766750044b31a2a88dc777f20a92", "9b627905c7e6ffaea918b874cf88ebd1ff7a88c8", "e4ad6e9f647dc07604b42733eecdff24dd30c519", "60e263e03b7b0d2d0b4642d5e788aa0593e075c8", "aa2b807026182fb176150bb0e2bcfeb640b2363d", "f86bff0f53877176b16f87e17fe6fe97a2cbd583", "b9b075f21a4276f080fafdf7e4dea566dc056207", "f5af7611c0dbbb5ea7d65ef5d916fa363aa0e82c", "1408c044b196d1a919f78c45829076f270cdeca7", "06ef9e9778ae2e1747b44bf994b241b890f07895", "45347bb81a76c303d91e1012805190b3f1accb2a", "02fa0903a206bbcc42b7daaadb66e428e0a4184e", "2382a70de56693cb2d618ea14ea15159ba95dbdb", "e766dd7b29440c7b5b60dbeb19e69a2924f0765c", "e64b089ed16b17e9ebe6f5441f74c19aa7d0b170", "17750a72f5d098f75057b794688e4c3779fbde2d", "6d2faf43233aa7bde346dde0f81e4d9464872850", "7957b0cc7e0e8b162441df4d6152a544346682f6", "c49f7f63fca144765bf7c2da41791769286dfccc", "8fa8344a8c0c72d167ce92b5f44f88beb43f325e"]
configuration_file = ["core-default.xml", "prod1.xml", "prod2.xml"]

DEBUG_PREFIX="===============[uRTS Evaluation: "

# Clone testing project
def clone():
    if os.path.exists(hadoop_root_path):
        shutil.rmtree(hadoop_root_path)
    if os.path.exists(hbase_root_path):
        shutil.rmtree(hbase_root_path)
    hadoop_clone_cmd = "git clone " + hadoop_url
    os.system(hadoop_clone_cmd)
    hbase_clone_cmd = "git clone " + hbase_url
    os.system(hbase_clone_cmd)


# Add uRTS to pom file
def modify_hbase_pom():
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


# Add ctest configuration resource file for injection
def modify_hbase_api():
    lines = []
    with open(hbase_api_file_path, 'r') as f:
        for line in f:
            lines.append(line)
    with open(hbase_api_file_path, 'w') as f:
        for line in lines:
            f.write(line)
            if "package org.apache.hadoop.hbase" in line:
                f.write("import java.lang.management.ManagementFactory;\n")
            if "conf.addResource(\"hbase-site.xml\");" in line:
                f.write('    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0]; //UNIFY_TESTS\n')
                f.write('    conf.addResource("hbase-ctest-" + pid + ".xml"); //UNIFY_TESTS\n')


# Run tests
def run_urts(config_file, curConfig, curCommit):
    os.chdir(project_module_path)
    shutil.copy(config_file, "curConfigFile.xml")
    print(DEBUG_PREFIX + "RUN uRTS]=================", flush=True)
    start = time.time()
    os.system(mvn_cmd)
    end = time.time()
    record_time_and_number("hbase", "URTS", time_number_file_path, end - start, curConfig, curCommit)
    os.chdir(cur_path)


# Install dependency module
def maven_install_module():
    os.chdir(hbase_root_path)
    os.system("mvn install -DskipTests -am -pl hbase-common,hbase-server")
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
    os.chdir(hbase_root_path)
    os.system("git checkout -f " + commit)
    os.chdir(cur_path)


# Prepare test that used to get config values
def copy_get_config_value_test():
    print(test_copied_path)
    source_path = os.path.join(cur_path, "TestGetConfigValueForConfigAware.java")
    target_path = os.path.join(test_copied_path, "TestGetConfigValueForConfigAware.java")
    shutil.copy(source_path, target_path)


# Prepare mapping
def copy_ctest_mapping():
    source_path = os.path.join(cur_path, "mapping")
    target_path = os.path.join(project_module_path, "mapping")
    shutil.copy(source_path, target_path)


# Modify Get/Set API
def modify_hadoop_api():
    lines = []
    with open(hadoop_api_file_path, 'r') as f:
        for line in f:
            lines.append(line)

    with open(hadoop_api_file_path, 'w') as f:
        inGetFunc = False
        inGetDefFunc = False
        inSetFuc = False
        for line in lines:
            if "package org.apache.hadoop.conf;" in line:
                f.write(line)
                f.write("\nimport org.urts.configAware.*;\n")
            # Instrument get(name) or getRaw(name)
            elif "public String get(String name) {" in line or "public String getRaw(String name) {" in line:
                inGetFunc = True
                f.write(line)
                f.write("    String unifyParam = name; //UNIFY_TESTS\n")
            elif inGetFunc:
                if "for(String n : names) {" in line:
                    f.write(line)
                    f.write("      unifyParam = n; //UNIFY_TESTS\n")
                elif "return result;" in line:
                    f.write("    ConfigListener.recordGetConfig(unifyParam, result); //UNIFY_TESTS\n")
                    f.write(line)
                    inGetFunc = False
                else:
                    f.write(line)

            # Instrument get(name, defaultValue)
            elif "public String get(String name, String defaultValue) {" in line:
                inGetDefFunc = True
                f.write(line)
                f.write("    String unifyParam = name; //UNIFY_TESTS\n")
                f.write("    Boolean defaultFlag = true; //UNIFY_TESTS\n")
            elif inGetDefFunc:
                if "for(String n : names) {" in line:
                    f.write(line)
                    f.write("      unifyParam = n; //UNIFY_TESTS\n")
                elif "result = substituteVars(getProps().getProperty(n, defaultValue));" in line:
                    f.write(line)
                    f.write("      if (getProps().getProperty(n) != null) {defaultFlag = false;}\n")
                elif "return result;" in line:
                    f.write(
                        "    if (defaultFlag) { ConfigListener.recordGetConfig(unifyParam, result + \"@DEFAULTVALUE4CONFIGAWARE@\"); }\n")
                    f.write("    else { ConfigListener.recordGetConfig(unifyParam, result); }\n")
                    f.write(line)
                    inGetDefFunc = False
                else:
                    f.write(line)

            # Instrument set()
            elif "public void set(String name, String value, String source) {" in line:
                inSetFuc = True
                f.write(line)
            elif inSetFuc:
                if "name = name.trim();" in line:
                    f.write(line)
                    f.write("    ConfigListener.recordSetConfig(name, value); //UNIFY_TESTS\n")
                elif "if(!n.equals(name)) {" in line:
                    f.write(line)
                    f.write("            ConfigListener.recordSetConfig(name, value); //UNIFY_TESTS\n")
                elif "for(String n : names) {" in line:
                    f.write(line)
                    f.write("        ConfigListener.recordSetConfig(name, value); //UNIFY_TESTS\n")
                    inSetFuc = False
                else:
                    f.write(line)
            else:
                f.write(line)


# Add uRTS to hadoop-common pom file
def modify_hadoop_pom():
    dependency = "<dependency>\n" \
                 "<groupId>org.urts</groupId>\n" \
                 "<artifactId>org.urts.core</artifactId>\n" \
                 "<version>1.0.0-SNAPSHOT</version>\n" \
                 "<scope>compile</scope>\n" \
                 "</dependency>\n"
    lines = []
    meet_dependencies = False
    with open(hadoop_api_pom_file_path, 'r') as f:
        for line in f:
            lines.append(line)

    with open(hadoop_api_pom_file_path, 'w') as f:
        for line in lines:
            f.write(line)
            if "<dependencies>" in line:
                meet_dependencies = True
                f.write(dependency)
                

# Install hadoop dependency module
def install_hcommon():
    os.chdir(hadoop_root_path)
    os.system("mvn install -DskipTests -pl hadoop-common-project/hadoop-common -am")
    os.chdir(cur_path)


# Prepare hadoop 
def build_hadoop():
    os.chdir(hadoop_root_path)
    sha = "e2f1f118e465e787d8567dfa6e2f3b72a0eb9194"
    os.system("git checkout -f " + sha)
    modify_hadoop_api()
    modify_hadoop_pom()
    install_hcommon()


# Inlucde regular test
def include_regular_test():
    include_list = []
    with open(regular_test_list, 'r') as f:
        include_list = f.readlines()
    pom_lines = []
    with open(module_pom_file_path, 'r') as f:
        for line in f:
            pom_lines.append(line)
    with open(module_pom_file_path, 'w') as f:
        surefire_pos_flag = False
        for line in pom_lines:
            f.write(line)
            if "<artifactId>maven-surefire-plugin</artifactId>" in line:
                surefire_pos_flag = True
            if surefire_pos_flag and "<configuration>" in line:
                f.write("".join(include_list))
                f.write("\n")
                surefire_pos_flag = False


# Turn off second part test exeuction in HBase
def removeSecondPartTestsExecution():
    hbase_root_pom_file = os.path.join(hbase_root_path, "pom.xml")
    lines = []
    with open(hbase_root_pom_file, 'r') as f:
        for line in f:
            lines.append(line)
    with open(hbase_root_pom_file, 'w') as f:
        meet_id = False
        for line in lines:
            if "<id>secondPartTestsExecution</id>" in line:
                meet_id = True
            elif meet_id and "</configuration>" in line:
                meet_id = False
            elif meet_id:
                continue
            else:
                f.write(line)  


# Prepare environment and all files
def do_preparation(commit):
    checkout_commit(commit)
    modify_hbase_pom()
    modify_hbase_api()
    removeSecondPartTestsExecution()
    include_regular_test()
    copy_ctest_mapping()
    copy_get_config_value_test()
    maven_install_module()


def copy_cofig_value(curCommit, cur_config_name):
    source_path = os.path.join(project_module_path, ".ConfigValue")
    target_path = os.path.join(cur_path, "config_value", "ConfigValue-" + cur_config_name + "-" + curCommit)
    shutil.copy(source_path, target_path)
    

def run():
    clone()
    build_hadoop()
    for i in range(len(commits)):
        curCommit = commits[i]
        do_preparation(curCommit)
        for curConfig in configuration_file:
            cur_config_name = curConfig.split(".")[0]
            config_file_name = curConfig + "-" + curCommit
            print(DEBUG_PREFIX + curCommit + " Config: " + cur_config_name + " ]===============", flush=True)
            replacedConfigFilePath = os.path.join(cur_path, "../../config_files/hbase/", config_file_name)
            targetConfigFilePath = os.path.join(project_module_path, curConfig)
            if not os.path.exists(replacedConfigFilePath):
                ValueError("Does not have configuration file: " + replacedConfigFilePath)
            copy_production_config_file(replacedConfigFilePath, targetConfigFilePath)
            prepare_urtsrc_file(cur_config_name)
            run_urts(replacedConfigFilePath, curConfig, curCommit)
            copy_dependency_folder_urts("hbase", project_module_path, cur_path, curCommit, cur_config_name, i)

if __name__ == '__main__':
    run()

