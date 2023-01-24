import os, shutil, time, sys
sys.path.append("../../")
from util import *

cur_path = os.getcwd()
regular_test_list = "regTestList"
ctest_list = "ctestList"
project_url = "https://github.com/apache/hbase.git"
project_path = os.path.join(cur_path, "hbase")
project_module = "hbase-server"
project_module_path = os.path.join(project_path, project_module)
api_file_path = os.path.join(project_path, "hbase-common/src/main/java/org/apache/hadoop/hbase/HBaseConfiguration.java")
set_security_manager_path = os.path.join(project_path, "hbase-common/src/test/java/org/apache/hadoop/hbase/SystemExitRule.java")
pom_file_path = os.path.join(project_module_path, "pom.xml")
time_number_file_path = os.path.join(cur_path, "time_number.txt")
#test_class_num_file_path = os.path.join(cur_path, "test_class_num.txt")
ctest_configuration_file_path = os.path.join(project_module_path, "src/test/resources/hbase-ctest.xml")
depend_configuration_file_path = os.path.join(project_module_path, "src/test/resources/hbase-site.xml")
production_configuration_file_path = os.path.join(project_module_path, "production-configuration.xml")
commits = ["a6329385a012aa8009ed0561a340e8a752b77a6a", "43455061d3ee2ce3152932c771e6572586819d26", "3cc539a561c4d35dc661c155768ffbfe2d2f073f", "c42d6ccc1a8950c14b349acfdca376040a5ce052", "52d1dc09153f732750977d90a068147834b79cba", "a372d437b3fd82b2965620b1eb31c1b24cd0dd51", "09ffa49df8c50e8278e2bfd070b4f6723dd74f20", "4149459ddef47b877d1c1f2aa1d4f4aebcd11ece", "67967f265f62d0f5319f11f822c485eb3299c802", "446630b71323d60e0fa5e54616cf9449d299575b", "d23a5b5aac4fd2b9bf7d0b4e4b46a8a68b9f5195", "35264037efda5be27ebadc3755a92e1bc9988ebb", "8392dc0ad2039805262354e3335f528961f82094", "1487d62d2119b4da3a2d71da2244d27e70ff92e9", "f382086ca294475804b2417457172dfed1e647ac", "2a318009db48dc6c8ce6fd1180cfed951b8550b1", "98259c679a19277ff0b918a18bff1c1734d7181f", "cc701e2c2859f224a8329d0e4e6f95d256cc3ef0", "0b7630bc1f392832eba36a62ef273d603fe75b56", "8ea548441c347590baab25538a88126e9523a44a", "71bd65c1f28a3815770a7e2d17f0f571237600ad", "a2c4cb67a21186f41fc7f9be3b05be7f88f3ea8a", "6057265388240b49273bc8e32633c2073dea060a", "9ccbd8c6682159881cb39d202dc138b082b54425", "563a404ac152541931b96cdcffc7c446b668c26b", "230ce3e6b58347c30e792e4cb8902fc56286f874", "455f7bfd7b1ba1d3e3e1afd99c6c5182cdf1056d", "5dd23e5b8c7bc965e8a079dd025bd4c904998b2d", "801f9926c54d93b29f4a6b6bbb710390f1b0e73b", "7ce597d34f8ccf3fbc2fa713cac4c0ff32c295ad", "24b700848f2c766750044b31a2a88dc777f20a92", "9b627905c7e6ffaea918b874cf88ebd1ff7a88c8", "e4ad6e9f647dc07604b42733eecdff24dd30c519", "60e263e03b7b0d2d0b4642d5e788aa0593e075c8", "aa2b807026182fb176150bb0e2bcfeb640b2363d", "f86bff0f53877176b16f87e17fe6fe97a2cbd583", "b9b075f21a4276f080fafdf7e4dea566dc056207", "f5af7611c0dbbb5ea7d65ef5d916fa363aa0e82c", "1408c044b196d1a919f78c45829076f270cdeca7", "06ef9e9778ae2e1747b44bf994b241b890f07895", "45347bb81a76c303d91e1012805190b3f1accb2a", "02fa0903a206bbcc42b7daaadb66e428e0a4184e", "2382a70de56693cb2d618ea14ea15159ba95dbdb", "e766dd7b29440c7b5b60dbeb19e69a2924f0765c", "e64b089ed16b17e9ebe6f5441f74c19aa7d0b170", "17750a72f5d098f75057b794688e4c3779fbde2d", "6d2faf43233aa7bde346dde0f81e4d9464872850", "7957b0cc7e0e8b162441df4d6152a544346682f6", "c49f7f63fca144765bf7c2da41791769286dfccc", "8fa8344a8c0c72d167ce92b5f44f88beb43f325e"]
configuration_list = ["core-default.xml", "prod1.xml", "prod2.xml"]
mvn_cmd = "mvn ekstazi:ekstazi -DfailIfNoTests=false | tee out.txt"
mvn_clean_cmd = "mvn ekstazi:clean"
prod1_config_changed_commit = ["446630b71323d60e0fa5e54616cf9449d299575b", "a2c4cb67a21186f41fc7f9be3b05be7f88f3ea8a", "6057265388240b49273bc8e32633c2073dea060a", "9ccbd8c6682159881cb39d202dc138b082b54425", "563a404ac152541931b96cdcffc7c446b668c26b", "e4ad6e9f647dc07604b42733eecdff24dd30c519", "f86bff0f53877176b16f87e17fe6fe97a2cbd583", "2382a70de56693cb2d618ea14ea15159ba95dbdb", "e64b089ed16b17e9ebe6f5441f74c19aa7d0b170", "7957b0cc7e0e8b162441df4d6152a544346682f6"]
prod2_config_changed_commit = ["3cc539a561c4d35dc661c155768ffbfe2d2f073f", "67967f265f62d0f5319f11f822c485eb3299c802", "98259c679a19277ff0b918a18bff1c1734d7181f", "cc701e2c2859f224a8329d0e4e6f95d256cc3ef0", "455f7bfd7b1ba1d3e3e1afd99c6c5182cdf1056d", "e4ad6e9f647dc07604b42733eecdff24dd30c519", "f86bff0f53877176b16f87e17fe6fe97a2cbd583", "b9b075f21a4276f080fafdf7e4dea566dc056207", "f5af7611c0dbbb5ea7d65ef5d916fa363aa0e82c", "7957b0cc7e0e8b162441df4d6152a544346682f6"]
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
    replaced_config_file_path = os.path.join(cur_path, "../../config_files/hbase/", config_file_name)
    shutil.copy(replaced_config_file_path, production_configuration_file_path)


# Run tests
def run_test(curConfig, curCommit):
    os.chdir(project_module_path)
    start = time.time()
    os.system(mvn_cmd)
    end = time.time()
    record_time_and_number("hbase", "EKST", time_number_file_path, end - start, curConfig, curCommit)
    os.chdir(cur_path)


# Checkout Revision
def checkout_commit(commit):
    os.chdir(project_path)
    os.system("git checkout -f " + commit)
    os.chdir(cur_path)
    

# Install Project
def maven_install():
    os.chdir(project_path)
    os.system("mvn clean install -DskipTests -am -pl hbase-common,hbase-server")
    os.chdir(cur_path)


# Include first round regular test for default round (round_index = 0)
# or production round (round_index = 1, 2)
def include_first_round_regular_test_or_ctest(cur_config_name):
    include_list = []
    if cur_config_name == "core-default":
        with open(regular_test_list, 'r') as f:
            include_list = f.readlines()
    else:
        with open(ctest_list, 'r') as f:
            include_list = f.readlines()
    os.chdir(project_module_path)
    os.system("git restore pom.xml")
    add_ekstazi_runner_pom()
    os.chdir(cur_path)

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
                f.write("".join(include_list))
                f.write("\n")
                surefire_pos_flag = False


# Turn off second part test exeuction in HBase
def removeSecondPartTestsExecution():
    root_pom_file = os.path.join(project_path, "pom.xml")
    lines = []
    with open(root_pom_file, 'r') as f:
        for line in f:
            lines.append(line)
    with open(root_pom_file, 'w') as f:
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
    with open(api_file_path, 'r') as f:
        for line in f:
            lines.append(line)
    
    with open(api_file_path, 'w') as f:
        for line in lines:
            f.write(line)
            if "package org.apache.hadoop.hbase" in line:
                f.write("import java.lang.management.ManagementFactory;\n")
            if "conf.addResource(\"hbase-site.xml\");" in line:
                f.write('    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0]; //UNIFY_TESTS\n')
                f.write('    conf.addResource("hbase-ctest-" + pid + ".xml"); //UNIFY_TESTS\n')


# Prepare injection file
def create_empty_config_file_for_running_ctest():
    source_path = os.path.join(cur_path, "hbase-ctest.xml")
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


def forbidSetSecurityManager():
    lines = []
    meet1 = meet2 = False
    with open(set_security_manager_path, 'r') as f:
        for line in f:
            lines.append(line)
    
    with open(set_security_manager_path, 'w') as f:
        for line in lines:
            if "forbidSystemExitCall();" in line:
                f.write("          // forbidSystemExitCall();\n")
                meet1 = True
            elif "System.setSecurityManager(null);" in line:
                f.write("          // System.setSecurityManager(null);\n")
                meet2 = True
            else:
                f.write(line)
        if meet1!=True or meet2!=True:
            raise Exception("fail to stop setting security manager")


def mimic_config_file_change(curCommit, cur_config_name):
    for commit in config_changed_commit[cur_config_name]:
        if commits.index(curCommit) >= commits.index(commit):
            with open(depend_configuration_file_path, 'a') as f:
                f.write(" <!-- {} -->\n".format(commit))


# Prepare environment and all files
def do_preparation(curCommit, cur_config_name):
    checkout_commit(curCommit)
    modify_config_api_to_add_ctest_file()
    create_empty_config_file_for_running_ctest()
    copy_config_mapping()
    prepare_ekstazi_config_file()
    add_ekstazi_runner_pom()
    removeSecondPartTestsExecution()
    forbidSetSecurityManager()
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
            include_first_round_regular_test_or_ctest(cur_config_name)
            copy_production_config_file(config_file_name)
            print(DEBUG_PREFIX + curCommit + " Config: " + cur_config_name + " ===============", flush=True)
            run_test(curConfig, curCommit)
            copy_dependency_folder_ekstazi("hbase", "EKST", project_module_path, cur_path, curCommit, cur_config_name, i)
        clean_dependency_folder()
            

if __name__ == '__main__':
    run()