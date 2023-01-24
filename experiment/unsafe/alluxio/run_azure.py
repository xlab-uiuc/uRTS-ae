import os, shutil, time, sys
sys.path.append("../../")
from util import *

cur_path = os.getcwd()
non_ctest_list = "nonCtestList"
project_url = "https://github.com/Alluxio/alluxio.git"
project_path = os.path.join(cur_path, "alluxio")
project_module = "core"
project_module_path = os.path.join(project_path, project_module)
api_file1_path = os.path.join(project_module_path, "common/src/main/java/alluxio/conf/AlluxioProperties.java")
api_file2_path = os.path.join(project_module_path, "common/src/main/java/alluxio/conf/path/SpecificPathConfiguration.java")
api_file3_path = os.path.join(project_module_path, "common/src/main/java/alluxio/conf/PropertyKey.java")
pom_file_path = os.path.join(project_module_path, "pom.xml")
time_number_file_path = os.path.join(cur_path, "time_number.txt")
#test_class_num_file_path = os.path.join(cur_path, "test_class_num.txt")
production_configuration_file_path = os.path.join(project_module_path, "production-configuration.properties")
commits = ["2d05d7c4b24ef1662afafdae8daf33b2e79a3270","fe7ee4a3ae52e4f3f1466ae0682bc265d86a0252","4da0071c805923489d9da506b58cc829b82cfe71","6a9794218cd1edfd88e7e159a1801419fa98da7d","11e47c48990a4703c606621a3ef1013ac0598419","7a2aaae3c605349a1e2c8d9f530a54b70c3305c4","698b4f0a01bb821a9daa796d3b28f35564478116","1dabaeba80c4c547fe4d32c0da429b1e7377479d","2e046aae83facd12cd2776b960e42d7a66cead8c","46116a76149d2f137a71d9a180d1f6193f4bf1c8","5e943c29e19d713659df98c3f19867dffaa0f882","6f2b2fa59fa5331942048f8e5e8a3a3a831f80b9","5cf33595cb8c6e6d2738a211e12d445b5e98b663","5063016989da5284e1a94e80b7b2f2258aadaa7e","e9136cfd354d28ca9e953b282f4c327a0362d587","e697da0a75d6f98f31d6cdd0c7190bf29d5da827","d3b045247ae9baecc6fe45af8d410df38335d43d","984251fe3345659541c4b2330d2185320fa27738","f64540541748ba088eafbbac37fbd8c0458c410e","3c0275f4b81af80f98c8e0f1043bab8c8803f07c","696cb89bfc8b7dd41393e3003307947d2110e21f","ed7588da4adabc0555a018140b82f2f8215fe506","4c8b555920da6a844f3f30cc70493b563178518d","3064609e7489ea3c2111fd6d3c85b48270dc18f8","a16bc958dd283dc9bc7c9fe7f17627ace327eb28","dc922657fdbb6c5ccbbf2e0c1d2e02c66c921204","278b9e263842d61d7168cc29cac44666bfeea0d0","fa54b1bcd9fc891413cbd168862706d0fad0ad02","067e9d432166d44a82fb26aa1ffa8660b665e2f0","0dfa1615292a5c1adca7023a5f1330110df82482","2df1da2d4f6caa47574640bf54c52447e8c0f3ea","03f686a42238c9e5868132052810f3b5f93be918","38f110d924e016eff159ac1c8bb5cac14d3a2696","ebc291ff23f8b4ab6809a6876283e23287b1c64d","b9c66ca164d363bed9bcefb776a0b0f438d96441","050882ecee4ae4dc1bcf3eadcf0ecab3ca192f53","c6e5c2c1243bb9b07ff013ccc9efbb4146b3c92c","f950a7229e1183fdf5142ba4de6c6c9d9de1b3f6","f4f80a2d571903dbbbd824bf8fc5290a9b1a8d8c","c81d35f48521dcc7bf717594531dc7f76795b9cb","6f3fe6f4637261044b3938e620ac00e6e6e75708","b8aad79c004719566d679632026adefdcba8dea7","efe8f4c3f050910179f480896b424c93990a6941","ff293c735c7f12eec4ad8e4ec66a1c2f9598c0a8","9f3676b5b81b8bbf165ed8c3fac85e6ced95c23b","c1e12fb78bb0110c6e5d85ca112d7a0d904a7c8a","6accf76ff7915a94e78c88d8e0f96a0951d379eb","40d2a06c1e8ceeff35e7f7ec0eaef549164c86dd","2d939de0cafdb31f5cc19d3170751cc3fa15a972","67288abbec68128272de74500b989dd82a24b2fb"]
configuration_list = ["core-default.properties", "prod1.properties", "prod2.properties"]
mvn_cmd = "mvn ekstazi:ekstazi -Dcheckstyle.skip -Dlicense.skip -Dfindbugs.skip -Dmaven.javadoc.skip=true -DfailIfNoTests=false | tee out.txt"
mvn_cmd_exclude = "mvn ekstazi:ekstazi -Dcheckstyle.skip -Dlicense.skip -Dfindbugs.skip -Dmaven.javadoc.skip=true -DfailIfNoTests=false | tee out.txt"
mvn_clean_cmd = "mvn ekstazi:clean"
component_folder_list = ["common", "client/fs", "client/hdfs", "server/common", "server/proxy", "server/worker"]
prod1_config_changed_commit = ["1dabaeba80c4c547fe4d32c0da429b1e7377479d","2e046aae83facd12cd2776b960e42d7a66cead8c","5e943c29e19d713659df98c3f19867dffaa0f882","ed7588da4adabc0555a018140b82f2f8215fe506","278b9e263842d61d7168cc29cac44666bfeea0d0","b9c66ca164d363bed9bcefb776a0b0f438d96441","050882ecee4ae4dc1bcf3eadcf0ecab3ca192f53","f4f80a2d571903dbbbd824bf8fc5290a9b1a8d8c","6f3fe6f4637261044b3938e620ac00e6e6e75708","2d939de0cafdb31f5cc19d3170751cc3fa15a972"]
prod2_config_changed_commit = ["11e47c48990a4703c606621a3ef1013ac0598419","698b4f0a01bb821a9daa796d3b28f35564478116","d3b045247ae9baecc6fe45af8d410df38335d43d","f64540541748ba088eafbbac37fbd8c0458c410e","4c8b555920da6a844f3f30cc70493b563178518d","278b9e263842d61d7168cc29cac44666bfeea0d0","067e9d432166d44a82fb26aa1ffa8660b665e2f0","b9c66ca164d363bed9bcefb776a0b0f438d96441","f4f80a2d571903dbbbd824bf8fc5290a9b1a8d8c","c81d35f48521dcc7bf717594531dc7f76795b9cb"]
config_changed_commit = {"prod1":prod1_config_changed_commit, "prod2":prod2_config_changed_commit}

DEBUG_PREFIX="===============[Ekstazi-unsafe Evaluation: "

# Clone testing project
def clone():
    if os.path.exists(project_path):
        shutil.rmtree(project_path)
    clone_cmd = "git clone " + project_url
    os.system(clone_cmd)


# Copy the production configuration to the project for configuration tests
def copy_production_config_file(config_file_name):
    replaced_config_file_path = os.path.join(cur_path, "../../config_files/alluxio/", config_file_name)
    shutil.copy(replaced_config_file_path, production_configuration_file_path)


# Run tests
def run_test(curConfig, curCommit):
    start = time.time()
    for folder in component_folder_list:
        testing_component_path = os.path.join(project_module_path, folder)   
        os.chdir(testing_component_path)
        if folder == "client/fs":
            os.system(mvn_cmd_exclude)
        else:
            os.system(mvn_cmd)
    end = time.time()
    record_time_and_number_alluxio("alluxio", "UNSAFE", component_folder_list, time_number_file_path, end - start, curConfig, curCommit, project_module_path, cur_path)
    os.chdir(cur_path)


# Checkout Revision
def checkout_commit(curCommit):
    os.chdir(project_path)
    os.system("git checkout -f " + curCommit)
    os.chdir(cur_path)


# Install dependency module
def maven_install():
    os.chdir(project_path)
    os.system("mvn clean install -DskipTests -Dcheckstyle.skip -Dlicense.skip -Dfindbugs.skip -Dmaven.javadoc.skip=true")
    os.chdir(project_module_path)
    os.system("mvn install -am -DskipTests -Dcheckstyle.skip -Dlicense.skip -Dfindbugs.skip -Dmaven.javadoc.skip=true")
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
        plugin_1 = "<plugin>\n \
                <groupId>org.apache.maven.plugins</groupId>\n\
                <artifactId>maven-surefire-plugin</artifactId>\n\
                <configuration>\n"
        plugin_2 = "</configuration>\n \
                    </plugin>\n"     
        for line in pom_lines:
            f.write(line)
            if "<plugins>" in line:
                f.write(plugin_1)
                f.write("".join(excludeLines))
                f.write("\n")
                f.write(plugin_2)


# Add ekstazi maven plugin and use 'mvn ekstazi:ekstazi'
# with instrumented JUnit Runner to dynamically set 
# production configuration value for ctest.
def add_ekstazi_runner_pom():
    plugin = "<build>\n" \
             "<plugins>\n" \
             "<plugin>\n" \
             "<groupId>org.ekstazi</groupId>\n" \
             "<artifactId>ekstazi-maven-plugin</artifactId>\n" \
             "<version>5.3.1-SNAPSHOT</version>\n" \
             "</plugin>\n" \
             "</plugins>\n" \
             "</build>\n"
    lines = []
    with open(pom_file_path, 'r') as f:
        for line in f:
            lines.append(line)

    with open(pom_file_path, 'w') as f:
        for line in lines:
            f.write(line)
            if "</properties>" in line:
                f.write(plugin)


# Add ctest configuration file to project API
def modify_config_api_to_add_ctest_file():
    lines = []
    with open(api_file1_path, 'r') as f:
        for line in f:
            lines.append(line)
            
    with open(api_file1_path, 'w') as f:
        for line in lines:
            if "import org.slf4j.LoggerFactory;" in line:
                f.write(line)
                f.write("import alluxio.util.ConfigurationUtils; // UNIFY_TEST\n")
                f.write("import java.lang.management.ManagementFactory;\n")
            elif "public class AlluxioProperties {" in line:
                f.write(line)
                f.write("  public static String CTEST_FILEPATH = System.getProperty(\"user.dir\").split(\"/alluxio/core/\")[0] + \"/alluxio/core/alluxio-ctest-\" + ManagementFactory.getRuntimeMXBean().getName().split(\"@\")[0] + \".properties\"; // UNIFY_TESTS\n")
            elif "public AlluxioProperties()" in line:
                f.write("  public AlluxioProperties() {\n")
                f.write("    Properties ctestProps = ConfigurationUtils.loadPropertiesFromFile(CTEST_FILEPATH); // UNIFY_TESTS\n")
                f.write("    this.merge2(ctestProps, Source.siteProperty(CTEST_FILEPATH));\n  }\n")
            elif "public AlluxioProperties(AlluxioProperties alluxioProperties) {" in line:
                f.write(line)
                f.write("    Properties ctestProps = ConfigurationUtils.loadPropertiesFromFile(CTEST_FILEPATH); // UNIFY_TESTS\n")
                f.write("    alluxioProperties.merge2(ctestProps, Source.siteProperty(CTEST_FILEPATH)); // UNIFY_TESTS\n")

            elif "public void put(PropertyKey key, String value, Source source) {" in line:
                f.write("  public void put_purged(PropertyKey key, String value, Source source) {\n")
                f.write("    if (!mUserProps.containsKey(key) || source.compareTo(getSource(key)) >= 0) {\n")
                f.write("      mUserProps.put(key, Optional.ofNullable(value));\n")
                f.write("      mSources.put(key, source);\n")
                f.write("      mHash.markOutdated();\n")
                f.write("    }\n")
                f.write("  }\n\n")
                f.write(line)
            elif "public void merge(Map<?, ?> properties, Source source) {" in line:
                f.write("  public void merge2(Map<?, ?> properties, Source source) {\n")
                f.write("    if (properties == null || properties.isEmpty()) {return;}\n")
                f.write("    for (Map.Entry<?, ?> entry : properties.entrySet()) {\n")
                f.write("      String key = entry.getKey().toString().trim();\n")
                f.write("      String value = entry.getValue() == null ? null : entry.getValue().toString().trim();\n")
                f.write("      PropertyKey propertyKey;\n")
                f.write("      if (PropertyKey.isValid(key)) {propertyKey = PropertyKey.fromString(key);} \n")
                f.write("      else {\n")
                f.write("        LOG.debug(\"Property {} from source {} is unrecognized\", key, source);\n")
                f.write("        propertyKey = PropertyKey.getOrBuildCustom(key);\n")
                f.write("      }\n")
                f.write("      put_purged(propertyKey, value, source);\n")
                f.write("    }\n")
                f.write("    mHash.markOutdated();\n  }\n")
                f.write(line)
            else:
                f.write(line)

    lines = []
    with open(api_file2_path, 'r') as f:
        for line in f:
            lines.append(line)
    
    with open(api_file2_path, 'w') as f:
        for line in lines:
            if "          config -> properties.put(key, config.get(key), Source.PATH_DEFAULT));" in line:
                f.write("          config -> properties.put_purged(key, config.get(key), Source.PATH_DEFAULT));\n")
            else:
                f.write(line)


# Prepare mapping
def copy_config_mapping():
    source_path = os.path.join(cur_path, "mapping")
    target_path = os.path.join(project_module_path, "mapping")
    shutil.copy(source_path, target_path)


# Prepare ekstazi config file
def prepare_ekstazi_config_file():
    for component in component_folder_list:
        source_path = os.path.join(cur_path, ".twoekstazirc")
        if component == "common/":
            source_path = os.path.join(cur_path, ".oneekstazirc")
        target_path = os.path.join(project_module_path, component, ".ekstazirc")
        shutil.copy(source_path, target_path)


# Do not skip Tests in Alluxio
def notSkipTestsInAlluxio():
    pom_file_path = os.path.join(project_path, "pom.xml")
    lines = []
    with open(pom_file_path, 'r') as f:
        for line in f:
            lines.append(line)
    
    with open(pom_file_path, 'w') as f:
        for line in lines:
            if "<skipTests>true</skipTests>" in line:
                continue
            else:
                f.write(line)
  

# Prepare environment and all files
def do_preparation(curCommit, cur_config_name):
    checkout_commit(curCommit)
    modify_config_api_to_add_ctest_file()
    copy_config_mapping()
    prepare_ekstazi_config_file()
    add_ekstazi_runner_pom()
    notSkipTestsInAlluxio()
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
            copy_dependency_folder_ekstazi_alluxio("alluxio", "UNSAFE", cur_path, component_folder_list, project_module_path, curCommit, cur_config_name, i)
        clean_dependency_folder()
        

if __name__ == '__main__':
    run()
