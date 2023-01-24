import os, shutil

DEBUG_PREFIX="===============[ Evaluation: "


# Record the experiment time
def record_time_and_number(project, mode, file_path, elapsed_time, curConfig, curCommit):
    print("{}TOTAL_TIME: {}-{} : {}s\n".format(DEBUG_PREFIX, curConfig, curCommit, elapsed_time), flush=True)
    p = os.popen("grep 'Tests ' out.txt | sed -e 's/^.*Tests //' -e 's/.\[0;1;32m//' -e 's/.\[m//' -e 's/.\[1m//' -e 's/.\[0;1m//g' -e 's/.\[m//g' | sed -n 's/run: \([1-9][0-9]*\),.*- in \(.*\)/\2     \1/p' | wc -l")
    if mode == "URTS":
        num = int(p.read()) - 1
    else:
        num = int(p.read())
    print("{}TEST_CLASS_NUM: {}-{} : {}\n".format(DEBUG_PREFIX, curConfig, curCommit, num, flush=True))
    with open(file_path, 'a') as f:
        f.write("{}_{}_{}_{}:{}s,{}\n".format(mode, project, curConfig, curCommit, elapsed_time, num))


# Record the experiment time for alluxio
def record_time_and_number_alluxio(project, mode, component_list, file_path, elapsed_time, curConfig, curCommit, project_module_path, cur_path):
    print("{}TOTAL_TIME: {}-{} : {}s\n".format(DEBUG_PREFIX, curConfig, curCommit, elapsed_time), flush=True)
    # p = os.popen("grep 'Tests ' out.txt | sed -e 's/^.*Tests //' -e 's/.\[0;1;32m//' -e 's/.\[m//' -e 's/.\[1m//' -e 's/.\[0;1m//g' -e 's/.\[m//g' | sed -n 's/run: \([1-9][0-9]*\),.*- in \(.*\)/\2     \1/p' | wc -l")
    # num = int(p.read())
    num = 0
    for component in component_list:
        component_path = os.path.join(project_module_path, component) 
        os.chdir(component_path)
        p = os.popen("grep 'Tests ' out.txt | sed -e 's/^.*Tests //' -e 's/.\[0;1;32m//' -e 's/.\[m//' -e 's/.\[1m//' -e 's/.\[0;1m//g' -e 's/.\[m//g' | sed -n 's/run: \([1-9][0-9]*\),.*- in \(.*\)/\2     \1/p' | wc -l")
        num += int(p.read())
    os.chdir(cur_path)
    print("{}TEST_CLASS_NUM: {}-{} : {}\n".format(DEBUG_PREFIX, curConfig, curCommit, num, flush=True))
    with open(file_path, 'a') as f:
        f.write("{}_{}_{}_{}:{}s,{}\n".format(mode, project, curConfig, curCommit, elapsed_time, num))


def copy_dependency_folder_urts(project, project_module_path, cur_path, curCommit, cur_config_name, i):
    if not os.path.exists(os.path.join(cur_path, "dependency_folder")):
        os.makedirs(os.path.join(cur_path, "dependency_folder"))
    source_path = os.path.join(project_module_path, ".urts-" + cur_config_name + "-Round" + str(i+1))
    target_path = os.path.join(cur_path, "dependency_folder", "URTS_" + project + "_" + cur_config_name + "_" + curCommit)
    if os.path.exists(target_path):
        shutil.rmtree(target_path)
    shutil.copytree(source_path, target_path)


def copy_dependency_folder_urts_alluxio(project, cur_path, component_list, project_module_path, curCommit, cur_config_name, i):
    if not os.path.exists(os.path.join(cur_path, "dependency_folder")):
        os.makedirs(os.path.join(cur_path, "dependency_folder"))
    for folder_name in component_list:
        source_path = os.path.join(project_module_path, folder_name, ".urts-" + cur_config_name + "-Round" + str(i+1))
        target_path = os.path.join(cur_path, "dependency_folder", folder_name, "URTS_" + project + "_" + cur_config_name + "_" + curCommit)
        if not os.path.exists(source_path):
            continue
        if os.path.exists(target_path):
            shutil.rmtree(target_path)
        shutil.copytree(source_path, target_path)
        
        
def copy_dependency_folder_ekstazi_alluxio(project, mode, cur_path, component_folder_list, project_module_path, curCommit, cur_config_name, i):
    if not os.path.exists(os.path.join(cur_path, "dependency_folder")):
        os.makedirs(os.path.join(cur_path, "dependency_folder"))
    for folder_name in component_folder_list:
        source_path = os.path.join(project_module_path, folder_name, ".ekstazi")
        target_path = os.path.join(cur_path, "dependency_folder", folder_name, mode + "_" + project + "_" + cur_config_name + "_" + curCommit)
        if not os.path.exists(source_path):
            continue
        if os.path.exists(target_path):
            shutil.rmtree(target_path)
        shutil.copytree(source_path, target_path)
        

def copy_dependency_folder_ekstazi(project, mode, project_module_path, cur_path, curCommit, cur_config_name, i):
    if not os.path.exists(os.path.join(cur_path, "dependency_folder")):
        os.makedirs(os.path.join(cur_path, "dependency_folder"))
    source_path = os.path.join(project_module_path, ".ekstazi")
    target_path = os.path.join(cur_path, "dependency_folder", mode + "_" + project + "_" + cur_config_name + "_" + curCommit)
    if os.path.exists(target_path):
        shutil.rmtree(target_path)
    shutil.copytree(source_path, target_path)



################# Not use for now #########################

# Clone testing project
def clone(project_root_path, project_url):
    if os.path.exists(project_root_path):
        shutil.rmtree(project_root_path)
    clone_cmd = "git clone " + project_url
    os.system(clone_cmd)


# Checkout to certain version
def checkout_commit(project_root_path, cur_path, commit):
    os.chdir(project_root_path)
    os.system("git checkout -f " + commit)
    os.chdir(cur_path)


# Prepare config file
def copy_production_config_file(replaced_config_file_path, original_config_file_path):
    shutil.copy(replaced_config_file_path, original_config_file_path)


# Prepare test that used to get config values
def copy_get_config_value_test(test_copied_path, cur_path):
    #print(test_copied_path)
    source_path = os.path.join(cur_path, "TestGetConfigValueForConfigAware.java")
    target_path = os.path.join(test_copied_path, "TestGetConfigValueForConfigAware.java")
    shutil.copy(source_path, target_path)


# Prepare mapping
def copy_ctest_mapping(project_module_path, cur_path):
    source_path = os.path.join(cur_path, "mapping")
    target_path = os.path.join(project_module_path, "mapping")
    shutil.copy(source_path, target_path)


# Prepare uRTS config file
def prepare_urtsrc_file(project_module_path, cur_path, config_name):
    source_path = os.path.join(cur_path, ".urtsrc-" + config_name)
    target_path = os.path.join(project_module_path, ".urtsrc")
    shutil.copy(source_path, target_path)