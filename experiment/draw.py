import matplotlib.pyplot as plt
import pandas as pd
import sys
import numpy as py
import os


def read_summary_csv(csv_file):
    df = pd.read_csv(csv_file)
    df = df.drop([50,51])  # Drop the Mean row

    y_time_urts = df['urts_total_time'].to_numpy()
    y_time_ekstazi_ext = df['safe_total_time'].to_numpy()
    y_time_ekstazi_unsafe = df['unsafe_total_time'].to_numpy()
    y_time_retestall = df['retestall_total_time'].to_numpy()
    
    y_classes_urts = df['urts_total_classes'].to_numpy()
    y_classes_ekstazi_ext = df['safe_total_classes'].to_numpy()
    y_classes_ekstazi_unsafe = df['unsafe_total_classes'].to_numpy()
    y_classes_retestall = df['retestall_total_classes'].to_numpy()

    y_time = [y_time_urts, y_time_ekstazi_ext, y_time_ekstazi_unsafe, y_time_retestall]
    y_classes = [y_classes_urts, y_classes_ekstazi_ext, y_classes_ekstazi_unsafe, y_classes_retestall]
    print(y_time)
    return [y_time, y_classes]


def draw_helper(proj, target_file, y_time, y_test):
    fig, ax = plt.subplots(1,2, figsize=(20,6), dpi=100)
    x = [i for i in range(-49, 1)]
    plt.subplot(1,2,1)
    plt.plot(x, y_time[0], label='uRTS', color='green', linestyle='solid', marker='.', markersize='10', markeredgecolor='k',markeredgewidth=1)
    plt.plot(x, y_time[1], label='Ekstazi-Ext', color='blue', linestyle='solid',  marker='.', markersize='10', markeredgecolor='k',markeredgewidth=1)
    plt.plot(x, y_time[2], label='Ekstazi-Unsafe', color='red', linestyle='solid', marker='.', markersize='10', markeredgecolor='k',markeredgewidth=1)
    plt.plot(x, y_time[3], label='ReTestAll', color='black', linestyle='solid', marker='.',  markersize='10', markeredgecolor='k',markeredgewidth=1)
    
    plt.xlabel('Revision', fontproperties='Times New Roman', fontsize=35)
    plt.ylabel('Time [sec]', fontproperties='Times New Roman', fontsize=35)
        
    plt.ylim(bottom=0.)
    plt.tick_params(axis='both', labelsize=14)
    # plt.yaxis.grid(True, linestyle='-.')
    plt.xticks(fontsize=20)
    plt.yticks(fontsize=20)
    # plt.rcParams.update({'font.size': 15})

    if proj == "hcommon":
        legend = plt.legend(bbox_to_anchor=(0,1.02,2,0.4), loc="lower left",  borderaxespad=0, ncol=4, handlelength=2, handletextpad=0.3, columnspacing=1.35, borderpad=0.3, prop={"family": "Times New Roman", "size": 35, "weight":'bold'})
    # # plt.fill_between(x, y1=0, y2=cw_dir[project], facecolor='lightskyblue', alpha=0.3)
    plt.fill_between(x, y1=y_time[0], y2=y_time[1], facecolor='skyblue', alpha=0.3)
    plt.fill_between(x, y1=y_time[0], y2=y_time[2], facecolor='red', alpha=0.3)
    
    plt.subplot(1,2,2)
    plt.plot(x, y_test[0], label='uRTS', color='green', linestyle='solid', marker='.', markersize='10', markeredgecolor='k',markeredgewidth=1)
    plt.plot(x, y_test[1], label='Ekstazi-Ext', color='blue', linestyle='solid',  marker='.', markersize='10', markeredgecolor='k',markeredgewidth=1)
    plt.plot(x, y_test[2], label='Ekstazi-Unsafe', color='red', linestyle='solid', marker='.', markersize='10', markeredgecolor='k',markeredgewidth=1)
    plt.plot(x, y_test[3], label='ReTestAll', color='black', linestyle='solid', marker='.',  markersize='10', markeredgecolor='k',markeredgewidth=1)
    
    plt.xlabel('Revision', fontproperties='Times New Roman', fontsize=35)
    plt.ylabel('# Test Classes', fontproperties='Times New Roman', fontsize=35)
        
    plt.ylim(bottom=0.)
    plt.tick_params(axis='both', labelsize=14)
    # plt.yaxis.grid(True, linestyle='-.')
    plt.xticks(fontsize=20)
    plt.yticks(fontsize=20)
    # plt.rcParams.update({'font.size': 15})

    # # plt.fill_between(x, y1=0, y2=cw_dir[project], facecolor='lightskyblue', alpha=0.3)
    plt.fill_between(x, y1=y_test[0], y2=y_test[1], facecolor='skyblue', alpha=0.3)
    plt.fill_between(x, y1=y_test[0], y2=y_test[2], facecolor='red', alpha=0.3)
    fig.savefig(target_file, bbox_inches='tight')

    
def draw(proj, csv_file, target_folder):
    y_time, y_test = read_summary_csv(csv_file)
    target_file = os.path.join(target_folder, "{}.pdf".format(proj))
    draw_helper(proj, target_file, y_time, y_test)
    

if __name__ == '__main__':
    proj = sys.argv[1]
    csv_file = sys.argv[2] #"csv_files/hcommon/summary.csv"
    target_folder = sys.argv[3] #"figures/hcommon"
    draw(proj, csv_file,target_folder)
         
