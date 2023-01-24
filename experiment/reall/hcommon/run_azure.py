import os, shutil, time, sys
sys.path.append("../../")
from util import *

cur_path = os.getcwd()
non_ctest_list = "nonCtestList"
project_url = "https://github.com/apache/hadoop.git"
project_path = os.path.join(cur_path, "hadoop")
project_module = "hadoop-common-project/hadoop-common/"
project_module_path = os.path.join(project_path, project_module)
api_file_path = os.path.join(project_module_path, "src/main/java/org/apache/hadoop/conf/Configuration.java")
pom_file_path = os.path.join(project_module_path, "pom.xml")
time_number_file_path = os.path.join(cur_path, "time_number.txt")
#test_class_num_file_path = os.path.join(cur_path, "test_class_num.txt")
ctest_configuration_file_path = os.path.join(project_module_path, "src/main/resources/core-ctest.xml")
production_configuration_file_path = os.path.join(project_module_path, "production-configuration.xml")
commits = ["1576f81dfe0156514ec06b6051e5df7928a294e2", "1d808f59d79194f0491938c4421dc518fd3e56b8"]
configuration_list = ["core-default.xml", "prod1.xml", "prod2.xml"]
mvn_cmd = "mvn urts:retestall | tee out.txt"

DEBUG_PREFIX="===============[Retestall Evaluation: "

# Clone testing project
def clone():
    if os.path.exists(project_path):
        shutil.rmtree(project_path)
    clone_cmd = "git clone " + project_url
    os.system(clone_cmd)


# Copy the production configuration to the project for configuration tests
def copy_production_config_file(config_file_name):
    replaced_config_file_path = os.path.join(cur_path, "../../config_files/hcommon/", config_file_name)
    shutil.copy(replaced_config_file_path, production_configuration_file_path)


# Run tests
def run_test(curConfig, curCommit):
    os.chdir(project_module_path)
    start = time.time()
    os.system(mvn_cmd)
    end = time.time()
    record_time_and_number("hcommon", "REALL", time_number_file_path, end - start, curConfig, curCommit)
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


# Add urts maven plugin and use 'mvn urts:retestall'
# with instrumented JUnit Runner to dynamically set 
# production configuration value for ctest.
def add_retestall_runner_pom():
    plugin = "<plugin>\n" \
             "<groupId>org.urts</groupId>\n" \
             "<artifactId>urts-maven-plugin</artifactId>\n" \
             "<version>1.0.0-SNAPSHOT</version>\n" \
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
            if "package org.apache.hadoop.conf" in line:
                f.write("import java.lang.management.ManagementFactory;\n")
            if "addDefaultResource(\"core-site.xml\");" in line:
                f.write('    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0]; //UNIFY_TESTS\n')
                f.write('addDefaultResource("core-ctest-" + pid + ".xml"); //UNIFY_TESTS\n')


# Prepare injection file
def create_empty_config_file_for_running_ctest():
    source_path = os.path.join(cur_path, "core-ctest.xml")
    shutil.copy(source_path, ctest_configuration_file_path)


# Prepare mapping
def copy_config_mapping():
    source_path = os.path.join(cur_path, "mapping")
    target_path = os.path.join(project_module_path, "mapping")
    shutil.copy(source_path, target_path)


# Prepare urts:retestall config file
def prepare_retestall_config_file():
    source_path = os.path.join(cur_path, ".retestallrc")
    target_path = os.path.join(project_module_path, ".urtsrc")
    shutil.copy(source_path, target_path)


# Prepare environment and all files
def do_preparation(curCommit):
    checkout_commit(curCommit)
    modify_config_api_to_add_ctest_file()
    create_empty_config_file_for_running_ctest()
    copy_config_mapping()
    prepare_retestall_config_file()
    add_retestall_runner_pom()
    maven_install()


def run():
    clone()
    for curCommit in commits:
        do_preparation(curCommit)
        for i in range(len(configuration_list)):
            curConfig = configuration_list[i]
            cur_config_name = curConfig.split(".")[0]
            config_file_name = curConfig + "-" + curCommit
            if i == 1:
                exclude_non_ctest()
            copy_production_config_file(config_file_name)
            print(DEBUG_PREFIX + curCommit + " Config: " + cur_config_name + " ===============", flush=True)
            run_test(curConfig, curCommit)
            # record_test_class_number(curConfig, curCommit)


if __name__ == '__main__':
    run()