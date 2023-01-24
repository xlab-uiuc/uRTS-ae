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
commits = ["1576f81dfe0156514ec06b6051e5df7928a294e2","c665ab02ed5c400b0c5e9e350686cd0e5b5e6972","832a3c6a8918c73fa85518d5223df65b48f706e9","98a74e23514392dc8859f407cd40d9c96d8c5923","8ce30f51f999c0a80db53a2a96b5be5505d4d151","59fc4061cb619c85538277588f326469dfa08fb8","4a26a61ecd54bd36b6d089f999359da5fca16723","f4b24c68e76df40d55258fc5391baabfa9ac362d","762a83e044b84250c6e2543e02f48136361ea3eb","4ef27a596fd1d7be5e437ab444b12fe450e79e79","6e5692e7e221a22a86257ba5f85746102528e96d","9e7c7ad129fcf466d9647e0672ecf7dd72213e72","c488abbc79cc1ad2596cbf509a0cde14acc5ad6b","20a4b1ae36483b5574c09516373557c764903c72","f639fbc29f7313ef6a54df4126bc56bee7c39f98","a5db6831bc674a24a3251cf1b20f22a4fd4fac9f","618c9218eeed2dc0388010e04349b3df8d6c5b70","ba325a8ada573291266c4d6447862072fdf88af5","ea90c5117dc4021e0c55ba12aa8d1657838714be","1dd03cc4b573270dc960117c3b6c74bb78215caa","87e00001372a37199e1c8d595e237dfb4016853b","df44178eb6d052b868cfb35adf40316b648f5a6c","e1d00addb5b6d7240884536aaa57846af34a0dd5","3a52bfc5db1b13a71d5a5c6ee0cd689fb8b27895","4c35466359dec71ac083ef3b6b5ceef83fa6121c","f813554769606d59c23bcdc184d52249793d0f12","fd13970d946ccfea529a8366227af72c418da7ec","e001f8ee39f9f2b4e661c3fe3af65f53348bbabf","798a0837c1c170c700ee97ba6f14f99def51e886","a218038960e905e6c9eae80e118575a7743cae7a","ee466d4b4002bc21b66f9c944263559a955c3055","ccfa072dc77d6b085d92f30d4ca667b0d6b25c4d","8616591b0c069919e332d29e1a0ccd704c1bf1a7","9fe1f24ec11c3e1c2ed5ee9db801d40a2f7a2ed6","5e54d92e6ec866dc49a750110863a3fa8b2bcf7c","b0b867e977ab853d1dfc434195c486cf0ca32dab","23e2a0b2021e87599339902353326f338505ba87","0c7b951e031d60310acc7b4b1aa5052624fcfb95","6342d5e523941622a140fd877f06e9b59f48c48b","6014a089fd9810422160840bad8301319ae1430b","ad54f5195c8c01f333703c55cd70703109d75f29","b53cae0ffb1b383824a86185ef23f698719d7a6d","aa9cdf2af6fd84aa24ec5a19da4f955472a8d5bd","4c94831364e9258247029c22a222a665771ab4c0","73a0c313705ac622bdc1ec465fc03b86ff347d9b","50dda774f13a83547e41db4e835d7106ae66c1ff","164608b5465ebc6a2d296376aafbf4b272d9cff0","4ea60b5733a1afa880d1d509bc4e55b83da66dc3","99a157fa4ad174c4ff979414af2edb67a98eb9fe","1d808f59d79194f0491938c4421dc518fd3e56b8"]
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
