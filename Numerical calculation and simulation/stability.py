import pandas as pd

# 创建包含数据的DataFrame
data = {
    '场景': ['高增长率', '低增长率', '高环境容纳能力', '低环境容纳能力', '高效资源分配', '低效资源分配'],
    '平均人口': [999.9999999344988, 904.1669888130152, 1994.2305354344724, 499.69543456474264, 999.9848635392943, 903.9791062492428],
    '标准差': [9.245295379554768e-08, 24.741085236695405, 3.2124838645528886, 0.16999512358506494, 0.01252914277232449, 24.293199441284372]
}

df = pd.DataFrame(data)

# 分析系统稳定性的函数
def analyze_stability(data):
    stable_scenarios = []
    unstable_scenarios = []

    for _, row in data.iterrows():
        if row['标准差'] < 1:
            stable_scenarios.append(row['场景'])
        else:
            unstable_scenarios.append(row['场景'])

    return stable_scenarios, unstable_scenarios

stable, unstable = analyze_stability(df)

print("稳定的场景：", stable)
print("不稳定的场景：", unstable)
