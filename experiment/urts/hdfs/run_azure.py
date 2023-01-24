import os, shutil, time, sys
sys.path.append("../../")
from util import *

cur_path = os.getcwd()
project_url = "https://github.com/apache/hadoop.git"
project_root_path = os.path.join(cur_path, "hadoop")
project_module_name = "hadoop-hdfs-project/hadoop-hdfs"
project_module_path = os.path.join(project_root_path, project_module_name)
module_pom_file_path = os.path.join(project_module_path, "pom.xml")
api_file_path = os.path.join(project_root_path, "hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/conf/Configuration.java")
api_pom_file_path = os.path.join(project_root_path, "hadoop-common-project/hadoop-common/pom.xml")
hdfs_api_file_path = os.path.join(project_root_path, "hadoop-hdfs-project/hadoop-hdfs-client/src/main/java/org/apache/hadoop/hdfs/HdfsConfiguration.java")
test_copied_path = os.path.join(project_module_path, "src/test/java/org/apache/hadoop")
time_number_file_path = os.path.join(cur_path, "time_number.txt")
#test_class_num_file_path = os.path.join(cur_path, "test_class_num.txt")
mvn_cmd = "mvn urts:urts  -DgetterClass=TestGetConfigValueForConfigAware -DfailIfNoTests=false -Dmaven.test.failure.ignore=true | tee out.txt"
commits = ["1576f81dfe0156514ec06b6051e5df7928a294e2","c665ab02ed5c400b0c5e9e350686cd0e5b5e6972","028ec4704b9323954c091bcda3433f7b79cb61de","832a3c6a8918c73fa85518d5223df65b48f706e9","3fdeb7435add3593a0a367fff6e8622a73ad9fa3","98a74e23514392dc8859f407cd40d9c96d8c5923","1abd03d68f4f236674ce929164cc460037730abb","8ce30f51f999c0a80db53a2a96b5be5505d4d151","bce14e746b3d00e692820f28b72ffe306f74d0b2","b8ab19373d1a291b5faa9944e545b6d5c812a6eb","b38b00e52839911ab4e0b97fd269c53bf7d1380f","59fc4061cb619c85538277588f326469dfa08fb8","4a26a61ecd54bd36b6d089f999359da5fca16723","f4b24c68e76df40d55258fc5391baabfa9ac362d","c748fce17ace8b45ee0f3c3967d87893765eea61","a2a0283c7be8eac641a256f06731cb6e4bab3b09","762a83e044b84250c6e2543e02f48136361ea3eb","a1a318417105f155ed5c9d34355309775eb43d11","eefa664fea1119a9c6e3ae2d2ad3069019fbd4ef","4ef27a596fd1d7be5e437ab444b12fe450e79e79","6e5692e7e221a22a86257ba5f85746102528e96d","51ebf7b2a0e82d8bcd34867aea915c7838541fb7","352949d07002a8435a8ff67eecf88e4aa8bd5935","ebee2aed00b5589fe610ff994d81feba86039297","c1bf3cb0daf0b6212aebb449c97b772af2133d98","839fcf7682270bc6ba151875549d64d81917f425","2b304ad6457bca4286be18f689f8b855395831c6","9e7c7ad129fcf466d9647e0672ecf7dd72213e72","56d249759fcee4d9c44b3e8f37ef316abab7e91f","1ad674e5489e6080f21c308673ecc73a54ba3752","643dfd60e442b35fc9bb2dbf32fcdf28c3dd2f58","c491f81a30162eb294bcccbc64b771f95231ea21","c255feb5d7880e41abd9bd3acd0807233dc6124f","bdc9c8809e1f38c0e8cbb394ffa4f6cd1cf2a819","c488abbc79cc1ad2596cbf509a0cde14acc5ad6b","10b79a26fe0677b266acf237e8458e93743424a6","d9fbb3c5082baf301b082f51eea2f8a2e25e8715","fdef2b4ccacb8753aac0f5625505181c9b4dc154","f78b6d47f9d5c05bd990aec3481e6cede93d43ea","0d078377120da9ea886bd95b19c8a618dc4d7ab5","748570b73c86ff02f1c056b988717ff0e1f2aee5","93a1685073d76bef5c6ad3819e7710317733bd7a","95454d821c6e4c08d49df195d7c1e0ea1aacafb9","4cac6ec40528f6a1252de43599003291cd5fe5c3","20a4b1ae36483b5574c09516373557c764903c72","d2e70034005b16b151e9ca9a43b1ac5893512a98","56c7ada7a524812208a2112412a757ec6be02e5e","52b9319e57881411a03b53eca6b3c5d123bed796","9be17339eb1dfda01afe255831fac36a33141d85","390f8603d3a54ffae743fe240b9b0195bd01de06"]
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
    successInsert = False
    with open(hdfs_api_file_path, 'r') as f:
        for line in f:
            lines.append(line)
    
    with open(hdfs_api_file_path, 'w') as f:
        for line in lines:
            f.write(line)
            if "package org.apache.hadoop.hdfs" in line:
                f.write("import java.lang.management.ManagementFactory;\n")
            if "Configuration.addDefaultResource(\"hdfs-rbf-site.xml\");" in line:
                f.write('    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0]; //UNIFY_TESTS\n')
                f.write('    Configuration.addDefaultResource("hdfs-ctest-" + pid + ".xml"); //UNIFY_TESTS\n')
                successInsert = True
    
    if not successInsert:
        lines = []
        with open(hdfs_api_file_path, 'r') as f:
            for line in f:
                lines.append(line)

        with open(hdfs_api_file_path, 'w') as f:
            for line in lines:
                f.write(line)
                if "package org.apache.hadoop.hdfs" in line:
                    f.write("import java.lang.management.ManagementFactory;\n")
                if "Configuration.addDefaultResource(\"hdfs-site.xml\");" in line:
                    f.write('    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0]; //UNIFY_TESTS\n')
                    f.write('    Configuration.addDefaultResource("hdfs-ctest-" + pid + ".xml"); //UNIFY_TESTS\n')
                    successInsert = True
    
    if not successInsert:
        raise ValueError("Can't insert into HDFSConfiguration.java ")
    
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
    maven_install_module()
    os.chdir(project_module_path)
    shutil.copy(config_file, "curConfigFile.xml")
    print(DEBUG_PREFIX + "RUN uRTS]=================", flush=True)
    start = time.time()
    os.system(mvn_cmd)
    end = time.time()
    record_time_and_number("hdfs", "URTS", time_number_file_path, end - start, curConfig, curCommit)
    os.chdir(cur_path)


# Install dependency module
def maven_install_module():
    os.chdir(project_root_path)
    os.system("mvn clean install -DskipTests  -Denforcer.skip=true -am -pl " + project_module_name)
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


def run():
    clone()
    for i in range(len(commits)):
        curCommit = commits[i]
        do_preparation(curCommit)
        for curConfig in configuration_file:
            cur_config_name = curConfig.split(".")[0]
            config_file_name = curConfig + "-" + curCommit
            print(DEBUG_PREFIX + curCommit + " Config: " + cur_config_name + " ]===============", flush=True)
            replacedConfigFilePath = os.path.join(cur_path, "../../config_files/hdfs/", config_file_name)
            targetConfigFilePath = os.path.join(project_module_path, curConfig)
            if not os.path.exists(replacedConfigFilePath):
                ValueError("Does not have configuration file: " + replacedConfigFilePath)
            copy_production_config_file(replacedConfigFilePath, targetConfigFilePath)
            prepare_urtsrc_file(cur_config_name)
            run_urts(replacedConfigFilePath, curConfig, curCommit)
            copy_dependency_folder_urts("hdfs", project_module_path, cur_path, curCommit, cur_config_name, i)


if __name__ == '__main__':
    run()
