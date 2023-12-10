import numpy as np
import matplotlib.pyplot as plt
from matplotlib.font_manager import FontProperties

plt.rcParams['font.sans-serif'] = ['Microsoft YaHei']  # 设置字体为微软雅黑
plt.rcParams['axes.unicode_minus'] = False  # 解决负号无法显示的问题

# 改进欧拉方法仿真函数
def simulate_population_growth(r, K, A, P0, time_steps):
    P = np.zeros(time_steps)
    P[0] = P0
    for t in range(1, time_steps):
        growth = r * P[t - 1] * (1 - P[t - 1] / K)
        P[t] = P[t - 1] + growth * A
    return P
# 分析稳定性的函数
def analyze_stability(P, time_steps):
    last_portion = int(time_steps * 0.2)  # 使用最后20%的时间数据进行分析
    mean_population = np.mean(P[-last_portion:])
    std_deviation = np.std(P[-last_portion:])
    return mean_population, std_deviation
# 设置模型参数和初始条件
r = 0.1  # 基本人口增长率
K = 1000  # 环境容纳能力
A = 1  # 资源分配策略效率
P0 = 100  # 初始人口数量
time_steps = 100  # 仿真时间步数

# 构建多种情况的仿真

scenarios = {
    '高增长率': {'r': 0.3, 'K': 1000, 'A': 1},
    '低增长率': {'r': 0.05, 'K': 1000, 'A': 1},
    '高环境容纳能力': {'r': 0.1, 'K': 2000, 'A': 1},
    '低环境容纳能力': {'r': 0.1, 'K': 500, 'A': 1},
    '高效资源分配': {'r': 0.1, 'K': 1000, 'A': 1.5},
    '低效资源分配': {'r': 0.1, 'K': 1000, 'A': 0.5}
}


# 运行仿真并分析稳定性
population_scenarios = {}
stability_analysis = {}
for name, params in scenarios.items():
    P = simulate_population_growth(params['r'], params['K'], params['A'], P0, time_steps)
    population_scenarios[name] = P
    mean_population, std_deviation = analyze_stability(P, time_steps)
    stability_analysis[name] = {'平均人口': mean_population, '标准差': std_deviation}

# 绘制图表
plt.figure(figsize=(12, 8))
for name, P in population_scenarios.items():
    plt.plot(P, label=name)
plt.title('不同场景下的人口增长')
plt.xlabel('时间步')
plt.ylabel('人口数量')
plt.legend()
plt.grid(True)
# 保存图表到文件
plt.savefig(r'C:\Users\lyttt\Desktop\img\population_growth_scenarios.png', dpi=300)  # 指定路径和文件名

plt.show()
# 打印稳定性分析结果
for name, analysis in stability_analysis.items():
    print(f"{name} - 平均人口: {analysis['平均人口']}, 标准差: {analysis['标准差']}")
