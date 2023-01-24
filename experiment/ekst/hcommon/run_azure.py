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
default_configuration_file_path = os.path.join(project_module_path, "src/main/resources/core-default.xml")
production_configuration_file_path = os.path.join(project_module_path, "production-configuration.xml")
commits = ["1576f81dfe0156514ec06b6051e5df7928a294e2","c665ab02ed5c400b0c5e9e350686cd0e5b5e6972","832a3c6a8918c73fa85518d5223df65b48f706e9","98a74e23514392dc8859f407cd40d9c96d8c5923","8ce30f51f999c0a80db53a2a96b5be5505d4d151","59fc4061cb619c85538277588f326469dfa08fb8","4a26a61ecd54bd36b6d089f999359da5fca16723","f4b24c68e76df40d55258fc5391baabfa9ac362d","762a83e044b84250c6e2543e02f48136361ea3eb","4ef27a596fd1d7be5e437ab444b12fe450e79e79","6e5692e7e221a22a86257ba5f85746102528e96d","9e7c7ad129fcf466d9647e0672ecf7dd72213e72","c488abbc79cc1ad2596cbf509a0cde14acc5ad6b","20a4b1ae36483b5574c09516373557c764903c72","f639fbc29f7313ef6a54df4126bc56bee7c39f98","a5db6831bc674a24a3251cf1b20f22a4fd4fac9f","618c9218eeed2dc0388010e04349b3df8d6c5b70","ba325a8ada573291266c4d6447862072fdf88af5","ea90c5117dc4021e0c55ba12aa8d1657838714be","1dd03cc4b573270dc960117c3b6c74bb78215caa","87e00001372a37199e1c8d595e237dfb4016853b","df44178eb6d052b868cfb35adf40316b648f5a6c","e1d00addb5b6d7240884536aaa57846af34a0dd5","3a52bfc5db1b13a71d5a5c6ee0cd689fb8b27895","4c35466359dec71ac083ef3b6b5ceef83fa6121c","f813554769606d59c23bcdc184d52249793d0f12","fd13970d946ccfea529a8366227af72c418da7ec","e001f8ee39f9f2b4e661c3fe3af65f53348bbabf","798a0837c1c170c700ee97ba6f14f99def51e886","a218038960e905e6c9eae80e118575a7743cae7a","ee466d4b4002bc21b66f9c944263559a955c3055","ccfa072dc77d6b085d92f30d4ca667b0d6b25c4d","8616591b0c069919e332d29e1a0ccd704c1bf1a7","9fe1f24ec11c3e1c2ed5ee9db801d40a2f7a2ed6","5e54d92e6ec866dc49a750110863a3fa8b2bcf7c","b0b867e977ab853d1dfc434195c486cf0ca32dab","23e2a0b2021e87599339902353326f338505ba87","0c7b951e031d60310acc7b4b1aa5052624fcfb95","6342d5e523941622a140fd877f06e9b59f48c48b","6014a089fd9810422160840bad8301319ae1430b","ad54f5195c8c01f333703c55cd70703109d75f29","b53cae0ffb1b383824a86185ef23f698719d7a6d","aa9cdf2af6fd84aa24ec5a19da4f955472a8d5bd","4c94831364e9258247029c22a222a665771ab4c0","73a0c313705ac622bdc1ec465fc03b86ff347d9b","50dda774f13a83547e41db4e835d7106ae66c1ff","164608b5465ebc6a2d296376aafbf4b272d9cff0","4ea60b5733a1afa880d1d509bc4e55b83da66dc3","99a157fa4ad174c4ff979414af2edb67a98eb9fe","1d808f59d79194f0491938c4421dc518fd3e56b8"]
configuration_list = ["core-default.xml", "prod1.xml", "prod2.xml"]
mvn_cmd = "mvn ekstazi:ekstazi -DfailIfNoTests=false | tee out.txt"
mvn_clean_cmd = "mvn ekstazi:clean"
prod1_config_changed_commit = ["832a3c6a8918c73fa85518d5223df65b48f706e9","4ef27a596fd1d7be5e437ab444b12fe450e79e79","6e5692e7e221a22a86257ba5f85746102528e96d","20a4b1ae36483b5574c09516373557c764903c72","df44178eb6d052b868cfb35adf40316b648f5a6c","4c35466359dec71ac083ef3b6b5ceef83fa6121c","8616591b0c069919e332d29e1a0ccd704c1bf1a7","5e54d92e6ec866dc49a750110863a3fa8b2bcf7c","ad54f5195c8c01f333703c55cd70703109d75f29","99a157fa4ad174c4ff979414af2edb67a98eb9fe"]
prod2_config_changed_commit = ["98a74e23514392dc8859f407cd40d9c96d8c5923","4a26a61ecd54bd36b6d089f999359da5fca16723","f4b24c68e76df40d55258fc5391baabfa9ac362d","9e7c7ad129fcf466d9647e0672ecf7dd72213e72","87e00001372a37199e1c8d595e237dfb4016853b","4c35466359dec71ac083ef3b6b5ceef83fa6121c","e001f8ee39f9f2b4e661c3fe3af65f53348bbabf","6342d5e523941622a140fd877f06e9b59f48c48b","b53cae0ffb1b383824a86185ef23f698719d7a6d","99a157fa4ad174c4ff979414af2edb67a98eb9fe"]
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
    replaced_config_file_path = os.path.join(cur_path, "../../config_files/hcommon/", config_file_name)
    shutil.copy(replaced_config_file_path, production_configuration_file_path)


# Run tests
def run_test(curConfig, curCommit):
    os.chdir(project_module_path)
    start = time.time()
    os.system(mvn_cmd)
    end = time.time()
    record_time_and_number("hcommon", "EKST", time_number_file_path, end - start, curConfig, curCommit)
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


# Prepare ekstazi config file
def prepare_ekstazi_config_file():
    source_path = os.path.join(cur_path, ".ekstazirc")
    target_path = os.path.join(project_module_path, ".ekstazirc")
    shutil.copy(source_path, target_path)


def mimic_config_file_change(curCommit, cur_config_name):
    for commit in config_changed_commit[cur_config_name]:
        if commits.index(curCommit) >= commits.index(commit):
            with open(default_configuration_file_path, 'a') as f:
                f.write(" <!-- {} -->\n".format(commit))


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
            copy_dependency_folder_ekstazi("hcommon", "EKST", project_module_path, cur_path, curCommit, cur_config_name, i)
        clean_dependency_folder()


if __name__ == '__main__':
    run()