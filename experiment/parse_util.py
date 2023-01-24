import os, sys
sys.path.append(".")
from commits import *

class Round:
    
    def __init__(self, commit, config):
        self.commit = commit
        self.config = config
        self.total_time = 0.0
        self.total_classes = 0
        self.analysis_time = 0.0
        self.skip_from_prev = 0
        self.skip_from_cur = 0
        self.skip_from_both = 0 

        
    def __hash__(self):
        return hash((self.commit, self.config, self.total_time, self.total_classes, self.analysis_time, self.skip_from_prev, self.skip_from_cur, self.skip_from_both))

    
    def __eq__(self, other):
        if not isinstance(other, type(self)): return NotImplemented
        return self.commit == other.commit and self.config == other.config


    def __gt__(self, other):
        if not isinstance(other, type(self)): return NotImplemented
        if self.analysis_time != 0 and other.analysis_time !=0:
            return self.analysis_time < other.analysis_time
        else:
            return self.total_time > other.total_time

    
    def isEmpty(self):
        return self.commit == ""
    
    
    def set_commit(self, commit):
        self.commit = commit

        
    def get_commit(self):
        return self.commit

    
    def set_config(self,config):
        self.config = config

        
    def get_config(self):
        return self.config

    
    def set_total_time(self, time):
        self.total_time += float(time)

        
    def get_total_time(self):
        return self.total_time


    def set_total_classes(self, num):
        self.total_classes += int(num)

        
    def get_total_classes(self, num):
        return self.total_classes

    
    def set_analysis_time(self, time):
        self.analysis_time += float(time)

        
    def get_analysis_time(self):
        return self.analysis_time

    
    def set_skip(self, prev, cur, both):
        self.skip_from_prev += int(prev)
        self.skip_from_cur += int(cur)
        self.skip_from_both += int(both)

        
    def get_skip(self):
        return [self.skip_from_prev, self.skip_from_cur, self.skip_from_both]

    
    def print_urts(self):
        total_num = self.total_classes + self.skip_from_both
        select_from_prev = total_num - self.skip_from_prev
        select_from_cur = total_num - self.skip_from_cur
        select_from_both = total_num - self.skip_from_both
        print("{},{},{},{},{},{},{},{}".format(self.commit, self.config, self.analysis_time, self.total_time, self.total_classes, select_from_prev, select_from_cur, select_from_both))


    def print_ekstazi(self):
        print("{},{},{},{},{}".format(self.commit, self.config, self.analysis_time, self.total_time, self.total_classes))
        

    def print_retestall(self):
        print("{},{},{},{}".format(self.commit, self.config, self.total_time, self.total_classes))
        
        
    def get_urts_data_str(self):
        total_num = self.total_classes + self.skip_from_both
        select_from_prev = total_num - self.skip_from_prev
        select_from_cur = total_num - self.skip_from_cur
        select_from_both = total_num - self.skip_from_both
        return "{},{},{},{},{},{}".format(self.analysis_time, self.total_time, self.total_classes, select_from_prev, select_from_cur, select_from_both)


    def get_ekstazi_data_str(self):
        return "{},{},{}".format(self.analysis_time, self.total_time, self.total_classes)
    

    def get_retestall_data_str(self):
        return "{},{}".format(self.total_time, self.total_classes)
    
    
def parse_single_file(file):
    default_rounds = []
    prod1_rounds = []
    prod2_rounds = []
    with open(file, 'r') as f:
        for line in f:
            if "Config" in line:
                commit = line.split(" ")[2]
                config = line.split(" ")[4]
                #print("commit = {}, config = {}".format(commit, config))
                round = Round(commit, config)
            elif "SKIP_NUM" in line:
                prev = line.split(" ")[6]
                cur = line.split(" ")[9]
                both = line.split(" ")[12].strip()
                #print("prev = {}, cur = {}, both = {}".format(prev, cur, both))
                round.set_skip(prev, cur, both)
            elif "ANALYSIS_TIME" in line:
                time = line.split(" ")[4]
                #print("analysis_time = {}".format(time))
                round.set_analysis_time(time)
            elif "TOTAL_TIME" in line:
                time = line.split(" ")[5].split("s")[0]
                #print("total_time = {}".format(time))
                round.set_total_time(time)
            elif "TEST_CLASS_NUM" in line:
                num = line.split(" ")[5]
                #print("total_classes = {}".format(num))
                round.set_total_classes(num)
                cur_config = round.get_config()
                if cur_config == "core-default":
                    default_rounds.append(round)
                elif cur_config == "prod1":
                    prod1_rounds.append(round)
                elif cur_config == "prod2":
                    prod2_rounds.append(round)
                else:
                    print("ERROR Decide configuration name")
                    exit(-1)
    #for round in rounds:
    #    round.print()
    return remove_duplicated(default_rounds), remove_duplicated(prod1_rounds), remove_duplicated(prod2_rounds)

        
def remove_duplicated(rounds):
    ret = []
    for i in range(0, len(rounds)):
        first_commit = True
        for j in range(0, len(rounds)):
            # not the first commit (first commit only shows once)
            if rounds[i] == rounds[j] and i != j:
                first_commit = False
                # pick the smaller total_time one, the other one is pre-collection one
                if rounds[i] > rounds[j]:
                    ret.append(rounds[j])
        if first_commit:
            ret.append(rounds[i])
    return ret


def print_parsed_result(source_file, mode):
    default_rounds, prod1_rounds, prod2_rounds = parse_single_file(source_file)
    for i in range(0, len(default_rounds)):
        if mode == "urts":
            default_rounds[i].print_urts()
            prod1_rounds[i].print_urts()
            prod2_rounds[i].print_urts()
        elif mode == "ekst":
            default_rounds[i].print_ekstazi()
            prod1_rounds[i].print_ekstazi()
            prod2_rounds[i].print_ekstazi()
        elif mode == "unsafe":
            default_rounds[i].print_ekstazi()
        elif mode == "reall":
            default_rounds[i].print_retestall()
            prod1_rounds[i].print_retestall()
            prod2_rounds[i].print_retestall()
        else:
            print("Can't parse {} mode".format(mode))
            exit(-1)


def get_str_all_three_config(mode, commit, default_rounds, prod1_rounds, prod2_rounds):
    default = Round("", "")
    prod1 = Round("", "")
    prod2 = Round("", "")
    for round in default_rounds:
        if round.get_commit() == commit:
            default = round
    for round in prod1_rounds:
        if round.get_commit() == commit:
            prod1 = round
    for round in prod2_rounds:
        if round.get_commit() == commit:
            prod2 = round

    if mode == "unsafe":
        if default.isEmpty():
            print("There is no commits equals to {}".format(commit))
            return ""
    else:
        if default.isEmpty() or prod1.isEmpty() or prod2.isEmpty():
            print("There is no commits equals to {}".format(commit))
            return ""
    
    if mode == "urts":
        return "{},{},{},{}\n".format(commit, default.get_urts_data_str(), prod1.get_urts_data_str(), prod2.get_urts_data_str())
    elif mode == "ekst":
        return "{},{},{},{}\n".format(commit, default.get_ekstazi_data_str(), prod1.get_ekstazi_data_str(), prod2.get_ekstazi_data_str())
    elif mode == "unsafe":
        return "{},{}\n".format(commit, default.get_ekstazi_data_str())
    elif mode == "reall":
        return "{},{},{},{}\n".format(commit, default.get_retestall_data_str(), prod1.get_retestall_data_str(), prod2.get_retestall_data_str())
    else:
        print("Unable to parse {} mode".format(mode))
        exit(-1)
        

def get_csv_header(mode):
    if mode == "urts":
        return "commit,def_a_time,def_total_time,def_total_classes,def_select_from_prev,def_select_from_cur,def_select_from_both,prod1_a_time,prod1_total_time,prod1_total_classes,prod1_select_from_prev,prod1_select_from_cur,prod1_select_from_both,prod2_a_time,prod2_total_time,prod2_total_classes,prod2_select_from_prev,prod2_select_from_cur,prod2_select_from_both\n"
    elif mode == "ekst": 
        return "commit,def_a_time,def_total_time,def_total_classes,prod1_a_time,prod1_total_time,prod1_total_classes,prod2_a_time,prod2_total_time,prod2_total_classes\n"
    elif mode == "unsafe":
        return "commit,def_a_time,def_total_time,def_total_classes\n"
    elif mode == "reall":
        return "commit,def_total_time,def_total_classes,prod1_total_time,prod1_total_classes,prod2_total_time,prod2_total_classes\n"

        
def generate_csv(source_file, target_file, mode, ordered_commits):
    default_rounds, prod1_rounds, prod2_rounds = parse_single_file(source_file) # List of Round object
    with open(target_file, 'w') as f:
        f.write(get_csv_header(mode))
        for commit in ordered_commits:
            f.write(get_str_all_three_config(mode, commit, default_rounds, prod1_rounds, prod2_rounds))


def get_ordered_commits(proj):
    if proj == "hcommon":
        return hcommon_commits
    elif proj == "hbase":
        return hbase_commits
    elif proj == "hdfs":
        return hdfs_commits
    elif proj == "zookeeper":
        return zookeeper_commits
    elif proj == "alluxio":
        return alluxio_commits
    else:
        print("Can't parse {} project".format(proj))
        exit(-1)

            
if __name__ == '__main__':
    proj = sys.argv[1]
    mode = sys.argv[2]
    source_file = sys.argv[3]
    target_file = sys.argv[4]
    generate_csv(source_file, target_file, mode, get_ordered_commits(proj))
    print_parsed_result(source_file, mode)

    

