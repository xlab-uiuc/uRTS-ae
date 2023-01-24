import os, sys, csv
cur_path = os.getcwd()
sha_list = []
item_list = []

class item:
    def __init__(self):
        self.sha = None
        self.default_time = None
        self.prod1_time = None
        self.prod2_time = None
        self.default_num = None
        self.prod1_num = None
        self.prod2_num = None


def parse_time():
    if not os.path.exists("time.txt"):
        raise Exception("time.txt does not exist")
    with open("time.txt", "r") as f:
        content = f.readlines()
    for line in content:
        sha = line.split(':')[0].split('-')[-1].strip()
        recorded_time = line.split(' ')[-1].strip('\n').strip('s')
        if sha not in sha_list:
            cur_item = item()
            cur_item.sha = sha
            item_list.append(cur_item)
            sha_list.append(sha)
        else:
            cur_item = item_list[sha_list.index(sha)]

        if "core-default" in line:
            cur_item.default_time = recorded_time
        elif "prod1" in line:
            cur_item.prod1_time = recorded_time
        elif "prod2" in line:
            cur_item.prod2_time = recorded_time


def parse_num():
    if not os.path.exists("test_class_num.txt"):
        raise Exception("test_class_num.txt does not exist")
    with open("test_class_num.txt", "r") as f:
        content = f.readlines()
    for line in content:
        sha = line.split(':')[0].split('-')[-1].strip()
        recorded_test_num = line.split(' ')[-1].strip('\n')
        if sha not in sha_list:
            cur_item = item()
            cur_item.sha = sha
            item_list.append(cur_item)
            sha_list.append(sha)
        else:
            cur_item = item_list[sha_list.index(sha)]
            
        if "core-default" in line:
            cur_item.default_num = recorded_test_num
        elif "prod1" in line:
            cur_item.prod1_num = recorded_test_num
        elif "prod2" in line:
            cur_item.prod2_num = recorded_test_num
    
def write_to_file():
    csv_file = open('result.csv', 'a', newline='')
    writer = csv.writer(csv_file)
    for item in item_list:
        writer.writerow([item.sha, item.default_time, item.default_num, item.prod1_time, item.prod1_num, item.prod2_time, item.prod2_num])


if __name__ == '__main__':
    if len(sys.argv) != 3:
        raise ValueError("usage: python3 parse_result.py mode project")
    mode, project = sys.argv[1:]
    os.chdir(os.path.join(cur_path, mode.lower(), project.lower()))
    parse_time()
    parse_num()
    write_to_file()
    os.chdir(cur_path)
    