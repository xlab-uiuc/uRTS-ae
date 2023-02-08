import os, shutil, time, sys
sys.path.append("../../")
from util import *

cur_path = os.getcwd()
project_url = "https://github.com/apache/hadoop.git"
project_root_path = os.path.join(cur_path, "hadoop")
project_module_name = "hadoop-common-project/hadoop-common"
project_module_path = os.path.join(project_root_path, project_module_name)
module_pom_file_path = os.path.join(project_module_path, "pom.xml")
api_file_path = os.path.join(project_module_path, "src/main/java/org/apache/hadoop/conf/Configuration.java")
api_pom_file_path = os.path.join(project_module_path, "pom.xml")
test_copied_path = os.path.join(project_module_path, "src/test/java/org/apache/hadoop")
time_number_file_path = os.path.join(cur_path, "time_number.txt")
#test_class_num_file_path = os.path.join(cur_path, "test_class_num.txt")
mvn_cmd = "mvn urts:urts -DgetterClass=TestGetConfigValueForConfigAware -DfailIfNoTests=false -Dmaven.test.failure.ignore=true | tee out.txt"
commits = ["df44178eb6d052b868cfb35adf40316b648f5a6c","e1d00addb5b6d7240884536aaa57846af34a0dd5","3a52bfc5db1b13a71d5a5c6ee0cd689fb8b27895"]
configuration_file = ["core-default.xml", "prod1.xml", "prod2.xml"]

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
    with open(api_file_path, 'r') as f:
        for line in f:
            lines.append(line)

    with open(api_file_path, 'w') as f:
        inGetFunc = False
        inGetDefFunc = False
        inSetFuc = False
        for line in lines:
            if "package org.apache.hadoop.conf;" in line:
                f.write(line)
                f.write("\nimport org.urts.configAware.*;\n")
                f.write("import java.lang.management.ManagementFactory;\n")
            
            elif 'addDefaultResource("core-site.xml");' in line:
                f.write(line)
                f.write('    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0]; //UNIFY_TESTS\n')
                f.write('    addDefaultResource("core-ctest-" + pid + ".xml"); //UNIFY_TESTS\n')

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
    shutil.copy(config_file, "curConfigFile.xml")
    print(DEBUG_PREFIX + "RUN uRTS]=================", flush=True)
    start = time.time()
    os.system(mvn_cmd)
    end = time.time()
    record_time_and_number("hcommon", "URTS", time_number_file_path, end - start, curConfig, curCommit)
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
    print(test_copied_path)
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
            replacedConfigFilePath = os.path.join(cur_path, "../../config_files/hcommon/", config_file_name)
            targetConfigFilePath = os.path.join(project_module_path, curConfig)
            if not os.path.exists(replacedConfigFilePath):
                ValueError("Does not have configuration file: " + replacedConfigFilePath)
            copy_production_config_file(replacedConfigFilePath, targetConfigFilePath)
            prepare_urtsrc_file(cur_config_name)
            run_urts(replacedConfigFilePath, curConfig, curCommit)
            copy_dependency_folder_urts("hcommon", project_module_path, cur_path, curCommit, cur_config_name, i)


if __name__ == '__main__':
    run()
