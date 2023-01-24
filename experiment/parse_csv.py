import os, sys, shutil
import pandas as pd
from scipy import stats

time_num_colomn_name = ["def_total_time", "def_total_classes", "prod1_total_time", "prod1_total_classes","prod2_total_time", "prod2_total_classes"]
time_column_list = ["def_total_time","prod1_total_time","prod2_total_time"]
classes_column_list = ["def_total_classes","prod1_total_classes", "prod2_total_classes"]


def get_unsafe_only_default(csv_file):
    total_df = pd.DataFrame()
    df = pd.read_csv(csv_file)
    total_df["unsafe_total_time"] = df["def_total_time"]
    total_df["unsafe_total_classes"] = df["def_total_classes"]
    #print(total_df)
    return total_df
    

def rename_columns(df, mode):
    for col in time_num_colomn_name:
        df = df.rename({col : "{}_{}".format(mode, col)}, axis='columns')
    return df


def add_avg_row(df, avg, col_list):
    avg_row = pd.DataFrame(data=[avg], columns=col_list)
    #print(avg_row)
    return pd.concat([df, avg_row], ignore_index=True)
    

def get_total(csv_file, mode):
    total_df = pd.DataFrame()
    df = pd.read_csv(csv_file)
    if mode == 'urts':
        total_df["commit"] = df["commit"]
    total_df["{}_total_time".format(mode)] = df[time_column_list].sum(axis=1)
    total_df["{}_total_classes".format(mode)] = df[classes_column_list].sum(axis=1)
    #print(total_df)
    return total_df


# Get retestall average time and number of classes
def parse_retestall(reall_file):
    #print(reall_file)
    df = pd.read_csv(reall_file)
    total_df = get_total(reall_file, "reall")
    avg = total_df.mean(axis=0)
    # return [avg_time, avg_classes]
    return [avg['reall_total_time'], avg['reall_total_classes']]
        

def generate_final_csv(reall_csv, ekst_csv, unsafe_csv, urts_csv, target_csv):
    reall_time, reall_classes = parse_retestall(reall_csv)
    safe_df = get_total(ekst_csv, 'safe')
    #unsafe_df = get_total(unsafe_csv, 'unsafe')
    unsafe_df = get_unsafe_only_default(unsafe_csv)
    urts_df = get_total(urts_csv, 'urts')
    #print(urts_df)
    #print(safe_df)
    result = pd.concat([urts_df, safe_df, unsafe_df], axis=1)
    result['retestall_total_time'] = [reall_time] * 50
    result['retestall_total_classes'] = [reall_classes] * 50
    result['TIME_urts_/_retestall'] = result['urts_total_time'] / reall_time
    result['TIME_urts_/_safe'] = result['urts_total_time'] / result['safe_total_time']
    result['TIME_urts_/_unsafe'] = result['urts_total_time'] / result['unsafe_total_time']
    result['TIME_safe_/_retestall'] = result['safe_total_time'] / reall_time
    result['TIME_safe_/_unsafe'] = result['safe_total_time'] / result['unsafe_total_time']
    result['TIME_unsafe_/_retestall'] = result['unsafe_total_time'] / reall_time

    result['CLASSES_urts_/_retestall'] = result['urts_total_classes'] / reall_classes
    result['CLASSES_urts_/_safe'] = result['urts_total_classes'] / result['safe_total_classes']
    result['CLASSES_urts_/_unsafe'] = result['urts_total_classes'] / result['unsafe_total_classes']
    result['CLASSES_safe_/_retestall'] = result['safe_total_classes'] / reall_classes
    result['CLASSES_safe_/_unsafe'] = result['safe_total_classes'] / result['unsafe_total_classes']
    result['CLASSES_unsafe_/_retestall'] = result['unsafe_total_classes'] / reall_classes
    arithmetic_mean = result.mean()
    geo_mean_list = list(stats.gmean(result.iloc[:,1:],axis=0))
    print(geo_mean_list)
    geo_mean = [""]
    geo_mean.extend(geo_mean_list)
    print(geo_mean)
    #col_list = list(result)
    #col_list.remove("commit")
    #for col in col_list:
    result.loc['Arithmetic_Mean'] = arithmetic_mean
    result.loc['Geo_Mean'] = geo_mean
    print(result)
    result.to_csv(target_csv)
    

if __name__ == '__main__':
    proj = sys.argv[1]
    file_dir = sys.argv[2]
    reall_file= os.path.join(file_dir, "{}/{}-reall.csv".format(proj, proj))
    ekst_file= os.path.join(file_dir, "{}/{}-ekst.csv".format(proj, proj)) 
    unsafe_file= os.path.join(file_dir, "{}/{}-unsafe.csv".format(proj, proj))
    urts_file= os.path.join(file_dir, "{}/{}-urts.csv".format(proj, proj)) 
    target_file = os.path.join(file_dir, "{}/summary.csv".format(proj))

    generate_final_csv(reall_file, ekst_file, unsafe_file, urts_file, target_file)
