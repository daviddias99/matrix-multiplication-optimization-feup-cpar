import os
from os import path
import numpy as np
import pandas as pd
from matplotlib import pyplot as plt

dir = path.dirname(__file__)
plots_dir = path.join(dir, 'plots')
results_dir = path.join(dir, 'results')
os.makedirs(path.join(dir, 'plots'), exist_ok=True)

exp_1 = 'exp_1_2021-03-03 09:03:20.271109.csv'
exp_2a = 'exp_2a_2021-03-03 16:41:42.152823.csv'
exp_2b = 'exp_2b_2021-03-03 18:15:05.165873.csv'
exp_3 = 'exp_3_2021-03-03 21:57:52.261995.csv'

df_1 = pd.read_csv(path.join(results_dir, exp_1))
df_2a = pd.read_csv(path.join(results_dir, exp_2a))
df_2b = pd.read_csv(path.join(results_dir, exp_2b))
df_3 = pd.read_csv(path.join(results_dir, exp_3))

df = df_1.append([df_2a, df_2b, df_3])

def gflops(mat_size): return 2 * (mat_size ** 3) * 1e-9

df['Performance'] = gflops(df['Matrix Size']) / df['Time']
df['L1 DCM/Gflop'] = df['L1 DCM'] / gflops(df['Matrix Size'])
df['L2 DCM/Gflop'] = df['L2 DCM'] / gflops(df['Matrix Size'])
df['L1 DCM/Matrix Size'] = df['L1 DCM'] / df['Matrix Size']


time_std = df.groupby(['Language', 'Matrix Size', 'Algorithm'], as_index=False).std()['Time']

data = df.groupby(['Language', 'Matrix Size', 'Algorithm'], as_index=False).mean()

data['Time STD'] = time_std
data.reset_index()


def algorithm_to_label(alg):
    labels = ['Column', 'Row']
    if isinstance(alg, int):
        return labels[alg-1]

    size = alg[1:-1].split(',')[1].strip()

    return 'Block ({})'.format(size)

def language_to_label(lang):
    if lang == 'cpp':
        return 'C++'
    if lang == 'java':
        return 'Java'

def metric_to_label(metric):
    if metric == 'Time':
        return 'Time (s)'
    if metric == 'Performance':
        return 'Performance (Gflop/s)'

    return metric


def plot_algorithm_comparison(dataframe, comp_column, save_path):
    cpp_df = dataframe[['Language', 'Matrix Size', comp_column, 'Algorithm']][dataframe['Language'] == 'cpp']

    algs = set(cpp_df['Algorithm'])
    colors = ['blue', 'red', 'green', 'orange', 'purple']
    ax = None

    for alg, color in zip(algs, colors):
        ax = cpp_df[cpp_df['Algorithm'] == alg].plot(x='Matrix Size', y=comp_column, ax=ax, label=algorithm_to_label(alg), kind='scatter', color=color, ylabel=metric_to_label(comp_column))

    plt.savefig(save_path)


def plot_lang_comparison(dataframe, comp_column, alg, save_path):
    alg_df = dataframe[['Language', 'Matrix Size',
                        comp_column, 'Algorithm']][dataframe['Algorithm'] == alg]

    langs = set(alg_df['Language'])
    colors = ['blue', 'red', 'green', 'orange', 'purple']

    ax = None
    for lang, color in zip(langs, colors):

        ax = alg_df[alg_df['Language'] == lang].plot(
            x='Matrix Size', y=comp_column, ax=ax, label=language_to_label(lang), kind='scatter', color=color, ylabel=metric_to_label(comp_column))

    plt.savefig(save_path)


plot_algorithm_comparison(data, 'Time', path.join(plots_dir, 'alg_comparison_time_c++.png'))
plot_algorithm_comparison(data, 'Performance', path.join(plots_dir, 'alg_comparison_perf_c++.png'))
plot_algorithm_comparison(data, 'L1 DCM/Gflop', path.join(plots_dir, 'alg_comparison_l1dcm-gflop_c++.png'))
plot_algorithm_comparison(data, 'L2 DCM/Gflop', path.join(plots_dir, 'alg_comparison_l2dcm-gflop_c++.png'))
plot_algorithm_comparison(data, 'L1 DCM', path.join(plots_dir, 'alg_comparison_l1dcm_c++.png'))
plot_algorithm_comparison(data, 'L2 DCM', path.join(plots_dir, 'alg_comparison_l2dcm_c++.png'))
plot_lang_comparison(data, 'Time', 1, path.join(plots_dir, 'alg_1_time'))
plot_lang_comparison(data, 'Time', 2, path.join(plots_dir, 'alg_2_time'))
plot_lang_comparison(data, 'Performance', 1, path.join(plots_dir, 'alg_1_perf'))
plot_lang_comparison(data, 'Performance', 2, path.join(plots_dir, 'alg_2_perf'))


